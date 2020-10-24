package com.itheima.service.cargo.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.cargo.*;
import com.itheima.domain.cargo.*;
import com.itheima.service.cargo.ExportService;
import com.itheima.vo.ExportProductResult;
import com.itheima.vo.ExportProductVo;
import com.itheima.vo.ExportResult;
import com.itheima.vo.ExportVo;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ExportDao exportDao; // 报运dao

    @Autowired
    private ExportProductDao exportProductDao;//报运单商品dao

    @Autowired
    private ExtEproductDao extEproductDao;  //报运单附件Dao

    @Autowired
    private ContractDao contractDao; // 合同dao

    @Autowired
    private ContractProductDao contractProductDao; //合同货物dao

    @Autowired
    private ExtCproductDao extCproductDao;  //合同附件Dao

    //保存
    @Override
    public void save(Export export) {
        // 通过传入的参数获取合同id，合同id可以是数组
        List<String> contractIdList = Arrays.asList(export.getContractIds().split(","));
        /**
         * 查询合同信息,封装报运单信息
         */
        // 创建合同查询实体
        ContractExample contractExample = new ContractExample();
        // 将id参数传入到查询实体中
        contractExample.createCriteria().andIdIn(contractIdList);
        // 将查询到数据封装到list中
        List<Contract> contractList = contractDao.selectByExample(contractExample);

        // 拼接信息
        StringBuilder stringBuilder = new StringBuilder();
        Integer proNum = 0;
        Integer extNum = 0;
        for (Contract contract : contractList) {
            stringBuilder.append(contract.getContractNo()).append(" ");
            proNum += contract.getProNum();
            extNum += contract.getExtNum();
        }
        // 设置报运单的合同号字符串
        export.setCustomerContract(stringBuilder.toString());
        // 设置报运单的合同的商品数
        export.setProNum(proNum);
        // 设置报运单的合同的附件数
        export.setExtNum(extNum);

        // 保存报运单
        exportDao.insertSelective(export);

        /**
         * @author mryhl
         * 查询合同货物信息,封装报运单货物信息
         */
        // 查询货物
        ContractProductExample contractProductExample = new ContractProductExample();
        // 封装查询条件
        contractProductExample.createCriteria().andContractIdIn(contractIdList);
        // 查询货物
        List<ContractProduct> contractProductList = contractProductDao.selectByExample(contractProductExample);

        // 封装数据
        for (ContractProduct contractProduct : contractProductList) {
            // 订单商品实体
            ExportProduct exportProduct = new ExportProduct();
            BeanUtils.copyProperties(contractProduct,exportProduct);
            // 生成id
            exportProduct.setId(UUID.randomUUID().toString());
            // 设置报运单的id
            exportProduct.setExportId(export.getId());
            // 保存数据
            exportProductDao.insertSelective(exportProduct);
        }

        /**
         * @author mryhl
         * 查询合同附件信息,封装报运单附件信息
         */
        // 报运单附件实体
        ExtCproductExample extCproductExample = new ExtCproductExample();
        // 查询当前报运单的附件
        extCproductExample.createCriteria().andContractIdIn(contractIdList);
        // 查询数据库
        List<ExtCproduct> extCproductList = extCproductDao.selectByExample(extCproductExample);
        //数据封装
        for (ExtCproduct extCproduct : extCproductList) {
            // 创建附件实体
            ExtEproduct extEproduct = new ExtEproduct();
            BeanUtils.copyProperties(extCproduct,extEproduct);
            extEproduct.setId(UUID.randomUUID().toString());
            extEproduct.setExportId(export.getId());
            // 保存数据
            extEproductDao.insertSelective(extEproduct);
        }



    }

    //更新
    @Override
    public void update(Export export) {
        //1. 修改报运单信息
        exportDao.updateByPrimaryKeySelective(export);
        for (ExportProduct exportProduct : export.getExportProducts()) {
            //2. 修改报运单下的货物信息
            exportProductDao.updateByPrimaryKeySelective(exportProduct);
        }
    }

    //删除
    @Override
    public void delete(String id) {
        // 查询当前报运单下的货物信息
        ExportProductExample exportProductExample = new ExportProductExample();
        exportProductExample.createCriteria().andExportIdEqualTo(id);
        List<ExportProduct> exportProductList = exportProductDao.selectByExample(exportProductExample);
        // 查询当前报运单下附件的信息
        ExtEproductExample extEproductExample = new ExtEproductExample();
        extEproductExample.createCriteria().andExportIdEqualTo(id);
        //查询到的附件信息
        List<ExtEproduct> extEproductList = extEproductDao.selectByExample(extEproductExample);

        // 删除报运单下的货物信息
        for (ExportProduct exportProduct : exportProductList) {
            exportProductDao.deleteByPrimaryKey(exportProduct.getId());
        }
        // 删除附件
        for (ExtEproduct extEproduct : extEproductList) {
            extEproductDao.deleteByPrimaryKey(extEproduct.getId());
        }
        // 删除报运单
        exportDao.deleteByPrimaryKey(id);


    }

    //根据id查询
    @Override
    public Export findById(String id) {
        return exportDao.selectByPrimaryKey(id);
    }

    //分页
    @Override
    public PageInfo findByPage(int pageNum, int pageSize, ExportExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<Export> list = exportDao.selectByExample(example);
        return new PageInfo(list);
    }

    @Override
    public void exportE(String id) {
        // 查询报运单信息,封装到ExportVo
        Export export = exportDao.selectByPrimaryKey(id);
        // 实体对象
        ExportVo exportVo = new ExportVo();
        BeanUtils.copyProperties(export,exportVo);
        exportVo.setExportId(id);
        exportVo.setExportDate(new Date());

        // 查询保运单下的货物信息
        ExportProductExample exportProductExample = new ExportProductExample();
        exportProductExample.createCriteria().andExportIdEqualTo(id);
        List<ExportProduct> exportProductList = exportProductDao.selectByExample(exportProductExample);
        for (ExportProduct eportProduct : exportProductList) {
            ExportProductVo exportProductVo = new ExportProductVo();
            BeanUtils.copyProperties(eportProduct,exportProductVo);
            exportProductVo.setExportProductId(eportProduct.getId());

            exportVo.getProducts().add(exportProductVo);
        }
        // 调用海关平台,然后将exportVo发出去
        WebClient.create("http://localhost:5003/ws/export/user").post(exportVo);

        // 修改当前报运单的状态
        export.setState(1);
        exportDao.updateByPrimaryKeySelective(export);
    }

    @Override
    public void findExportResult(String id) {

        try {
            // 调用接口接收返回结果
            ExportResult exportResult = WebClient.create("http://localhost:5003/ws/export/user/" + id).get(ExportResult.class);
            // 根据结果跟新报运单信息
            Export export = new Export();
            export.setId(id);
            export.setState(exportResult.getState());
            // 保存数据
            exportDao.updateByPrimaryKeySelective(export);
            // 根据exportResult对象中的products更新保运单中的信息
            for (ExportProductResult exportResultProduct : exportResult.getProducts()) {
                ExportProduct exportProduct = new ExportProduct();
                exportProduct.setId(exportResultProduct.getExportProductId());
                // 将查询到的税设置到实体保存到数据库
                exportProduct.setTax(exportResultProduct.getTax());
                exportProductDao.updateByPrimaryKeySelective(exportProduct);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("查询失败,请检查输入的内容");
        }

    }

}
