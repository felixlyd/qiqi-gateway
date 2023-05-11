package com.github.felixlyd;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * class QiqiMetricApplication: do something
 *
 * @author : liuyaodong
 * @date 2023/5/11
 */
@SpringBootApplication
@EnableAdminServer
public class QiqiMetricApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiqiMetricApplication.class, args);
    }
}
