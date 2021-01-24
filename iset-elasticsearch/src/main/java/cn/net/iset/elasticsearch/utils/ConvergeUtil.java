package cn.net.iset.elasticsearch.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ClassName: ConvergeUtil.java
 * Description:
 *
 * @author yule
 * @date 2020/11/18
 */
public class ConvergeUtil {

    /**
     * 根据对象id和设备id组成数据key
     * @param objectId
     * @param deviceId
     * @return
     */
    public static String createKeyId(String objectId,Long deviceId){
        return String.valueOf(deviceId).concat(objectId);
    }


    /**
     * 集合拆分
     * @param origin
     * @param size
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> divide(List<T> origin, int size) {
        if (CollectionUtils.isEmpty(origin)) {
            return Collections.emptyList();
        }
        int block = (origin.size() + size - 1) / size;
        return IntStream.range(0, block).
                boxed().map(i -> {
            int start = i * size;
            int end = Math.min(start + size, origin.size());
            return origin.subList(start, end);
        }).collect(Collectors.toList());
    }
}
