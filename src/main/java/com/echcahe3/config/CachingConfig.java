package com.echcahe3.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.core.config.DefaultConfiguration;
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
        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
        jCacheCacheManager.setCacheManager(cacheManager);

        return jCacheCacheManager;
    }

    @Bean(destroyMethod = "close")
    public CacheManager cacheManager(CacheConfiguration<Object, Object> cacheConfiguration) {
        Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();

        /**
         * 여기에 관리하는 캐시 목록 추가
         */
        caches.put("getTest", cacheConfiguration);

        // Eh107CacheManager 제공자
        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching
                .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");

        /**
         * ResourcePools Disk Persistence 사용시 매개변수 추가 - 물리데이터로 저장되는 경로
         * 
         * new DefaultPersistenceConfiguration(new File("D:/tempDir")))
         */
        DefaultConfiguration configuration = new DefaultConfiguration(caches, provider.getDefaultClassLoader());

        return provider.getCacheManager(provider.getDefaultURI(), configuration);
    }

    /**
     *  리소스 사용 풀 설정
     */
    @Bean
    public ResourcePools resourcePools() {
        return ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(2000, EntryUnit.ENTRIES)  // 최대 2000개의 오브젝트까지 허용, 용량으로 설정가능
                // .offheap(size, unit) // offHeap GC가 일어나지 않는 Ehcache가 관리하는 메모리(?)
                // .disk(100, MemoryUnit.MB, true) // 물리디스크 저장시 사용
                // 위에서 아래로 티어가 나뉜다. 속도도 위에서 아래로
                .build();
    }

    /**
     * 캐시별로 설정 적용
     */
    @Bean
    public CacheConfiguration<Object, Object> cacheConfiguration(ResourcePools resourcePools) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, resourcePools)
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofSeconds(300)))
                .build();
    }

    /**
     * Key 이름을 정하는 로직 작성
     */
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
            System.out.println(keyNameBuilder.toString());
            return keyNameBuilder.toString();
        };
    }
}
