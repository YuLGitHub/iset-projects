package cn.net.iset.elasticsearch.service.impl;

import cn.cloudwalk.ocean.common.result.page.OceanPageAble;
import cn.cloudwalk.ocean.common.result.page.OceanPageInfo;
import cn.net.iset.elasticsearch.bean.AllStructCoreData;
import cn.net.iset.elasticsearch.bean.EachDetailDataResult;
import cn.net.iset.elasticsearch.bean.IndexDO;
import cn.net.iset.elasticsearch.bean.RedisCursorData;
import cn.net.iset.elasticsearch.bean._do.CountInIndexGroupResultDO;
import cn.net.iset.elasticsearch.bean.bo.AbstractQueryBO;
import cn.net.iset.elasticsearch.bean.bo.FilterQueryBO;
import cn.net.iset.elasticsearch.builder.EachDetailDataBuilder;
import cn.net.iset.elasticsearch.config.QueryConfig;
import cn.net.iset.elasticsearch.extend.QueryMethodExtendService;
import cn.net.iset.elasticsearch.service.PageService;
import cn.net.iset.elasticsearch.utils.ConvergeUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * ClassName: ComprehensiveDaoBizService.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @date 2020/11/19
 */
@Slf4j
@Service("pageService")
@Scope("prototype")
public class PageServiceImpl implements PageService {

    @Autowired
    private EachDetailDataBuilder eachDetailDataBuilder;

    /**
     * redis数据
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * redis游标数据
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplateCursor;

    @Autowired
    private QueryConfig queryConfig;

    @Autowired
    private QueryMethodExtendService queryMethodExtendService;
    
    /**
     * threadPoolExecutor服务
     */
    protected ThreadPoolExecutor getEsDataExecutor;

    @PostConstruct
    public void init() {
        getEsDataExecutor = new ThreadPoolExecutor(queryConfig.getCorePoolSize()
                , queryConfig.getMaxPoolSize(), 30, TimeUnit.SECONDS
                , new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors())
                , new ThreadFactoryBuilder().setNameFormat("query es data").setDaemon(false).build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 分页查询 -- clickhouse + es
     *
     * @param queryCHBO
     * @param filterQuery
     * @param pageInfo
     * @return
     */
    @Override
    public <T extends AbstractQueryBO> OceanPageAble<Map<String, Object>> pageQuery(T queryCHBO, FilterQueryBO filterQuery, OceanPageInfo pageInfo) {

        long check = System.currentTimeMillis();
        // 设置hashkey
        generateCursorKey(filterQuery.getHashKey());
        long hashKey = System.currentTimeMillis();
        log.debug("[ES分页查询]生成HashKey耗时：{}ms", hashKey - check);

        // 根据配置归并设备
        List<List<Long>> deviceIdList = ConvergeUtil.divide(filterQuery.getDeviceIds(), queryConfig.getFragmentDeviceCount());

        long deviceSpilt = System.currentTimeMillis();
        log.debug("[ES分页查询]根据配置归并设备耗时：{}ms", deviceSpilt - hashKey);

        long part;
        List<CountInIndexGroupResultDO> data;
        if (hasRedisData(filterQuery.getHashKey())) {
            long hasRedis = System.currentTimeMillis();
            log.debug("[ES分页查询]判断是否存在count缓存耗时：{}ms", hasRedis - deviceSpilt);
            // redis中获取数据
            data = getRedisCountData(filterQuery.getHashKey());

            part = System.currentTimeMillis();
            log.debug("[ES分页查询]获取count缓存耗时：{}ms", part - hasRedis);
        } else {

            long hasRedis = System.currentTimeMillis();
            log.debug("[ES分页查询]判断是否存在count缓存耗时：{}ms", hasRedis - deviceSpilt);
            // 重新查数据
            data = getEsCountData(queryCHBO, deviceIdList);
            long getCount = System.currentTimeMillis();
            log.debug("[ES分页查询]数据库查询count耗时：{}ms", getCount - hasRedis);
            // 设置count数据
            setRedisCountData(filterQuery.getHashKey(), data);

            part = System.currentTimeMillis();
            log.debug("[ES分页查询]设置redis count耗时：{}ms", part - getCount);
        }

        if (CollectionUtils.isEmpty(data)) {
            return OceanPageAble.buildPage(new ArrayList<Map<String, Object>>(), pageInfo, 0);
        } else {
            // 获取详细信息
            OceanPageAble<Map<String, Object>> result = pageDetailData(data, queryCHBO, pageInfo, deviceIdList);
            long pageDetail = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail耗时：{}ms", pageDetail - part);
            return result;
        }
    }

    private void generateCursorKey(String hashKey) {
        hashKeyCursor = "QUERY_CURSOR:".concat(hashKey);
    }


    /**
     * hasKey
     */
    private String hashKeyCursor;

    /**
     * 分页查询数据
     *
     * @param data
     * @param pageInfo
     * @return
     */
    private <T extends AbstractQueryBO> OceanPageAble<Map<String, Object>> pageDetailData(List<CountInIndexGroupResultDO> data
            , T queryCHBO, OceanPageInfo pageInfo, List<List<Long>> deviceList) {

        long start = System.currentTimeMillis();
        // 准备参数
        List<CountInIndexGroupResultDO> request = prepareDetailParam(data, pageInfo);
        long param = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-param耗时：{}ms", param - start);
        // 获取最近的组合游标
        RedisCursorData cursorData = getCursorData(pageInfo);
        long getCursor = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-获取游标耗时：{}ms", getCursor - param);

        // 计算总数
        AtomicLong totalCount = new AtomicLong();
        data.stream().forEach(countInIndexGroupResultDO -> {
            // 获取总数
            if (countInIndexGroupResultDO.getDeviceAndCountDO() != null) {
                totalCount.addAndGet(countInIndexGroupResultDO.getDeviceAndCountDO().getCount());
            }
        });

        // 如果操过最大总数数据，直接返回
        if (totalCount.get() <= pageInfo.getPageStart()) {
            return OceanPageAble.buildPage(new ArrayList(), pageInfo, totalCount.get());
        }

        long count = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-统计count耗时：{}ms", count - getCursor);
        Set<String> targetFields = queryCHBO.getTargetFields();
        Set<String> target = new HashSet<>();
        target.add("captureTime");
        queryCHBO.setTargetFields(target);
        // 所有分索引的总数据
        EachDetailDataResult allPartResults = getAllPartResults(request, queryCHBO, pageInfo, deviceList, cursorData, totalCount.get());

        long getAll = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms", getAll - count);

        // 获取当前页
        List<AllStructCoreData> coreDataList = null;
        int totalPageSize = (int)(totalCount.get() / pageInfo.getPageSize() + (totalCount.get() % pageInfo.getPageSize() > 0 ? 1 : 0));
        if (CollectionUtils.isNotEmpty(allPartResults.getHitPageCoreData())) {
            if (allPartResults.getFromLastPage() != null && allPartResults.getFromLastPage()) {
                List<AllStructCoreData> collect = allPartResults.getHitPageCoreData().stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime)
                        .thenComparing(AllStructCoreData::getDataId)).collect(Collectors.toList());
                if (totalPageSize == pageInfo.getCurrentPage()) {
                    long size = totalCount.get() % pageInfo.getPageSize();
                    if (collect.size() > size) {
                        coreDataList = collect.subList(0, (int) (size == 0 ? pageInfo.getPageSize() : size));
                    } else {
                        coreDataList = collect;
                    }
                } else {
                    if (collect.size() > pageInfo.getPageSize()) {
                        coreDataList = collect.subList(0, pageInfo.getPageSize());
                    } else {
                        coreDataList = collect;
                    }
                }
            } else {
                List<AllStructCoreData> collect = allPartResults.getHitPageCoreData().stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime, Comparator.reverseOrder())
                        .thenComparing(AllStructCoreData::getDataId, Comparator.reverseOrder())).collect(Collectors.toList());
                if (totalPageSize == pageInfo.getCurrentPage()) {
                    long size = totalCount.get() % pageInfo.getPageSize();
                    if (collect.size() > size) {
                        coreDataList = collect.subList(0, (int) (size == 0 ? pageInfo.getPageSize() : size));
                    } else {
                        coreDataList = collect;
                    }
                } else {
                    if (collect.size() > pageInfo.getPageSize()) {
                        coreDataList = collect.subList(0, pageInfo.getPageSize());
                    } else {
                        coreDataList = collect;
                    }
                }
            }
        }

        List<String> indexNames = null;

        // 设置开始结束key
        if (CollectionUtils.isNotEmpty(coreDataList)) {
            setRedisCursorData(coreDataList, pageInfo.getCurrentPage());
            // 获取索引列表
            indexNames = coreDataList.stream().map(AllStructCoreData::getIndexName).distinct().collect(Collectors.toList());
        }

        long setData = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-组装详情查询耗时：{}ms", setData - getAll);


        // 调用es查询具体值
        targetFields.add("captureTime");
        queryCHBO.setTargetFields(targetFields);
        List<Map<String, Object>> results = getEsAllStructData(indexNames, coreDataList, queryCHBO.getTargetFields());

        long getDetail = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-查询详情耗时：{}ms", getDetail - setData);
        // 构建分页结果
        return OceanPageAble.buildPage(results, pageInfo, totalCount.get());

    }

    /**
     * 获取所有具体数据
     */
    private List<Map<String, Object>> getEsAllStructData(List<String> indexNames, List<AllStructCoreData> coreDataList, Set<String> targetFields) {
        // 遍历所有主键ID进行查询
        if (CollectionUtils.isNotEmpty(coreDataList)) {
            List<Long> collect = coreDataList.stream().map(AllStructCoreData::getDataId).collect(Collectors.toList());
            return queryMethodExtendService.getDataById(indexNames, targetFields, collect);
        }
        return new ArrayList<>();
    }

    /**
     * 设置redis游标数据
     *
     * @param coreDataList 已排序好的集合
     * @param score        分数
     */
    private RedisCursorData setRedisCursorData(List<AllStructCoreData> coreDataList, int score) {

        RedisCursorData first = null;
        if (CollectionUtils.isNotEmpty(coreDataList)) {

            // sort the list
            List<AllStructCoreData> collect = coreDataList.stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime, Comparator.reverseOrder())
                    .thenComparing(AllStructCoreData::getDataId, Comparator.reverseOrder())).collect(Collectors.toList());
            first = new RedisCursorData();
            // 开始
            AllStructCoreData start = collect.get(0);
            first.setStartMarkId(start.getDataId());
            first.setStartMarkTime(start.getCaptureTime());
            first.setIndexBucketId(start.getIndexBucketId());
            first.setCurrentPage(score);

        }
        if (first != null) {
            // 加锁进行处理
            Set<String> set = redisTemplateCursor.opsForZSet().rangeByScore(hashKeyCursor, score, score);
            if (CollectionUtils.isNotEmpty(set)) {
                return null;
            }
            redisTemplateCursor.opsForZSet().add(hashKeyCursor, JSON.toJSONString(first), score);
            redisTemplateCursor.expire(hashKeyCursor, queryConfig.getRedisExpiredTime(), TimeUnit.SECONDS);
        }
        return first;
    }

    /**
     * 获取最近的组合游标
     *
     * @param pageInfo
     * @return
     */
    private RedisCursorData getCursorData(OceanPageInfo pageInfo) {
        Set<String> cursorData = redisTemplateCursor.opsForZSet().rangeByScore(hashKeyCursor, pageInfo.getCurrentPage(), pageInfo.getCurrentPage());

        if (CollectionUtils.isNotEmpty(cursorData)) {
            return JSON.parseObject(cursorData.iterator().next(), RedisCursorData.class);
        } else {
            Cursor<ZSetOperations.TypedTuple<String>> scan =
                    redisTemplateCursor.opsForZSet().scan(hashKeyCursor, ScanOptions.NONE);
            double minLength = Integer.MAX_VALUE;
            Double score = null;
            while (scan.hasNext()) {
                Double nextScore = scan.next().getScore();
                double absScore = Math.abs(nextScore - pageInfo.getCurrentPage());
                if (absScore < minLength) {
                    minLength = absScore;
                    score = nextScore;
                }
            }
            if (score != null) {
                Set<String> data = redisTemplateCursor.opsForZSet().rangeByScore(hashKeyCursor, score, score);
                if (CollectionUtils.isNotEmpty(data)) {
                    RedisCursorData redisCursorData = JSON.parseObject(data.iterator().next(), RedisCursorData.class);
                    if (redisCursorData != null) {
                        redisCursorData.setCurrentPage(score.intValue());
                    }
                    return redisCursorData;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有索引的数据
     *
     * @param request
     * @param queryCHBO
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult getAllPartResults(List<CountInIndexGroupResultDO> request
            , T queryCHBO, OceanPageInfo pageInfo, List<List<Long>> deviceList
            , RedisCursorData cursorData, Long totalRows) {
        long begin = System.currentTimeMillis();
        EachDetailDataResult results;
        // 先判断是否在同一个索引组
        AtomicBoolean isTheSameIndex = new AtomicBoolean(false);

        // 获取所有的es索引值
        if (CollectionUtils.isNotEmpty(request)) {
            List<IndexDO> indexDOList = new ArrayList<>();
            request.stream().forEach(countInIndexGroupResultDO -> {
                if (CollectionUtils.isNotEmpty(countInIndexGroupResultDO.getIndexDOList())) {
                    indexDOList.addAll(countInIndexGroupResultDO.getIndexDOList());
                    // 判断是否在同一个索引组
                    Integer indexBucketId = countInIndexGroupResultDO.getIndexBucketId();
                    if (cursorData != null && cursorData.getIndexBucketId() != null && cursorData.getIndexBucketId().equals(indexBucketId)) {
                        isTheSameIndex.set(true);
                    }
                }
            });
        }

        long index = System.currentTimeMillis();
        log.debug("[ES分页查询]pageDetail-获取所有ID耗时-判断索引是否在同一个组：{}ms", index - begin);
        if (isTheSameIndex.get()) {
            // 按游标算
            results = handleTopSizeDataWithCursor(request, queryCHBO, pageInfo, deviceList, cursorData, totalRows);
        } else {
            // 不按游标算
            results = handleTopSizeDataWithCursor(request, queryCHBO, pageInfo, deviceList, null, totalRows);
        }

        // 记录所有分页游标数据
        if (CollectionUtils.isNotEmpty(results.getAllStructCoreData())) {
            List<AllStructCoreData> collect = results.getAllStructCoreData().stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime, Comparator.reverseOrder())
                    .thenComparing(AllStructCoreData::getDataId, Comparator.reverseOrder())).collect(Collectors.toList());
            // 设置redis游标信息
            List<List<AllStructCoreData>> divide = ConvergeUtil.divide(collect, pageInfo.getPageSize());
            Integer start = results.getStartPage();
            Integer end = results.getEndPage();
            for (int i = 0; i < end - start; i++) {
                setRedisCursorData(divide.get(i), start + i);
            }
        }
        return results;
    }

    /**
     * 处理数据
     *
     * @param request
     * @param queryCHBO
     * @param pageInfo
     * @param deviceList
     * @param cursorData
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult handleTopSizeDataWithCursor(List<CountInIndexGroupResultDO> request, T queryCHBO, OceanPageInfo pageInfo, List<List<Long>> deviceList, RedisCursorData cursorData, Long totalRows) {

        EachDetailDataResult dataResult = new EachDetailDataResult();
        List<AllStructCoreData> allStructCoreData = new ArrayList<>();
        List<AllStructCoreData> hitCoreData = new ArrayList<>();
        dataResult.setHitPageCoreData(hitCoreData);
        dataResult.setAllStructCoreData(allStructCoreData);
        dataResult.setFromLastPage(false);

        // 分批获取数据
        List<CompletableFuture<EachDetailDataResult>> futureList = new ArrayList<>();
        // 并发处理
        request.stream().forEach(requestParam -> {
            CompletableFuture<EachDetailDataResult> future = CompletableFuture.supplyAsync(() ->
                    //  根据条件查询数据
                    currentGetDetail(queryCHBO, pageInfo, deviceList, cursorData, requestParam, totalRows), getEsDataExecutor);
            futureList.add(future);
        });

        // 取值
        futureList.stream().forEach(completableFuture -> {
            try {
                EachDetailDataResult result = completableFuture.get();
                if (result != null) {
                    if (CollectionUtils.isNotEmpty(result.getAllStructCoreData())) {
                        dataResult.getAllStructCoreData().addAll(result.getAllStructCoreData());
                    }
                    if (CollectionUtils.isNotEmpty(result.getHitPageCoreData())) {
                        dataResult.getHitPageCoreData().addAll(result.getHitPageCoreData());
                    }
                    if (result.getFromLastPage() != null && result.getFromLastPage() && dataResult.getFromLastPage() != null && !dataResult.getFromLastPage()) {
                        dataResult.setFromLastPage(true);
                    }
                    dataResult.setStartPage(result.getStartPage());
                    dataResult.setEndPage(result.getEndPage());
                }
            } catch (Exception e) {
                log.error("分设备列表并行获取数据出现异常，原因：{}，但任务继续 {}", e.getMessage(), e);
            }
        });

        return dataResult;
    }

    /**
     * 并发处理该详细数据
     *
     * @param queryCHBO
     * @param pageInfo
     * @param deviceList
     * @param cursorData
     * @param request
     */
    private  <T extends AbstractQueryBO> EachDetailDataResult currentGetDetail(T queryCHBO, OceanPageInfo pageInfo, List<List<Long>> deviceList, RedisCursorData cursorData, CountInIndexGroupResultDO request, Long totalRows) {
        List<IndexDO> indexDOList = request.getIndexDOList();
        EachDetailDataResult coreDataList = new EachDetailDataResult();
        List<AllStructCoreData> allStructCoreData = new ArrayList<>();
        List<AllStructCoreData> hitCoreData = new ArrayList<>();
        coreDataList.setHitPageCoreData(hitCoreData);
        coreDataList.setAllStructCoreData(allStructCoreData);
        coreDataList.setFromLastPage(false);
        if (CollectionUtils.isNotEmpty(deviceList)) {
            // 分批获取数据
            List<CompletableFuture<EachDetailDataResult>> futureList = new ArrayList<>();
            // 并发处理
            deviceList.stream().forEach(deviceIds -> {
                CompletableFuture<EachDetailDataResult> future = CompletableFuture.supplyAsync(() ->
                        //  根据条件查询数据
                        eachDetailDataBuilder.queryEachDetailData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData, request.getIndexBucketId(), totalRows), getEsDataExecutor);
                futureList.add(future);
            });

            // 取值
            futureList.stream().forEach(completableFuture -> {
                try {
                    EachDetailDataResult result = completableFuture.get();
                    if (result != null) {
                        if (CollectionUtils.isNotEmpty(result.getAllStructCoreData())) {
                            coreDataList.getAllStructCoreData().addAll(result.getAllStructCoreData());
                        }
                        if (CollectionUtils.isNotEmpty(result.getHitPageCoreData())) {
                            coreDataList.getHitPageCoreData().addAll(result.getHitPageCoreData());
                        }
                        if (result.getFromLastPage() != null && result.getFromLastPage() && coreDataList.getFromLastPage() != null && !coreDataList.getFromLastPage()) {
                            coreDataList.setFromLastPage(true);
                        }
                        coreDataList.setStartPage(result.getStartPage());
                        coreDataList.setEndPage(result.getEndPage());
                    }
                } catch (Exception e) {
                    log.error("分设备列表并行获取数据出现异常，原因：{}，但任务继续 {}", e.getMessage(), e);
                }
            });
        } else {
            EachDetailDataResult result = eachDetailDataBuilder.queryEachDetailData(indexDOList, queryCHBO, new ArrayList<>(), pageInfo, cursorData, request.getIndexBucketId(), totalRows);
            if (CollectionUtils.isNotEmpty(result.getAllStructCoreData())) {
                coreDataList.getAllStructCoreData().addAll(result.getAllStructCoreData());
            }
            if (CollectionUtils.isNotEmpty(result.getHitPageCoreData())) {
                coreDataList.getHitPageCoreData().addAll(result.getHitPageCoreData());
            }
            if (result.getFromLastPage() != null && result.getFromLastPage() && coreDataList.getFromLastPage() != null && !coreDataList.getFromLastPage()) {
                coreDataList.setFromLastPage(true);
            }
            coreDataList.setStartPage(result.getStartPage());
            coreDataList.setEndPage(result.getEndPage());
        }

        return coreDataList;
    }

    /**
     * 获取第二次查询详情的参数
     *
     * @param data
     * @param pageInfo
     * @return
     */
    private List<CountInIndexGroupResultDO> prepareDetailParam(List<CountInIndexGroupResultDO> data, OceanPageInfo pageInfo) {

        // 先排序
        Collections.sort(data);

        // 排序后
        List<CountInIndexGroupResultDO> results = new ArrayList<>(data.size());
        int pageStart = pageInfo.getPageStart();
        int pageEnd = pageInfo.getPageSize() + pageStart;
        Long sum = 0L;
        if (CollectionUtils.isNotEmpty(data)) {
            // 获取data中所有count
            Long total = 0L;
            for (int i = 0; i < data.size(); i++) {
                CountInIndexGroupResultDO resultDO = data.get(i);
                if (resultDO != null && resultDO.getDeviceAndCountDO() != null && resultDO.getDeviceAndCountDO().getCount() != null) {
                    total += resultDO.getDeviceAndCountDO().getCount();
                }
            }


            for (int i = 0; i < data.size(); i++) {
                CountInIndexGroupResultDO countInIndexGroupResultDO = data.get(i);
                sum += countInIndexGroupResultDO.getDeviceAndCountDO().getCount();

//                countInIndexGroupResultDO.setIndexBucketId(i);

                boolean flag = false;
                if (sum >= pageStart) {
                    flag = true;
                    results.add(countInIndexGroupResultDO);
                }
                if (sum > pageEnd) {
                    if (!flag) {
                        results.add(countInIndexGroupResultDO);
                    }
                    break;
                } else if (sum > pageStart && pageEnd > total) {
                    if (!flag) {
                        results.add(countInIndexGroupResultDO);
                    }
                }
            }
        }

        return results;
    }

    /**
     * 设置redis
     *
     * @param data
     */
    private void setRedisCountData(String key, List<CountInIndexGroupResultDO> data) {
        redisTemplate.opsForValue().set("QUERY_COUNT:".concat(key), JSON.toJSONString(data), queryConfig.getRedisExpiredTime(), TimeUnit.SECONDS);
    }

    /**
     * 获取ES数量
     *
     * @param queryCHBO
     * @param deviceIdList
     * @return
     */
    private <T extends AbstractQueryBO> List<CountInIndexGroupResultDO> getEsCountData(T queryCHBO, List<List<Long>> deviceIdList) {

        List<CountInIndexGroupResultDO> results = new ArrayList<>();

        // 根据抓拍时间获取索引列表（默认倒序排序）
        List<IndexDO> indexDOList = queryMethodExtendService.calculateEsIndex(queryCHBO.getCaptureTimeB(), queryCHBO.getCaptureTimeE());

        // 根据配置归并索引
        List<List<IndexDO>> divide = ConvergeUtil.divide(indexDOList, queryConfig.getFragmentIndexCount());

        // 分批获取数据
        List<CompletableFuture<List<CountInIndexGroupResultDO>>> futureList = new ArrayList<>();
        // 并发处理
        if (CollectionUtils.isNotEmpty(divide)) {
            for (int i = 0; i < divide.size(); i++) {
                int finalI = i;
                CompletableFuture<List<CountInIndexGroupResultDO>> future = CompletableFuture.supplyAsync(() ->
                        //  根据条件查询分设备count
                        queryDeviceCountData(divide.get(finalI), queryCHBO, deviceIdList, finalI), getEsDataExecutor);
                futureList.add(future);
            }
        }

        // 取值
        futureList.stream().forEach(completableFuture -> {
            try {
                List<CountInIndexGroupResultDO> resultDOList = completableFuture.get();
                if (CollectionUtils.isNotEmpty(resultDOList)) {
                    results.addAll(resultDOList);
                }
            } catch (Exception e) {
                log.error("并行获取数据出现异常，原因：{}，但任务继续 {}", e.getMessage(), e);
            }
        });
        return results;
    }

    /**
     * 根据索引查询分设备count数量
     *
     * @param indexs
     * @param queryCHBO
     * @param deviceIdList
     * @return
     */
    private<T extends AbstractQueryBO> List<CountInIndexGroupResultDO> queryDeviceCountData(List<IndexDO> indexs, T queryCHBO, List<List<Long>> deviceIdList, Integer indexBucketId) {

        List<CountInIndexGroupResultDO> results = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deviceIdList)) {
            // 分批获取数据
            List<CompletableFuture<CountInIndexGroupResultDO>> futureList = new ArrayList<>();
            // 并发处理
            deviceIdList.stream().forEach(deviceIds -> {
                CompletableFuture<CountInIndexGroupResultDO> future = CompletableFuture.supplyAsync(() ->
                        // 根据条件查询分设备count
                        queryEachPartDeviceCountData(indexs, queryCHBO, deviceIds), getEsDataExecutor);
                futureList.add(future);
            });
            // 取值
            futureList.stream().forEach(completableFuture -> {
                try {
                    CountInIndexGroupResultDO resultDO = completableFuture.get();
                    if (resultDO != null) {
                        resultDO.setIndexBucketId(indexBucketId);
                        results.add(resultDO);
                    }
                } catch (Exception e) {
                    log.error("并行获取数据出现异常，原因：{}，但任务继续 {}", e.getMessage(), e);
                }
            });
        } else {
            CountInIndexGroupResultDO countInIndexGroupResultDO = queryEachPartDeviceCountData(indexs, queryCHBO, new ArrayList<>());
            if (countInIndexGroupResultDO != null) {
                countInIndexGroupResultDO.setIndexBucketId(indexBucketId);
                results.add(countInIndexGroupResultDO);
            }
        }

        return results;
    }

    /**
     * 查询ES结果
     *
     * @param indexs
     * @param queryCHBO
     * @param deviceIds
     * @return
     */
    private <T extends AbstractQueryBO> CountInIndexGroupResultDO queryEachPartDeviceCountData(List<IndexDO> indexs, T queryCHBO, List<Long> deviceIds) {
        return queryMethodExtendService.getDeviceCountData(indexs, queryCHBO, deviceIds);
    }

    /**
     * 获取redis中count数据
     *
     * @param hashKey
     * @return
     */
    private List<CountInIndexGroupResultDO> getRedisCountData(String hashKey) {
        return JSON.parseArray(redisTemplate.opsForValue().get("QUERY_COUNT:".concat(hashKey)), CountInIndexGroupResultDO.class);
    }

    /**
     * 判断是否有redis数据
     *
     * @param hashKey
     * @return
     */
    private boolean hasRedisData(String hashKey) {
        return redisTemplate.opsForValue().getOperations().hasKey("QUERY_COUNT:".concat(hashKey));
    }

}
