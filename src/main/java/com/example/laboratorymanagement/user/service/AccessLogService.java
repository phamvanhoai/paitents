package com.example.laboratorymanagement.user.service;

import com.example.laboratorymanagement.common.entity.AccessLog;
import com.example.laboratorymanagement.user.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public void logActive(String username, String action, String target, String detail) {
        AccessLog log = AccessLog.builder()
                .username(username != null ? username : "system")
                .action(action)
                .target(target)
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .build();
        accessLogRepository.save(log);
    }
}
