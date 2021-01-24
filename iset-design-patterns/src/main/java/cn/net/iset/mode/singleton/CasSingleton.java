package cn.net.iset.mode.singleton;

import java.util.concurrent.atomic.AtomicReference;

/**
 * ClassName: CASSingleton.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/2
 */
public class CasSingleton {

    /**
     * 1.私有化构造函数
     */
    private CasSingleton(){}

    /**
     * 2.借助AtomicReference实现
     */
    private static AtomicReference<CasSingleton> INSTANCE = new AtomicReference<>();

    /**
     * CAS获取单例
     */
    public static CasSingleton getInstance() {
        for(;;) {
            CasSingleton casSingleton =
                    INSTANCE.get();
            if (casSingleton != null) {
                return casSingleton;
            }
            casSingleton = new CasSingleton();
            if (INSTANCE.compareAndSet(null, casSingleton)) {
                return casSingleton;
            }
        }
    }
}
