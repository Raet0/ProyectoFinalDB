package com.example.potcast_back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestRunner {
    // Inyectamos la conexión a Redis que configuraste en el application.yml
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/")
    public String prueba() {
        try {
            // 1. INTENTO DE ESCRITURA: Guardamos un dato simple
            redisTemplate.opsForValue().set("test:bruno", "Conexión Exitosa desde Bruno");
            
            // 2. INTENTO DE LECTURA: Leemos el dato para confirmar
            String valorRecuperado = redisTemplate.opsForValue().get("test:bruno");
            
            // Si llegamos aquí sin errores, Redis está vivo.
            return "✅ ESTADO: OPERATIVO. Redis respondió: " + valorRecuperado;
            
        } catch (Exception e) {
            // Si algo falla (contraseña mal, docker apagado), caerá aquí.
            return "❌ ESTADO: CRÍTICO. Falló la conexión a Redis. Error: " + e.getMessage();
        }
    }
}
