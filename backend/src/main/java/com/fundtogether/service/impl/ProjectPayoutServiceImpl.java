package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.ProjectPayout;
import com.fundtogether.mapper.ProjectPayoutMapper;
import com.fundtogether.service.ProjectPayoutService;
import org.springframework.stereotype.Service;

@Service
public class ProjectPayoutServiceImpl extends ServiceImpl<ProjectPayoutMapper, ProjectPayout> implements ProjectPayoutService {
}
