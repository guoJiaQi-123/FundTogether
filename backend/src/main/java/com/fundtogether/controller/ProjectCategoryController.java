package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.ProjectCategory;
import com.fundtogether.service.ProjectCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectCategoryController {

    @Autowired
    private ProjectCategoryService projectCategoryService;

    @GetMapping("/public/categories")
    public Result<List<ProjectCategory>> getAllCategories() {
        return Result.success(projectCategoryService.getAllCategories());
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> createCategory(@RequestBody ProjectCategory category) {
        projectCategoryService.save(category);
        return Result.success("分类创建成功");
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ProjectCategory category) {
        category.setId(id);
        projectCategoryService.updateById(category);
        return Result.success("分类更新成功");
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> deleteCategory(@PathVariable Long id) {
        projectCategoryService.removeById(id);
        return Result.success("分类删除成功");
    }
}
