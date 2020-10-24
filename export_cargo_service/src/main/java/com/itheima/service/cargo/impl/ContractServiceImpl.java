package com.itheima.service.cargo.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.cargo.ContractDao;
import com.itheima.dao.cargo.ContractProductDao;
import com.itheima.dao.cargo.ExtCproductDao;
import com.itheima.domain.cargo.*;
import com.itheima.service.cargo.ContractService;
import com.itheima.vo.ContractProductVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractProductDao contractProductDao;
    @Autowired
    private ExtCproductDao extCproductDao;

    @Override
    public Contract findById(String id) {
        return contractDao.selectByPrimaryKey(id);
    }

    @Override
    public void save(Contract contract) {
        contractDao.insertSelective(contract);
    }

    @Override
    public void update(Contract contract) {
        contractDao.updateByPrimaryKeySelective(contract);
    }
    /**
     * @description 事务性删除  当删除订单的时候需要先删除依赖订单的货物和附件
     * @author mryhl
     * @date 2020/10/15 16:58
     * @return
     */
    @Override
    public void delete(String id) {
        // 查询当前合同下的附件
        ExtCproductExample extCproductExample = new ExtCproductExample();
        extCproductExample.createCriteria().andContractIdEqualTo(id);
        List<ExtCproduct> extCproducts = extCproductDao.selectByExample(extCproductExample);

        // 查询当前合同下所有的货物
        ContractProductExample contractProductExample = new ContractProductExample();
        contractProductExample.createCriteria().andContractIdEqualTo(id);
        List<ContractProduct> contractProducts = contractProductDao.selectByExample(contractProductExample);

        // 删除附件
        for (ExtCproduct extCproduct : extCproducts) {
            extCproductDao.deleteByPrimaryKey(extCproduct.getId());
        }
        // 删除货物
        for (ContractProduct contractProduct : contractProducts) {
            contractProductDao.deleteByPrimaryKey(contractProduct.getId());
        }

        contractDao.deleteByPrimaryKey(id);
    }

    @Override
    public List<Contract> findAll(ContractExample example) {
        return contractDao.selectByExample(example);
    }

    @Override
    public PageInfo findByPage(int pageNum, int pageSize, ContractExample example) {
        PageHelper.startPage(pageNum, pageSize);
        List<Contract> list = contractDao.selectByExample(example);
        return new PageInfo(list, 10);
    }

    @Override
    public List<ContractProductVo> findContractProductVo(String inputDate, String companyId) {
        return contractDao.findContractProductVo(inputDate,companyId);
    }
}
