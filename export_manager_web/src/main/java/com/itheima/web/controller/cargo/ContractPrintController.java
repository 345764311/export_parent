package com.itheima.web.controller.cargo;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.service.cargo.ContractService;
import com.itheima.utils.DownloadUtil;
import com.itheima.vo.ContractProductVo;
import com.itheima.web.controller.BaseController;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
@RequestMapping("/cargo/contract")
public class ContractPrintController extends BaseController {

	@Reference
	private ContractService contractService;


	/**
	 * 跳转出货表页面
	 */
	@RequestMapping(value = "/print", name = "跳转出货表页面")
	public String print() {
		return "/cargo/print/contract-print";
	}

	/**
	 * 导出商品
	 */
	@RequestMapping(value = "/printExcel", name = "导出商品")
	public void printExcel(String inputDate) throws IOException {
		// 根据传输的参数查询数据库
		List<ContractProductVo> list = contractService.findContractProductVo(inputDate, getCompanyId());
		// 创建一个工作簿
		Workbook workbook = new XSSFWorkbook();
		// 根据工作簿创建一个工作表
		Sheet sheet = workbook.createSheet();

		/**
		 * @author mryhl
		 * 合并单元格
		 *  CellRangeAddress(int firstRow, int lastRow, int firstCol, int lastCol)
		 */
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8));
		// 设置列宽
		for (int i = 1; i < 9; i++) {
			// setColumnWidth(int columnIndex, int width) columnIndex索引 width宽度
			sheet.setColumnWidth(i, 15 * 256);
		}


		// 创建第0行
		Row row0 = sheet.createRow(0);
		for (int i = 1; i < 9; i++) {
			Cell cell = row0.createCell(i);
			// 添加样式
			cell.setCellStyle(bigTitleStyle(workbook));
		}
		// 替换出入文件的格式
		String s = inputDate.replaceAll("-0", "年").replaceAll("-", "年") + "月份出货表";
		// 设置第一行的内容
		row0.getCell(1).setCellValue(s);
		// 创建数组,保存字段名
		String[] objs = {"客户", "合同号", "货号", "数量", "工厂", "工厂交期", "船期", "贸易条款"};
		// 创建第1行		
		Row row1 = sheet.createRow(1);
		for (int i = 1; i < 9; i++) {
			Cell cell = row1.createCell(i);
			// 出入数据
			cell.setCellValue(objs[i - 1]);
			// 添加样式
			cell.setCellStyle(littleTitleStyle(workbook));
		}

		// 使用查询到的数据创建第n行
		int n = 2;
		for (ContractProductVo contractProductVo : list) {
			// 使用行创建单元格
			Row row = sheet.createRow(n++);
			for (int i = 1; i < 9; i++) {
				Cell cell = row.createCell(i);
				//  添加样式
				cell.setCellStyle(textStyle(workbook));
			}
			// 添加数据
			row.getCell(1).setCellValue(contractProductVo.getCustomName());
			row.getCell(2).setCellValue(contractProductVo.getContractNo());
			row.getCell(3).setCellValue(contractProductVo.getProductNo());
			row.getCell(4).setCellValue(contractProductVo.getCnumber());
			row.getCell(5).setCellValue(contractProductVo.getFactoryName());
			row.getCell(6).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getDeliveryPeriod()));
			row.getCell(7).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getShipTime()));
			row.getCell(8).setCellValue(contractProductVo.getTradeTerms());

		}


		// 文件下载
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		DownloadUtil.download(outputStream, response, "出货表.xlsx");
	}


	/**
	 * 按照模板导出商品
	 */
	@RequestMapping(value = "/printExcelWithTemplate", name = "按照模板导出商品")
	public void printExcelWithTemplate(String inputDate) throws IOException {
		// 根据传输的参数查询数据库
		List<ContractProductVo> list = contractService.findContractProductVo(inputDate, getCompanyId());
		// 读取模板
		String realPath = session.getServletContext().getRealPath("/make/xlsprint/tOUTPRODUCT.xlsx");
		// 通过读入模板文件创建一个工作簿
		Workbook workbook = new XSSFWorkbook(new FileInputStream(realPath));
		// 根据工作簿创建一个工作表
		Sheet sheet = workbook.getSheetAt(0);


		// 替换出入文件的格式
		String s = inputDate.replaceAll("-0", "年").replaceAll("-", "年") + "月份出货表";
		// 设置第一行的内容
		sheet.getRow(0).getCell(1).setCellValue(s);

		// 读取第2行
		Row row2 = sheet.getRow(2);
		// 创建样式数组
		CellStyle[] cellStyles = new CellStyle[8];
		// 向数组中写入数据
		for (int i = 0; i < 8; i++) {
			cellStyles[i] = row2.getCell(i + 1).getCellStyle();
		}

		// 使用查询到的数据创建第n行
		int n = 2;
		for (ContractProductVo contractProductVo : list) {
			// 使用行创建单元格
			Row row = sheet.createRow(n++);
			for (int i = 1; i < 9; i++) {
				Cell cell = row.createCell(i);
				//  添加样式
				cell.setCellStyle(textStyle(workbook));
				cell.setCellStyle(cellStyles[i - 1]);

			}
			// 添加数据
			row.getCell(1).setCellValue(contractProductVo.getCustomName());
			row.getCell(2).setCellValue(contractProductVo.getContractNo());
			row.getCell(3).setCellValue(contractProductVo.getProductNo());
			row.getCell(4).setCellValue(contractProductVo.getCnumber());
			row.getCell(5).setCellValue(contractProductVo.getFactoryName());
			row.getCell(6).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getDeliveryPeriod()));
			row.getCell(7).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getShipTime()));
			row.getCell(8).setCellValue(contractProductVo.getTradeTerms());

		}


		// 文件下载
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		DownloadUtil.download(outputStream, response, "出货表.xlsx");
	}


	/**
	 * 百万数据导出
	 */
	@RequestMapping(value = "/printExcelMillion", name = "百万数据导出")
	public void printExcelMillion(String inputDate) throws IOException {
		//1. 根据输入的参数查询货物列表
		List<ContractProductVo> list = contractService.findContractProductVo(inputDate, getCompanyId());

		//2. 封装列表为一个工作簿
		//2-1) 创建一个工作簿
		Workbook workbook = new SXSSFWorkbook();

		//2-2)　使用工作簿创建工作表
		Sheet sheet = workbook.createSheet();
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8));
		for (int i = 1; i < 9; i++) {
			//设置列宽
			sheet.setColumnWidth(i, 15 * 256);
		}

		//2-3) 使用工作表创建第0行
		Row row0 = sheet.createRow(0);
		for (int i = 1; i < 9; i++) {
			Cell cell = row0.createCell(i);
		}
		String title = inputDate.replaceAll("-0", "年").replaceAll("-", "年");
		row0.getCell(1).setCellValue(title + "月份出货表");

		//2-4) 使用工作表创建第1行
		Row row1 = sheet.createRow(1);
		for (int i = 1; i < 9; i++) {
			Cell cell = row1.createCell(i);
		}
		row1.getCell(1).setCellValue("客户");
		row1.getCell(2).setCellValue("合同号");
		row1.getCell(3).setCellValue("货号");
		row1.getCell(4).setCellValue("数量");
		row1.getCell(5).setCellValue("工厂");
		row1.getCell(6).setCellValue("工厂交期");
		row1.getCell(7).setCellValue("船期");
		row1.getCell(8).setCellValue("贸易条款");

		//2-5) 使用工作表创建第n行
		int n = 2;
		for (ContractProductVo contractProductVo : list) {
			for (int x = 0; x < 8000; x++) {
				//2-6) 使用行创建单元格
				Row rown = sheet.createRow(n++);
				for (int i = 1; i < 9; i++) {
					Cell cell = rown.createCell(i);
				}

				//2-7) 向单元格中设置数据(样式)
				rown.getCell(1).setCellValue(contractProductVo.getCustomName());
				rown.getCell(2).setCellValue(contractProductVo.getContractNo());
				rown.getCell(3).setCellValue(contractProductVo.getProductNo());
				rown.getCell(4).setCellValue(contractProductVo.getCnumber());
				rown.getCell(5).setCellValue(contractProductVo.getFactoryName());
				rown.getCell(6).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getDeliveryPeriod()));
				rown.getCell(7).setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(contractProductVo.getShipTime()));
				rown.getCell(8).setCellValue(contractProductVo.getTradeTerms());
			}
		}

		//3. 文件下载
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		DownloadUtil.download(outputStream, response, "出货表.xlsx");
	}

	/**
	 * 大标题的样式
	 */
	public CellStyle bigTitleStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);//字体加粗
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);                //横向居中
		style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
		return style;
	}

	/**
	 * 小标题的样式
	 */
	public CellStyle littleTitleStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);                //横向居中
		style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
		style.setBorderTop(BorderStyle.THIN);                        //上细线
		style.setBorderBottom(BorderStyle.THIN);                    //下细线
		style.setBorderLeft(BorderStyle.THIN);                        //左细线
		style.setBorderRight(BorderStyle.THIN);                        //右细线
		return style;
	}

	/**
	 * 文字样式
	 */
	public CellStyle textStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontName("Times New Roman");
		font.setFontHeightInPoints((short) 10);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.LEFT);                //横向居左
		style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
		style.setBorderTop(BorderStyle.THIN);                        //上细线
		style.setBorderBottom(BorderStyle.THIN);                    //下细线
		style.setBorderLeft(BorderStyle.THIN);                        //左细线
		style.setBorderRight(BorderStyle.THIN);                        //右细线

		return style;
	}

}
