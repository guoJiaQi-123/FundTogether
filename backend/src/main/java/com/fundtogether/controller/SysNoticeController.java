package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fundtogether.common.Result;
import com.fundtogether.entity.SysNotice;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.SysUserMessage;
import com.fundtogether.service.SysNoticeService;
import com.fundtogether.service.SysUserMessageService;
import com.fundtogether.service.SysUserService;
import com.fundtogether.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统公告控制器
 * <p>
 * 提供系统公告的管理API功能，包括：
 * <ul>
 *   <li>创建公告：创建新公告，若状态为已发布则自动推送通知</li>
 *   <li>分页查询公告：按创建时间倒序分页查询公告列表</li>
 *   <li>查询生效公告：查询所有已发布状态的公告</li>
 *   <li>更新公告状态：修改公告发布状态，从草稿变为发布时自动推送通知</li>
 *   <li>删除公告：删除指定公告</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/notices")
public class SysNoticeController {

    /** 系统公告服务，处理公告的增删改查 */
    @Autowired
    private SysNoticeService sysNoticeService;

    /** 用户服务，用于公告发布时查询所有用户生成站内信 */
    @Autowired
    private SysUserService sysUserService;

    /** 用户消息服务，用于公告发布时批量保存站内信 */
    @Autowired
    private SysUserMessageService sysUserMessageService;

    /**
     * 处理公告发布后的通知逻辑
     * <p>
     * 当公告状态为已发布(status=1)时，为所有用户创建一条站内信，
     * 并通过WebSocket广播通知前端刷新。
     * </p>
     *
     * @param notice 已发布的系统公告实体
     */
    private void handleNoticePublished(SysNotice notice) {
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            // 查询所有用户，为每人生成一条站内信
            List<SysUser> users = sysUserService.list();
            List<SysUserMessage> messages = users.stream().map(user -> {
                SysUserMessage msg = new SysUserMessage();
                msg.setUserId(user.getId());
                msg.setType(1); // 1-系统公告
                msg.setTitle("新系统公告: " + notice.getTitle());
                msg.setContent(notice.getContent());
                msg.setRelatedId(notice.getId());
                msg.setIsRead(0);
                return msg;
            }).collect(Collectors.toList());

            if (!messages.isEmpty()) {
                sysUserMessageService.saveBatch(messages);
            }

            // 通过WebSocket广播新公告通知
            WebSocketServer.broadcastMessage("New notice published: " + notice.getTitle());
        }
    }

    /**
     * 创建系统公告
     * <p>
     * 创建公告后，如果公告状态为已发布，会自动触发通知推送逻辑。
     * </p>
     *
     * @param sysNotice 公告实体，包含标题、内容、状态等
     * @return 创建结果提示
     */
    @PostMapping
    public Result<?> createNotice(@RequestBody SysNotice sysNotice) {
        sysNoticeService.save(sysNotice);
        handleNoticePublished(sysNotice);
        return Result.success("公告创建成功");
    }

    /**
     * 分页查询公告列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页公告列表，按创建时间倒序排列
     */
    @GetMapping
    public Result<IPage<SysNotice>> listNotices(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<SysNotice> page = new Page<>(current, size);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysNotice::getCreatedAt);
        IPage<SysNotice> noticePage = sysNoticeService.page(page, wrapper);
        return Result.success(noticePage);
    }

    /**
     * 查询所有已发布的生效公告
     * <p>
     * 返回状态为已发布(status=1)的公告列表，按创建时间倒序排列。
     * </p>
     *
     * @return 已发布公告列表
     */
    @GetMapping("/active")
    public Result<List<SysNotice>> getActiveNotices() {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1)
               .orderByDesc(SysNotice::getCreatedAt);
        List<SysNotice> notices = sysNoticeService.list(wrapper);
        return Result.success(notices);
    }

    /**
     * 更新公告状态
     * <p>
     * 修改公告的发布状态。当公告从非发布状态变为发布状态时，
     * 会自动触发通知推送逻辑，为所有用户创建站内信并广播WebSocket消息。
     * </p>
     *
     * @param id         公告ID
     * @param sysNotice  公告实体，包含新的status值
     * @return 状态更新结果提示
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody SysNotice sysNotice) {
        SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        Integer oldStatus = notice.getStatus();
        notice.setStatus(sysNotice.getStatus());
        sysNoticeService.updateById(notice);

        // 当公告从非发布状态变为发布状态时，触发通知推送
        if ((oldStatus == null || oldStatus != 1) && sysNotice.getStatus() != null && sysNotice.getStatus() == 1) {
            handleNoticePublished(notice);
        }

        return Result.success("状态更新成功");
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 删除结果提示
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.success("删除成功");
    }
}
