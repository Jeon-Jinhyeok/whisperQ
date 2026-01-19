package com.example.whisperQ;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.whisperQ.global.config.EmbeddedRedisConfig;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = WhisperQApplication.class)
@Import(EmbeddedRedisConfig.class)
public class RedisTest {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redisConnectionTest() {
        // Given
        String key = "test:connection";
        String value = "Hello Redis";

        // When
        redisTemplate.opsForValue().set(key, value);
        String fetchedValue = redisTemplate.opsForValue().get(key);

        // Then
        System.out.println("가져온 값: " + fetchedValue);
        assert value.equals(fetchedValue);
    }
}
