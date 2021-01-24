package cn.net.iset.elasticsearch.bean.bo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: FilterQueryBO.java
 * Description:
 *
 * @author yule
 * @date 2020/11/17
 */
@Data
public class FilterQueryBO {

    /**
     * 设备ID
     */
    private List<Long> deviceIds;

    /**
     * 用于入参hash key
     */
    private String hashKey;



}
