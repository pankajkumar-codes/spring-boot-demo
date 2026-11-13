package com.learning.springbootdemo;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/** A number or boolean sent where a string is expected is a client error, not something to convert silently. */
@Configuration
public class JacksonStrictTypesConfig {

    @Bean
    JsonMapperBuilderCustomizer strictTextualCoercion() {
        return builder -> builder.withCoercionConfig(LogicalType.Textual, cfg -> {
            cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
            cfg.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
            cfg.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        });
    }
}
