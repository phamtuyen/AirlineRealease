package com.mbv.ticketsystem.airline.support;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;

public class RedisAirFarePriceCache implements AirFarePriceCache {

    private RedisTemplate<String, AirFarePriceInfos> template;
    private long timeout = 300;
    private final String prefix = "FarePriceInfos:";

    public void setConnectionFactory(RedisConnectionFactory connectionFactory) {
        template = new RedisTemplate<String, AirFarePriceInfos>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<AirFarePriceInfos>(AirFarePriceInfos.class));
        template.afterPropertiesSet();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public AirFarePriceInfos find(String id) {
        System.out.println(prefix + id);
        return template.opsForValue().get(prefix + id);
    }
    
    public Date findByFare(String id)
    {
    	Date dateCurent = new Date();
    	return dateCurent;
    }
    
    public void deleteId(String id){
    	
    }
    
    public boolean hasResult(String id) {
        return template.hasKey(prefix + id);
    }

    public void update(String id, AirFarePriceInfos result) {
        template.opsForValue().set(prefix + id, result, timeout, TimeUnit.SECONDS);
    }
}
