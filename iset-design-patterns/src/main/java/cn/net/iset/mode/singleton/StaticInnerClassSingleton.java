package cn.net.iset.mode.singleton;

/**
 * ClassName: InnerClassSingleton.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/2
 */
public class StaticInnerClassSingleton {

    /**
     * 构建静态内部类
     */
    private static class StaticInnerClass {
        private static final StaticInnerClassSingleton INSTANCE = new StaticInnerClassSingleton();
    }

    /**
     * 私有化构造函数
     */
    private StaticInnerClassSingleton(){}

    /**
     * 创建获取参数
     */
    public static StaticInnerClassSingleton getInstance(){
        return StaticInnerClass.INSTANCE;
    }

}
