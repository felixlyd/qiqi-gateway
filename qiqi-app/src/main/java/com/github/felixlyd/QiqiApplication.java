package com.github.felixlyd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * class QiqiApplication: do something
 *
 * @author : liuyaodong
 * @date 2023/5/9
 */
@EnableDiscoveryClient
@SpringBootApplication
public class QiqiApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiqiApplication.class,args);
    }
}
