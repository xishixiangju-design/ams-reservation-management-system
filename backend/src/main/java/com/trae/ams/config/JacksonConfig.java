package com.trae.ams.config;

import com.trae.ams.config.json.XssStringJsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> {
            builder.deserializerByType(String.class, new XssStringJsonDeserializer());
        };
    }
}
