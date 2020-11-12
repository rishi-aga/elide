/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.contrib.dynamicconfighelpers;

import static com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHelper.NEWLINE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dynamic Model Schema validation.
 */
@Slf4j
public class DynamicConfigSchemaValidator {

    private static final JsonSchemaFactory FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
    private JsonSchema tableSchema;
    private JsonSchema securitySchema;
    private JsonSchema variableSchema;
    private JsonSchema dbConfigSchema;

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
     * @throws JsonProcessingException
     */
    public boolean verifySchema(Config configType, String jsonConfig, String fileName) throws JsonProcessingException {
        Set<ValidationMessage> results = null;

        switch (configType) {
        case TABLE :
            results = this.tableSchema.validate(new ObjectMapper().readTree(jsonConfig));
            break;
        case SECURITY :
            results = this.securitySchema.validate(new ObjectMapper().readTree(jsonConfig));
            break;
        case MODELVARIABLE :
        case DBVARIABLE :
            results = this.variableSchema.validate(new ObjectMapper().readTree(jsonConfig));
            break;
        case SQLDBConfig :
            results = this.dbConfigSchema.validate(new ObjectMapper().readTree(jsonConfig));
            break;
        default :
            log.error("Not a valid config type :" + configType);
            break;
        }

        if (!results.isEmpty()) {
            throw new IllegalStateException("Schema validation failed for: " + fileName + getErrorMessages(results));
        }

        return results.isEmpty();
    }

    private static String getErrorMessages(Set<ValidationMessage> report) {
        List<String> list = new ArrayList<String>();
        report.forEach(msg -> list.add(msg.getMessage()));

        return NEWLINE + String.join(NEWLINE, list);
    }

    private JsonSchema loadSchema(String resource) {
        return FACTORY.getSchema(DynamicConfigHelpers.class.getResourceAsStream(resource));
    }
}
