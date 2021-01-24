package cn.net.iset.mode.template.method.demo;

import cn.net.iset.mode.template.method.BizBeanFactory;
import cn.net.iset.mode.template.method.demo.bean.TemplateMethodParam;
import cn.net.iset.mode.template.method.demo.bean.TemplateMethodResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName: TemplateMethodManger.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Component
public class TemplateMethodManger {

    @Autowired
    private BizBeanFactory bizBeanFactory;

    /**
     * 测试模板方法实现
     * @param param
     * @return
     */
    public List<TemplateMethodResult> getTemplateMethodResult(TemplateMethodParam param) {
        final DemoBiz biz = bizBeanFactory.getBiz(param, DemoBiz.class);
        return biz.invoke();
    }
}
