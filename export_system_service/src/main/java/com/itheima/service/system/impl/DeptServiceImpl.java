package com.itheima.service.system.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.system.DeptDao;
import com.itheima.domain.system.Dept;
import com.itheima.service.system.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.List;
@Service
public class DeptServiceImpl implements DeptService {
	
	@Autowired
	private DeptDao deptDao;

	@Override
	public List<Dept> findAll(String companyId) {

		return deptDao.findAll(companyId);
	}

	@Override
	public void save(Dept dept) {
		deptDao.save(dept);
	}

	@Override
	public Dept findById(String id) {
		return deptDao.findById(id);
	}

	@Override
	public void update(Dept dept) {
		deptDao.update(dept);
	}

	@Override
	public void delete(String id) {
		deptDao.deleteById(id);
	}

	@Override
	public PageInfo findByPage(String companyId,Integer pageNum, Integer pageSize) {
		//1. 设置pageNum和pageSize
		PageHelper.startPage(pageNum,pageSize);

		//2. 调用一个查询所有的方法
		List<Dept> list = deptDao.findAll(companyId);

		//3. 直接返回PageInfo
		return new PageInfo(list,10);
	}
}
