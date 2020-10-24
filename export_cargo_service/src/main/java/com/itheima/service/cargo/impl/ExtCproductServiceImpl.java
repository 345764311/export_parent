package com.itheima.service.cargo.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.cargo.ContractDao;
import com.itheima.dao.cargo.ExtCproductDao;
import com.itheima.domain.cargo.Contract;
import com.itheima.domain.cargo.ExtCproduct;
import com.itheima.domain.cargo.ExtCproductExample;
import com.itheima.service.cargo.ExtCproductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ExtCproductServiceImpl implements ExtCproductService {

    @Autowired
    private ExtCproductDao extCproductDao;
    @Autowired
    private ContractDao contractDao;

    @Override
    public void save(ExtCproduct extCproduct) {
        // 查询合同对象
        Contract contract = contractDao.selectByPrimaryKey(extCproduct.getContractId());
        // 计算小计金额
        double amount = extCproduct.getCnumber() * extCproduct.getPrice();
        extCproduct.setAmount(amount);

        // 保存附件
        extCproductDao.insertSelective(extCproduct);
        // 修改合同的附件种数
        contract.setExtNum(contract.getExtNum() + 1);
        // 修改合同的总金额
        contract.setTotalAmount(contract.getTotalAmount() + amount);
        contractDao.updateByPrimaryKeySelective(contract);
    }

    @Override
    public void update(ExtCproduct extCproduct) {


        //根据合同id,得到合同对象
        Contract contract = contractDao.selectByPrimaryKey(extCproduct.getContractId());

        //根据附件id查询附件的信息
        ExtCproduct extCproductOld = extCproductDao.selectByPrimaryKey(extCproduct.getId());


        //附件修改
        //计算小计金额
        Double amount = extCproduct.getPrice() * extCproduct.getCnumber();
        extCproduct.setAmount(amount);

        //执行修改
        extCproductDao.updateByPrimaryKeySelective(extCproduct);

        //合同修改
        //修改总金额 - 源小计  + 新小计
        contract.setTotalAmount(contract.getTotalAmount() - extCproductOld.getAmount() + amount);

        // 执行合同修改
        contractDao.updateByPrimaryKeySelective(contract);
    }

    @Override
    public void delete(String id) {
        //1. 根据附件id查询附件信息
        ExtCproduct extCproduct = extCproductDao.selectByPrimaryKey(id);

        //2. 根据合同id查询合同信息
        Contract contract = contractDao.selectByPrimaryKey(extCproduct.getContractId());


        //附件删除
        extCproductDao.deleteByPrimaryKey(id);

        //货物无影响
        //合同修改
        //1. 修改合同中的附件数量 - 1
        contract.setExtNum(contract.getExtNum() - 1);

        //2. 修改合同中的总金额 - 要删除的附件的小计
        contract.setTotalAmount(contract.getTotalAmount() - extCproduct.getAmount());

        //3. 执行修改
        contractDao.updateByPrimaryKeySelective(contract);
    }

    @Override
    public ExtCproduct findById(String id) {
        return extCproductDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ExtCproduct> findAll(ExtCproductExample example) {
        return extCproductDao.selectByExample(example);
    }

    @Override
    public PageInfo findByPage(int pageNum, int pageSize, ExtCproductExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExtCproduct> list = extCproductDao.selectByExample(example);
        return new PageInfo(list, 10);
    }
}
