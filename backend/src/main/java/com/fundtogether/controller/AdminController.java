package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fundtogether.common.Result;
import com.fundtogether.dto.UserRoleUpdateDTO;
import com.fundtogether.dto.CategoryStatExcelDTO;
import com.fundtogether.dto.AdminProjectContentUpdateDTO;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.Project;
import com.fundtogether.vo.SupporterVO;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.ProjectService;
import com.fundtogether.mapper.ProjectMapper;
import com.fundtogether.mapper.SupportOrderMapper;
import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PutMapping("/projects/takedown/{id}")
    public Result<?> takedownProject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("下架原因不能为空");
        }
        projectService.takedownProject(id, reason);
        return Result.success("项目已下架");
    }

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SupportOrderMapper supportOrderMapper;

    @Autowired
    private com.fundtogether.service.SysNoticeService sysNoticeService;

    @Autowired
    private com.fundtogether.service.SysUserMessageService sysUserMessageService;

    private void handleNoticePublished(com.fundtogether.entity.SysNotice notice) {
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            List<SysUser> users = sysUserService.list();
            List<com.fundtogether.entity.SysUserMessage> messages = users.stream().map(user -> {
                com.fundtogether.entity.SysUserMessage msg = new com.fundtogether.entity.SysUserMessage();
                msg.setUserId(user.getId());
                msg.setType(1); // 1-系统公告
                msg.setTitle("新系统公告: " + notice.getTitle());
                msg.setContent(notice.getContent());
                msg.setRelatedId(notice.getId());
                msg.setIsRead(0);
                return msg;
            }).collect(java.util.stream.Collectors.toList());

            if (!messages.isEmpty()) {
                sysUserMessageService.saveBatch(messages);
            }

            // Broadcast via WebSocket
            com.fundtogether.websocket.WebSocketServer.broadcastMessage("New notice published: " + notice.getTitle());
        }
    }

    @GetMapping("/notices")
    public Result<IPage<com.fundtogether.entity.SysNotice>> getNotices(@RequestParam(defaultValue = "1") Integer current,
                                                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<com.fundtogether.entity.SysNotice> page = new Page<>(current, size);
        LambdaQueryWrapper<com.fundtogether.entity.SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(com.fundtogether.entity.SysNotice::getCreatedAt);
        return Result.success(sysNoticeService.page(page, wrapper));
    }

    @PostMapping("/notices")
    public Result<?> createNotice(@RequestBody com.fundtogether.entity.SysNotice sysNotice) {
        sysNoticeService.save(sysNotice);
        handleNoticePublished(sysNotice);
        return Result.success("公告创建成功");
    }

    @PutMapping("/notices/{id}/publish")
    public Result<?> publishNotice(@PathVariable Long id) {
        com.fundtogether.entity.SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            return Result.error("公告已发布");
        }
        notice.setStatus(1);
        sysNoticeService.updateById(notice);
        handleNoticePublished(notice);
        return Result.success("发布成功");
    }

    @DeleteMapping("/notices/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/users")
    public Result<IPage<SysUser>> getUsers(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size) {
        Page<SysUser> page = new Page<>(current, size);
        IPage<SysUser> userPage = sysUserService.page(page);
        // Desensitize password
        userPage.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(userPage);
    }

    @PostMapping("/user/role")
    public Result<?> updateUserRole(@RequestBody @Valid UserRoleUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        
        SysUser user = sysUserService.getById(dto.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setRole(dto.getRole());
        sysUserService.updateById(user);
        
        return Result.success("角色修改成功");
    }

    @GetMapping("/projects/pending")
    public Result<IPage<Project>> getPendingProjects(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        IPage<Project> page = projectService.getPendingProjects(current, size);
        return Result.success(page);
    }

    @GetMapping("/projects/all")
    public Result<IPage<Project>> getAllProjects(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getCreatedAt);
        IPage<Project> projectPage = projectService.page(page, wrapper);
        
        // 补充项目发起人名称
        for (Project project : projectPage.getRecords()) {
            if (project.getSponsorId() != null) {
                com.fundtogether.entity.SysUser sponsor = sysUserService.getById(project.getSponsorId());
                if (sponsor != null) {
                    project.setSponsorName(sponsor.getNickname() != null ? sponsor.getNickname() : "发起人_" + sponsor.getId());
                }
            }
        }
        
        return Result.success(projectPage);
    }

    @PutMapping("/projects/approve/{id}")
    public Result<?> approveProject(@PathVariable Long id) {
        projectService.approveProject(id);
        return Result.success("项目审核通过");
    }

    @PutMapping("/projects/reject/{id}")
    public Result<?> rejectProject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("驳回原因不能为空");
        }
        projectService.rejectProject(id, reason);
        return Result.success("项目已驳回");
    }

    @Autowired
    private com.fundtogether.service.SupportOrderService supportOrderService;

    @GetMapping("/projects/{id}/supporters")
    public Result<?> getProjectSupporters(@PathVariable Long id) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fundtogether.entity.SupportOrder> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.fundtogether.entity.SupportOrder::getProjectId, id)
               .eq(com.fundtogether.entity.SupportOrder::getStatus, 1) // 已支付
               .orderByDesc(com.fundtogether.entity.SupportOrder::getPayTime);
        
        java.util.List<com.fundtogether.entity.SupportOrder> orders = supportOrderService.list(wrapper);
        java.util.List<SupporterVO> voList = orders.stream().map(order -> {
            SupporterVO vo = new SupporterVO();
            // Map explicitly to avoid BeanUtils type-safety warnings and keep output stable.
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setAmount(order.getAmount());
            vo.setMessage(order.getMessage());
            vo.setPayTime(order.getPayTime());
            vo.setDeliveryStatus(order.getDeliveryStatus());
            vo.setDeliveryTime(order.getDeliveryTime());
            vo.setExpressNo(order.getExpressNo());
            SysUser user = sysUserService.getById(order.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(voList);
    }

    @PutMapping("/projects/update-content")
    public Result<?> updateProjectContent(@RequestBody @Valid AdminProjectContentUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Project project = projectService.getById(dto.getId());
        if (project == null) {
            return Result.error("项目不存在");
        }

        project.setSummary(dto.getSummary());
        project.setContent(dto.getContent());
        project.setVideoUrl(dto.getVideoUrl());
        if (dto.getStartTime() != null) {
            project.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            project.setEndTime(dto.getEndTime());
        }
        projectService.updateById(project);

        return Result.success("项目内容已更新");
    }

    @PutMapping("/user/status/{id}")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String disableReason = (String) body.get("disableReason");
        if (status == null) {
            return Result.error("状态不能为空");
        }
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        if (status == 0) {
            user.setDisableReason(disableReason);
        } else {
            user.setDisableReason(null);
        }
        sysUserService.updateById(user);
        return Result.success("用户状态更新成功");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalAmount = supportOrderMapper.getTotalAmount();
        Long projectCount = projectService.count();
        Long activeUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Long successfulProjects = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getStatus, 5)); // Assuming 5 is completed/successful

        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        stats.put("totalProjects", projectCount);
        stats.put("totalUsers", activeUsers);
        stats.put("successfulProjects", successfulProjects);
        
        return Result.success(stats);
    }

    @GetMapping("/stats/platform")
    public Result<Map<String, Object>> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalAmount = supportOrderMapper.getTotalAmount();
        Long projectCount = projectService.count();
        Long activeUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        
        stats.put("totalAmount", totalAmount);
        stats.put("projectCount", projectCount);
        stats.put("activeUsers", activeUsers);
        
        return Result.success(stats);
    }

    @GetMapping("/stats/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = projectMapper.getCategoryStats();
        return Result.success(stats);
    }

    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getFundingTrend() {
        List<Map<String, Object>> trend = supportOrderMapper.getFundingTrend();
        return Result.success(trend);
    }

    @GetMapping("/stats/export")
    public void exportStats(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("分类统计数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<Map<String, Object>> stats = projectMapper.getCategoryStats();
        List<CategoryStatExcelDTO> dtoList = new ArrayList<>();
        
        for (Map<String, Object> stat : stats) {
            CategoryStatExcelDTO dto = new CategoryStatExcelDTO();
            dto.setCategoryName((String) stat.get("categoryName"));
            dto.setProjectCount(((Number) stat.get("projectCount")).longValue());
            Object totalAmountObj = stat.get("totalAmount");
            if (totalAmountObj instanceof BigDecimal) {
                dto.setTotalAmount((BigDecimal) totalAmountObj);
            } else if (totalAmountObj instanceof Number) {
                dto.setTotalAmount(new BigDecimal(totalAmountObj.toString()));
            } else {
                dto.setTotalAmount(BigDecimal.ZERO);
            }
            dtoList.add(dto);
        }

        EasyExcel.write(response.getOutputStream(), CategoryStatExcelDTO.class)
                 .sheet("分类统计")
                 .doWrite(dtoList);
    }
}
