package com.mms.EdgeRouter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
public class EdgeRouterApplication
{
    public static void main(String[] args)
    {
        log.info("Starting MMS Edge Router");
        SpringApplication.run(EdgeRouterApplication.class, args);
    }
}
