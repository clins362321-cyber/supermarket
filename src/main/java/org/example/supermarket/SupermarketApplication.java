package org.example.supermarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 不限定扫描包前缀，默认扫描 org.example.supermarket 及其子包
@SpringBootApplication
@MapperScan("org.example.supermarket.dao")
public class SupermarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketApplication.class, args);
    }
}
