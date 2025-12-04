package com.jyp.java.api.service;


import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSchemaExtractor {

    private final DataSource dataSource;

    public DatabaseSchemaExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取数据库的精简结构描述
     * 格式示例：
     * users (id INT, username VARCHAR, email VARCHAR);
     * orders (id INT, amount DECIMAL);
     */
    public String getConciseSchema() {
        StringBuilder schemaBuilder = new StringBuilder();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 1. 获取所有表信息 (参数: catalog, schemaPattern, tableNamePattern, types)
            // H2 数据库通常 schema 是 "PUBLIC"，或者传 null 获取所有
            try (ResultSet tables = metaData.getTables(null, "PUBLIC", null, new String[]{"TABLE"})) {

                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    schemaBuilder.append(tableName).append(" (");

                    // 2. 获取该表的所有列信息
                    List<String> columns = new ArrayList<>();
                    try (ResultSet cols = metaData.getColumns(null, "PUBLIC", tableName, null)) {
                        while (cols.next()) {
                            String columnName = cols.getString("COLUMN_NAME");
                            String typeName = cols.getString("TYPE_NAME"); // 如 VARCHAR, INT
                            columns.add(columnName + " " + typeName);
                        }
                    }

                    // 拼接列信息：id INT, name VARCHAR
                    schemaBuilder.append(String.join(", ", columns));
                    schemaBuilder.append(");\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("提取数据库元数据失败", e);
        }

        return schemaBuilder.toString();
    }
}
