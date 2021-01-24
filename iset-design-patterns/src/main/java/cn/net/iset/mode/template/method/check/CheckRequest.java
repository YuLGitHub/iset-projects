package cn.net.iset.mode.template.method.check;


import cn.net.iset.mode.template.method.ICheckFunction;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: CheckRequest.java
 * Description:
 *
 * @author yule
 * @date 2021/01/15
 */
@Slf4j
public class CheckRequest implements ICheckFunction {

    private Long startTime;

    private Long endTime;

    public CheckRequest(Long startTime, Long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public void check() {
        // todo check
        if (startTime > endTime) {
            log.error("start time is large then end time, so that exist the system");
        }
    }
}
