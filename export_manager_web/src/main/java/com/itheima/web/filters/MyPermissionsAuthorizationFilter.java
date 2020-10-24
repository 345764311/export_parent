package com.itheima.web.filters;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @description:
 * @author: mryhl
 * @date: Created in 2020/10/11 18:02
 * @version:
 * @modified By:
 */
public class MyPermissionsAuthorizationFilter extends AuthorizationFilter {
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		//subject里面就有当前用户的权限    ["查看部门","新增部门","修改部门"]
		Subject subject = getSubject(request, response);
		//访问资源需要的权限 ["新增部门","删除部门"]
		String[] perms = (String[]) mappedValue;

		//如果没有配置,放行
		if (perms == null || perms.length == 0) {
			return true;
		}

		for (String perm : perms) {
			if (subject.isPermitted(perm)) {
				return true;
			}
		}

		return false;
	}
}
