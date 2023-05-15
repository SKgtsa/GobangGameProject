package com.clankalliance.backbeta.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class RoomCodeGenerator {

    /**
     * key: roomCode
     */
    @Resource
    private RedisTemplate<String,String> redisTemplateRoomCode;

    @PostConstruct
    private void initialize(){
        String target;
        StringBuilder sb;
        for(int i = 1;i < 100000;i ++){
            sb = new StringBuilder();
            sb.append(i);
            while (sb.length() < 5)
                sb.insert(0,0);
            redisTemplateRoomCode.opsForSet().add("roomSet", sb.toString());
        }
    }

    public String getCode(){
        return redisTemplateRoomCode.opsForSet().pop("roomSet");
    }

    public void returnCode(String code){
        int temp;
        try{
            temp = Integer.parseInt(code);
        }catch (Exception e){
            return;
        }
        if(temp > 0 && temp < 100000){
            redisTemplateRoomCode.opsForSet().add("roomSet", code);
        }
    }

}
