package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.ProjectCategory;
import com.fundtogether.mapper.ProjectCategoryMapper;
import com.fundtogether.service.ProjectCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectCategoryServiceImpl extends ServiceImpl<ProjectCategoryMapper, ProjectCategory> implements ProjectCategoryService {

    @Override
    public List<ProjectCategory> getAllCategories() {
        LambdaQueryWrapper<ProjectCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProjectCategory::getSort);
        return this.list(wrapper);
    }
}
