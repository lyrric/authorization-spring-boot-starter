package com.github.lyrric.auth.annotation;

import java.lang.annotation.*;

/**
 * Created on 2020-04-20.
 * 方法级权限注解，在controller层方法中加入此注解
 * 1.如果未加注解，则该接口不鉴权
 * 2.如果加了注解，未赋值code，则为登录即可访问
 * 3.如果加了注解，并且赋值code，则根据code鉴权
 * @author wangxiaodong
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auth {

    /**
     * 需要的权限代码列表，如有多个，则关系为‘或’
     * @return
     */
    int[] resources() default {};
}
