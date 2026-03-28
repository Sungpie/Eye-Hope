package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.config.GeminiConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GeminiService {

    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 1.5;

    private final Semaphore rateLimiter = new Semaphore(5);
    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    // Micrometer 메트릭
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter rateLimitCounter;
    private final Counter overloadCounter;
    private final Counter retryCounter;
    private final Timer responseTimer;

    @Autowired
    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig, MeterRegistry registry) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;

        // 성공/실패 카운터
        this.successCounter = Counter.builder("gemini.api.requests")
                .tag("status", "success")
                .description("Gemini API 성공 횟수")
                .register(registry);
        this.errorCounter = Counter.builder("gemini.api.requests")
                .tag("status", "error")
                .description("Gemini API 실패 횟수")
                .register(registry);

        // Rate limit 초과 카운터
        this.rateLimitCounter = Counter.builder("gemini.api.ratelimit")
                .description("Gemini API Rate limit 초과 횟수")
                .register(registry);

        // 모델 과부하 카운터
        this.overloadCounter = Counter.builder("gemini.api.overload")
                .description("Gemini API 모델 과부하 발생 횟수")
                .register(registry);

        // 재시도 카운터
        this.retryCounter = Counter.builder("gemini.api.retry")
                .description("Gemini API 재시도 횟수")
                .register(registry);

        // 응답시간 타이머
        this.responseTimer = Timer.builder("gemini.api.response.time")
                .description("Gemini API 응답시간")
                .register(registry);
    }

    public String generateContent(String prompt) {
        AtomicBoolean acquired = new AtomicBoolean(false);
        return responseTimer.record(() -> {
            try {
                log.debug("Waiting for rate limiter permit...");
                acquired.set(rateLimiter.tryAcquire(30, TimeUnit.SECONDS));
                if (!acquired.get()) {
                    log.warn("Failed to acquire rate limiter permit after 30 seconds");
                    rateLimitCounter.increment();
                    errorCounter.increment();
                    return "Error: Rate limit exceeded, please try again later";
                }
                log.debug("Rate limiter permit acquired");

                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                JSONObject generationConfig = new JSONObject();
                JSONObject thinkingConfig = new JSONObject();
                thinkingConfig.put("thinkingBudget", 0);
                generationConfig.put("thinkingConfig", thinkingConfig);
                requestBody.put("generationConfig", generationConfig);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

                int retries = 0;
                long backoffTime = INITIAL_BACKOFF_MS;
                String response = null;

                while (retries <= MAX_RETRIES) {
                    try {
                        response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, String.class);
                        break;
                    } catch (HttpClientErrorException | HttpServerErrorException e) {
                        retries++;
                        retryCounter.increment();

                        if (e.getResponseBodyAsString().contains("The model is overloaded") ||
                                e.getResponseBodyAsString().contains("model overloaded")) {
                            overloadCounter.increment();

                            if (retries <= MAX_RETRIES) {
                                log.warn("Gemini API overloaded, retrying in {} ms (attempt {}/{})",
                                        backoffTime, retries, MAX_RETRIES);
                                try {
                                    Thread.sleep(backoffTime);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    log.error("Retry interrupted", ie);
                                    errorCounter.increment();
                                    return "Error: Retry interrupted";
                                }
                                backoffTime = (long) (backoffTime * BACKOFF_MULTIPLIER);
                            } else {
                                log.error("Gemini API still overloaded after {} retries", MAX_RETRIES);
                                errorCounter.increment();
                                return "Error: Gemini API is currently overloaded, please try again later";
                            }
                        } else {
                            log.error("Gemini API error: {}", e.getMessage(), e);
                            errorCounter.increment();
                            return "Error generating content: " + e.getMessage();
                        }
                    } catch (RestClientException e) {
                        log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
                        errorCounter.increment();
                        return "Error generating content: " + e.getMessage();
                    }
                }

                if (response == null) {
                    errorCounter.increment();
                    return "Error: Failed to get response from Gemini API after retries";
                }

                successCounter.increment();
                return extractTextFromResponse(response);

            } catch (Exception e) {
                log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
                errorCounter.increment();
                return "Error generating content: " + e.getMessage();
            } finally {
                if (acquired.get()) {
                    rateLimiter.release();
                    log.debug("Rate limiter permit released");
                }
            }
        });
    }

    private String extractTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
            return "No response generated";
        } catch (Exception e) {
            log.error("Gemini API 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return "Error parsing response: " + e.getMessage();
        }
    }
}