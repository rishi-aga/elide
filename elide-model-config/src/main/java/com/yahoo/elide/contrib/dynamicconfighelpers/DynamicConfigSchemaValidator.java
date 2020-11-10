/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.contrib.dynamicconfighelpers;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.IOException;

import java.util.stream.Collectors;

/**
 * Dynamic Model Schema validation.
 */
@Slf4j
public class DynamicConfigSchemaValidator {
    private Schema tableSchema;
    private Schema securitySchema;
    private Schema variableSchema;
    private Schema dbConfigSchema;

    public DynamicConfigSchemaValidator() {
        tableSchema = loadSchema(Config.TABLE.getConfigSchema());
        securitySchema = loadSchema(Config.SECURITY.getConfigSchema());
        variableSchema = loadSchema(Config.MODELVARIABLE.getConfigSchema());
        dbConfigSchema = loadSchema(Config.SQLDBConfig.getConfigSchema());
    }
    /**
     *  Verify config against schema.
     * @param configType
     * @param jsonConfig
     * @param fileName
     * @return whether config is valid
     */
    public boolean verifySchema(Config configType, String jsonConfig, String fileName) {
        try {
            JSONObject parsedJson = new JSONObject(jsonConfig);
            switch (configType) {
                case TABLE:
                    tableSchema.validate(parsedJson);
                    break;
                case SECURITY:
                    securitySchema.validate(parsedJson);
                    break;
                case MODELVARIABLE:
                case DBVARIABLE:
                    variableSchema.validate(parsedJson);
                    break;
                case SQLDBConfig:
                    dbConfigSchema.validate(parsedJson);
                    break;
                default:
                    log.error("Not a valid config type :" + configType);
                    break;
            }
        } catch (ValidationException e) {
            throw new IllegalStateException("Schema validation failed: " + getErrorMessage(e));
        }
        return true;
    }

    private static String getErrorMessage(ValidationException exception) {
        StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append("[");

        errorBuilder.append(
                exception.getAllMessages().stream()
                .collect(Collectors.joining(", ")));

        errorBuilder.append("]");

        return errorBuilder.toString();
    }

    private Schema loadSchema(String resource) {
        try (InputStream inputStream = DynamicConfigHelpers.class.getResourceAsStream(resource)) {
            JSONObject parsedSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.load(parsedSchema);
        } catch (IOException e) {
            log.error("Error loading schema file " + resource + " to verify");
            throw new IllegalStateException(e.getMessage());
        }
    }
}
