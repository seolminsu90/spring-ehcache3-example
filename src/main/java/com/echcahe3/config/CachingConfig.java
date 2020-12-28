package com.echcahe3.config;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

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
        return jCacheCacheManager;
    }

    @Bean
    public JCacheManagerFactoryBean jCacheManagerFactoryBean() throws URISyntaxException {
        JCacheManagerFactoryBean jCacheManagerFactoryBean = new JCacheManagerFactoryBean();
        jCacheManagerFactoryBean.setCacheManagerUri(getClass().getResource("/ehcache.xml").toURI());
        return jCacheManagerFactoryBean;
    }

    @Bean(name = "concatKeyGenerator")
    public KeyGenerator selectTablesKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder KeyNameBuilder = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Map) {
                    KeyNameBuilder.append(params[i].toString());
                } else if (params[i] instanceof List) {
                    List<?> paramsList = (List) params[i];
                    paramsList.stream().forEach(obj -> KeyNameBuilder.append(obj.toString()));
                } else {
                    KeyNameBuilder.append(params[i].toString());
                }

                if (i != params.length - 1)
                    KeyNameBuilder.append(",");
            }
            System.out.println(method.getName() + " : " + KeyNameBuilder.toString());
            return KeyNameBuilder.toString();
        };
    }
}