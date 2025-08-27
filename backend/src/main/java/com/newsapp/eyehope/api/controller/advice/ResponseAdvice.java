package com.newsapp.eyehope.api.controller.advice;

import com.newsapp.eyehope.api.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ApiResponse로 이미 래핑된 응답은 처리하지 않음
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // Swagger UI, 에러 응답 등은 처리하지 않음
        if (request.getURI().getPath().contains("/swagger-ui/") ||
            request.getURI().getPath().contains("/v3/api-docs")) {
            return body;
        }

        // 이미 ApiResponse로 래핑된 경우 그대로 반환
        if (body instanceof ApiResponse) {
            return body;
        }

        // 나머지 응답은 ApiResponse로 래핑
        return ApiResponse.success(body);
    }
}
