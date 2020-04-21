package com.github.lyrric.demo.controller;

import com.github.lyrric.auth.annotation.Auth;
import com.github.lyrric.auth.core.BaseAuthUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wangxiaodong
 * Created by wangxiaodong on 2018/6/6.
 */
@RestController
public class TestController {

    @Resource
    private BaseAuthUserService baseAuthUserService;

    @ApiOperation(value = "登录")
    @PostMapping(value = "/login")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户唯一标识", name = "userId", paramType = "query",  dataType = "int", defaultValue = "10000"),
            @ApiImplicitParam(value = "权限资源列表，数字，以逗号分割", name = "resourcesStr", paramType = "query",  dataType = "String", defaultValue = "2,3")
    })
    String login(Integer userId, String resourcesStr){
        resourcesStr = resourcesStr.replace("，", ",");
        Set<Integer> resources = Arrays.stream(resourcesStr.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
        //需要手动保存一下用户权限
        baseAuthUserService.saveUserResources(userId.toString(), resources);
        return "登录成功";
    }


    @ApiOperation(value = "登录即可访问的接口")
    @GetMapping(value = "/login-required")
    @Auth
    String loginRequired(){
        return "你成功访问了该接口";
    }

    @ApiOperation(value = "登录，并且拥有codes（1）权限，才可访问的接口")
    @GetMapping(value = "/single-resources-required")
    @Auth(resources = {1})
    String singleResourcesRequired(){
        return "你成功访问了该接口";
    }

    @ApiOperation(value = "登录，并且拥有codes（2，3，4）任意之一权限，才可访问的接口")
    @GetMapping(value = "/multi-resources-required")
    @Auth(resources = {2,3,4})
    String multiResourcesRequired(){
        return "你成功访问了该接口";
    }


    @ApiOperation(value = "退出登录")
    @GetMapping(value = "/logout")
    String logout(){
        return "你已退出登录";
    }

}
