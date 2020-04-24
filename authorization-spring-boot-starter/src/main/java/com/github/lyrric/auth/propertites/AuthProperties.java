package com.github.lyrric.auth.propertites;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created on 2020-04-20.
 *
 * @author wangxiaodong
 */
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 总开关，默认为true
     */
    private Boolean open = true;
    /**
     * 用户资源保存时间，默认为12小时，单位秒
     */
    private Long maxTime = 60*60*12L;
    /**
     * 数据存储在redis中的前缀
     */
    private String redisPreFix = "auth:user:";
    /**
     * cookie的key
     */
    private String cookieKey = "auth-key";
    /**
     * 要拦截的url，支持通配符
     */
    private String[] includePathPatterns;

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Long maxTime) {
        this.maxTime = maxTime;
    }

    public String getRedisPreFix() {
        return redisPreFix;
    }

    public void setRedisPreFix(String redisPreFix) {
        this.redisPreFix = redisPreFix;
    }

    public String getCookieKey() {
        return cookieKey;
    }

    public void setCookieKey(String cookieKey) {
        this.cookieKey = cookieKey;
    }

    public String[] getIncludePathPatterns() {
        return includePathPatterns;
    }

    public void setIncludePathPatterns(String[] includePathPatterns) {
        this.includePathPatterns = includePathPatterns;
    }
}
