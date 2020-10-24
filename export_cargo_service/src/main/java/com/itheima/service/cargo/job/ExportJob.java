package com.itheima.service.cargo.job;

import com.itheima.dao.cargo.ExportDao;
import com.itheima.dao.cargo.ExportProductDao;
import com.itheima.domain.cargo.Export;
import com.itheima.domain.cargo.ExportExample;
import com.itheima.domain.cargo.ExportProduct;
import com.itheima.vo.ExportProductResult;
import com.itheima.vo.ExportResult;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @description:
 * @author: mryhl
 * @date: Created in 2020/10/21 15:56
 * @version:
 */
public class ExportJob {
	@Autowired
	private ExportDao exportDao;

	@Autowired
	private ExportProductDao exportProductDao;

	public void updateExportResult() {
		//1 查询状态为1的报运单
		ExportExample exportExample = new ExportExample();
		exportExample.createCriteria().andStateEqualTo(1L);
		List<Export> exportList = exportDao.selectByExample(exportExample);

		for (Export export : exportList) {
			try {
				//2 调用海关平台查询
				ExportResult exportResult = WebClient.create("http://localhost:5003/ws/export/user/" + export.getId()).get(ExportResult.class);

				//3 更新报运单状态
				export.setId(exportResult.getExportId());//id
				export.setState(exportResult.getState());//状态
				export.setRemark(exportResult.getRemark());//备注
				exportDao.updateByPrimaryKeySelective(export);//一定支持动态SQL

				//4 更新报运单下货物的税
				for (ExportProductResult product : exportResult.getProducts()) {
					ExportProduct exportProduct = new ExportProduct();
					exportProduct.setId(product.getExportProductId());
					exportProduct.setTax(product.getTax());

					exportProductDao.updateByPrimaryKeySelective(exportProduct);
				}
			}catch (Exception e){
				System.out.println("未查到相关信息");
			}
		}
	}
}

