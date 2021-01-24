package cn.net.iset.elasticsearch.bean._do;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName:
 * @Author:jiangjipeng
 * @Date:Created in 3:28 下午 2020/11/18
 * @Description:
 * @Version:
 * @Email:jiangjipeng@cloudwalk.cn
 */
@Data
@Component
public class DeviceAndCountDO {
    /**
     *查询出来的数量
     */
    private Long count;

    /**
     * 设备列表
     */
    private List<Long> deviceIds;
}
