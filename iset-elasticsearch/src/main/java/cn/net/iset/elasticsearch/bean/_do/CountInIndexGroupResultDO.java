package cn.net.iset.elasticsearch.bean._do;

import cn.net.iset.elasticsearch.bean.IndexDO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CountInIndexGroupResultDO implements Comparable<CountInIndexGroupResultDO> {

    /**
     * 设备列表和查询个数的结果对象
     */
    private DeviceAndCountDO deviceAndCountDO;

    /**
     * 索引列表
     */
    private List<IndexDO> indexDOList;

    /**
     * 当前索引所在桶编号
     */
    private Integer indexBucketId;


    /**
     * 根据索引timeTag进行倒序排序
     * @param o
     * @return
     */
    @Override
    public int compareTo(CountInIndexGroupResultDO o) {
        Long sourceTimeTag = 0L;
        for (IndexDO indexDO : this.getIndexDOList()) {
            Long timeTag = indexDO.getOrderId();
            if (timeTag != null) {
                sourceTimeTag += timeTag;
            }
        }

        Long targetTimeTag = 0L;
        for (IndexDO indexDO : o.getIndexDOList()) {
            Long timeTag = indexDO.getOrderId();
            if (timeTag != null) {
                targetTimeTag += timeTag;
            }
        }
        long start = sourceTimeTag / (this.getIndexDOList().size() != 0 ? this.getIndexDOList().size() : 1);
        long end = targetTimeTag / (o.getIndexDOList().size() != 0 ? o.getIndexDOList().size() : 1);

        return (int) (end - start);
    }
}
