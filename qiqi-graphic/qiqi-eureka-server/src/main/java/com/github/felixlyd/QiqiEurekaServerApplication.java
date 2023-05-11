package com.github.felixlyd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * class QiqiEurekaServerApplication: do something
 *
 * @author : liuyaodong
 * @date 2023/5/11
 */
@SpringBootApplication
@EnableEurekaServer
public class QiqiEurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiqiEurekaServerApplication.class, args);
    }
}
