package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.ProjectCategory;
import com.fundtogether.service.ProjectCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目分类控制器
 * <p>
 * 提供项目分类的增删改查API功能，包括：
 * <ul>
 *   <li>查询所有分类：公开接口，无需登录，用于前端展示分类列表</li>
 *   <li>创建分类：仅管理员可操作</li>
 *   <li>更新分类：仅管理员可操作</li>
 *   <li>删除分类：仅管理员可操作</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api")
public class ProjectCategoryController {

    /** 项目分类服务，处理分类的增删改查 */
    @Autowired
    private ProjectCategoryService projectCategoryService;

    /**
     * 查询所有项目分类
     * <p>
     * 公开接口，无需登录认证，用于前端展示项目分类筛选列表。
     * </p>
     *
     * @return 所有项目分类列表
     */
    @GetMapping("/public/categories")
    public Result<List<ProjectCategory>> getAllCategories() {
        return Result.success(projectCategoryService.getAllCategories());
    }

    /**
     * 创建项目分类（仅管理员）
     *
     * @param category 分类实体，包含分类名称、描述、排序等信息
     * @return 创建结果提示
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> createCategory(@RequestBody ProjectCategory category) {
        projectCategoryService.save(category);
        return Result.success("分类创建成功");
    }

    /**
     * 更新项目分类（仅管理员）
     *
     * @param id       分类ID
     * @param category 分类实体，包含待更新的字段
     * @return 更新结果提示
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ProjectCategory category) {
        category.setId(id);
        projectCategoryService.updateById(category);
        return Result.success("分类更新成功");
    }

    /**
     * 删除项目分类（仅管理员）
     *
     * @param id 分类ID
     * @return 删除结果提示
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> deleteCategory(@PathVariable Long id) {
        projectCategoryService.removeById(id);
        return Result.success("分类删除成功");
    }
}
