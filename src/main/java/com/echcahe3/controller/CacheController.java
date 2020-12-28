package com.echcahe3.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.echcahe3.service.CacheService;

@RestController
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @GetMapping("/test1")
    public Map<String, Object> cacheTest1() {
        return cacheService.getTest1();
    }

    @GetMapping("/test2")
    public Map<String, Object> cacheTest2(@RequestParam String other) {
        return cacheService.getTest2(other);
    }

    @GetMapping("/test3")
    public Map<String, Object> cacheTest3(@RequestParam String other, @RequestParam String other2) {
        Map<String, Object> params = new HashMap<>();
        params.put("other2", other2);
        return cacheService.getTest3(other, params);
    }

}
