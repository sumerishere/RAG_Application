package dev.sid.webpage_ai_rag.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    	
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use StringRedisSerializer for keys
        //template.setKeySerializer(new StringRedisSerializer());

        // Use GenericToStringSerializer for values
        template.setValueSerializer(new GenericToStringSerializer<>(String.class));

        // Optional: Configure hash key and value serializers
        //template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(String.class));

        // Initialize the template
        template.afterPropertiesSet();

        return template;
    }
}
