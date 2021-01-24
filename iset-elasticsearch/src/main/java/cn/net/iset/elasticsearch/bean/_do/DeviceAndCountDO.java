package cn.net.iset.elasticsearch.bean._do;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

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
