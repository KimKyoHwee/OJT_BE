package com.kyohwee.ojt.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;


import static com.kyohwee.ojt.global.uri.RequestUri.USER_URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
@Slf4j
public class HealthController {
    @GetMapping("/health")
    public String healthCheck() {
        log.info("✅ /api/v1/health 호출됨 - 헬스체크 통과"); // ← 로그 출력
        return "OK";
    }
}
