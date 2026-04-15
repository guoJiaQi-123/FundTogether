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

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private SysUserMessageService sysUserMessageService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @GetMapping("/my-list")
    public Result<IPage<SysUserMessage>> getMyMessages(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) Integer type) {
        IPage<SysUserMessage> page = sysUserMessageService.getMyMessages(getCurrentUserId(), current, size);
        return Result.success(page);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        sysUserMessageService.markAsRead(id, getCurrentUserId());
        return Result.success("已标记为已读");
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        LambdaQueryWrapper<SysUserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserMessage::getUserId, getCurrentUserId());
        wrapper.eq(SysUserMessage::getIsRead, 0);
        long count = sysUserMessageService.count(wrapper);
        return Result.success(Map.of("count", count));
    }
}
