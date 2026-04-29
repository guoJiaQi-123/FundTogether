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
import com.fundtogether.entity.ProjectRecommend;
import com.fundtogether.vo.SupporterVO;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.ProjectRecommendService;
import com.fundtogether.service.HeatRuleService;
import com.fundtogether.service.HeatCalculationService;
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

/**
 * 管理员控制器
 * <p>
 * 提供系统管理后台的所有管理功能，包括：
 * <ul>
 * <li>系统公告管理：创建、发布、删除公告，发布时自动推送消息给所有用户</li>
 * <li>用户管理：分页查询用户列表、修改用户角色、更新用户状态（启用/禁用）</li>
 * <li>项目管理：查看待审核项目、审核通过/驳回项目、下架项目、修改项目内容、查看项目支持者</li>
 * <li>数据统计：平台总览统计、分类统计、筹款趋势、导出分类统计Excel</li>
 * <li>推荐管理：查看/添加/删除/排序推荐项目、查询可推荐的在线项目</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** 下架项目 —— 管理员强制下架指定项目，需提供下架原因 */
    @PutMapping("/projects/takedown/{id}")
    public Result<?> takedownProject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("下架原因不能为空");
        }
        projectService.takedownProject(id, reason);
        return Result.success("项目已下架");
    }

    /** 用户服务，用于用户查询、角色修改、状态更新等操作 */
    @Autowired
    private SysUserService sysUserService;

    /** 项目服务，用于项目审核、内容修改、下架等操作 */
    @Autowired
    private ProjectService projectService;

    /** 项目推荐服务，用于管理首页推荐项目 */
    @Autowired
    private ProjectRecommendService projectRecommendService;

    /** 项目Mapper，用于查询分类统计等原生SQL操作 */
    @Autowired
    private ProjectMapper projectMapper;

    /** 支持订单Mapper，用于查询筹款总额、筹款趋势等统计 */
    @Autowired
    private SupportOrderMapper supportOrderMapper;

    /** 系统公告服务，用于公告的增删改查 */
    @Autowired
    private com.fundtogether.service.SysNoticeService sysNoticeService;

    /** 用户消息服务，用于公告发布时批量创建用户站内信 */
    @Autowired
    private com.fundtogether.service.SysUserMessageService sysUserMessageService;

    @Autowired
    private HeatRuleService heatRuleService;

    @Autowired
    private HeatCalculationService heatCalculationService;

    /**
     * 处理公告发布后的通知逻辑
     * <p>
     * 当公告状态为已发布(status=1)时，为所有用户创建一条站内信，
     * 并通过WebSocket广播通知前端刷新。
     * </p>
     *
     * @param notice 已发布的系统公告实体
     */
    private void handleNoticePublished(com.fundtogether.entity.SysNotice notice) {
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            // 查询所有用户，为每人生成一条站内信
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

            // 通过WebSocket广播新公告通知
            com.fundtogether.websocket.WebSocketServer.broadcastMessage("New notice published: " + notice.getTitle());
        }
    }

    /**
     * 分页查询系统公告列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页公告列表，按创建时间倒序排列
     */
    @GetMapping("/notices")
    public Result<IPage<com.fundtogether.entity.SysNotice>> getNotices(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<com.fundtogether.entity.SysNotice> page = new Page<>(current, size);
        LambdaQueryWrapper<com.fundtogether.entity.SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(com.fundtogether.entity.SysNotice::getCreatedAt);
        return Result.success(sysNoticeService.page(page, wrapper));
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
    @PostMapping("/notices")
    public Result<?> createNotice(@RequestBody com.fundtogether.entity.SysNotice sysNotice) {
        sysNoticeService.save(sysNotice);
        handleNoticePublished(sysNotice);
        return Result.success("公告创建成功");
    }

    /**
     * 发布系统公告
     * <p>
     * 将草稿状态的公告发布，发布后自动触发通知推送逻辑。
     * </p>
     *
     * @param id 公告ID
     * @return 发布结果提示
     */
    @PutMapping("/notices/{id}/publish")
    public Result<?> publishNotice(@PathVariable Long id) {
        com.fundtogether.entity.SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            return Result.error("公告已发布");
        }
        notice.setStatus(1); // 设置为已发布状态
        sysNoticeService.updateById(notice);
        handleNoticePublished(notice);
        return Result.success("发布成功");
    }

    /**
     * 删除系统公告
     *
     * @param id 公告ID
     * @return 删除结果提示
     */
    @DeleteMapping("/notices/{id}")
    public Result<?> deleteNotice(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询用户列表
     * <p>
     * 返回的用户信息中会脱敏处理密码字段（置为null）。
     * </p>
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页用户列表（密码已脱敏）
     */
    @GetMapping("/users")
    public Result<IPage<SysUser>> getUsers(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getAccount, kw)
                    .or().like(SysUser::getNickname, kw)
                    .or().like(SysUser::getEmail, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (role != null && role > 0) {
            // 按位匹配角色掩码
            wrapper.apply("role & {0} > 0", role);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        IPage<SysUser> userPage = sysUserService.page(page, wrapper);
        // 脱敏处理：将密码字段置为null，防止泄露
        userPage.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(userPage);
    }

    /**
     * 修改用户角色
     * <p>
     * 管理员可修改指定用户的角色（如普通用户、管理员等）。
     * </p>
     *
     * @param dto           用户角色更新DTO，包含用户ID和目标角色
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PostMapping("/user/role")
    public Result<?> updateUserRole(@RequestBody @Valid UserRoleUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        SysUser user = sysUserService.getById(dto.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setRole(dto.getRole()); // 更新用户角色
        sysUserService.updateById(user);

        return Result.success("角色修改成功");
    }

    /**
     * 分页查询待审核项目列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页待审核项目列表
     */
    @GetMapping("/projects/pending")
    public Result<IPage<Project>> getPendingProjects(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Project> page = projectService.getPendingProjects(current, size);
        return Result.success(page);
    }

    /**
     * 分页查询所有项目列表
     * <p>
     * 返回所有项目并补充项目发起人名称信息，按创建时间倒序排列。
     * </p>
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页项目列表（含发起人名称）
     */
    @GetMapping("/projects/all")
    public Result<IPage<Project>> getAllProjects(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Project> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                current, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(Project::getCreatedAt);
        IPage<Project> projectPage = projectService.page(page, wrapper);

        // 补充项目发起人名称
        for (Project project : projectPage.getRecords()) {
            if (project.getSponsorId() != null) {
                com.fundtogether.entity.SysUser sponsor = sysUserService.getById(project.getSponsorId());
                if (sponsor != null) {
                    project.setSponsorName(
                            sponsor.getNickname() != null ? sponsor.getNickname() : "发起人_" + sponsor.getId());
                }
            }
        }

        return Result.success(projectPage);
    }

    /**
     * 审核通过项目
     *
     * @param id 项目ID
     * @return 审核结果提示
     */
    @PutMapping("/projects/approve/{id}")
    public Result<?> approveProject(@PathVariable Long id) {
        projectService.approveProject(id);
        return Result.success("项目审核通过");
    }

    /**
     * 驳回项目
     * <p>
     * 管理员驳回待审核项目，需提供驳回原因。
     * </p>
     *
     * @param id   项目ID
     * @param body 请求体，包含reason字段（驳回原因）
     * @return 驳回结果提示
     */
    @PutMapping("/projects/reject/{id}")
    public Result<?> rejectProject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("驳回原因不能为空");
        }
        projectService.rejectProject(id, reason);
        return Result.success("项目已驳回");
    }

    /** 支持订单服务，用于查询项目支持者信息 */
    @Autowired
    private com.fundtogether.service.SupportOrderService supportOrderService;

    /**
     * 查询指定项目的支持者列表
     * <p>
     * 返回该项目所有已支付的支持订单，并关联查询支持者的昵称和头像。
     * </p>
     *
     * @param id 项目ID
     * @return 支持者VO列表，包含用户昵称、头像、支持金额、留言、发货状态等
     */
    @GetMapping("/projects/{id}/supporters")
    public Result<?> getProjectSupporters(@PathVariable Long id) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fundtogether.entity.SupportOrder> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.fundtogether.entity.SupportOrder::getProjectId, id)
                .eq(com.fundtogether.entity.SupportOrder::getStatus, 1) // 已支付
                .orderByDesc(com.fundtogether.entity.SupportOrder::getPayTime);

        java.util.List<com.fundtogether.entity.SupportOrder> orders = supportOrderService.list(wrapper);
        java.util.List<SupporterVO> voList = orders.stream().map(order -> {
            SupporterVO vo = new SupporterVO();
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setAmount(order.getAmount());
            vo.setMessage(order.getMessage());
            vo.setPayTime(order.getPayTime());
            vo.setDeliveryStatus(order.getDeliveryStatus());
            vo.setDeliveryTime(order.getDeliveryTime());
            vo.setExpressNo(order.getExpressNo());
            // 关联查询支持者用户信息
            SysUser user = sysUserService.getById(order.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 管理员修改项目内容
     * <p>
     * 允许管理员直接修改项目的摘要、详情、视频地址、起止时间等内容。
     * </p>
     *
     * @param dto           项目内容更新DTO，包含项目ID及待修改字段
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PutMapping("/projects/update-content")
    public Result<?> updateProjectContent(@RequestBody @Valid AdminProjectContentUpdateDTO dto,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        Project project = projectService.getById(dto.getId());
        if (project == null) {
            return Result.error("项目不存在");
        }

        // 更新项目各字段
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

    /**
     * 更新用户状态（启用/禁用）
     * <p>
     * 禁用用户时需提供禁用原因，启用时清除禁用原因。
     * </p>
     *
     * @param id   用户ID
     * @param body 请求体，包含status（0-禁用，1-启用）和disableReason（禁用原因）
     * @return 更新结果提示
     */
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
            // 禁用用户时记录禁用原因
            user.setDisableReason(disableReason);
        } else {
            // 启用用户时清除禁用原因
            user.setDisableReason(null);
        }
        sysUserService.updateById(user);
        return Result.success("用户状态更新成功");
    }

    /**
     * 获取平台总览统计数据
     * <p>
     * 返回筹款总额、项目总数、活跃用户数、成功项目数等统计指标。
     * </p>
     *
     * @return 统计数据Map，包含totalAmount、totalProjects、totalUsers、successfulProjects
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalAmount = supportOrderMapper.getTotalAmount();
        Long projectCount = projectService.count();
        Long activeUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Long successfulProjects = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getStatus, 5)); // status=5表示已完成/成功的项目

        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        stats.put("totalProjects", projectCount);
        stats.put("totalUsers", activeUsers);
        stats.put("successfulProjects", successfulProjects);

        return Result.success(stats);
    }

    /**
     * 获取平台统计数据（精简版）
     * <p>
     * 返回筹款总额、项目总数、活跃用户数。
     * </p>
     *
     * @return 统计数据Map，包含totalAmount、projectCount、activeUsers
     */
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

    /**
     * 获取分类统计数据
     * <p>
     * 按项目分类统计各分类下的项目数量和筹款总额。
     * </p>
     *
     * @return 分类统计列表，每项包含categoryName、projectCount、totalAmount
     */
    @GetMapping("/stats/category")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = projectMapper.getCategoryStats();
        return Result.success(stats);
    }

    /**
     * 获取筹款趋势数据
     * <p>
     * 按时间维度统计筹款趋势，用于绘制趋势图表。
     * </p>
     *
     * @return 趋势数据列表，每项包含日期和对应金额
     */
    @GetMapping("/stats/trend")
    public Result<List<Map<String, Object>>> getFundingTrend() {
        List<Map<String, Object>> trend = supportOrderMapper.getFundingTrend();
        return Result.success(trend);
    }

    /**
     * 导出分类统计数据为Excel文件
     * <p>
     * 使用EasyExcel将分类统计数据写入xlsx文件并通过HTTP响应下载。
     * </p>
     *
     * @param response HTTP响应对象，用于写入Excel文件流
     * @throws IOException 文件写入异常
     */
    @GetMapping("/stats/export")
    public void exportStats(HttpServletResponse response) throws IOException {
        // 设置响应头为Excel文件类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("分类统计数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 查询分类统计数据并转换为Excel DTO
        List<Map<String, Object>> stats = projectMapper.getCategoryStats();
        List<CategoryStatExcelDTO> dtoList = new ArrayList<>();

        for (Map<String, Object> stat : stats) {
            CategoryStatExcelDTO dto = new CategoryStatExcelDTO();
            dto.setCategoryName((String) stat.get("categoryName"));
            dto.setProjectCount(((Number) stat.get("projectCount")).longValue());
            // 处理金额类型转换，兼容不同数据库返回的数值类型
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

        // 使用EasyExcel写入响应输出流
        EasyExcel.write(response.getOutputStream(), CategoryStatExcelDTO.class)
                .sheet("分类统计")
                .doWrite(dtoList);
    }

    /**
     * 获取推荐项目列表
     * <p>
     * 返回所有推荐项目及其关联的项目信息。
     * </p>
     *
     * @return 推荐项目列表（含项目详情）
     */
    @GetMapping("/recommends")
    public Result<List<ProjectRecommend>> getRecommends() {
        List<ProjectRecommend> list = projectRecommendService.listWithProjectInfo();
        return Result.success(list);
    }

    /**
     * 添加推荐项目
     * <p>
     * 将指定项目添加到推荐列表，可指定排序权重。
     * </p>
     *
     * @param body 请求体，包含projectId（项目ID）和sortOrder（排序权重，可选）
     * @return 添加结果提示
     */
    @PostMapping("/recommends")
    public Result<?> addRecommend(@RequestBody Map<String, Object> body) {
        Object projectIdObj = body.get("projectId");
        Object sortOrderObj = body.get("sortOrder");
        if (projectIdObj == null) {
            return Result.error("项目ID不能为空");
        }
        Long projectId = Long.valueOf(projectIdObj.toString());
        Integer sortOrder = sortOrderObj != null ? Integer.valueOf(sortOrderObj.toString()) : null;
        try {
            projectRecommendService.addRecommend(projectId, sortOrder);
            return Result.success("添加推荐成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除推荐项目
     *
     * @param id 推荐记录ID
     * @return 移除结果提示
     */
    @DeleteMapping("/recommends/{id}")
    public Result<?> removeRecommend(@PathVariable Long id) {
        try {
            projectRecommendService.removeRecommend(id);
            return Result.success("移除推荐成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新推荐项目排序
     * <p>
     * 接收按新排序顺序排列的推荐ID列表，批量更新排序权重。
     * </p>
     *
     * @param orderedIds 按新顺序排列的推荐记录ID列表
     * @return 排序更新结果提示
     */
    @PutMapping("/recommends/sort")
    public Result<?> batchUpdateSortOrder(@RequestBody List<Long> orderedIds) {
        projectRecommendService.batchUpdateSortOrder(orderedIds);
        return Result.success("排序更新成功");
    }

    /**
     * 查询可推荐的在线项目列表
     * <p>
     * 返回状态为在线(status=1)的项目，支持按关键词搜索，
     * 按热度倒序排列，用于管理员选择项目添加到推荐列表。
     * </p>
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @param keyword 搜索关键词（匹配标题或摘要），可选
     * @return 分页在线项目列表
     */
    @GetMapping("/recommends/available-projects")
    public Result<IPage<Project>> getAvailableProjects(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, 1); // 仅查询在线项目
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 按标题或摘要模糊匹配
            wrapper.and(w -> w.like(Project::getTitle, keyword).or().like(Project::getSummary, keyword));
        }
        wrapper.orderByDesc(Project::getHeat); // 按热度倒序
        IPage<Project> result = projectService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/heat-rules")
    public Result<List<com.fundtogether.entity.HeatRule>> getHeatRules() {
        LambdaQueryWrapper<com.fundtogether.entity.HeatRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(com.fundtogether.entity.HeatRule::getId);
        return Result.success(heatRuleService.list(wrapper));
    }

    @PutMapping("/heat-rules/{id}/weight")
    public Result<?> updateHeatRuleWeight(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object weightObj = body.get("weight");
        if (weightObj == null) {
            return Result.error("权重不能为空");
        }
        BigDecimal weight = new BigDecimal(weightObj.toString());
        if (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(new BigDecimal("5")) > 0) {
            return Result.error("权重必须在0-5之间");
        }
        heatRuleService.updateRuleWeight(id, weight);
        return Result.success("权重更新成功");
    }

    @PutMapping("/heat-rules/{id}/toggle")
    public Result<?> toggleHeatRule(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object enabledObj = body.get("enabled");
        if (enabledObj == null) {
            return Result.error("启用状态不能为空");
        }
        boolean enabled = Boolean.parseBoolean(enabledObj.toString());
        heatRuleService.toggleRule(id, enabled);
        return Result.success(enabled ? "规则已启用" : "规则已禁用");
    }

    @PostMapping("/heat-rules/reset")
    public Result<?> resetHeatRules() {
        heatRuleService.resetToDefault();
        return Result.success("规则已重置为默认值");
    }

    @PostMapping("/heat-rules/recalculate")
    public Result<Map<String, Object>> recalculateHeat() {
        int count = heatCalculationService.recalculateActiveProjectHeat();
        Map<String, Object> result = new HashMap<>();
        result.put("updatedCount", count);
        return Result.success(result);
    }

    @Autowired
    private com.fundtogether.mapper.SysUserMapper sysUserMapper;

    /**
     * 数据大盘概览：汇总核心 KPI，用于管理后台首页指标卡片。
     */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getOverviewStats() {
        Map<String, Object> data = new HashMap<>();

        BigDecimal totalAmount = supportOrderMapper.getTotalAmount();
        BigDecimal todayAmount = supportOrderMapper.getTodayAmount();
        BigDecimal yesterdayAmount = supportOrderMapper.getYesterdayAmount();
        Long todayOrders = supportOrderMapper.getTodayOrderCount();

        Long projectCount = projectService.count();
        Long onlineProjects = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getStatus, 1));
        Long pendingProjects = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getStatus, 0));
        Long successfulProjects = projectService.count(new LambdaQueryWrapper<Project>().eq(Project::getStatus, 5));

        Long totalUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0));
        Long activeUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Long disabledUsers = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 0));

        Map<String, Object> reached = projectMapper.getReachedRate();
        double reachedRate = 0.0;
        if (reached != null && reached.get("total") != null) {
            long total = ((Number) reached.get("total")).longValue();
            long r = reached.get("reached") == null ? 0 : ((Number) reached.get("reached")).longValue();
            reachedRate = total > 0 ? (r * 100.0 / total) : 0.0;
        }

        double dod = 0.0;
        if (yesterdayAmount != null && yesterdayAmount.compareTo(BigDecimal.ZERO) > 0) {
            dod = todayAmount.subtract(yesterdayAmount)
                    .multiply(new BigDecimal("100"))
                    .divide(yesterdayAmount, 2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
        }

        data.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        data.put("todayAmount", todayAmount != null ? todayAmount : BigDecimal.ZERO);
        data.put("todayOrders", todayOrders != null ? todayOrders : 0L);
        data.put("dayOverDay", dod);
        data.put("totalProjects", projectCount);
        data.put("onlineProjects", onlineProjects);
        data.put("pendingProjects", pendingProjects);
        data.put("successfulProjects", successfulProjects);
        data.put("totalUsers", totalUsers);
        data.put("activeUsers", activeUsers);
        data.put("disabledUsers", disabledUsers);
        data.put("reachedRate", reachedRate);
        return Result.success(data);
    }

    /**
     * 近 N 天每日筹款趋势
     */
    @GetMapping("/stats/daily-funding")
    public Result<List<Map<String, Object>>> getDailyFunding(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(supportOrderMapper.getDailyFundingTrend(days));
    }

    /**
     * 近 N 天支付时段分布
     */
    @GetMapping("/stats/hourly-distribution")
    public Result<List<Map<String, Object>>> getHourlyDistribution(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(supportOrderMapper.getHourlyDistribution(days));
    }

    /**
     * Top N 筹款项目
     */
    @GetMapping("/stats/top-projects")
    public Result<List<Map<String, Object>>> getTopProjects(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(supportOrderMapper.getTopProjects(limit));
    }

    /**
     * Top N 支持用户
     */
    @GetMapping("/stats/top-supporters")
    public Result<List<Map<String, Object>>> getTopSupporters(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(supportOrderMapper.getTopSupporters(limit));
    }

    /**
     * 项目状态分布
     */
    @GetMapping("/stats/project-status")
    public Result<List<Map<String, Object>>> getProjectStatusDistribution() {
        return Result.success(projectMapper.getStatusDistribution());
    }

    /**
     * 近 N 天新增项目趋势
     */
    @GetMapping("/stats/daily-projects")
    public Result<List<Map<String, Object>>> getDailyProjects(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(projectMapper.getDailyProjectCreation(days));
    }

    /**
     * 近 N 天新注册用户趋势
     */
    @GetMapping("/stats/daily-users")
    public Result<List<Map<String, Object>>> getDailyUsers(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(sysUserMapper.getDailyUserRegistration(days));
    }

    /**
     * 用户角色分布
     */
    @GetMapping("/stats/role-distribution")
    public Result<Map<String, Object>> getRoleDistribution() {
        return Result.success(sysUserMapper.getRoleDistribution());
    }
}
