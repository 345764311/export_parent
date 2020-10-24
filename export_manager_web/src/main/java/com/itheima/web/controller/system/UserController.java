package com.itheima.web.controller.system;

import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.system.Dept;
import com.itheima.domain.system.Role;
import com.itheima.domain.system.User;
import com.itheima.service.system.DeptService;
import com.itheima.service.system.RoleService;
import com.itheima.service.system.UserService;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/user")
public class UserController extends BaseController {




	@Reference
	private UserService userService;
	@Reference
	private DeptService deptService;
	@Reference
	private RoleService roleService;

	@Autowired
	private AmqpTemplate template;

	/**
	 * 生成列表
	 */
	@RequestMapping(value = "/list", name = "部门列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {


		PageInfo pageInfo = userService.findByPage(getCompanyId(), pageNum, pageSize);
		request.setAttribute("page", pageInfo);

		return "/system/user/user-list";
	}

	/**
	 * 跳转新增页面
	 */
	@RequestMapping(value = "/toAdd", name = "跳转部门新增页面")
	public String toAdd() {


		// 查询所在部门
		List<Dept> deptList = deptService.findAll(getCompanyId());
		request.setAttribute("deptList", deptList);

		return "/system/user/user-add";
	}
	/**
	 * 新增,修改功能
	 */
	@RequestMapping(value = "/edit", name = "部门新增")
	public String edit(User user) {
		String oldPwd = user.getPassword();
		if (StringUtils.isNotEmpty(oldPwd)){
			String pwd = new Md5Hash(oldPwd,user.getEmail(),2).toString();
			user.setPassword(pwd);
		}

		if (StringUtils.isEmpty(user.getId())) {
			//1. 设置主键
			user.setId(UUID.randomUUID().toString());

			//2. 设置企业信息
			user.setCompanyId(getCompanyId());
			user.setCompanyName(getCompanyName());

			userService.save(user);
			// 这里发送邮件
			String to = user.getEmail();
			String title = "saas平台--用户新增成功";
			String content = "恭喜您,您的账号已经在saas平台开通成功,请使用当前邮箱作用账号,使用" + oldPwd + "作为密码进行登录";
			Map map = new HashMap<>();

			map.put("to",to);
			map.put("title",title);
			map.put("content",content);
			//MailUtil.sendMail(to, title, content);
			template.convertAndSend("mail.send",map);

		} else {
			userService.update(user);
		}

		//重定向到list方法
		return "redirect:/system/user/list.do";
	}

	/**
	 * 跳转到修改
	 */
	@RequestMapping(value = "/toUpdate", name = "跳转部门编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当前部门信息
		User user = userService.findById(id);
		user.setPassword(null);
		request.setAttribute("user", user);

		//2. 查询所有部门
		List<Dept> deptList = deptService.findAll(getCompanyId());
		request.setAttribute("deptList", deptList);

		//3. 转发到修改页面
		return "/system/user/user-update";
	}
	/**
	 * 删除功能
	 */
	@RequestMapping(value = "/delete", name = "部门删除")
	public String delete(String id) {
		//调用service删除
		userService.delete(id);

		//重定向到list方法
		return "redirect:/system/user/list.do";
	}

	/**
	 * 角色分配跳转
	 */
	@RequestMapping(value = "/roleList", name = "角色分配跳转")
	public String roleList(String id) {
		//1. 显示出用户名称(查询用户信息)
		User user = userService.findById(id);
		//返回到前台页面
		request.setAttribute("user",user);
		//2. 显示出所有的角色, 等待勾选( 查询所有角色)
		List<Role> roleList = roleService.findAll(user.getCompanyId());
		// 将查询到的角色返回到前台页面
		request.setAttribute("roleList",roleList);
		//3. 回显当前用户已经分配了的角色 ( 查询中间表 )
		List<String> userRoleStr =userService.findRolesIdByUserID(id);
		// 返回查询结果
		request.setAttribute("userRoleStr",userRoleStr);
		//4. 跳转到给用户分配角色页面
		return "/system/user/user-role";
	}

	/**
	 * 保存角色
	 */
	@RequestMapping(value = "/changeRole", name = "保存用户分配的角色信息")
	public String changeRole(@RequestParam("userid") String userId,String[] roleIds) {
		//考虑到那个事务安全的问题,将此功能转移到service层完成
		userService.changeRole(userId,roleIds);

		//重定向到list方法
		return "redirect:/system/user/list.do";
	}
}
