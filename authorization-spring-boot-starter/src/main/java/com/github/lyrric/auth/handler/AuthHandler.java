package com.github.lyrric.auth.handler;

import com.github.lyrric.auth.annotation.Auth;
import com.github.lyrric.auth.core.BaseAuthUserService;
import com.github.lyrric.auth.propertites.AuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private AntPathMatcher pathMatcher = new AntPathMatcher();

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
                //如果没有加注解，并且也不在配置的拦截url内
                if(auth == null && !checkIncludeUrl(request.getRequestURI())){
                    return true;
                }
                //进行登录校验
                if(!baseAuthUserService.isLogin()){
                    baseAuthUserService.onWithoutLogin(response);
                    return false;
                }
                int[] codes = auth.resources();
                if(!auth(codes)){
                    baseAuthUserService.onWithoutPermission(response);
                    return false;
                }
            }
        }
        return true;
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
     * 判断当前求情url是否在配置的拦截url中
     * @return
     */
    private boolean checkIncludeUrl(String currentUrl){
        String[] includePathPatterns = authProperties.getIncludePathPatterns();
        if(includePathPatterns == null || includePathPatterns.length == 0){
            return false;
        }
        for (String pattern : includePathPatterns) {
            if(pathMatcher.match(pattern, currentUrl)){
                return true;
            }
        }
        return false;
    }

}
