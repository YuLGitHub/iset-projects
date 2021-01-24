package cn.net.iset.mode.singleton;

import java.io.Serializable;

/**
 * ClassName: LazySingleton.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/2
 */
public class LazySingleton implements Serializable {
    private static final long serialVersionUID = 4682170584980130729L;

    /**
     * 1.私有化构造函数
     */
    private LazySingleton(){}

    /**
     * 2.提供私有化属性定义
     */
    private volatile static LazySingleton instance;

    /**
     * 3.提供方法进行获取
     */
    public static LazySingleton getInstance() {
        // 双重校验锁
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null)  {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }

    /**
     * 防止序列化对单例的破坏
     *
     * @return
     */
    private Object readResolve() {
        return instance;
    }

}
