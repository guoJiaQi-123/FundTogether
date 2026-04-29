package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.entity.SysUserMessage;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.SysUserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户消息控制器
 * <p>
 * 提供当前登录用户的站内信消息管理功能，包括：
 * <ul>
 *   <li>查询消息列表：分页查询当前用户的消息列表</li>
 *   <li>标记已读：将指定消息标记为已读状态</li>
 *   <li>未读消息数：查询当前用户的未读消息数量</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    /** 用户消息服务，处理站内信的查询与状态更新 */
    @Autowired
    private SysUserMessageService sysUserMessageService;

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
     * 分页查询当前用户的消息列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @param type    消息类型，可选（1-系统公告等）
     * @return 分页消息列表
     */
    @GetMapping("/my-list")
    public Result<IPage<SysUserMessage>> getMyMessages(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) Integer type) {
        IPage<SysUserMessage> page = sysUserMessageService.getMyMessages(getCurrentUserId(), current, size);
        return Result.success(page);
    }

    /**
     * 标记消息为已读
     *
     * @param id 消息ID
     * @return 标记结果提示
     */
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        sysUserMessageService.markAsRead(id, getCurrentUserId());
        return Result.success("已标记为已读");
    }

    /**
     * 查询当前用户未读消息数量
     *
     * @return 包含未读消息数量的Map，key为"count"
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        LambdaQueryWrapper<SysUserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserMessage::getUserId, getCurrentUserId());
        wrapper.eq(SysUserMessage::getIsRead, 0); // 0-未读
        long count = sysUserMessageService.count(wrapper);
        return Result.success(Map.of("count", count));
    }
}
