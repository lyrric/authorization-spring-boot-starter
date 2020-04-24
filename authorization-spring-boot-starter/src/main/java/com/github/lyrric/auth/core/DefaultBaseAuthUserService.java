package com.github.lyrric.auth.core;

import com.alibaba.fastjson.JSONObject;
import com.github.lyrric.auth.model.HttpResult;
import com.github.lyrric.auth.propertites.AuthProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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


    public DefaultBaseAuthUserService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }


    @Override
    public void saveUserResources(Set<Integer> resources) {
        //设置cookie
        String userUniqueIdentification = getUserUniqueIdentification();
        if(StringUtils.isEmpty(userUniqueIdentification)){
            userUniqueIdentification = UUID.randomUUID().toString().replaceAll("-", "");
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            Objects.requireNonNull(response).addCookie(new Cookie(authProperties.getCookieKey(), userUniqueIdentification));
        }
        super.saveUserResources(resources);
    }


    @Override
    String getUserUniqueIdentification() {
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
    public void onWithoutLogin(HttpServletResponse response) {
        print(JSONObject.toJSONString(HttpResult.failure("你还没有登录")), response);
    }

    @Override
    public void onWithoutPermission(HttpServletResponse response) {
        print(JSONObject.toJSONString(HttpResult.failure("权限不足")), response);

    }

    /**
     * 鉴权失败时，返回json
     * @param json
     * @return
     */
    private void print(String json, HttpServletResponse response){
        try {
            PrintWriter writer = response.getWriter();
            response.setContentType("application/json;charset=UTF-8");
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
