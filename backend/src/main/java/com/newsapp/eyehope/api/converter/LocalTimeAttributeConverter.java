package com.newsapp.eyehope.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;

/**
 * LocalTime을 데이터베이스와 애플리케이션 간에 일관되게 변환하기 위한 컨버터.
 * LocalTime은 시간대 정보가 없지만 JPA에서 time_zone 설정의 영향을 받을 수 있으므로
 * 이를 우회하여 일관된 시간 표현을 보장합니다.
 */
@Converter(autoApply = false)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalTime localTime) {
        return localTime != null ? localTime.toString() : null;
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        return dbData != null ? LocalTime.parse(dbData) : null;
    }
}