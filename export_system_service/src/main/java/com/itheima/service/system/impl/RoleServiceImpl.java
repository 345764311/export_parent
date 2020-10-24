package com.itheima.service.system.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.dao.system.RoleDao;
import com.itheima.domain.system.Role;
import com.itheima.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService  {

    @Autowired
    private RoleDao roleDao;

    @Override
    public List<Role> findAll(String companyId) {
        return roleDao.findAll(companyId);
    }

    @Override
    public void save(Role role) {
        roleDao.save(role);
    }

    @Override
    public Role findById(String id) {
        return  roleDao.findById(id);
    }

    @Override
    public void update(Role role) {
        roleDao.update(role);
    }

    @Override
    public void delete(String id) {
        roleDao.delete(id);
    }

    public PageInfo<Role> findByPage(String companyId,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Role> list = roleDao.findAll(companyId);
        return new PageInfo<Role>(list,5);
    }

    @Override
    public List<String> findModuleByRoleId(String roleId) {
        return roleDao.findModuleByRoleId(roleId);
    }

    @Override
    public void changeModule(String roleId, String[] moduleIds) {
        //1. 删除中间表中角色的现有权限对应的id
        roleDao.deleteRoleModuleByRoleId(roleId);
        //2. 重新向中间表插入新的角色和权限id
        if (moduleIds != null && moduleIds.length > 0) {
            for (String moduleId : moduleIds) {
                roleDao.saveRoleModule(roleId,moduleId);
            }
        }
    }
}