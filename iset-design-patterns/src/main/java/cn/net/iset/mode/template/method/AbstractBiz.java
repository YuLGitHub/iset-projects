package cn.net.iset.mode.template.method;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 模板方法类
 * @author yule
 * @param <I>
 * @param <O>
 */
@Slf4j
public abstract class AbstractBiz<I, O> {

    /**
     * 入参
     */
    protected I request;


    public I getRequest() {
        return request;
    }

    public void setRequest(I request) {
        this.request = request;
    }


    /**
     * 可在参数设置后进行上下文初始化
     */
    public void init(){
        // todo init something
    }

    /**
     * invoke
     * @return
     */
    public abstract O invoke();


    protected void handleCheck() {

        List<ICheckFunction> checkFunctions = this.getCheckFunctions();
        if (null != checkFunctions) {
            for (ICheckFunction check : checkFunctions) {
                if (null != check) {
                    check.check();
                }
            }
        }
    }

    /**
     * getCheckFunctions
     * @return
     */
    protected abstract List<ICheckFunction> getCheckFunctions();
}
