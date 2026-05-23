package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {

    @Bean
    RedisTemplate<String, TranscriptionOutput> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        var keySerializer   = new StringRedisSerializer();
        var valueSerializer = new JacksonJsonRedisSerializer<>(objectMapper, TranscriptionOutput.class);

        RedisTemplate<String, TranscriptionOutput> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }
}