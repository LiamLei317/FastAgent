package com.fast.agent.web.controller;

import com.fast.agent.common.result.R;
import com.fast.agent.model.dto.ChatRequest;
import com.fast.agent.model.dto.ChatResponse;
import com.fast.agent.model.entity.Message;
import com.fast.agent.model.entity.Session;
import com.fast.agent.model.vo.SessionVO;
import com.fast.agent.service.ConversationContextService;
import com.fast.agent.service.MessageService;
import com.fast.agent.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SessionService sessionService;
    private final MessageService messageService;
    private final ConversationContextService contextService;

    /**
     * 创建会话
     */
    @PostMapping("/session/create")
    public R<SessionVO> createSession(@RequestParam String userId) {
        // 创建 Session 实体
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle("新会话");
        Session createdSession = sessionService.createSession(session);
        
        // 转换为 SessionVO
        SessionVO sessionVO = new SessionVO();
        sessionVO.setId(createdSession.getId());
        sessionVO.setUserId(createdSession.getUserId());
        sessionVO.setTitle(createdSession.getTitle());
        sessionVO.setStatus(1); // 默认活跃状态
        sessionVO.setCreateTime(createdSession.getCreateTime());
        sessionVO.setUpdateTime(createdSession.getUpdateTime());
        
        return R.success(sessionVO);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/session/list")
    public R<List<SessionVO>> getSessionList(@RequestParam String userId) {
        // 获取 Session 列表并转换为 SessionVO
        List<Session> sessions = sessionService.getSessionsByUserId(userId);
        List<SessionVO> sessionVOs = sessions.stream().map(session -> {
            SessionVO vo = new SessionVO();
            vo.setId(session.getId());
            vo.setUserId(session.getUserId());
            vo.setTitle(session.getTitle());
            vo.setStatus(1); // 默认活跃状态
            vo.setCreateTime(session.getCreateTime());
            vo.setUpdateTime(session.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());
        
        return R.success(sessionVOs);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public R<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            // 校验 sessionId 是否存在
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return R.fail("sessionId 不能为空");
            }
            
            Session session = sessionService.getSessionById(Long.parseLong(sessionId));
            if (session == null) {
                return R.fail("会话不存在，sessionId: " + sessionId);
            }

            log.info("普通聊天开始，sessionId: {}, message: {}", sessionId, request.getMessage());
            
            // 保存用户消息到数据库
            try {
                Message userMessage = new Message();
                userMessage.setSessionId(Long.parseLong(sessionId));
                userMessage.setRole("user");
                userMessage.setContent(request.getMessage());
                userMessage.setCreateTime(java.time.LocalDateTime.now());
                
                Message savedUserMessage = messageService.createMessage(userMessage);
                log.info("用户消息已保存到数据库，messageId: {}", savedUserMessage.getId());
            } catch (Exception e) {
                log.error("保存用户消息失败", e);
                // 即使保存失败也继续处理对话
            }
            
            // 保存用户消息到 Redis 上下文
            contextService.saveMessage(sessionId, "user", request.getMessage());
            
            // 获取最近5轮对话上下文
            String context = contextService.formatContext(sessionId, 5);
            
            // 模拟AI回复内容（基于上下文）
            String aiResponse = "聊天功能待实现";
            if (!context.isEmpty()) {
                aiResponse = "基于对话上下文的回复：" + aiResponse + "（已包含" + sessionId + "的上下文）";
            }
            
            // 打印完整消息内容
            String fullMessage = context.isEmpty() ? request.getMessage() : context + "\n\n" + request.getMessage();
            log.info("=== 普通对话 - 发送给大模型的完整消息 ===");
            log.info("sessionId: {}", sessionId);
            log.info("原始消息: {}", request.getMessage());
            log.info("上下文长度: {} 字符", context.length());
            log.info("完整消息长度: {} 字符", fullMessage.length());
            log.info("完整消息内容: {}", fullMessage);
            log.info("============================================");
            
            // 保存AI回复到数据库和 Redis 上下文
            try {
                // 保存到数据库
                Message aiMessage = new Message();
                aiMessage.setSessionId(Long.parseLong(sessionId));
                aiMessage.setRole("assistant");
                aiMessage.setContent(aiResponse);
                aiMessage.setCreateTime(java.time.LocalDateTime.now());
                
                Message savedAiMessage = messageService.createMessage(aiMessage);
                log.info("AI回复已保存到数据库，messageId: {}", savedAiMessage.getId());
                
                // 保存到 Redis 上下文
                contextService.saveMessage(sessionId, "assistant", aiResponse);
                log.info("AI回复已保存到Redis上下文，sessionId: {}", sessionId);
            } catch (Exception e) {
                log.error("保存AI回复失败", e);
            }
            
            return R.success(ChatResponse.builder()
                    .sessionId(sessionId)
                    .content(aiResponse)
                    .finish(true)
                    .totalTokens(0)
                    .build());
        } catch (NumberFormatException e) {
            log.error("sessionId 格式错误: {}", request.getSessionId(), e);
            return R.fail("sessionId 格式错误，必须是数字");
        } catch (Exception e) {
            log.error("聊天异常", e);
            return R.fail("聊天处理异常: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public R<Void> deleteSession(@PathVariable String sessionId) {
        try {
            // 校验 sessionId 是否存在
            Session session = sessionService.getSessionById(Long.parseLong(sessionId));
            if (session == null) {
                return R.fail("会话不存在，sessionId: " + sessionId);
            }
            
            boolean result = sessionService.deleteSession(Long.parseLong(sessionId));
            if (result) {
                return R.success();
            } else {
                return R.fail("删除会话失败");
            }
        } catch (NumberFormatException e) {
            log.error("sessionId 格式错误: {}", sessionId, e);
            return R.fail("sessionId 格式错误，必须是数字");
        } catch (Exception e) {
            log.error("删除会话异常", e);
            return R.fail("删除会话异常: " + e.getMessage());
        }
    }
}
