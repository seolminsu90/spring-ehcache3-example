package com.echcahe3.config;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public JCacheCacheManager jCacheCacheManager(CacheManager cacheManager) {
        return new JCacheCacheManager(cacheManager);
    }

    @Bean(destroyMethod = "close")
    public CacheManager cacheManager(CacheConfiguration<Object, Object> cacheConfiguration) {
        Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();

        caches.put("getTest", cacheConfiguration);

        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching
                .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");

        String tempFolder = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempFolder);

        DefaultConfiguration configuration = new DefaultConfiguration(caches, provider.getDefaultClassLoader(),
                new DefaultPersistenceConfiguration(tempFile));
 
        return provider.getCacheManager(provider.getDefaultURI(), configuration);
    }

    @Bean
    public CacheConfiguration<Object, Object> cacheConfiguration() {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, 
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(2000, EntryUnit.ENTRIES)  // Heap 계층 pool : 2000개 Entity 저장가능
                        .disk(1000, MemoryUnit.MB)      // Disk 계층 pool : 디스크는 1g까지
                        .build())
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(60))) // 1분간 안쓰면 삭제
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(300))) // 최초 생성후 5분 유지
                .withSizeOfMaxObjectSize(100, MemoryUnit.MB)     // 객체 크기는 10MB까지 (기본값 MAX)
                .withSizeOfMaxObjectGraph(Integer.MAX_VALUE)    // 객체 그래프 복잡성 제한 없음 (기본값 1000)
                .build();
    }

    @Bean(name = "concatKeyGenerator")
    public KeyGenerator concatKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder keyNameBuilder = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Map) {
                    keyNameBuilder.append(params[i].toString());
                } else if (params[i] instanceof List) {
                    List<?> paramsList = (List) params[i];
                    paramsList.stream().forEach(obj -> keyNameBuilder.append(obj.toString()));
                } else {
                    keyNameBuilder.append(params[i].toString());
                }

                if (i != params.length - 1)
                    keyNameBuilder.append(",");
            }
            return keyNameBuilder.toString();
        };
    }
}
