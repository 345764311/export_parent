package com.itheima.web.controller.cargo;

import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.cargo.ExtCproduct;
import com.itheima.domain.cargo.ExtCproductExample;
import com.itheima.domain.cargo.Factory;
import com.itheima.domain.cargo.FactoryExample;
import com.itheima.service.cargo.ExtCproductService;
import com.itheima.service.cargo.FactoryService;
import com.itheima.utils.FileUploadUtil;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/cargo/extCproduct")
public class ExtCproductController extends BaseController {


	@Autowired
	private FileUploadUtil fileUploadUtil;

	@Reference
	private ExtCproductService extCproductService;

	@Reference
	private FactoryService factoryService;

	/**
	 * 生成列表
	 */
	@RequestMapping(value = "/list", name = "附件列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize,
			String contractId,String contractProductId) {


		ExtCproductExample extCproductExample = new ExtCproductExample();

		//1 查询当前合同下的所有附件
		extCproductExample.createCriteria().andContractProductIdEqualTo(contractProductId);
		PageInfo pageInfo = extCproductService.findByPage(pageNum, pageSize, extCproductExample);
		request.setAttribute("page", pageInfo);


		//2 查询所有生产附件的厂家列表
		FactoryExample factoryExample = new FactoryExample();
		factoryExample.createCriteria().andCtypeEqualTo("附件");
		List<Factory> factoryList = factoryService.findAll(factoryExample);
		request.setAttribute("factoryList", factoryList);

		//3. 回传合同id
		request.setAttribute("contractId", contractId);
		request.setAttribute("contractProductId", contractProductId);
		return "/cargo/extc/extc-list";
	}


	/**
	 * 新增,修改功能
	 */
	@RequestMapping(value = "/edit", name = "附件新增,修改")
	public String edit(ExtCproduct extCproduct, MultipartFile productPhoto ) {
		// 文件上传

		try {
			String filePath = fileUploadUtil.upload(productPhoto);
			extCproduct.setProductImage(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("文件上传失败");
			throw new RuntimeException(e);
		}

		if (StringUtils.isEmpty(extCproduct.getId())) {
			//1. 设置主键
			extCproduct.setId(UUID.randomUUID().toString());
			//2. 设置企业信息
			extCproduct.setCompanyId(getCompanyId());
			extCproduct.setCompanyName(getCompanyName());

			extCproductService.save(extCproduct);


		} else {
			extCproductService.update(extCproduct);
		}
		//重定向到list方法
		return "redirect:/cargo/extCproduct/list.do?contractId=" + extCproduct.getContractId() + "&contractProductId=" + extCproduct.getContractProductId();
	}

	/**
	 * 跳转到修改
	 */
	@RequestMapping(value = "/toUpdate", name = "跳转附件编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当前附件信息
		ExtCproduct extCproduct = extCproductService.findById(id);
		request.setAttribute("extCproduct", extCproduct);


		//2 查询所有生产附件的厂家列表
		FactoryExample factoryExample = new FactoryExample();
		factoryExample.createCriteria().andCtypeEqualTo("附件");
		List<Factory> factoryList = factoryService.findAll(factoryExample);
		request.setAttribute("factoryList", factoryList);
		//

		//3. 转发到修改页面
		return "/cargo/extc/extc-update";
	}
	/**
	 * 删除功能
	 */
	@RequestMapping(value = "/delete", name = "附件删除")
	public String delete(String id,String contractId,String contractProductId) {
		//调用service删除
		extCproductService.delete(id);
		//重定向到list方法
		return "redirect:/cargo/extCproduct/list.do?contractId=" + contractId + "&contractProductId=" + contractProductId;
	}

}
