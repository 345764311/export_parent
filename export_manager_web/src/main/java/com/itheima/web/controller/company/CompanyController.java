package com.itheima.web.controller.company;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.company.Company;
import com.itheima.service.company.CompanyService;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/company")
public class CompanyController extends BaseController {

	@Reference
	private CompanyService companyService;

	/**
	 * 查看列表
	 * name字段主要用于打印日志
	 */
	//@RequiresPermissions("企业管理")  代表只有用户有企业管理的权限,才能访问当前方法
	//相当于XML中的   /company/list.do = perms["企业管理"]
	@RequiresPermissions("企业管理")
	@RequestMapping(value = "/list",name = "企业列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "1") Integer pageSize
	){
		PageInfo pageInfo = companyService.findByPage(pageNum, pageSize);
		request.setAttribute("page", pageInfo);
		return "company/company-list";
	}

	/**
	 * 添加功能
	 */
	@RequestMapping(value = "/toAdd",name = "跳转到新增企业的页面")
	public String toAdd(){
		return "company/company-add";
	}

	/**
	 * 修改、新增公用同一个方法
	 */
	@RequestMapping(value = "/edit",name = "新增企业")
	public String edit(Company company){
		// 判断id是否为空,空进行新增操作,非空进行修改操作
		if (StringUtils.isEmpty(company.getId())) {
			// 设置id
			company.setId(UUID.randomUUID().toString());
			// 调用service保持
			companyService.save(company);
			// 重定向到列表页
		}else {
			companyService.update(company);
		}

		return "redirect:/company/list.do";
	}

	/**
	 * 修改功能数据回显
	 */
	@RequestMapping(value = "/toUpdate",name = "跳转到修改企业的页面")
	public String toUpdate(String id){
		// 根据id查询
		Company company = companyService.findById(id);
		// 保存的request
		request.setAttribute("company",company);
		// 转发页面
		return "company/company-update";
	}

	/**
	 * 删除内容
	 */
	@RequestMapping(value = "/delete",name = "删除企业")
	public String delete(String id){
		// 调用service删除
		companyService.delete(id);
		// 重定向到list
		return "redirect:/company/list.do";
	}

}
