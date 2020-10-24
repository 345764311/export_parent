package com.itheima.service.cargo.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.cargo.ContractDao;
import com.itheima.dao.cargo.ContractProductDao;
import com.itheima.dao.cargo.ExtCproductDao;
import com.itheima.domain.cargo.*;
import com.itheima.service.cargo.ContractProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ContractProductServiceImpl implements ContractProductService {

    @Autowired
    private ContractProductDao contractProductDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ExtCproductDao extCproductDao;

    @Override
    public void save(ContractProduct contractProduct) {
        // 根据id查询合同信息
        Contract contract = contractDao.selectByPrimaryKey(contractProduct.getContractId());

        // 设置商品的金额
        double amount = contractProduct.getPrice() * contractProduct.getCnumber();
        contractProduct.setAmount(amount);


        contractProductDao.insertSelective(contractProduct);

        // 保存合同的信息
        // 商品种类数
        contract.setProNum(contract.getProNum() + 1);
        // 合同总金额
        contract.setTotalAmount(contract.getTotalAmount() + amount);

        contractDao.updateByPrimaryKeySelective(contract);

    }

    @Override
    public void update(ContractProduct contractProduct) {

        // 根据id查询合同信息
        Contract contract = contractDao.selectByPrimaryKey(contractProduct.getContractId());

        ContractProduct contractProduct1 = contractProductDao.selectByPrimaryKey(contractProduct.getId());

        // 设置商品的金额
        double amount = contractProduct.getPrice() * contractProduct.getCnumber();

        contractProduct.setAmount(amount);

        contractProductDao.updateByPrimaryKeySelective(contractProduct);
        // 判断原来的数据库中保存的值,因为有错误的测试插入,可能存在空值
        if (contractProduct1.getAmount()==null){
            contractProduct1.setAmount(0.0);
        }

        contract.setTotalAmount(contract.getTotalAmount() + amount - contractProduct1.getAmount());

        contractDao.updateByPrimaryKeySelective(contract);


    }

    @Override
    public void delete(String id) {
        // 查询商品信息
        ContractProduct contractProduct = contractProductDao.selectByPrimaryKey(id);
        // 查询附件
        ExtCproductExample extCproductExample = new ExtCproductExample();
        extCproductExample.createCriteria().andContractProductIdEqualTo(id);
        List<ExtCproduct> extCproducts = extCproductDao.selectByExample(extCproductExample);

        // 查询合同
        Contract contract = contractDao.selectByPrimaryKey(contractProduct.getContractId());

        // 执行删除附件
        double amount = 0.0;
        for (ExtCproduct extCproduct : extCproducts) {
            amount += extCproduct.getAmount();
            extCproductDao.deleteByPrimaryKey(extCproduct.getId());
        }
        // 删除货物
        contractProductDao.deleteByPrimaryKey(id);

        // 修改合同 商品种类数
        contract.setProNum(contract.getProNum() - 1);
        // 附件种类数
        contract.setExtNum(contract.getProNum() - extCproducts.size());


        // 金额
        contract.setTotalAmount(contract.getTotalAmount() - contractProduct.getAmount() - amount );

        contractDao.updateByPrimaryKeySelective(contract);
    }

    @Override
    public ContractProduct findById(String id) {
        return contractProductDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ContractProduct> findAll(ContractProductExample example) {
        return contractProductDao.selectByExample(example);
    }

    @Override
    public PageInfo findByPage(int pageNum, int pageSize, ContractProductExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<ContractProduct> list = contractProductDao.selectByExample(example);
        return new PageInfo(list, 10);
    }

    @Override
    public void patchSave(List<ContractProduct> list) {
        for (ContractProduct contractProduct : list) {
            this.save(contractProduct);
        }
    }
}
