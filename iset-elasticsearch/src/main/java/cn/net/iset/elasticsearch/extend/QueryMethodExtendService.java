package cn.net.iset.elasticsearch.extend;

import cn.cloudwalk.ocean.common.result.page.OceanPageInfo;
import cn.net.iset.elasticsearch.bean.IndexDO;
import cn.net.iset.elasticsearch.bean._do.CountInIndexGroupResultDO;
import cn.net.iset.elasticsearch.bean.bo.AbstractQueryBO;
import cn.net.iset.elasticsearch.bean.bo.MarkBO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: QueryMethodExtendService.java
 * Description: 
 *
 * @author yule1@cloudwalk.com
 * @date 2020/12/24
 */
public interface QueryMethodExtendService {

    /**
     * 根据时间获取ES索引
     * @param captureTimeB
     * @param captureTimeE
     * @return
     */
    List<IndexDO> calculateEsIndex(Long captureTimeB, Long captureTimeE);

    /**
     * 根据索引获取分设备count
     * @param index
     * @param queryCHBO
     * @param deviceIds
     * @param <T>
     * @return
     */
    <T extends AbstractQueryBO> CountInIndexGroupResultDO getDeviceCountData(List<IndexDO> index, T queryCHBO, List<Long> deviceIds);

    <T extends AbstractQueryBO> void firstPageResult(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo oceanPageInfo, MarkBO markBO, Boolean isAllFirstPage);

    <T extends AbstractQueryBO>  List<Map<String, Object>> lastPageData(List<IndexDO> indexDOList, T queryCHBO, List<Long> deviceIds, OceanPageInfo oceanPageInfo, MarkBO markBO, Boolean isAllFirstPage);

    List<Map<String, Object>> getDataById(List<String> indexNames, Set<String> targetFields, List<Long> collect);
}
