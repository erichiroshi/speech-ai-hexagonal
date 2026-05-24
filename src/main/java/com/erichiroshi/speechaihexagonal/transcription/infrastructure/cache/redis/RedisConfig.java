package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
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
    RedisTemplate<String, Transcription> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper objectMapper) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<Transcription> valueSerializer = new JacksonJsonRedisSerializer<>(objectMapper, Transcription.class);

        RedisTemplate<String, Transcription> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }
}