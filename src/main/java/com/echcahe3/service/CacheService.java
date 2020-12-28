package com.echcahe3.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Cacheable(value = "getTest", keyGenerator = "concatKeyGenerator")
    public Map<String, Object> getTest1() {
        System.out.println("로직 수행중 ..");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, Object> testObj = new HashMap<>();
        testObj.put("안녕", "123");
        return testObj;
    }

    @Cacheable(value = "getTest", keyGenerator = "concatKeyGenerator")
    public Map<String, Object> getTest2(String Other) {
        System.out.println("로직 수행중 ..");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, Object> testObj = new HashMap<>();
        testObj.put("안녕", "5476");
        testObj.put("Other", Other);
        return testObj;
    }

    @Cacheable(value = "getTest", keyGenerator = "concatKeyGenerator")
    public Map<String, Object> getTest3(String Other, Map<String, Object> params) {
        System.out.println("로직 수행중 ..");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, Object> testObj = new HashMap<>();
        testObj.put("안녕", "568");
        testObj.put("Other", Other);
        testObj.put("Params", params);
        return testObj;
    }
}
