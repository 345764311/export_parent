package com.itheima.dao.system;


import com.itheima.domain.system.Dept;

import java.util.List;

/**
 * 创建dao接口
 */
public interface DeptDao {
	/**
	 * 查询列表
	 */
	// 此id用于做企业隔离
	List<Dept> findAll(String companyId);

	void save(Dept dept);

	Dept findById(String id);

	void update(Dept dept);

	void deleteById(String id);
}
