package cn.net.iset.mode.singleton;

/**
 * ClassName: HungrySingleton.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/2
 */
public class HungrySingleton {

    /**
     * 1.构建私有构造函数
     */
    private HungrySingleton() {

    }

    /**
     * 2.实例私有化
     */
    private static final HungrySingleton INSTANCE = new HungrySingleton();

    /**
     * 3.提供方法进行获取
     */
    public static HungrySingleton getInstance() {
        return INSTANCE;
    }

}
