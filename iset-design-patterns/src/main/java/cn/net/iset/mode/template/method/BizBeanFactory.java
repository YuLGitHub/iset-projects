package cn.net.iset.mode.template.method;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * ClassName: BizBeanFactory.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/24
 */
@Service
public class BizBeanFactory  implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * 获取处理对象
     *
     * @param request
     * @param clazz
     * @param <T>
     * @param <R>
     * @return
     */
    public <T extends AbstractBiz, R extends Object> T getBiz(R request, Class<T> clazz) {
        T biz = this.applicationContext.getBean(clazz);
        biz.setRequest(request);
        biz.init();
        return biz;
    }
}
