package com.itheima.web.controller.cargo;

import cn.hutool.core.lang.UUID;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.itheima.domain.cargo.ContractProduct;
import com.itheima.domain.cargo.ContractProductExample;
import com.itheima.domain.cargo.Factory;
import com.itheima.domain.cargo.FactoryExample;
import com.itheima.service.cargo.ContractProductService;
import com.itheima.service.cargo.FactoryService;
import com.itheima.utils.FileUploadUtil;
import com.itheima.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cargo/contractProduct")
public class ContractProductController extends BaseController {

	@Autowired
	private FileUploadUtil fileUploadUtil;


	@Reference
	private ContractProductService contractProductService;

	@Reference
	private FactoryService factoryService;

	/**
	 * 生成列表
	 */
	@RequestMapping(value = "/list", name = "货物列表查询")
	public String list(
			@RequestParam(defaultValue = "1", name = "page") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize,
			String contractId) {


		ContractProductExample contractProductExample = new ContractProductExample();

		//1 查询当前合同下的所有货物
		ContractProductExample.Criteria criteria = contractProductExample.createCriteria();
		criteria.andCompanyIdEqualTo(getCompanyId());
		criteria.andContractIdEqualTo(contractId);

		PageInfo pageInfo = contractProductService.findByPage(pageNum, pageSize, contractProductExample);
		request.setAttribute("page", pageInfo);

		//2 查询所有生产货物的厂家列表
		FactoryExample factoryExample = new FactoryExample();
		factoryExample.createCriteria().andCtypeEqualTo("货物");
		List<Factory> factoryList = factoryService.findAll(factoryExample);
		request.setAttribute("factoryList", factoryList);
		//
		////3. 回传合同id
		request.setAttribute("contractId", contractId);

		return "/cargo/product/product-list";
	}


	/**
	 * 新增,修改功能
	 */
	@RequestMapping(value = "/edit", name = "货物新增,修改")
	public String edit(ContractProduct contractProduct, MultipartFile productPhoto ) {
		// 文件上传

		try {
			String filePath = fileUploadUtil.upload(productPhoto);
			contractProduct.setProductImage(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("文件上传失败");
			throw new RuntimeException(e);
		}

		if (StringUtils.isEmpty(contractProduct.getId())) {
			//1. 设置主键
			contractProduct.setId(UUID.randomUUID().toString());
			//2. 设置企业信息
			contractProduct.setCompanyId(getCompanyId());
			contractProduct.setCompanyName(getCompanyName());

			contractProductService.save(contractProduct);


		} else {
			contractProductService.update(contractProduct);
		}
		//重定向到list方法
		return "redirect:/cargo/contractProduct/list.do?contractId=" + contractProduct.getContractId();
	}

	/**
	 * 跳转到修改
	 */
	@RequestMapping(value = "/toUpdate", name = "跳转货物编辑页面")
	public String toUpdate(String id) {
		//1. 根据id查询当前货物信息
		ContractProduct contractProduct = contractProductService.findById(id);
		request.setAttribute("contractProduct", contractProduct);


		//2 查询所有生产货物的厂家列表
		FactoryExample factoryExample = new FactoryExample();
		factoryExample.createCriteria().andCtypeEqualTo("货物");
		List<Factory> factoryList = factoryService.findAll(factoryExample);
		request.setAttribute("factoryList", factoryList);
		//

		//3. 转发到修改页面
		return "/cargo/product/product-update";
	}
	/**
	 * 删除功能
	 */
	@RequestMapping(value = "/delete", name = "货物删除")
	public String delete(String id,String contractId) {
		//调用service删除
		contractProductService.delete(id);
		//重定向到list方法
		return "redirect:/cargo/contractProduct/list.do?contractId=" + contractId;
	}
	/**
	 * 跳转到导入页面
	 */
	@RequestMapping(value = "/toImport", name = "跳转到导入页面")
	public String toImport(String contractId) {
		//回显合同id
		request.setAttribute("contractId", contractId);
		return "/cargo/product/product-import";
	}

	/**
	 * 导入货物
	 */
	@RequestMapping(value = "/import", name = "导入货物")
	public String imports(String contractId,MultipartFile file) throws IOException {
		// 通过文件获取输入流获取工作簿对象
		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		// 获取工作表
		XSSFSheet sheet = workbook.getSheetAt(0);
		// 获取行对象,最后一行的索引
		int lastRowNum = sheet.getLastRowNum();
		// 循环获取每一行的信息
		List<ContractProduct> list = new ArrayList<>();
		for (int i = 1; i <= lastRowNum; i++) {
			XSSFRow row = sheet.getRow(i);
			Object[] objects = new Object[9];
			for (int j = 1; j < row.getLastCellNum();j++){
				objects[j-1] = getCellValue(row.getCell(j));
			}

			ContractProduct contractProduct = new ContractProduct(objects);
			//补全信息
			contractProduct.setId(UUID.randomUUID().toString());
			contractProduct.setContractId(contractId);
			contractProduct.setCompanyId(getCompanyId());
			contractProduct.setCompanyName(getCompanyName());

			FactoryExample factoryExample = new FactoryExample();
			factoryExample.createCriteria().andFactoryNameEqualTo(contractProduct.getFactoryName());
			List<Factory> factories = factoryService.findAll(factoryExample);
			contractProduct.setFactoryId(factories.get(0).getId());

			//. 封装一个List集合对象
			list.add(contractProduct);

		}

		contractProductService.patchSave(list);

		return "redirect:/cargo/contractProduct/list.do?contractId=" + contractId;
	}

	//解析每个单元格的数据
	public static Object getCellValue(Cell cell) {
		Object obj = null;
		CellType cellType = cell.getCellType(); //获取单元格数据类型
		switch (cellType) {
			case STRING: {
				obj = cell.getStringCellValue();//字符串
				break;
			}
			//excel默认将日期也理解为数字
			case NUMERIC: {
				if (DateUtil.isCellDateFormatted(cell)) {
					obj = cell.getDateCellValue();//日期
				} else {
					obj = cell.getNumericCellValue(); // 数字
				}
				break;
			}
			case BOOLEAN: {
				obj = cell.getBooleanCellValue(); // 布尔
				break;
			}
			default: {
				break;
			}
		}
		return obj;
	}
}
