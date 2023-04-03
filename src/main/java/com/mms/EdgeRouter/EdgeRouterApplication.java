package com.mms.EdgeRouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class EdgeRouterApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(EdgeRouterApplication.class, args);
    }
}
