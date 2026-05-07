package com.fast.agent.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.agent.model.entity.Session;
import com.fast.agent.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Tag(name = "会话管理", description = "会话相关接口")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/create")
    @Operation(summary = "创建会话", description = "创建新的会话")
    public ResponseEntity<Session> createSession(@Validated @RequestBody Session session) {
        try {
            Session createdSession = sessionService.createSession(session);
            return ResponseEntity.ok(createdSession);
        } catch (Exception e) {
            log.error("创建会话失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取会话详情", description = "根据ID获取会话详细信息")
    public ResponseEntity<Session> getSession(
            @Parameter(description = "会话ID") @PathVariable String id) {
        try {
            Session session = sessionService.getSessionById(id);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取会话失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户会话列表", description = "获取指定用户的所有会话")
    public ResponseEntity<List<Session>> getSessionsByUserId(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        try {
            List<Session> sessions = sessionService.getSessionsByUserId(userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("获取用户会话列表失败，用户ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "分页获取用户会话", description = "分页获取指定用户的会话列表")
    public ResponseEntity<Page<Session>> getSessionsByUserIdPage(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "当前页码，默认1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "页大小，默认10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<Session> page = sessionService.getSessionsByUserIdPage(userId, current, size);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("分页获取用户会话失败，用户ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新会话", description = "更新会话信息")
    public ResponseEntity<Boolean> updateSession(@Validated @RequestBody Session session) {
        try {
            boolean result = sessionService.updateSession(session);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新会话失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会话", description = "根据ID删除会话")
    public ResponseEntity<Boolean> deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        try {
            boolean result = sessionService.deleteSession(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除会话失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "删除用户所有会话", description = "删除指定用户的所有会话")
    public ResponseEntity<Boolean> deleteSessionsByUserId(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        try {
            boolean result = sessionService.deleteSessionsByUserId(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除用户所有会话失败，用户ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
