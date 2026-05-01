package com.app.ChromaDress.core.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(redisCacheConfiguration()).build();
  }

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(7))
        .disableCachingNullValues();
  }
}
