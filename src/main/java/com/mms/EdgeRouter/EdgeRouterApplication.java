package com.mms.EdgeRouter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;


@Slf4j
@SpringBootApplication
public class EdgeRouterApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(EdgeRouterApplication.class, args);
    }
}
