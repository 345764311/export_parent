package com.itheima.web.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.domain.company.Company;
import com.itheima.service.company.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: mryhl
 * @date: Created in 2020/10/13 21:27
 * @version: 1.1
 */
@Controller
public class CompanyController {
	@Reference
	private CompanyService companyService;

	@RequestMapping("/apply")
	@ResponseBody
	public String apply(Company company) {
		try {
			//设置企业ID和状态
			company.setId(UUID.randomUUID().toString());
			company.setState(0);

			//调用service保存
			companyService.save(company);

			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}
}
