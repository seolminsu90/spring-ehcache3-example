package com.echcahe3.config;

import java.io.File;
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
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.impl.config.store.heap.DefaultSizeOfEngineProviderConfiguration;
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

        // 여기에캐시 목록 추가
        caches.put("getTest", cacheConfiguration);

        // jsr107CacheManager 공급자
        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching
                .getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");

        // 물리 disk 방식 사용 시 저장 경로
        String tempFolder = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempFolder);

        // 캐시 기본 설정
        DefaultConfiguration configuration = new DefaultConfiguration(caches, provider.getDefaultClassLoader(),
                new DefaultSizeOfEngineProviderConfiguration(100, MemoryUnit.MB, Integer.MAX_VALUE),    // 최대 100MB크기제한, 오브젝트수 무제한 처리
                new DefaultPersistenceConfiguration(tempFile)); // 영속성 물리 disk 설정
 
        return provider.getCacheManager(provider.getDefaultURI(), configuration);
    }

    // 개별 리소스 풀 설정
    @Bean
    public ResourcePools resourcePools() {
        return ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(2000, EntryUnit.ENTRIES)  // 캐시당 2000개 Entry 허용, 사이즈방식 사용 가능
                // .offheap(200, MemoryUnit.MB)  // 오프힙 사용시 선택 (JVM메모리사용)
                .disk(1000, MemoryUnit.MB, true) // 디스크 방식 최대 크기 제한
                .build();
    }

    // 개별 캐시 설정
    @Bean
    public CacheConfiguration<Object, Object> cacheConfiguration(ResourcePools resourcePools) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, resourcePools)
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofSeconds(300))) // 300초 이후 제거됨
                .build();
    }

    // Entry Key 이름을 결정짓는 로직
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
