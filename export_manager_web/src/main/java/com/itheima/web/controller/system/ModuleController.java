package com.itheima.web.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.system.Module;
import com.itheima.service.system.ModuleService;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/system/module")
public class ModuleController extends BaseController {

	@Reference
	private ModuleService moduleService;

	@RequestMapping(value = "/list", name = "模块列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {

		PageInfo pageInfo = moduleService.findByPage(pageNum, pageSize);
		request.setAttribute("page", pageInfo);

		return "/system/module/module-list";
	}


	@RequestMapping(value = "/toAdd", name = "跳转模块新增页面")
	public String toAdd() {
		//1. 查询所有模块
		List<Module> moduleList = moduleService.findAll();
		request.setAttribute("menus", moduleList);

		return "/system/module/module-add";
	}


	@RequestMapping(value = "/toUpdate", name = "跳转模块编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当前模块信息
		Module module = moduleService.findById(id);
		request.setAttribute("module", module);

		//2. 查询所有模块
		List<Module> moduleList = moduleService.findAll();
		request.setAttribute("menus", moduleList);

		//3. 转发到修改页面
		return "/system/module/module-update";
	}

	//新增和修改统一用一个方法处理
	@RequestMapping(value = "/edit", name = "模块新增")
	public String edit(Module module) {

		if (StringUtils.isEmpty(module.getId())) {
			//1. 设置主键
			module.setId(UUID.randomUUID().toString());

			moduleService.save(module);
		} else {
			moduleService.update(module);
		}

		//重定向到list方法
		return "redirect:/system/module/list.do";
	}

	@RequestMapping(value = "/delete", name = "模块删除")
	public String delete(String id) {
		//调用service删除
		moduleService.delete(id);

		//重定向到list方法
		return "redirect:/system/module/list.do";
	}
}