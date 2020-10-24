package com.itheima.web.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.domain.system.Module;
import com.itheima.domain.system.User;
import com.itheima.service.system.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class LoginController extends BaseController{

	@Reference
	private UserService userService;
	/**
	 * @description 用户登陆认证
	 * @author mryhl
	 * @date 2020/10/11 16:25 No such property: code for class: Script1
	 * @return
	 */
	@RequestMapping("/login")
	public String login(String email,String password) {
		// 1. 封装email和password为Token
		AuthenticationToken authenticationToken = new UsernamePasswordToken(email, new Md5Hash(password, email, 2).toString());
		// 2. 获取subject,并且调用login方法
		// 通过SecurityUtils获取subject
		Subject subject = SecurityUtils.getSubject();
		/**
		 * @author mryhl
		 * 调用login方法, 传入token.
		 * 并对此进行捕获异常
		 */
		try {
			subject.login(authenticationToken);
			// 登陆成功
			User user = (User) subject.getPrincipal();
			// 通过则保存用户数据
			session.setAttribute("loginUser",user);
			//根据用户查询对应的权限
			List<Module> moduleList=userService.findModuleByUser(user);
			session.setAttribute("modules", moduleList);
			return "redirect:/home/main.do";

		} catch (AuthenticationException e){
				request.setAttribute("error", "用户名或密码错误");
				return "forward:/login.jsp";
		}

	}

    @RequestMapping("/home/main")
    public String main(){
        return "home/main";
    }

    @RequestMapping("/home/home")
    public String home(){
	    return "home/home";
    }

    /**
     * @description 退出登陆
     * @author mryhl
     * @date 2020/10/11 16:22 No such property: code for class: Script1
     * @return
     */
    @RequestMapping(value = "/logout",name="用户登出")
    public String logout(){
	    //登出
        SecurityUtils.getSubject().logout();
        return "forward:login.jsp";
    }
}
