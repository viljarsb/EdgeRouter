package com.mms.EdgeRouter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication
public class EdgeRouterApplication
{
    public static void main(String[] args)
    {
        log.info("Starting EdgeRouter");
        SpringApplication.run(EdgeRouterApplication.class, args);
    }
}
