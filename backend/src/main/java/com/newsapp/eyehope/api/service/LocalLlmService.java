package com.newsapp.eyehope.api.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class LocalLlmService {

    private static final String LOCAL_LLM_URL = "http://127.0.0.1:8888/v1/chat/completions";
    private static final int TIMEOUT_MS = 120_000; // 2분

    private final RestTemplate restTemplate;

    public LocalLlmService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Local LLM(llama-server)을 사용하여 콘텐츠 생성
     * @param prompt 프롬프트
     * @return 생성된 콘텐츠
     */
    public String generateContent(String prompt) {
        try {
            // OpenAI 호환 API 요청 형식 구성
            JSONObject requestBody = new JSONObject();

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            String response = restTemplate.postForObject(LOCAL_LLM_URL, entity, String.class);

            if (response == null) {
                return "Error: Local LLM으로부터 응답을 받지 못했습니다.";
            }

            return extractTextFromResponse(response);
        } catch (RestClientException e) {
            log.error("Local LLM 호출 중 오류 발생: {}", e.getMessage(), e);
            return "Error generating content: " + e.getMessage();
        } catch (Exception e) {
            log.error("Local LLM 호출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return "Error generating content: " + e.getMessage();
        }
    }

    /**
     * OpenAI 호환 API 응답에서 텍스트 추출
     * @param response JSON 응답
     * @return 추출된 텍스트
     */
    private String extractTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");

            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content");
            }

            return "No response generated";
        } catch (Exception e) {
            log.error("Local LLM 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return "Error parsing response: " + e.getMessage();
        }
    }
}
