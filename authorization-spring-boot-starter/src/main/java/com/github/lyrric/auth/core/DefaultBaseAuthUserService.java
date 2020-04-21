package com.github.lyrric.auth.core;

import com.alibaba.fastjson.JSONObject;
import com.github.lyrric.auth.model.HttpResult;
import com.github.lyrric.auth.propertites.AuthProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 2020-04-20.
 *
 * @author wangxiaodong
 */
public class DefaultBaseAuthUserService extends BaseAuthUserService {

    private AuthProperties authProperties;

    private StringRedisTemplate redisTemplate;

    public DefaultBaseAuthUserService(AuthProperties authProperties, StringRedisTemplate redisTemplate) {
        this.authProperties = authProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveUserResources(String userUniqueIdentification, Set<Integer> resources) {
        super.saveUserResources(userUniqueIdentification, resources);
        //保存用户登录信息
        String uuid = UUID.randomUUID().toString();
        String key = authProperties.getRedisPreFix().concat("login:").concat(uuid);
        redisTemplate.opsForValue().set(key, userUniqueIdentification, authProperties.getMaxTime(), TimeUnit.SECONDS);
        //设置cookie
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        Objects.requireNonNull(response).addCookie(new Cookie(authProperties.getCookieKey(), uuid));
    }

    @Override
    String getUserUniqueIdentification() {
        String token = getToken();
        if(StringUtils.isEmpty(token)){
            return null;
        }
        String key = authProperties.getRedisPreFix().concat("login:").concat(token);
        return redisTemplate.opsForValue().get(key);
    }

    private String getToken(){
        //从cookie中获取uuid
        AtomicReference<String> token = new AtomicReference<>();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        if (request.getCookies() == null || request.getCookies().length == 0) {
            return null;
        }
        Arrays.stream(request.getCookies())
                .filter(cookie -> authProperties.getCookieKey().equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> token.set(cookie.getValue()));
        return token.get();
    }

    @Override
    public String msgWithoutLogin() {
        return JSONObject.toJSONString(HttpResult.failure("你还没有登录"));
    }

    @Override
    public String msgWithoutPermission() {
        return JSONObject.toJSONString(HttpResult.failure("权限不足"));
    }
}
