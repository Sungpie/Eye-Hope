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
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    @Autowired
    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }

    /**
     * Generate content using the Gemini API
     * @param prompt The prompt to send to the Gemini API
     * @return The generated content
     */
    public String generateContent(String prompt) {
        try {
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

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Make the request
            String response = restTemplate.postForObject(geminiConfig.getApiUrl(), entity, String.class);

            // Parse the response
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "Error generating content: " + e.getMessage();
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
