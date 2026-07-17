package com.xju.enrollment.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FeedbackAnalyzer {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FeedbackAnalyzer(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    /**
     * Analyze a single feedback content for sentiment and keywords.
     * Returns a JSON string: {"sentiment": "...", "keywords": [...]}
     */
    public String analyze(String content) {
        if (content == null || content.isBlank()) {
            return "{\"sentiment\": \"中性\", \"keywords\": []}";
        }

        String prompt = """
                分析以下反馈的情感倾向(正面/中性/负面)并提取3个关键词。
                返回JSON格式: {"sentiment": "...", "keywords": ["...", "...", "..."]}
                只返回JSON，不要有任何其他内容。

                反馈内容: %s
                """.formatted(content);

        try {
            String result = chatLanguageModel.generate(prompt);
            if (result != null && !result.isBlank()) {
                return result.trim();
            }
        } catch (Exception e) {
            // LLM call failed, return default
        }

        return "{\"sentiment\": \"中性\", \"keywords\": []}";
    }

    /**
     * Summarize multiple feedback contents into a summary report.
     */
    public String summarize(List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return "暂无反馈内容可供总结。";
        }

        StringBuilder feedbackText = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            feedbackText.append(i + 1).append(". ").append(contents.get(i)).append("\n");
        }

        String prompt = """
                请对以下多条反馈进行总结分析，生成一份简洁的总结报告。
                报告应包括：
                1. 总体概况（反馈数量、整体情感倾向）
                2. 主要问题和建议（提取共性问题）
                3. 正面反馈亮点
                4. 改进建议

                反馈列表：
                %s
                """.formatted(feedbackText.toString());

        try {
            String result = chatLanguageModel.generate(prompt);
            if (result != null && !result.isBlank()) {
                return result.trim();
            }
        } catch (Exception e) {
            // LLM call failed, return simple summary
        }

        return "共收到 " + contents.size() + " 条反馈，AI分析暂时不可用，请稍后重试。";
    }

    /**
     * 批量分析多条反馈：一次调用返回整体情感倾向、关键词和总结报告。
     * 返回结构化 Map: {sentiment, keywords[], summary, count}
     */
    public Map<String, Object> analyzeAll(List<String> contents) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("sentiment", "中性");
        fallback.put("keywords", List.of());
        fallback.put("summary", "AI分析暂时不可用，请稍后重试。");
        fallback.put("count", contents == null ? 0 : contents.size());

        if (contents == null || contents.isEmpty()) {
            fallback.put("summary", "暂无反馈内容可供分析。");
            return fallback;
        }

        StringBuilder feedbackText = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            feedbackText.append(i + 1).append(". ").append(contents.get(i)).append("\n");
        }

        String prompt = """
                你是高校招生宣传平台的反馈分析助手。请分析以下 %d 条活动反馈，返回JSON：
                {
                  "sentiment": "整体情感倾向，取值：正面/中性/负面",
                  "keywords": ["提取5个以内最具代表性的关键词"],
                  "summary": "一段总结报告，依次包含：总体概况、主要共性问题、正面亮点、改进建议，用换行分段"
                }
                只返回JSON，不要包含任何其他内容或代码块标记。

                反馈列表：
                %s
                """.formatted(contents.size(), feedbackText.toString());

        try {
            String raw = chatLanguageModel.generate(prompt);
            Map<String, Object> parsed = parseJson(raw);
            if (parsed != null) {
                parsed.put("count", contents.size());
                parsed.putIfAbsent("sentiment", "中性");
                parsed.putIfAbsent("keywords", List.of());
                parsed.putIfAbsent("summary", "");
                // LLM 有时把换行输出为字面 \n，规范化为真实换行
                if (parsed.get("summary") instanceof String s) {
                    parsed.put("summary", s.replace("\\n", "\n"));
                }
                return parsed;
            }
        } catch (Exception e) {
            // LLM call failed, return fallback
        }
        return fallback;
    }

    /**
     * 解析 LLM 返回的 JSON（容忍 ```json 代码块包裹及前后多余文本）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String text = raw.trim();
        // 去掉代码块围栏
        text = text.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```", "").trim();
        // 截取第一个 { 到最后一个 } 之间的内容
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            return objectMapper.readValue(text.substring(start, end + 1), Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
