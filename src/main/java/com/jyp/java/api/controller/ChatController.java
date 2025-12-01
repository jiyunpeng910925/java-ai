package com.jyp.java.api.controller;


import com.jyp.java.api.service.SqlGeneratorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sql")
public class ChatController {

    private final SqlGeneratorService sqlGeneratorService;
    private final JdbcTemplate jdbcTemplate;

    // 构造注入
    public ChatController(SqlGeneratorService sqlGeneratorService, JdbcTemplate jdbcTemplate) {
        this.sqlGeneratorService = sqlGeneratorService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ask")
    public Object askData(@RequestParam String question) {
        // 1. 调用 AI 生成 SQL
        String generatedSql = sqlGeneratorService.generateSql(question);

        System.out.println("用户问题: " + question);
        System.out.println("AI生成的SQL: " + generatedSql);

        if ("N/A".equals(generatedSql)) {
            return "抱歉，我无法根据现有数据回答这个问题。";
        }

        // 2. (可选) 安全检查：防止 AI 生成 DELETE/UPDATE 语句
        if (!generatedSql.trim().toLowerCase().startsWith("select")) {
            return "安全警告：生成的 SQL 不是查询语句，已拦截。";
        }

        try {
            // 3. 执行 SQL 并返回结果
            // queryForList 会返回 List<Map<String, Object>>，直接转 JSON 给前端
            List<Map<String, Object>> results = jdbcTemplate.queryForList(generatedSql);

            return Map.of(
                    "sql", generatedSql,
                    "data", results
            );
        } catch (Exception e) {
            return Map.of("error", "SQL执行失败: " + e.getMessage());
        }
    }
}
