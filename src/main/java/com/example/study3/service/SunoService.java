package com.example.study3.service;

import com.example.study3.dto.SunoRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service

public class SunoService {
    @Value("${suno.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 음악 생성 요청 처리 매서드: SunoRequest 객체를 받아 API 요청을 보내고 taksId 반환
    public String generateMusic(SunoRequest request) {

        String taskId = UUID.randomUUID().toString(); // 고유한 taskId 생성
        String callbackUrl = "https://museek-backend-976640207402.asia-northeast3.run.app/api/suno/callback?taskId=" + taskId;  // 콜백 URL에 taskId를 포함하여 생성

        try{
            String postUrl = "https://apibox.erweima.ai/api/v1/generate";

            // HTTP 헤더 설정: JSON 타입 + Bearer 인증
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", request.getPrompt());
            body.put("title", request.getTitle());
            body.put("style", request.getStyle());
            body.put("customMode", true);
            body.put("instrumental", false);
            body.put("model", "V4_5");
            body.put("negativeTags", request.getNegativeTags());
            body.put("callBackUrl", callbackUrl);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(postUrl, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            return taskId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
