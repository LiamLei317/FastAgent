package com.fast.agent.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fast.agent.model.entity.Message;
import com.fast.agent.service.MessageService;
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
 * 消息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息相关接口")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/create")
    @Operation(summary = "创建消息", description = "创建新的消息")
    public ResponseEntity<Message> createMessage(@Validated @RequestBody Message message) {
        try {
            Message createdMessage = messageService.createMessage(message);
            return ResponseEntity.ok(createdMessage);
        } catch (Exception e) {
            log.error("创建消息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取消息详情", description = "根据ID获取消息详细信息")
    public ResponseEntity<Message> getMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        try {
            Message message = messageService.getMessageById(id);
            if (message != null) {
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取消息失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "获取会话消息列表", description = "获取指定会话的所有消息")
    public ResponseEntity<List<Message>> getMessagesBySessionId(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        try {
            List<Message> messages = messageService.getMessagesBySessionId(sessionId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("获取会话消息列表失败，会话ID: {}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/session/{sessionId}/page")
    @Operation(summary = "分页获取会话消息", description = "分页获取指定会话的消息列表")
    public ResponseEntity<Page<Message>> getMessagesBySessionIdPage(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @Parameter(description = "当前页码，默认1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "页大小，默认20") @RequestParam(defaultValue = "20") Integer size) {
        try {
            Page<Message> page = messageService.getMessagesBySessionIdPage(sessionId, current, size);
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("分页获取会话消息失败，会话ID: {}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/session/{sessionId}/count")
    @Operation(summary = "获取会话消息数量", description = "获取指定会话的消息总数")
    public ResponseEntity<Long> getMessageCountBySessionId(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        try {
            Long count = messageService.getMessageCountBySessionId(sessionId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("获取会话消息数量失败，会话ID: {}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新消息", description = "更新消息信息")
    public ResponseEntity<Boolean> updateMessage(@Validated @RequestBody Message message) {
        try {
            boolean result = messageService.updateMessage(message);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新消息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "根据ID删除消息")
    public ResponseEntity<Boolean> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        try {
            boolean result = messageService.deleteMessage(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除消息失败，ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话所有消息", description = "删除指定会话的所有消息")
    public ResponseEntity<Boolean> deleteMessagesBySessionId(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        try {
            boolean result = messageService.deleteMessagesBySessionId(sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除会话所有消息失败，会话ID: {}", sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
