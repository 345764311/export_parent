package com.itheima.web.realm;

import com.itheima.domain.system.Module;
import com.itheima.domain.system.User;
import com.itheima.service.system.UserService;
import com.itheima.web.utils.SpringUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.List;

public class SaasRealm extends AuthorizingRealm {


	/**
	 * @description 用户授权
	 * @author mryhl
	 * @date 2020/10/11 16:15
	 * @return simpleAuthorizationInfo 简单授权信息
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		System.out.println("=====================授权=====================");
		UserService userService = (UserService) SpringUtil.getBean("userService");
		// 查询当前用户的权限信息
		User user = (User) principalCollection.getPrimaryPrincipal();
		List<Module> moduleList = userService.findModuleByUser(user);
		// 将信息传递到Shiro
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		for (Module module : moduleList) {
			simpleAuthorizationInfo.addStringPermission(module.getName());
		}
		System.out.println(simpleAuthorizationInfo);
		return simpleAuthorizationInfo;
	}

	/**
	 * @description 用户认证
	 * @author mryhl
	 * @date 2020/10/11 16:56
	 * @return AuthenticationInfo 的实现类 SimpleAuthenticationInfo 简单认证信息
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
		// 进入方法标识
		System.out.println("=====================认证=====================");
		UserService userService = (UserService) SpringUtil.getBean("userService");
		// 强制类型转换
		UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
		// 获取传入的email
		String email = usernamePasswordToken.getUsername();
		// 根据用户进行查询
		User user = userService.findByEmail(email);
		// 判断返回信息
		if (user==null) {
			// 返回空的简单身份认证信息,代表没有找到内容
			return new SimpleAuthenticationInfo();
		}else {
			/**
			 * @author mryhl
			 * SimpleAuthenticationInfo三个参数
			 * Object principal 主角---->user
			 * Object credentials  密码--->user.getPassword()
			 * String realmName 当前realm的名称--->this.getName()
			 */
			return new SimpleAuthenticationInfo(user,user.getPassword(),this.getName());
		}

	}
}
