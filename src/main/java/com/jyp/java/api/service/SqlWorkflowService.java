package com.jyp.java.api.service;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SqlWorkflowService {

    //    private static final Logger log = LoggerFactory.getLogger(SqlWorkflowService.class);
    private final SqlGeneratorService aiService;
    private final JdbcTemplate jdbcTemplate;

    public SqlWorkflowService(SqlGeneratorService aiService, JdbcTemplate jdbcTemplate) {
        this.aiService = aiService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQueryWithRetry(String userQuestion) {
        // 1. 生成一个唯一的会话 ID，代表"这一次查询任务"
        // 这样可以确保多次重试是在同一个上下文中，AI 能记住之前的报错
        String executionId = UUID.randomUUID().toString();

        String currentInput = userQuestion;
        int maxRetries = 3; // 最多重试3次

        for (int i = 0; i < maxRetries; i++) {
            System.out.println("----- 第 " + (i + 1) + " 次尝试 -----");

            // 2. 调用 AI 生成 SQL (传入 memory，AI 会自动读取历史并追加新回答)
            String sql = aiService.generateSql(executionId, currentInput);
            System.out.println("AI 生成 SQL: " + sql);

            // 清理一下可能的 markdown 符号（以防万一）
            sql = sql.replace("```sql", "").replace("```", "").trim();

            // ========== 【搞破坏代码开始】 ==========
            // 如果是第一次尝试 (i=0)，我们故意在 SQL 后面加乱码，模拟 AI 生成了错误语法
            if (i == 0) {
                System.out.println(">>> 😈 测试模式：故意破坏第一条 SQL，触发重试机制...");
                sql = sql + " INVALID_SYNTAX_HERE";
            }
            // ========== 【搞破坏代码结束】 ==========

            try {
                // 3. 尝试执行 SQL
                if (!sql.toLowerCase().startsWith("select")) {
                    throw new RuntimeException("安全拦截: 仅允许 SELECT 查询");
                }

                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                System.out.println("SQL 执行成功!");
                return result; // 成功则直接返回

            } catch (Exception e) {
                // 4. 捕获异常 (Self-Healing 核心)
                String errorMsg = e.getMessage();
                System.out.println("SQL 执行失败: " + errorMsg);

                // 5. 构造“反馈信息”给 AI
                // 下一次循环时，AI 会看到：自己的SQL + 系统的报错
                // 它的任务就是根据这个报错去修正 SQL
                currentInput = "上一步生成的 SQL 执行报错了：\n" + errorMsg + "\n请修正 SQL 并重新输出。";
            }
        }

        throw new RuntimeException("AI 经过 " + maxRetries + " 次重试仍未能生成正确的 SQL。");
    }
}
