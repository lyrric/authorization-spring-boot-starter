package com.github.lyrric.auth.handler;

import com.github.lyrric.auth.annotation.Auth;
import com.github.lyrric.auth.core.BaseAuthUserService;
import com.github.lyrric.auth.propertites.AuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;

/**
 * Created on 2020-04-20.
 * auth拦截器
 * @author wangxiaodong
 */
public class AuthHandler extends HandlerInterceptorAdapter {

    private AuthProperties authProperties;

    private BaseAuthUserService baseAuthUserService;

    public AuthHandler(AuthProperties authProperties, BaseAuthUserService baseAuthUserService) {
        this.authProperties = authProperties;
        this.baseAuthUserService = baseAuthUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断配置是否开启鉴权
        if(authProperties.getOpen()){
            if(handler instanceof HandlerMethod){
                Auth auth = ((HandlerMethod) handler).getMethod().getAnnotation(Auth.class);
                if(auth == null){
                    return true;
                }
                //进行登录校验
                if(!isLogin()){
                    print(baseAuthUserService.msgWithoutLogin(), response);
                    return false;
                }
                int[] codes = auth.resources();
                if(!auth(codes)){
                    print(baseAuthUserService.msgWithoutPermission(), response);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否登录判断
     * @return
     */
    private boolean isLogin(){
        return baseAuthUserService.getUserResources() != null;
    }

    /**
     * 资源鉴权
     * @param codes
     * @return
     */
    private boolean auth(int[] codes) {
        if(codes == null || codes.length == 0){
            return true;
        }
        Set<Integer> userCodes = baseAuthUserService.getUserResources();
        return Arrays.stream(codes).anyMatch(userCodes::contains);
    }

    /**
     * 鉴权失败时，返回json
     * @param json
     * @return
     */
    private void print(String json, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json;charset=UTF-8");
        writer.write(json);
        writer.flush();
        writer.close();
    }
}
