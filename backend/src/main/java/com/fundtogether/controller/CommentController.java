package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.dto.CommentCreateDTO;
import com.fundtogether.entity.UserComment;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.UserCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 * <p>
 * 提供项目评论相关的API功能，包括：
 * <ul>
 *   <li>发布评论：用户对项目发表评论，支持回复（parentId非空时为回复评论）</li>
 *   <li>查询评论：分页查询指定项目的评论列表</li>
 *   <li>删除评论：删除自己发表的评论</li>
 *   <li>点赞评论：对评论进行点赞</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    /** 用户评论服务，处理评论的增删查及点赞逻辑 */
    @Autowired
    private UserCommentService userCommentService;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    /**
     * 发布评论
     * <p>
     * 用户对指定项目发表评论，如果parentId不为空则表示回复某条评论。
     * </p>
     *
     * @param dto           评论创建DTO，包含projectId、parentId（可选）、content
     * @param bindingResult 参数校验结果
     * @return 评论发布结果提示
     */
    @PostMapping("/create")
    public Result<?> addComment(@RequestBody @Valid CommentCreateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        userCommentService.addComment(getCurrentUserId(), dto.getProjectId(), dto.getParentId(), dto.getContent());
        return Result.success("评论发布成功");
    }

    /**
     * 分页查询指定项目的评论列表
     *
     * @param projectId 项目ID
     * @param current   当前页码，默认1
     * @param size      每页条数，默认10
     * @return 分页评论列表
     */
    @GetMapping("/project/{projectId}")
    public Result<IPage<UserComment>> getProjectComments(@PathVariable Long projectId,
                                                         @RequestParam(defaultValue = "1") Integer current,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userCommentService.getProjectComments(projectId, current, size));
    }

    /**
     * 删除评论
     * <p>
     * 仅允许删除自己发表的评论，通过当前用户ID进行权限校验。
     * </p>
     *
     * @param id 评论ID
     * @return 删除结果提示
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        userCommentService.deleteComment(id, getCurrentUserId());
        return Result.success("评论已删除");
    }

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 点赞结果提示
     */
    @PostMapping("/{id}/like")
    public Result<?> likeComment(@PathVariable Long id) {
        userCommentService.likeComment(id, getCurrentUserId());
        return Result.success("点赞成功");
    }
}
