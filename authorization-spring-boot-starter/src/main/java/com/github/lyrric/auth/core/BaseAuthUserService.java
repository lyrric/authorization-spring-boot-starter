package com.github.lyrric.auth.core;

import com.github.lyrric.auth.propertites.AuthProperties;
import com.github.lyrric.auth.util.SpringContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020-04-20.
 * 用户信息操作，与redis交互
 * @author wangxiaodong
 */
public abstract class BaseAuthUserService {

    private RedisTemplate<String, Integer> redisTemplate;

    private AuthProperties authProperties;

    @SuppressWarnings("all")
    private void  init() {
        if(redisTemplate == null){
            redisTemplate = SpringContextUtil.getBean("redisTemplate", RedisTemplate.class);
        }
        if(authProperties == null){
            authProperties = SpringContextUtil.getBean(AuthProperties.class);
        }
    }

    /**
     * 保存权限到redis
     * @param resources 权限列表
     */
    public void saveUserResources(Set<Integer> resources){
        init();
        String userUniqueIdentification = getUserUniqueIdentification();
        if(userUniqueIdentification == null){
            throw new NullPointerException("用户唯一标志不能为空");
        }
        String resourceKey = authProperties.getRedisPreFix().concat("resources:").concat(userUniqueIdentification);
        //先清除旧数据
        redisTemplate.delete(resourceKey);
        BoundSetOperations<String, Integer> setOps = redisTemplate.boundSetOps(resourceKey);
        //如果该用户没有任何资源，也要保存用户的登录状态
        if(resources == null || resources.size() == 0){
            setOps.add(-1);
        }else{
            for (Integer resource : resources) {
                setOps.add(resource);
            }
        }
        redisTemplate.expire(resourceKey, authProperties.getMaxTime(), TimeUnit.SECONDS);
    }


    /**
     * 获取权限列表
     */
    public Set<Integer> getUserResources(){
        init();
        String userUniqueIdentification = getUserUniqueIdentification();
        if(userUniqueIdentification == null){
            return null;
        }
        String resourceKey = authProperties.getRedisPreFix().concat("resources:").concat(userUniqueIdentification);
        return redisTemplate.boundSetOps(resourceKey).members();
    }
    /**
     * 获取权限列表
     */
    public Boolean isLogin(){
        init();
        String userUniqueIdentification = getUserUniqueIdentification();
        if(userUniqueIdentification == null){
            return false;
        }
        String resourceKey = authProperties.getRedisPreFix().concat("resources:").concat(userUniqueIdentification);
        return redisTemplate.hasKey(resourceKey);
    }

    /**
     * 注销登录，删除用户redis数据
     */
    public void logout(){
        String userUniqueIdentification = getUserUniqueIdentification();
        if(StringUtils.isNotEmpty(userUniqueIdentification)){
            String resourceKey = authProperties.getRedisPreFix().concat("resources:").concat(userUniqueIdentification);
            redisTemplate.delete(resourceKey);
        }
    }
    /**
     * 获取当前线程的用户唯一标识符
     * 需要重写
     * @return 不存在（未登录）返回null
     */
    abstract String getUserUniqueIdentification();

    /**
     * 未登录时
     * @param response
     * @return 返回的信息，json格式
     */
    abstract public void onWithoutLogin(HttpServletResponse response);

    /**
     * 已登录，但是没有权限时
     * @param response
     * @return 返回的信息，json格式
     */
    abstract public void onWithoutPermission(HttpServletResponse response);


}
