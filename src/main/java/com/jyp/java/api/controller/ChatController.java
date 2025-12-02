package com.jyp.java.api.controller;


import com.jyp.java.api.service.SqlGeneratorService;
import com.jyp.java.api.service.SqlWorkflowService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sql")
public class ChatController {

    private final SqlWorkflowService sqlWorkflowService;

    public ChatController(SqlWorkflowService sqlWorkflowService) {
        this.sqlWorkflowService = sqlWorkflowService;
    }

    @GetMapping("/ask")
    public Object askData(@RequestParam String question) {
        try {
            List<Map<String, Object>> data = sqlWorkflowService.executeQueryWithRetry(question);
            return Map.of("success", true, "data", data);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
