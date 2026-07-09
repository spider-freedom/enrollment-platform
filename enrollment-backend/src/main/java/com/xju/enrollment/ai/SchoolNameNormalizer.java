package com.xju.enrollment.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SchoolNameNormalizer {

    private final ChatLanguageModel chatLanguageModel;

    /**
     * Known school names mapping: common variant -> official full name.
     */
    private static final Map<String, String> KNOWN_SCHOOLS = new LinkedHashMap<>();

    static {
        KNOWN_SCHOOLS.put("新疆大学", "新疆大学");
        KNOWN_SCHOOLS.put("新大", "新疆大学");
        KNOWN_SCHOOLS.put("xjdx", "新疆大学");
        KNOWN_SCHOOLS.put("xinjiang university", "新疆大学");
        KNOWN_SCHOOLS.put("石河子大学", "石河子大学");
        KNOWN_SCHOOLS.put("石大", "石河子大学");
        KNOWN_SCHOOLS.put("新疆医科大学", "新疆医科大学");
        KNOWN_SCHOOLS.put("新医大", "新疆医科大学");
        KNOWN_SCHOOLS.put("新疆师范大学", "新疆师范大学");
        KNOWN_SCHOOLS.put("新师大", "新疆师范大学");
        KNOWN_SCHOOLS.put("新疆农业大学", "新疆农业大学");
        KNOWN_SCHOOLS.put("新农大", "新疆农业大学");
        KNOWN_SCHOOLS.put("新疆财经大学", "新疆财经大学");
        KNOWN_SCHOOLS.put("新财大", "新疆财经大学");
        KNOWN_SCHOOLS.put("新疆艺术学院", "新疆艺术学院");
        KNOWN_SCHOOLS.put("新疆工程学院", "新疆工程学院");
        KNOWN_SCHOOLS.put("清华大学", "清华大学");
        KNOWN_SCHOOLS.put("清华", "清华大学");
        KNOWN_SCHOOLS.put("北京大学", "北京大学");
        KNOWN_SCHOOLS.put("北大", "北京大学");
        KNOWN_SCHOOLS.put("复旦大学", "复旦大学");
        KNOWN_SCHOOLS.put("复旦", "复旦大学");
        KNOWN_SCHOOLS.put("上海交通大学", "上海交通大学");
        KNOWN_SCHOOLS.put("上海交大", "上海交通大学");
        KNOWN_SCHOOLS.put("浙江大学", "浙江大学");
        KNOWN_SCHOOLS.put("浙大", "浙江大学");
        KNOWN_SCHOOLS.put("南京大学", "南京大学");
        KNOWN_SCHOOLS.put("南大", "南京大学");
        KNOWN_SCHOOLS.put("西安交通大学", "西安交通大学");
        KNOWN_SCHOOLS.put("西安交大", "西安交通大学");
        KNOWN_SCHOOLS.put("哈尔滨工业大学", "哈尔滨工业大学");
        KNOWN_SCHOOLS.put("哈工大", "哈尔滨工业大学");
        KNOWN_SCHOOLS.put("武汉大学", "武汉大学");
        KNOWN_SCHOOLS.put("武大", "武汉大学");
        KNOWN_SCHOOLS.put("华中科技大学", "华中科技大学");
        KNOWN_SCHOOLS.put("华科", "华中科技大学");
        KNOWN_SCHOOLS.put("中山大学", "中山大学");
        KNOWN_SCHOOLS.put("中大", "中山大学");
    }

    public SchoolNameNormalizer(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    /**
     * Normalize a school name to its official full name.
     * First checks known mappings, then tries fuzzy matching, then falls back to LLM.
     */
    public String normalize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String trimmed = input.trim();

        // 1. Exact match in known schools
        for (Map.Entry<String, String> entry : KNOWN_SCHOOLS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }

        // 2. Fuzzy match using edit distance
        String bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String officialName : new HashSet<>(KNOWN_SCHOOLS.values())) {
            int distance = levenshteinDistance(trimmed.toLowerCase(), officialName.toLowerCase());
            if (distance < bestDistance && distance <= Math.max(3, trimmed.length() / 2)) {
                bestDistance = distance;
                bestMatch = officialName;
            }
        }

        if (bestMatch != null) {
            return bestMatch;
        }

        // 3. If no match found, ask LLM
        try {
            String prompt = "请将以下学校名称标准化为官方全称，只返回标准名称：{input}"
                    .replace("{input}", trimmed);
            String llmResult = chatLanguageModel.generate(prompt);
            if (llmResult != null && !llmResult.isBlank()) {
                return llmResult.trim();
            }
        } catch (Exception e) {
            // LLM call failed, return original input
        }

        return trimmed;
    }

    /**
     * Suggest school names matching the given keyword (fuzzy contains match).
     */
    public List<String> suggest(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(new LinkedHashSet<>(KNOWN_SCHOOLS.values()));
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        // Get unique official names
        Set<String> officialNames = new LinkedHashSet<>(KNOWN_SCHOOLS.values());

        return officialNames.stream()
                .filter(name -> name.toLowerCase().contains(lowerKeyword)
                        || lowerKeyword.contains(name.toLowerCase())
                        || levenshteinDistance(name.toLowerCase(), lowerKeyword) <= 2)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Compute Levenshtein (edit) distance between two strings.
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
