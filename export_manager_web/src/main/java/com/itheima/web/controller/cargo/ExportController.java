package com.itheima.web.controller.cargo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.cargo.ContractExample;
import com.itheima.domain.cargo.Export;
import com.itheima.domain.cargo.ExportExample;
import com.itheima.domain.cargo.ExportProduct;
import com.itheima.service.cargo.ContractService;
import com.itheima.service.cargo.ExportProductService;
import com.itheima.service.cargo.ExportService;
import com.itheima.web.controller.BaseController;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cargo/export")
public class ExportController extends BaseController {




	@Reference
	private ContractService contractService;
	@Reference
	private ExportService exportService;
	@Reference
	private ExportProductService exportProductService;

	/**
	 * 已提交状态的合同列表查询
	 */
	@RequestMapping(value = "/contractList", name = "已提交状态的合同列表查询")
	public String contractList(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		ContractExample contractExample = new ContractExample();
		//1 封装条件
		ContractExample.Criteria criteria = contractExample.createCriteria();
		criteria.andCompanyIdEqualTo(getCompanyId());
		//状态提交
		criteria.andStateEqualTo(1);


		PageInfo pageInfo = contractService.findByPage(pageNum, pageSize,contractExample);
		request.setAttribute("page", pageInfo);

		return "/cargo/export/export-contractList";
	}
	/**
	 * 报运单列表查询
	 */
	@RequestMapping(value = "/list", name = "报运单列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		ExportExample exportExample = new ExportExample();
		//1 封装条件
		exportExample.createCriteria().andCompanyIdEqualTo(getCompanyId());

		PageInfo pageInfo = exportService.findByPage(pageNum, pageSize,exportExample);
		request.setAttribute("page", pageInfo);

		return "/cargo/export/export-list";
	}



	/**
	 * 跳转报运单新增页面
	 */
	@RequestMapping(value = "/toExport", name = "跳转报运单新增页面")
	public String toExport(String id) {
		request.setAttribute("id",id);
		return "/cargo/export/export-toExport";
	}
	/**
	 * 出口报运单新增,修改功能
	 */
	@RequestMapping(value = "/edit", name = "报运单新增,修改")
	public String edit(Export export) {

		if (StringUtils.isEmpty(export.getId())) {
			//1. 设置主键
			export.setId(UUID.randomUUID().toString());
			//2. 设置企业信息
			export.setCompanyId(getCompanyId());
			export.setCompanyName(getCompanyName());
			//3.制单时间
			export.setInputDate(new Date());
			//4. 设置当前合同的状态
			export.setState(0);
			exportService.save(export);
		} else {
			exportService.update(export);
		}
		//重定向到list方法
		return "redirect:/cargo/export/list.do";
	}


	/**
	 * 跳转到修改
	 */
	@RequestMapping(value = "/toUpdate", name = "跳转报运单修改编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当报运单信息
		Export export = exportService.findById(id);
		request.setAttribute("export",export);
		//2. 当前报运单下的货物
		List<ExportProduct> exportProductList = exportProductService.findByExportId(id);
		request.setAttribute("eps",exportProductList);
		//3. 转发到修改页面
		return "/cargo/export/export-update";
	}
	/**
	 * 删除功能
	 */
	@RequestMapping(value = "/delete", name = "报运单删除")
	public String delete(String id) {
		//调用service删除
		exportService.delete(id);

		//重定向到list方法
		return "redirect:/cargo/export/list.do";
	}
	/**
	 * 跳转查看详情页面
	 */
	@RequestMapping(value = "/toView", name = "跳转报运单新增页面")
	public String toView(String id) {

		Export export = exportService.findById(id);
		request.setAttribute("export",export);
		return "/cargo/export/export-view";
	}
	/**
	 * 海关电子报运
	 */
	@RequestMapping(value = "/exportE", name = "海关电子报运")
	public String exportE(String id) {

		exportService.exportE(id);
		return "redirect:/cargo/export/list.do";
	}
	/**
	 * 查看报运结果
	 */
	@RequestMapping(value = "/findExportResult", name = "查看报运结果")
	public String findExportResult(String id) {

		exportService.findExportResult(id);
		return "redirect:/cargo/export/list.do";
	}


	@RequestMapping(value = "/exportPdf", name = "下载文件")
	public void exportPdf1(String id) throws JRException, IOException {
		//1. 获取模板
		String realPath = session.getServletContext().getRealPath("jasper/export.jasper");
		//1. 获取数据 条件 报运单id    结果 报运单信息和报运单下货物信息
		//1-1) 报运单信息
		Export export = exportService.findById(id);
		Map<String, Object> map = BeanUtil.beanToMap(export);

		//1-2) 货物信息
		List<ExportProduct> list = exportProductService.findByExportId(id);
		JRDataSource dataSource = new JRBeanCollectionDataSource(list);

		//2  向模板填充数据
		JasperPrint jasperPrint = JasperFillManager.fillReport(realPath, map, dataSource);

		//3.输出到浏览器
		JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

		//ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
		//DownloadUtil.download(byteArrayOutputStream,response,"报运单.pdf");
	}

}
