package com.github.lyrric.auth;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.github.lyrric.auth.core.BaseAuthUserService;
import com.github.lyrric.auth.core.DefaultBaseAuthUserService;
import com.github.lyrric.auth.handler.AuthHandler;
import com.github.lyrric.auth.propertites.AuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created on 2020-04-20.
 *
 * @author wangxiaodong
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
@ComponentScan(basePackages = "com.github.lyrric.auth")
public class AuthConfiguration implements WebMvcConfigurer {

    @Resource
    private AuthProperties authProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisConnectionFactory factory;

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(){
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new FastJsonRedisSerializer<>(Integer.class));
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }
    /**
     * 如果用户没有实现AuthUserService，则使用默认的DefaultAuthUserService
     * @return
     */
    @ConditionalOnMissingBean
    @Bean
    public BaseAuthUserService baseAuthUserService(){
        return new DefaultBaseAuthUserService(authProperties, stringRedisTemplate);
    }

    @Bean
    public AuthHandler authHandler(){
        return new AuthHandler(authProperties, baseAuthUserService());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(authHandler());
        String[] includePathPatterns = authProperties.getIncludePathPatterns();
        String[] excludePathPatterns = authProperties.getExcludePathPatterns();
        if(includePathPatterns == null || includePathPatterns.length == 0){
            registration.addPathPatterns("/**");
        }else{
            registration.addPathPatterns(Arrays.asList(includePathPatterns));
        }
        if(excludePathPatterns != null && excludePathPatterns.length != 0){
            registration.excludePathPatterns(Arrays.asList(excludePathPatterns));
        }
    }
}
