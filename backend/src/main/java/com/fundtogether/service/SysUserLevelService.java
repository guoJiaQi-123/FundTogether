package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.SysUserLevel;

public interface SysUserLevelService extends IService<SysUserLevel> {
    
    /**
     * Get the user's level based on their total support amount
     * @param userId the user ID
     * @return the matched user level, or null if no level matches
     */
    SysUserLevel getCurrentUserLevel(Long userId);
}
