package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.SysUser;
import com.fundtogether.service.SysUserService;
import com.fundtogether.vo.PublicUserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for user profile display.
 * Note: Only exposes non-sensitive fields.
 */
@RestController
@RequestMapping("/api/public/user")
public class UserPublicController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/{id}")
    public Result<PublicUserProfileVO> getPublicUserProfile(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        PublicUserProfileVO vo = new PublicUserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }
}

