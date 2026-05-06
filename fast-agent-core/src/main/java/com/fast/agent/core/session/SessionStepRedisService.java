package com.fast.agent.core.session;

import com.fast.agent.model.enums.SessionStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 会话步骤Redis服务
 * 负责存储和获取当前会话所处的流程阶段状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionStepRedisService {

    private final StringRedisTemplate redisTemplate;
    
    /**
     * 会话步骤状态Redis Key前缀
     */
    private static final String SESSION_STEP_KEY_PREFIX = "session:step:";
    
    /**
     * 会话步骤状态过期时间：7天
     */
    private static final Duration EXPIRATION_TIME = Duration.ofDays(7);

    /**
     * 获取会话当前所处的流程步骤
     * @param sessionId 会话ID
     * @return 当前流程步骤，未找到时返回 CREATIVE_CLARIFY
     */
    public SessionStep getSessionStep(String sessionId) {
        try {
            String key = SESSION_STEP_KEY_PREFIX + sessionId;
            String stepCode = redisTemplate.opsForValue().get(key);
            
            if (stepCode == null || stepCode.trim().isEmpty()) {
                log.debug("会话{}未找到步骤状态，返回默认步骤：CREATIVE_CLARIFY", sessionId);
                return SessionStep.CREATIVE_CLARIFY;
            }
            
            SessionStep step = SessionStep.fromCode(stepCode);
            log.debug("获取会话{}的当前步骤：{}", sessionId, step);
            return step;
            
        } catch (Exception e) {
            log.error("获取会话步骤状态失败，sessionId：{}，使用默认步骤", sessionId, e);
            return SessionStep.CREATIVE_CLARIFY;
        }
    }

    /**
     * 设置会话当前所处的流程步骤
     * @param sessionId 会话ID
     * @param step 流程步骤
     * @return 设置是否成功
     */
    public boolean setSessionStep(String sessionId, SessionStep step) {
        try {
            String key = SESSION_STEP_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, step.getCode(), EXPIRATION_TIME);
            
            log.info("设置会话{}的步骤状态为：{}", sessionId, step);
            return true;
            
        } catch (Exception e) {
            log.error("设置会话步骤状态失败，sessionId：{}，step：{}", sessionId, step, e);
            return false;
        }
    }

    /**
     * 推进会话到下一个流程步骤
     * @param sessionId 会话ID
     * @return 推进后的步骤，推进失败时返回当前步骤
     */
    public SessionStep proceedToNextStep(String sessionId) {
        try {
            SessionStep currentStep = getSessionStep(sessionId);
            
            // 如果当前步骤不能推进（已完成或无效），直接返回
            if (!currentStep.canProceed()) {
                log.debug("会话{}当前步骤{}无法推进，直接返回", sessionId, currentStep);
                return currentStep;
            }
            
            SessionStep nextStep = currentStep.getNextStep();
            boolean success = setSessionStep(sessionId, nextStep);
            
            if (success) {
                log.info("会话{}步骤推进成功：{} -> {}", sessionId, currentStep, nextStep);
                return nextStep;
            } else {
                log.warn("会话{}步骤推进失败，保持当前步骤：{}", sessionId, currentStep);
                return currentStep;
            }
            
        } catch (Exception e) {
            log.error("推进会话步骤失败，sessionId：{}", sessionId, e);
            return getSessionStep(sessionId);
        }
    }

    /**
     * 初始化新会话的步骤状态
     * @param sessionId 会话ID
     * @return 初始化是否成功
     */
    public boolean initializeSessionStep(String sessionId) {
        return setSessionStep(sessionId, SessionStep.CREATIVE_CLARIFY);
    }

    /**
     * 检查会话是否已完成全流程
     * @param sessionId 会话ID
     * @return 是否已完成
     */
    public boolean isSessionFinished(String sessionId) {
        SessionStep currentStep = getSessionStep(sessionId);
        return currentStep.isFinished();
    }

    /**
     * 清除会话的步骤状态
     * @param sessionId 会话ID
     * @return 清除是否成功
     */
    public boolean clearSessionStep(String sessionId) {
        try {
            String key = SESSION_STEP_KEY_PREFIX + sessionId;
            redisTemplate.delete(key);
            log.info("清除会话{}的步骤状态成功", sessionId);
            return true;
        } catch (Exception e) {
            log.error("清除会话步骤状态失败，sessionId：{}", sessionId, e);
            return false;
        }
    }

    /**
     * 获取会话步骤状态的所有信息
     * @param sessionId 会话ID
     * @return 步骤状态信息
     */
    public SessionStepInfo getSessionStepInfo(String sessionId) {
        SessionStep currentStep = getSessionStep(sessionId);
        SessionStep nextStep = currentStep.getNextStep();
        
        return SessionStepInfo.builder()
                .sessionId(sessionId)
                .currentStep(currentStep)
                .nextStep(nextStep)
                .isFinished(currentStep.isFinished())
                .canProceed(currentStep.canProceed())
                .build();
    }

    /**
     * 会话步骤状态信息
     */
    @lombok.Data
    @lombok.Builder
    public static class SessionStepInfo {
        private String sessionId;
        private SessionStep currentStep;
        private SessionStep nextStep;
        private Boolean isFinished;
        private Boolean canProceed;
    }
}
