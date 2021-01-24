package cn.net.iset.mode.template.method.demo;

import cn.net.iset.mode.template.method.AbstractBiz;
import cn.net.iset.mode.template.method.ICheckFunction;

import java.util.List;

/**
 * ClassName: DemoBiz.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
public abstract class DemoAbstractBiz<I, O> extends AbstractBiz<I, O> {

    /**
     * 使用模板调用方法
     * @return
     */
    @Override
    public O invoke() {
        // 方法1
        prepareMethod();

        // 方法2
        doSomething();

        // 返回结果
        return getResult();

    }

    /**
     * 返回结果
     * @return
     */
    protected abstract O getResult();

    /**
     * 操作
     */
    protected abstract void doSomething();

    /**
     * 准备参数
     */
    protected abstract void prepareMethod();

    @Override
    protected List<ICheckFunction> getCheckFunctions() {
        return null;
    }
}
