package com.newsapp.eyehope.api.controller.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.util.HtmlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.Map;

/**
 * Response advice that applies HTML escaping to all string values in API responses
 * to prevent XSS attacks
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class XssResponseAdvice implements ResponseBodyAdvice<Object> {

    private final HtmlUtils htmlUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all responses
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return null;
        }

        // Swagger UI, API docs 등은 처리하지 않음
        if (request.getURI().getPath().contains("/swagger-ui/") ||
            request.getURI().getPath().contains("/v3/api-docs")) {
            return body;
        }

        // If it's an ApiResponse, process its data field
        if (body instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) body;
            Object data = apiResponse.getData();

            if (data != null) {
                // Process the data field recursively
                Object processedData = processValue(data);

                // Create a new ApiResponse with the processed data
                // We need to use reflection or ObjectMapper to set the data field
                // since ApiResponse doesn't have a setData method
                try {
                    String json = objectMapper.writeValueAsString(apiResponse);
                    ApiResponse<?> newResponse = objectMapper.readValue(json, ApiResponse.class);
                    // Use reflection to set the data field
                    java.lang.reflect.Field dataField = ApiResponse.class.getDeclaredField("data");
                    dataField.setAccessible(true);
                    dataField.set(newResponse, processedData);
                    dataField.setAccessible(false);
                    return newResponse;
                } catch (Exception e) {
                    log.error("Error processing ApiResponse for XSS prevention", e);
                    return body;
                }
            }
        }

        // For other types, process them directly
        return processValue(body);
    }

    /**
     * Recursively processes a value to escape HTML in strings
     */
    @SuppressWarnings("unchecked")
    private Object processValue(Object value) {
        if (value == null) {
            return null;
        }

        // If it's a string, escape HTML
        if (value instanceof String) {
            return htmlUtils.escapeHtml((String) value);
        }

        // If it's a collection, process each element
        if (value instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) value;
            // Create a new collection with processed values
            // This is safer than trying to modify the original collection
            return collection.stream()
                .map(this::processValue)
                .toList();
        }

        // If it's a map, process each value
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            // Create a new map with processed values
            return map.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> processValue(entry.getValue())
                ));
        }

        // For other objects, we can't easily process them
        // In a real application, you might want to use reflection or Jackson's ObjectMapper
        // to process all string fields of complex objects
        return value;
    }
}
