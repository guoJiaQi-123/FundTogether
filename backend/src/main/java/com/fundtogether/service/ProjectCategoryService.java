package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.ProjectCategory;
import java.util.List;

public interface ProjectCategoryService extends IService<ProjectCategory> {
    List<ProjectCategory> getAllCategories();
}
