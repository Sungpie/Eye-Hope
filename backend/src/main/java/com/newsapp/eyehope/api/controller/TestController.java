package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final GeminiService geminiService;

    @Autowired
    public TestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/test")
    public String test(){
        return "test_application";
    }

    @GetMapping("/gemini")
    public String testGemini(@RequestParam(defaultValue = "Tell me about artificial intelligence") String prompt) {
        return geminiService.generateContent(prompt);
    }
}
