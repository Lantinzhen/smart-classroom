package com.oh.oh.controller;

import com.oh.oh.entity.OhosUserInfo;
import com.oh.oh.service.OhosUserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "用户管理")
@Slf4j
public class OhosUserInfoController {

    private final OhosUserInfoService service;

    public OhosUserInfoController(OhosUserInfoService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResponseEntity<?> register(@RequestBody OhosUserInfo userInfo) {
        try {
            int result = service.register(userInfo);
            return ResponseEntity.ok().body("{\"message\": \"注册成功\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<?> login(@RequestBody OhosUserInfo userInfo) {
        try {
            OhosUserInfo result = service.login(userInfo.getName(), userInfo.getPassword());
            if (result != null) {
                return ResponseEntity.ok().body("{\"message\": \"登录成功\"}");
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"用户名或密码错误\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"登录失败\"}");
        }
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有用户")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public ResponseEntity<?> updatePassword(@RequestParam String count, @RequestParam String newPassword) {
        try {
            int result = service.updatePassword(count, newPassword);
            return ResponseEntity.ok().body("{\"message\": \"密码修改成功\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"密码修改失败\"}");
        }
    }
} 