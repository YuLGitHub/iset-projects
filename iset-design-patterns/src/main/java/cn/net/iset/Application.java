package cn.net.iset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;

/**
 * ClassName: Application.java
 * Description:
 *
 * @author yule1
 * @since 2021/1/2
 */
@Slf4j
public class Application {

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    /**
     * application start
     *
     * @param args arguments
     */
    public static void main(String[] args) throws InterruptedException {

        log.info("开始启动!");
        ApplicationContext ctx = new SpringApplicationBuilder()
                .sources(Application.class)
                .web(WebApplicationType.NONE).bannerMode(Banner.Mode.LOG)
                .run(args);
        log.info("项目启动!");
        CountDownLatch closeLatch = ctx.getBean(CountDownLatch.class);
        closeLatch.await();
    }

}
