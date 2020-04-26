# 欢迎使用方法级权限控制器（authorization-spring-boot-starter）

**方法级权限控制器（依赖于Spring Boot）**


authorization依赖于Spring Boot的，它可以实现方法级的权限控制，为每个接口分配不同的权限


## authorization简介

*authorization只是实现了权限的控制，并没有实现权限的分配，这是因为在不同的业务系统中，权限分配是和业务紧密联系的，无法实现一个权限管理系统来满足所有的需求，及时是功能相近的权限管理系统，其实现的逻辑也是不可通用的。*  

> authorization依赖了spring-data-redis，目的是为了存储用户对于的资源权限，从而可以快速的进行权限校验，因此项目中必须配置好redis*

> authorization依赖了Spring Boot，因此只能在Spring Boot中使用，并且依赖的Srping Boot的版本是2.2.6.RELEASE，在其它版本中可能会存在未知问题*


## authorization详细介绍

### 权限控制类型

1. 无需认证类型（默认类型），此类型不需要做任何额外操作

2. 登录即可访问类型，该类型只需要在接口加上@Auth即可
```javascript
    @ApiOperation(value = "登录即可访问的接口")
    @GetMapping(value = "/login-required")
    @Auth
    String loginRequired(){
        return "你成功访问了该接口";
    }
```
3. 登录且需要拥有指定资源的权限，加入@Auth注解，并且为注解的resources赋值，如下面的代码表示的是登录并且拥有codes（2，3，4）任意之一资源权限，才可访问接口
```javascript
    @ApiOperation(value = "登录，并且拥有codes（2，3，4）任意之一权限，才可访问的接口")
    @GetMapping(value = "/multi-resources-required")
    @Auth(resources = {2,3,4})
    String multiResourcesRequired(){
        return "你成功访问了该接口";
    }
```


### 使用方法
1. 在Spring Boot项目中加入maven依赖
```html
<dependency>
	<groupId>com.github.lyrric</groupId>
	<artifactId>authorization-spring-boot-starter</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
2. 配置好你的redis

3. 在登录接口中注入BaseAuthUserService，并且调用saveUserResources方法（参数为用户拥有的资源列表，@Auth(resources = {2,3,4})注解中resources参数对应这个参数）
```javascript
    @ApiOperation(value = "登录")
    @PostMapping(value = "/login")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", paramType = "query",  dataType = "int", defaultValue = "test"),
            @ApiImplicitParam(value = "权限资源列表，数字，以逗号分割", name = "resourcesStr", paramType = "query",  dataType = "String", defaultValue = "2,3")
    })
    String login(String username, String resourcesStr){
        resourcesStr = resourcesStr.replace("，", ",");
        Set<Integer> resources = Arrays.stream(resourcesStr.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
        //需要手动保存一下用户权限
        baseAuthUserService.saveUserResources(resources);
        return "登录成功";
    }
```

5. 在注销登录接口中，注入BaseAuthUserService，并调用logout方法

4. 根据需求，在接口中加入@auth注解，启动你的项目就可以体验到默认的权限控制了。

### 配置说明
	auth:
		include-path-patterns: #要拦截的url，支持通配符（登录即可访问权限）
		open: #总开关，默认为true
		max-time: #用户资源保存时间，默认为12小时，单位秒，此值请于系统的用户登录有效期保存一致
		redis-pre-fix: #数据存储在redis中的前缀，默认为auth:user:
		cookie-key: #cookie的key，默认为auth-key
