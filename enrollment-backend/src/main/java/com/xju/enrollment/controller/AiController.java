package com.xju.enrollment.controller;

import com.xju.enrollment.ai.FeedbackAnalyzer;
import com.xju.enrollment.ai.SchoolNameNormalizer;
import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.entity.Policy;
import com.xju.enrollment.mapper.PolicyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.List;
import java.util.Map;
import java.io.IOException;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SchoolNameNormalizer schoolNameNormalizer;
    private final FeedbackAnalyzer feedbackAnalyzer;
    private final ChatLanguageModel chatLanguageModel;
    private final StreamingChatLanguageModel streamingModel;
    private final PolicyMapper policyMapper;

    @GetMapping("/policy/list")
    public ApiResponse<List<Policy>> policyList() {
        return ApiResponse.ok(policyMapper.selectList(
            new LambdaQueryWrapper<Policy>().orderByDesc(Policy::getCreateTime)));
    }

    @GetMapping("/school/suggest")
    public ApiResponse<List<String>> suggest(@RequestParam(defaultValue = "") String keyword) {
        List<String> suggestions = schoolNameNormalizer.suggest(keyword);
        return ApiResponse.ok(suggestions);
    }

    @PostMapping("/school/normalize")
    public ApiResponse<String> normalize(@RequestBody Map<String, String> body) {
        String input = body.get("name");
        String normalized = schoolNameNormalizer.normalize(input);
        return ApiResponse.ok(normalized);
    }

    @PostMapping("/feedback/analyze")
    public ApiResponse<String> analyze(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String result = feedbackAnalyzer.analyze(content);
        return ApiResponse.ok(result);
    }

    @PostMapping("/feedback/summarize")
    public ApiResponse<String> summarize(@RequestBody Map<String, List<String>> body) {
        List<String> contents = body.get("contents");
        String result = feedbackAnalyzer.summarize(contents);
        return ApiResponse.ok(result);
    }

    @PostMapping("/approval/suggest")
    public ApiResponse<Map<String, Object>> approvalSuggest(@RequestBody Map<String, Object> body) {
        String studentName = (String) body.get("studentName");
        String studentRole = (String) body.get("studentRole");
        String collegeName = (String) body.get("collegeName");
        Double gpa = body.get("gpa") != null ? ((Number) body.get("gpa")).doubleValue() : null;
        String targetSchool = (String) body.get("targetSchool");
        String activityTitle = (String) body.get("activityTitle");
        int currentCount = body.get("currentCount") != null ? ((Number) body.get("currentCount")).intValue() : 0;
        int maxCount = body.get("maxCount") != null ? ((Number) body.get("maxCount")).intValue() : 0;

        String prompt = String.format(
            "你是新疆大学招生宣传活动的审批助手。请分析以下报名申请并给出审批建议。\n" +
            "申请人: %s (%s)\n学院: %s\n绩点: %s\n目标学校: %s\n活动: %s\n当前已通过: %d/%d人\n\n" +
            "请以JSON格式返回，只返回JSON不要其他内容：\n" +
            "{\"suggestion\": \"建议通过\"或\"建议谨慎\"或\"建议拒绝\", \"reason\": \"理由\", \"risk\": \"风险点(无则写无)\"}",
            studentName, studentRole, collegeName, gpa != null ? String.format("%.1f", gpa) : "无",
            targetSchool, activityTitle, currentCount, maxCount
        );

        try {
            String response = chatLanguageModel.generate(prompt);
            // Strip markdown code blocks if present
            String json = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            Map<String, Object> result = new ObjectMapper().readValue(json, Map.class);
            return ApiResponse.ok("AI分析完成", result);
        } catch (Exception e) {
            return ApiResponse.ok("AI服务暂时不可用，请人工判断",
                Map.of("suggestion", "请人工判断", "reason", "AI服务暂不可用: " + e.getMessage(), "risk", "无"));
        }
    }

    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody Map<String,String> body) {
        String question = body.getOrDefault("question", "");
        if (question.isBlank()) return ApiResponse.fail("请输入问题");
        try {
            String prompt = "你是新疆大学招生AI助手，请用中文回答用户问题。用户问题：" + question;
            String answer = chatLanguageModel.generate(prompt);
            return ApiResponse.ok(answer);
        } catch (Exception e) {
            return ApiResponse.ok("抱歉，AI服务暂时不可用，请稍后重试。如需帮助，请拨打招生办电话 0991-8585671。");
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String,String> body) {
        String question = body.getOrDefault("question", "");
        SseEmitter emitter = new SseEmitter(120000L);
        if (question.isBlank()) {
            try { emitter.send(SseEmitter.event().data("请输入问题")); emitter.complete(); } catch (IOException ignored) {}
            return emitter;
        }
        String prompt = "你是新疆大学招生AI助手，请用中文简洁回答。用户问题：" + question;
        new Thread(() -> {
            try {
                String answer = chatLanguageModel.generate(prompt);
                // Stream characters with small delay for typing effect
                for (int i = 0; i < answer.length(); i += 3) {
                    int end = Math.min(i + 3, answer.length());
                    emitter.send(SseEmitter.event().data(answer.substring(i, end)));
                    Thread.sleep(20);
                }
                emitter.complete();
            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().data("抱歉，AI服务暂时不可用。")); emitter.complete(); } catch (IOException ignored) {}
            }
        }).start();
        return emitter;
    }
}
