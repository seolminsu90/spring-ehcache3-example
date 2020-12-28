package com.echcahe3.config;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.jcache.JCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public JCacheCacheManager jCacheCacheManager(JCacheManagerFactoryBean jCacheManagerFactoryBean) {
        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
        jCacheCacheManager.setCacheManager(jCacheManagerFactoryBean.getObject());

        CacheManager cacheManager = jCacheCacheManager.getCacheManager();
        cacheManager.createCache("getTest", config());

        return jCacheCacheManager;
    }

    private MutableConfiguration<String, Object> config() {
        return new MutableConfiguration<String, Object>()
                    .setTypes(String.class, Object.class)
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES))
                    .setStoreByValue(false);
    }

    @Bean
    public JCacheManagerFactoryBean jCacheManagerFactoryBean() throws URISyntaxException {
        JCacheManagerFactoryBean jCacheManagerFactoryBean = new JCacheManagerFactoryBean();
        //jCacheManagerFactoryBean.setCacheManagerUri(getClass().getResource("/ehcache.xml").toURI());
        return jCacheManagerFactoryBean;
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
            System.out.println(keyNameBuilder.toString());
            return keyNameBuilder.toString();
        };
    }
}
