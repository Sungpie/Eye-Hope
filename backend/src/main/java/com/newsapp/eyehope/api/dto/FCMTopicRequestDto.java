package com.newsapp.eyehope.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMTopicRequestDto {
    
    // List of device tokens
    private List<String> tokens;
    
    // Topic name
    private String topic;
}