package cn.net.iset.elasticsearch.builder;

import cn.cloudwalk.ocean.common.result.page.OceanPageInfo;
import cn.net.iset.elasticsearch.bean.AllStructCoreData;
import cn.net.iset.elasticsearch.bean.EachDetailDataResult;
import cn.net.iset.elasticsearch.bean.IndexDO;
import cn.net.iset.elasticsearch.bean.RedisCursorData;
import cn.net.iset.elasticsearch.bean.bo.AbstractQueryBO;
import cn.net.iset.elasticsearch.bean.bo.MarkBO;
import cn.net.iset.elasticsearch.config.QueryConfig;
import cn.net.iset.elasticsearch.extend.QueryMethodExtendService;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: DetailDataUtil.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @date 2020/12/7
 */
@Slf4j
@Component
public class EachDetailDataBuilder {


    @Autowired
    private QueryMethodExtendService queryMethodExtendService;

    @Autowired
    private QueryConfig queryConfig;

    /**
     * 获取详细数据
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @return
     */
    public <T extends AbstractQueryBO>EachDetailDataResult queryEachDetailData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows) {
        // 首先没有获取到游标
        if (cursorData == null) {
            return builderDataWithNoCursor(indexDOList, queryCHBO, deviceIds, pageInfo, null, indexBucketId, totalRows);
        } else {
            // 有游标处理
            return builderDataWithCursor(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData, indexBucketId, totalRows);
        }
    }

    /**
     * 包含游标查询
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     */
    private <T extends AbstractQueryBO>EachDetailDataResult builderDataWithCursor(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows) {
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
                    /*
                     确定当前页距离第一页和最后一页，哪个更近
                     */
        // 距离第一页的页码
        int firstDistance = pageInfo.getCurrentPage() - 1;
        // 总数
        int totalPages = (int) (totalRows / pageInfo.getPageSize() + (totalRows % pageInfo.getPageSize() > 0 ? 1 : 0));
        // 距离最后一页的页码
        int lastDistance = totalPages - pageInfo.getCurrentPage();
        // 距离游标的页码
        int cursorDistance = pageInfo.getCurrentPage() - cursorData.getCurrentPage();

        // 求最小值
        List<Integer> distances = new ArrayList<>();
        distances.add(firstDistance);
        distances.add(lastDistance);
        distances.add(Math.abs(cursorDistance));
        List<Integer> collect = distances.stream().sorted().collect(Collectors.toList());
        // 最小值
        Integer distance = collect.get(0);
        if (distance == firstDistance) {
            // 离第一页近，从前往后取值
            EachDetailDataResult eachDetailDataResult = buildFirstDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, firstDistance);
            eachDetailDataResult.setStartPage(1);
            eachDetailDataResult.setEndPage(pageInfo.getCurrentPage());
            return eachDetailDataResult;
        } else if (distance == lastDistance) {
            // 离最后一页近
            EachDetailDataResult eachDetailDataResult = buildLastDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, lastDistance);
            eachDetailDataResult.setStartPage(pageInfo.getCurrentPage());
            eachDetailDataResult.setEndPage(totalPages);
            return eachDetailDataResult;
        } else {
            return buildCursorDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, cursorDistance);
        }
    }

    /**
     * 构建游标数据
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param cursorDistance
     * @param supportQuery
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult buildCursorDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, int cursorDistance) {
        // 判断游标在前还是游标在后
        if (cursorDistance == 0) {
            // 正中游标
            return handleCursorDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, cursorDistance);
        } else if (cursorDistance > 0) {
            // 游标在前
            return handlePreCursorDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, cursorDistance);
        } else {
            // 游标在后
            return handleSufCursorDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, Math.abs(cursorDistance));
        }

    }

    /**
     * 正中游标
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param cursorDistance
     * @param supportQuery
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult handleCursorDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, int cursorDistance) {
        EachDetailDataResult eachDetailDataResult = new EachDetailDataResult();
        eachDetailDataResult.setFromLastPage(false);
        eachDetailDataResult.setStartPage(cursorData.getCurrentPage());
        eachDetailDataResult.setEndPage(cursorData.getCurrentPage());
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
        MarkBO markBO = new MarkBO();
        markBO.setMarkTime(cursorData.getStartMarkTime());
        markBO.setOrderId(cursorData.getStartMarkId());
        int totalPageSize = (int) (totalRows / pageInfo.getPageSize() + (totalRows % pageInfo.getPageSize() > 0 ? 1 : 0));
        if (totalPageSize == pageInfo.getCurrentPage()) {
            // 命中游标，并且是最后一页
            long lastSize = (totalRows % pageInfo.getPageSize() == 0) ? pageInfo.getPageSize() : (totalRows % pageInfo.getPageSize());
            oceanPageInfo.setPageSize((int) lastSize);
        } else {
            // 不是最后一页
            oceanPageInfo.setPageSize(pageInfo.getPageSize());
        }
        queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, list, markBO, true, indexBucketId);

        // 不为空
        if (CollectionUtils.isNotEmpty(list)) {
            eachDetailDataResult.setAllStructCoreData(list);
            eachDetailDataResult.setHitPageCoreData(list);
        }

        return eachDetailDataResult;
    }

    /**
     * 游标在后
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param cursorDistance
     * @param supportQuery
     * @return
     */
    private <T extends AbstractQueryBO>EachDetailDataResult handleSufCursorDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, int cursorDistance) {
        EachDetailDataResult eachDetailDataResult = new EachDetailDataResult();
        eachDetailDataResult.setFromLastPage(true);
        eachDetailDataResult.setStartPage(pageInfo.getCurrentPage());
        eachDetailDataResult.setEndPage(cursorData.getCurrentPage());
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
        MarkBO markBO = new MarkBO();
        markBO.setMarkTime(cursorData.getStartMarkTime());
        markBO.setOrderId(cursorData.getStartMarkId());
        //        long lastSize = (totalRows % pageInfo.getPageSize() == 0) ? pageInfo.getPageSize() : (totalRows % pageInfo.getPageSize());
        long rows = cursorDistance * pageInfo.getPageSize();
        buildLastPage(indexDOList, queryCHBO, deviceIds, pageInfo, indexBucketId, eachDetailDataResult, list, oceanPageInfo, markBO, rows);
        return eachDetailDataResult;
    }

    /**
     * 从后取值
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param indexBucketId
     * @param supportQuery
     * @param eachDetailDataResult
     * @param list
     * @param oceanPageInfo
     * @param markBO
     * @param rows
     */
    private <T extends AbstractQueryBO>void buildLastPage(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, Integer indexBucketId, EachDetailDataResult eachDetailDataResult, List<AllStructCoreData> list, OceanPageInfo oceanPageInfo, MarkBO markBO, long rows) {
        if (rows <= queryConfig.getRollSize()) {

            long start = System.currentTimeMillis();
            oceanPageInfo.setPageSize((int) rows);
            queryLastPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, list, null, true, indexBucketId);
            long firstPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从后往前查{}条数据", firstPageTime - start, rows);
        } else {
            long times = rows / queryConfig.getRollSize();
            boolean isAllFirstPage = true;
            for (int i = 0; i < times; i++) {
                long start = System.currentTimeMillis();
                oceanPageInfo.setPageSize(queryConfig.getRollSize());
                List<AllStructCoreData> newList = new ArrayList<>();
                queryLastPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, isAllFirstPage, indexBucketId);
                // 上一页的最后一条作为下一次查询的开始时间
                if (CollectionUtils.isNotEmpty(newList)) {
                    markBO = new MarkBO();
                    isAllFirstPage = false;
                    AllStructCoreData lastData = newList.get(newList.size() - 1);
                    markBO.setOrderId(lastData.getDataId());
                    markBO.setMarkTime(lastData.getCaptureTime());
                    list.addAll(newList);
                }

                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从后往前查{}条数据", firstPageTime - start, queryConfig.getRollSize());
            }
            long lastRows = rows % queryConfig.getRollSize();
            if (lastRows != 0) {

                long start = System.currentTimeMillis();
                List<AllStructCoreData> newList = new ArrayList<>();
                oceanPageInfo.setPageSize((int) lastRows);
                if (CollectionUtils.isNotEmpty(list)) {
                    markBO = new MarkBO();
                    isAllFirstPage = false;
                    AllStructCoreData lastData = list.get(list.size() - 1);
                    markBO.setOrderId(lastData.getDataId());
                    markBO.setMarkTime(lastData.getCaptureTime());
                }
                queryLastPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, isAllFirstPage, indexBucketId);
                if (CollectionUtils.isNotEmpty(newList)) {
                    list.addAll(newList);
                }

                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从后往前查{}条数据", firstPageTime - start, lastRows);

            }
        }
        // 去取具体的那一页的值
        if (CollectionUtils.isNotEmpty(list)) {
            long start = System.currentTimeMillis();
            List<AllStructCoreData> collect = list.stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime)
                    .thenComparing(AllStructCoreData::getDataId)).collect(Collectors.toList());
            AllStructCoreData lastData = collect.get(collect.size() - 1);
            MarkBO newMarkBO = new MarkBO();
            newMarkBO.setOrderId(lastData.getDataId());
            newMarkBO.setMarkTime(lastData.getCaptureTime());
            List<AllStructCoreData> newList = new ArrayList<>();
            queryLastPageESCommon(indexDOList, queryCHBO, deviceIds, pageInfo, newList, newMarkBO, false, indexBucketId);
            if (CollectionUtils.isNotEmpty(newList)) {
                list.addAll(newList);
                eachDetailDataResult.setAllStructCoreData(list);
                eachDetailDataResult.setHitPageCoreData(newList);
            }

            long firstPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 去取具体的那一页的{}条数据", firstPageTime - start, pageInfo.getPageSize());
        }

    }

    /**
     * 游标在前
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param cursorDistance
     * @param supportQuery
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult handlePreCursorDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, int cursorDistance) {
        EachDetailDataResult eachDetailDataResult = new EachDetailDataResult();
        eachDetailDataResult.setFromLastPage(false);
        eachDetailDataResult.setStartPage(cursorData.getCurrentPage());
        eachDetailDataResult.setEndPage(pageInfo.getCurrentPage());
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
        MarkBO markBO = new MarkBO();
        markBO.setMarkTime(cursorData.getStartMarkTime());
        markBO.setOrderId(cursorData.getStartMarkId());

        // 计算相差距离
        int rows = cursorDistance * pageInfo.getPageSize();
        boolean equals = true;
        if (rows <= queryConfig.getRollSize()) {
            long start = System.currentTimeMillis();
            oceanPageInfo.setPageSize(rows);
            queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, list, markBO, true, indexBucketId);
            long firstPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后取{}条数据", firstPageTime - start, rows);
        } else {
            int times = rows / queryConfig.getRollSize();
            for (int i = 0; i < times; i++) {
                long start = System.currentTimeMillis();
                oceanPageInfo.setPageSize(queryConfig.getRollSize());
                List<AllStructCoreData> newList = new ArrayList<>();
                queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, equals, indexBucketId);
                // 上一页的最后一条作为下一次查询的开始时间
                if (CollectionUtils.isNotEmpty(newList)) {
                    AllStructCoreData lastData = newList.get(newList.size() - 1);
                    markBO.setOrderId(lastData.getDataId());
                    markBO.setMarkTime(lastData.getCaptureTime());
                    list.addAll(newList);
                    equals = false;
                }
                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后取{}条数据", firstPageTime - start, queryConfig.getRollSize());

            }
            int lastRows = rows % queryConfig.getRollSize();
            if (lastRows > 0) {

                long start = System.currentTimeMillis();
                oceanPageInfo.setPageSize(lastRows);
                if (CollectionUtils.isNotEmpty(list)) {
                    AllStructCoreData lastData = list.get(list.size() - 1);
                    markBO.setOrderId(lastData.getDataId());
                    markBO.setMarkTime(lastData.getCaptureTime());
                }
                List<AllStructCoreData> newList = new ArrayList<>();
                queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, false, indexBucketId);
                if (CollectionUtils.isNotEmpty(newList)) {
                    list.addAll(newList);
                }
                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后取{}条数据", firstPageTime - start, lastRows);

            }
        }
        // 获取具体的数据
        if (CollectionUtils.isNotEmpty(list)) {
            long start = System.currentTimeMillis();
            List<AllStructCoreData> collect = list.stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime, Comparator.reverseOrder())
                    .thenComparing(AllStructCoreData::getDataId, Comparator.reverseOrder())).collect(Collectors.toList());
            AllStructCoreData lastData = collect.get(collect.size() - 1);
            MarkBO newMarkBO = new MarkBO();
            newMarkBO.setOrderId(lastData.getDataId());
            newMarkBO.setMarkTime(lastData.getCaptureTime());
            int totalPageSize = (int) (totalRows / pageInfo.getPageSize() + (totalRows % pageInfo.getPageSize() > 0 ? 1 : 0));
            if (totalPageSize == pageInfo.getCurrentPage()) {
                long lastSize = (totalRows % pageInfo.getPageSize() == 0) ? pageInfo.getPageSize() : (totalRows % pageInfo.getPageSize());
                oceanPageInfo.setPageSize((int) lastSize);
            } else {
                oceanPageInfo.setPageSize(pageInfo.getPageSize());
            }
            List<AllStructCoreData> newList = new ArrayList<>();
            queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, newMarkBO, false, indexBucketId);
            if (CollectionUtils.isNotEmpty(newList)) {
                list.addAll(newList);
                eachDetailDataResult.setAllStructCoreData(list);
                eachDetailDataResult.setHitPageCoreData(newList);
            }
            long firstPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后取{}条数据", firstPageTime - start, oceanPageInfo.getPageSize());

        }

        return eachDetailDataResult;
    }


    /**
     * 离第一页近的数据
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param firstDistance
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult buildFirstDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, int firstDistance) {
        EachDetailDataResult eachDetailDataResult = new EachDetailDataResult();
        eachDetailDataResult.setFromLastPage(false);
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
        MarkBO markBO = null;
        if (firstDistance == 0) {
            long start = System.currentTimeMillis();
            // 如果就是第一页， 直接查第一页数据
            List<AllStructCoreData> newList = new ArrayList<>();
            queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, pageInfo, newList, null, true, indexBucketId);
            if (CollectionUtils.isNotEmpty(newList)) {
                list.addAll(newList);
                eachDetailDataResult.setAllStructCoreData(list);
                eachDetailDataResult.setHitPageCoreData(newList);
            }
            long firstPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 查第一页{}条数据", firstPageTime - start, pageInfo.getPageSize());
        } else {
                        /*
                        先查中间数据，然后查具体数据
                         */
            // 先查中间数据
            int rows = firstDistance * pageInfo.getPageSize();
            if (rows <= queryConfig.getRollSize()) {
                long start = System.currentTimeMillis();
                // 小于配置一次查询条数
                oceanPageInfo.setPageSize(rows);
                queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, list, null, true, indexBucketId);
                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后查{}条数据", firstPageTime - start, rows);
            } else {
                // 分几页处理
                int times = rows / queryConfig.getRollSize();
                // 第一页要包含
                boolean isAllFirstPage = true;
                for (int i = 0; i < times; i++) {
                    long start = System.currentTimeMillis();
                    oceanPageInfo.setPageSize(queryConfig.getRollSize());
                    List<AllStructCoreData> newList = new ArrayList<>();
                    queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, isAllFirstPage, indexBucketId);
                    if (CollectionUtils.isNotEmpty(newList)) {
                        markBO = new MarkBO();
                        isAllFirstPage = false;
                        AllStructCoreData lastData = newList.get(newList.size() - 1);
                        markBO.setOrderId(lastData.getDataId());
                        markBO.setMarkTime(lastData.getCaptureTime());
                        list.addAll(newList);
                    }

                    long firstPageTime = System.currentTimeMillis();
                    log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后查{}条数据", firstPageTime - start, queryConfig.getRollSize());
                }
                int lastRows = rows % queryConfig.getRollSize();
                if (lastRows != 0) {
                    long start = System.currentTimeMillis();
                    List<AllStructCoreData> newList = new ArrayList<>();
                    oceanPageInfo.setPageSize(lastRows);
                    if (CollectionUtils.isNotEmpty(list)) {
                        isAllFirstPage = false;
                        markBO = new MarkBO();
                        AllStructCoreData lastData = list.get(list.size() - 1);
                        markBO.setOrderId(lastData.getDataId());
                        markBO.setMarkTime(lastData.getCaptureTime());
                    }
                    queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, markBO, isAllFirstPage, indexBucketId);
                    if (CollectionUtils.isNotEmpty(newList)) {
                        list.addAll(newList);
                    }
                    long firstPageTime = System.currentTimeMillis();
                    log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 从前往后查{}条数据", firstPageTime - start, lastRows);

                }
            }
            // 获取具体的那一页值
            if (CollectionUtils.isNotEmpty(list)) {
                long start = System.currentTimeMillis();
                List<AllStructCoreData> collect = list.stream().sorted(Comparator.comparing(AllStructCoreData::getCaptureTime, Comparator.reverseOrder())
                        .thenComparing(AllStructCoreData::getDataId, Comparator.reverseOrder())).collect(Collectors.toList());
                AllStructCoreData lastData = collect.get(collect.size() - 1);
                MarkBO newMarkBO = new MarkBO();
                newMarkBO.setOrderId(lastData.getDataId());
                newMarkBO.setMarkTime(lastData.getCaptureTime());
                List<AllStructCoreData> newList = new ArrayList<>();
                queryFirstPageESCommon(indexDOList, queryCHBO, deviceIds, pageInfo, newList, newMarkBO, false, indexBucketId);
                if (CollectionUtils.isNotEmpty(newList)) {
                    list.addAll(newList);
                    eachDetailDataResult.setAllStructCoreData(list);
                    eachDetailDataResult.setHitPageCoreData(newList);
                }

                long firstPageTime = System.currentTimeMillis();
                log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 查询具体位置{}条数据", firstPageTime - start, pageInfo.getPageSize());

            }

        }
        return eachDetailDataResult;
    }


    /**
     * 从后取值
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     * @param lastDistance
     * @param supportQuery
     * @return
     */
    private <T extends AbstractQueryBO> EachDetailDataResult buildLastDistanceData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows, Integer lastDistance) {

        EachDetailDataResult eachDetailDataResult = new EachDetailDataResult();
        eachDetailDataResult.setFromLastPage(true);
        List<AllStructCoreData> list = new ArrayList<>();
        OceanPageInfo oceanPageInfo = OceanPageInfo.buildPageInfo();
        // 默认为传入的值
        oceanPageInfo.setCurrentPage(pageInfo.getCurrentPage());
        oceanPageInfo.setPageSize(pageInfo.getPageSize());
        MarkBO markBO = null;
        long lastSize = (totalRows % pageInfo.getPageSize() == 0) ? pageInfo.getPageSize() : (totalRows % pageInfo.getPageSize());
        if (lastDistance == 0) {
            long start = System.currentTimeMillis();
                        /*
                         就是最后一页
                         */
            // 算最后一页的页码
            List<AllStructCoreData> newList = new ArrayList<>();
            oceanPageInfo.setPageSize((int) lastSize);
            queryLastPageESCommon(indexDOList, queryCHBO, deviceIds, oceanPageInfo, newList, null, true, indexBucketId);
            if (CollectionUtils.isNotEmpty(newList)) {
                list.addAll(newList);
                eachDetailDataResult.setAllStructCoreData(list);
                eachDetailDataResult.setHitPageCoreData(newList);
            }
            long lastPageTime = System.currentTimeMillis();
            log.debug("[ES分页查询]pageDetail-获取所有ID耗时：{}ms 查询最后一页{}条数据", lastPageTime - start, lastSize);

        } else {
                        /*
                         不是最后一页
                         */
            long rows = (lastDistance - 1) * pageInfo.getPageSize() + lastSize;
            buildLastPage(indexDOList, queryCHBO, deviceIds, pageInfo, indexBucketId, eachDetailDataResult, list, oceanPageInfo, markBO, rows);
        }

        return eachDetailDataResult;
    }

    /**
     * 不包含游标查询
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param pageInfo
     * @param cursorData
     * @param indexBucketId
     * @param totalRows
     */
    private <T extends AbstractQueryBO> EachDetailDataResult builderDataWithNoCursor(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo pageInfo, RedisCursorData cursorData, Integer indexBucketId, Long totalRows) {
                    /*
                     确定当前页距离第一页和最后一页，哪个更近
                     */
        // 距离第一页的页码
        int firstDistance = pageInfo.getCurrentPage() - 1;
        // 总数
        int totalPages = (int) (totalRows / pageInfo.getPageSize() + (totalRows % pageInfo.getPageSize() > 0 ? 1 : 0));
        // 距离最后一页的页码
        int lastDistance = totalPages - pageInfo.getCurrentPage();
        if (firstDistance < lastDistance) {
            // 离第一页近，从前往后取值
            EachDetailDataResult eachDetailDataResult = buildFirstDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, firstDistance);
            eachDetailDataResult.setStartPage(1);
            eachDetailDataResult.setEndPage(pageInfo.getCurrentPage());
            return eachDetailDataResult;
        } else {
            // 离最后一页近
            EachDetailDataResult eachDetailDataResult = buildLastDistanceData(indexDOList, queryCHBO, deviceIds, pageInfo, cursorData,
                    indexBucketId, totalRows, lastDistance);
            eachDetailDataResult.setStartPage(pageInfo.getCurrentPage());
            eachDetailDataResult.setEndPage(totalPages);
            return eachDetailDataResult;
        }
    }


    /**
     * 查第一页
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param oceanPageInfo
     * @param list
     * @param markBO
     */
    private <T extends AbstractQueryBO> void queryFirstPageESCommon(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo oceanPageInfo, List<AllStructCoreData> list, MarkBO markBO, Boolean isAllFirstPage, Integer indexBucketId) {

        queryMethodExtendService.firstPageResult(indexDOList, queryCHBO, deviceIds, oceanPageInfo, markBO, isAllFirstPage);
    }


    /**
     * 查第一页
     *
     * @param indexDOList
     * @param queryCHBO
     * @param deviceIds
     * @param oceanPageInfo
     * @param list
     * @param markBO
     */
    private <T extends AbstractQueryBO> void queryLastPageESCommon(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo oceanPageInfo, List<AllStructCoreData> list, MarkBO markBO, Boolean isAllFirstPage, Integer indexBucketId) {
        List<Map<String, Object>> maps = queryMethodExtendService.lastPageData(indexDOList, queryCHBO, deviceIds, oceanPageInfo, markBO, isAllFirstPage);
        if (CollectionUtils.isNotEmpty(maps)) {
            maps.stream().forEach(map -> {
                AllStructCoreData coreData = new AllStructCoreData();
                final BeanMap beanMap = BeanMap.create(coreData);
                beanMap.putAll(map);
                coreData.setIndexBucketId(indexBucketId);
                list.add(coreData);
            });
        }
    }

}
