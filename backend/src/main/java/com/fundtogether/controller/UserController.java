package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.common.annotation.RateLimit;
import com.fundtogether.dto.*;
import com.fundtogether.security.LoginUser;
import com.fundtogether.entity.UserPaymentMethod;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.UserPaymentMethodService;
import com.fundtogether.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.fundtogether.service.UserAuthInfoService;
import com.fundtogether.entity.UserAuthInfo;

/**
 * 用户控制器
 * <p>
 * 提供用户账户核心功能的API，包括：
 * <ul>
 *   <li>用户注册：新用户注册（限流保护）</li>
 *   <li>用户登录：用户登录获取Token（限流保护）</li>
 *   <li>查询用户信息：获取当前登录用户的个人信息（密码脱敏）</li>
 *   <li>修改密码：用户修改登录密码</li>
 *   <li>重置密码：通过验证信息重置密码</li>
 *   <li>修改个人资料：更新昵称、头像等个人信息</li>
 *   <li>绑定支付方式：绑定提现用的支付账户</li>
 *   <li>查询支付方式：获取当前用户已绑定的支付方式列表</li>
 *   <li>提交实名认证：提交实名认证申请</li>
 *   <li>查询实名认证：获取当前用户的实名认证信息</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /** 用户服务，处理用户注册、登录、信息修改等核心操作 */
    @Autowired
    private SysUserService sysUserService;

    /** 用户支付方式服务，处理支付方式的绑定与查询 */
    @Autowired
    private UserPaymentMethodService userPaymentMethodService;

    /** 用户实名认证服务，处理实名认证的提交与查询 */
    @Autowired
    private UserAuthInfoService userAuthInfoService;

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
     * 获取当前登录用户信息
     * <p>
     * 返回用户完整信息，密码字段置为null进行脱敏处理。
     * </p>
     *
     * @return 用户信息（密码已脱敏）
     */
    @GetMapping("/info")
    public Result<com.fundtogether.entity.SysUser> getUserInfo() {
        com.fundtogether.entity.SysUser user = sysUserService.getById(getCurrentUserId());
        if (user != null) {
            user.setPassword(null); // 脱敏处理：密码置为null
        }
        return Result.success(user);
    }

    /**
     * 用户注册
     * <p>
     * 新用户注册接口，限流保护：60秒内最多3次请求。
     * </p>
     *
     * @param registerDTO   注册DTO，包含用户名、密码、昵称等
     * @param bindingResult 参数校验结果
     * @return 注册结果提示
     */
    @PostMapping("/register")
    @RateLimit(permits = 3, seconds = 60, key = "register")
    public Result<?> register(@RequestBody @Valid UserRegisterDTO registerDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            sysUserService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     * <p>
     * 用户登录接口，验证用户名密码后返回JWT Token。限流保护：30秒内最多5次请求。
     * </p>
     *
     * @param loginDTO      登录DTO，包含用户名和密码
     * @param bindingResult 参数校验结果
     * @return 登录结果，包含Token和用户信息的LoginVO
     */
    @PostMapping("/login")
    @RateLimit(permits = 5, seconds = 30, key = "login")
    public Result<LoginVO> login(@RequestBody @Valid UserLoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(500, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            LoginVO loginVO = sysUserService.login(loginDTO);
            return Result.success(loginVO);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 修改密码
     * <p>
     * 用户修改登录密码，修改成功后需重新登录。
     * </p>
     *
     * @param dto           密码修改DTO，包含旧密码和新密码
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody @Valid UserUpdatePasswordDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            sysUserService.updatePassword(getCurrentUserId(), dto);
            return Result.success("密码修改成功，请重新登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置密码
     * <p>
     * 通过验证信息重置密码，重置成功后需重新登录。
     * </p>
     *
     * @param dto           密码重置DTO，包含验证信息和新密码
     * @param bindingResult 参数校验结果
     * @return 重置结果提示
     */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody @Valid UserResetPasswordDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            sysUserService.resetPassword(dto);
            return Result.success("密码重置成功，请重新登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改个人资料
     * <p>
     * 更新用户的昵称、头像、个人简介等资料信息。
     * </p>
     *
     * @param dto           个人资料更新DTO，包含昵称、头像、简介等
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody @Valid UserProfileUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            sysUserService.updateProfile(getCurrentUserId(), dto);
            return Result.success("个人信息修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 绑定支付方式
     * <p>
     * 用户绑定提现用的支付账户（如银行卡、支付宝等）。
     * </p>
     *
     * @param dto           支付方式DTO，包含支付类型、账户名、账号等
     * @param bindingResult 参数校验结果
     * @return 绑定结果提示
     */
    @PostMapping("/payment-method")
    public Result<?> bindPaymentMethod(@RequestBody @Valid UserPaymentMethodDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            userPaymentMethodService.bindPaymentMethod(getCurrentUserId(), dto);
            return Result.success("支付方式绑定成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户已绑定的支付方式列表
     *
     * @return 支付方式列表
     */
    @GetMapping("/payment-methods")
    public Result<List<UserPaymentMethod>> getPaymentMethods() {
        return Result.success(userPaymentMethodService.getUserPaymentMethods(getCurrentUserId()));
    }

    /**
     * 提交实名认证申请
     * <p>
     * 用户提交实名认证信息（真实姓名、身份证号等），等待管理员审核。
     * </p>
     *
     * @param dto           实名认证DTO，包含真实姓名、证件号、证件照片等
     * @param bindingResult 参数校验结果
     * @return 提交结果提示
     */
    @PostMapping("/auth-info")
    public Result<?> submitAuthInfo(@RequestBody @Valid UserAuthInfoDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            userAuthInfoService.submitAuthInfo(getCurrentUserId(), dto);
            return Result.success("实名认证申请提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户的实名认证信息
     *
     * @return 实名认证信息
     */
    @GetMapping("/auth-info")
    public Result<UserAuthInfo> getAuthInfo() {
        return Result.success(userAuthInfoService.getAuthInfo(getCurrentUserId()));
    }
}
