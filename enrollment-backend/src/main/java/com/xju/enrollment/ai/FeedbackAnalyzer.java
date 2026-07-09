package com.xju.enrollment.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeedbackAnalyzer {

    private final ChatLanguageModel chatLanguageModel;

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
}
