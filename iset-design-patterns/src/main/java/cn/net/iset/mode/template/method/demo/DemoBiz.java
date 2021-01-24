package cn.net.iset.mode.template.method.demo;

import cn.net.iset.mode.template.method.demo.bean.TemplateMethodParam;
import cn.net.iset.mode.template.method.demo.bean.TemplateMethodResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DemoBiz.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Component
@Scope("prototype")
public class DemoBiz extends DemoAbstractBiz<TemplateMethodParam, List<TemplateMethodResult>> {

    /**
     * 定义结果
     */
    private List<TemplateMethodResult> results;

    @Override
    protected List<TemplateMethodResult> getResult() {
        return results;
    }

    @Override
    protected void doSomething() {
        // todo something
        results = new ArrayList<>();
    }

    @Override
    protected void prepareMethod() {
        // todo prepare method
    }
}
