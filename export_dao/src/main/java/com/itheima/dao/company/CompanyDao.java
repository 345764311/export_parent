package com.itheima.dao.company;

import com.itheima.domain.company.Company;

import java.util.List;

/**
 * 创建dao接口
 */
public interface CompanyDao {
	/**
	 * 查询列表
	 */
	List<Company> findAll();

	void save(Company company);

	Company findById(String id);

	void update(Company company);

	void delete(String id);

	Long findTotal();

	List<Company> findList(int startIndex, Integer pageSize);
}
