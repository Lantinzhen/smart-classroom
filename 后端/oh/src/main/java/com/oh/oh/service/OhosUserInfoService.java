package com.oh.oh.service;

import com.oh.oh.entity.OhosUserInfo;
import com.oh.oh.mapper.OhosUserInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OhosUserInfoService {

    private final OhosUserInfoMapper mapper;

    public OhosUserInfoService(OhosUserInfoMapper mapper) {
        this.mapper = mapper;
    }

    public int register(OhosUserInfo userInfo) {
        // 检查用户名是否已存在
        OhosUserInfo existingUser = mapper.findByCount(userInfo.getName());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        return mapper.insertUserInfo(userInfo);
    }

    public OhosUserInfo login(String count, String password) {
        return mapper.findByCountAndPassword(count, password);
    }

    public List<OhosUserInfo> getAllUsers() {
        return mapper.findAll();
    }

    public int updatePassword(String count, String newPassword) {
        return mapper.updatePassword(count, newPassword);
    }
} 