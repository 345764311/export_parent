package com.itheima.web.controller.cargo;

import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.cargo.Contract;
import com.itheima.domain.cargo.ContractExample;
import com.itheima.service.cargo.ContractService;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Controller
@RequestMapping("/cargo/contract")
public class ContractController extends BaseController {




	@Reference
	private ContractService contractService;

	/**
	 * 生成列表
	 */
	@RequestMapping(value = "/list", name = "合同列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		ContractExample contractExample = new ContractExample();
		//1 封装条件
		ContractExample.Criteria criteria = contractExample.createCriteria();
		criteria.andCompanyIdEqualTo(getCompanyId());

		//判断用户等级,根据等级不同去拼接不同条件
		Integer degree = getUser().getDegree();
		if (degree == 4) {//普通员工
			criteria.andCreateByEqualTo(getUser().getId());
		} else if (degree == 3) {//部门经理
			criteria.andCreateDeptEqualTo(getUser().getDeptId());
		} else if (degree == 2) {//总经理
			criteria.andCreateDeptLike(getUser().getDeptId() + "%");
		}

		//2 排序
		contractExample.setOrderByClause("create_time desc");

		PageInfo pageInfo = contractService.findByPage(pageNum, pageSize,contractExample);
		request.setAttribute("page", pageInfo);

		return "/cargo/contract/contract-list";
	}

	/**
	 * 跳转新增页面
	 */
	@RequestMapping(value = "/toAdd", name = "跳转合同新增页面")
	public String toAdd() {

		return "/cargo/contract/contract-add";
	}
	/**
	 * 新增,修改功能
	 */
	@RequestMapping(value = "/edit", name = "合同新增,修改")
	public String edit(Contract contract) {

		if (StringUtils.isEmpty(contract.getId())) {
			//1. 设置主键
			contract.setId(UUID.randomUUID().toString());
			//2. 设置企业信息
			contract.setCompanyId(getCompanyId());
			contract.setCompanyName(getCompanyName());

			//3.创建人id\创建部门id\创建时间
			contract.setCreateBy(getUser().getId());
			contract.setCreateDept(getUser().getDeptId());
			contract.setCreateTime(new Date());

			//4. 设置当前合同的状态
			contract.setState(0);//草稿
			contractService.save(contract);
		} else {
			contractService.update(contract);
		}
		//重定向到list方法
		return "redirect:/cargo/contract/list.do";
	}

	/**
	 * 跳转到修改
	 */
	@RequestMapping(value = "/toUpdate", name = "跳转合同编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当前合同信息
		Contract contract = contractService.findById(id);
		request.setAttribute("contract", contract);



		//3. 转发到修改页面
		return "/cargo/contract/contract-update";
	}
	/**
	 * 删除功能
	 */
	@RequestMapping(value = "/delete", name = "合同删除")
	public String delete(String id) {
		//调用service删除
		contractService.delete(id);

		//重定向到list方法
		return "redirect:/cargo/contract/list.do";
	}
	/**
	 * 提交合同
	 */
	@RequestMapping(value = "/submit", name = "提交合同")
	public String submit(String id) {
		Contract contract = new Contract();
		contract.setId(id);
		contract.setState(1);
		contractService.update(contract);

		//重定向到list方法
		return "redirect:/cargo/contract/list.do";
	}
	/**
	 * 撤销提交
	 */
	@RequestMapping(value = "/cancel", name = "撤销提交")
	public String cancel(String id) {
		Contract contract = new Contract();
		contract.setId(id);
		contract.setState(0);
		contractService.update(contract);

		//重定向到list方法
		return "redirect:/cargo/contract/list.do";
	}

}
