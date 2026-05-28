package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisAnalysisConfig {

    @Bean
    public RedisTemplate<String, Summary> summaryRedisTemplate(RedisConnectionFactory factory,
                                                               ObjectMapper objectMapper) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<Summary> serializer =
                new JacksonJsonRedisSerializer<>(objectMapper, Summary.class);

        RedisTemplate<String, Summary> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(serializer);
        return template;
    }
}
