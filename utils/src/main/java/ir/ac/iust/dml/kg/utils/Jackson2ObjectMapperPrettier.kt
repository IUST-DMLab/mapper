package ir.ac.iust.dml.kg.utils

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
open class Jackson2ObjectMapperPrettier {
    @Bean
    open fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        builder.indentOutput(true)
        return builder
    }
}