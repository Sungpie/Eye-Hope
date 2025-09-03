package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.config.GeminiConfig;
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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GeminiService {

    // Retry configuration
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 1.5;

    // Rate limiting - allow 5 concurrent requests
    private final Semaphore rateLimiter = new Semaphore(5);

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    @Autowired
    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }

    /**
     * Generate content using the Gemini API with retry and rate limiting
     * @param prompt The prompt to send to the Gemini API
     * @return The generated content
     */
    public String generateContent(String prompt) {
        boolean acquired = false;
        try {
            // Apply rate limiting - wait for a permit
            log.debug("Waiting for rate limiter permit...");
            acquired = rateLimiter.tryAcquire(30, TimeUnit.SECONDS); // Wait up to 30 seconds for a permit
            if (!acquired) {
                log.warn("Failed to acquire rate limiter permit after 30 seconds");
                return "Error: Rate limit exceeded, please try again later";
            }
            log.debug("Rate limiter permit acquired");

            // Create the request body
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

            // 추론 기능을 끄기 위한 thinkingConfig 설정
            // 나중에 킬거면 -1로 변경하면 됨
            JSONObject generationConfig = new JSONObject();
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("thinkingBudget", 0); // 추론 기능 끄기
            generationConfig.put("thinkingConfig", thinkingConfig);
            requestBody.put("generationConfig", generationConfig);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Implement retry with exponential backoff
            int retries = 0;
            long backoffTime = INITIAL_BACKOFF_MS;
            String response = null;

            while (retries <= MAX_RETRIES) {
                try {
                    // Make the request
                    response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, String.class);
                    // If successful, break out of the retry loop
                    break;
                } catch (HttpClientErrorException | HttpServerErrorException e) {
                    retries++;

                    // Check if it's the "model overloaded" error
                    if (e.getResponseBodyAsString().contains("The model is overloaded") || 
                        e.getResponseBodyAsString().contains("model overloaded")) {

                        if (retries <= MAX_RETRIES) {
                            log.warn("Gemini API overloaded, retrying in {} ms (attempt {}/{})", 
                                    backoffTime, retries, MAX_RETRIES);

                            try {
                                Thread.sleep(backoffTime);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                log.error("Retry interrupted", ie);
                                return "Error: Retry interrupted";
                            }

                            // Increase backoff time for next retry
                            backoffTime = (long) (backoffTime * BACKOFF_MULTIPLIER);
                        } else {
                            log.error("Gemini API still overloaded after {} retries", MAX_RETRIES);
                            return "Error: Gemini API is currently overloaded, please try again later";
                        }
                    } else {
                        // For other errors, don't retry
                        log.error("Gemini API error: {}", e.getMessage(), e);
                        return "Error generating content: " + e.getMessage();
                    }
                } catch (RestClientException e) {
                    log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
                    return "Error generating content: " + e.getMessage();
                }
            }

            if (response == null) {
                return "Error: Failed to get response from Gemini API after retries";
            }

            // Parse the response
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "Error generating content: " + e.getMessage();
        } finally {
            // Always release the permit if acquired
            if (acquired) {
                rateLimiter.release();
                log.debug("Rate limiter permit released");
            }
        }
    }

    /**
     * Extract the text from the Gemini API response
     * @param response The JSON response from the Gemini API
     * @return The extracted text
     */
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
