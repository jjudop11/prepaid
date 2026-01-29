package com.prepaid.config;

import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 분산 추적 (Distributed Tracing) 설정
 * 
 * Micrometer Tracing + Brave를 사용하여 요청 흐름 추적
 */
@Configuration
public class TracingConfig {
    
    /**
     * 추적 샘플링 비율 설정
     * 
     * - 개발/테스트 환경: 100% 샘플링 (모든 요청 추적)
     * - 프로덕션 환경: 10-20% 샘플링 (성능 고려)
     */
    @Bean
    public Sampler defaultSampler() {
        // ALWAYS_SAMPLE: 모든 요청 추적 (개발 환경)
        // 프로덕션에서는 Sampler.create(0.1) 등으로 조정
        return Sampler.ALWAYS_SAMPLE;
    }
}
