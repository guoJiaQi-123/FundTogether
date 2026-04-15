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

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private UserPaymentMethodService userPaymentMethodService;
    
    @Autowired
    private UserAuthInfoService userAuthInfoService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @GetMapping("/info")
    public Result<com.fundtogether.entity.SysUser> getUserInfo() {
        com.fundtogether.entity.SysUser user = sysUserService.getById(getCurrentUserId());
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

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

    @GetMapping("/payment-methods")
    public Result<List<UserPaymentMethod>> getPaymentMethods() {
        return Result.success(userPaymentMethodService.getUserPaymentMethods(getCurrentUserId()));
    }

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

    @GetMapping("/auth-info")
    public Result<UserAuthInfo> getAuthInfo() {
        return Result.success(userAuthInfoService.getAuthInfo(getCurrentUserId()));
    }
}
