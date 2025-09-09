package com.newsapp.eyehope.api.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for HTML escaping to prevent XSS attacks
 */
@Component
public class HtmlUtils {

    /**
     * Escapes HTML special characters in a string to prevent XSS attacks
     * 
     * @param input the input string to escape
     * @return the escaped string
     */
    public String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }
    
    /**
     * Checks if a string contains potentially malicious content
     * 
     * @param input the input string to check
     * @return true if the string contains potentially malicious content
     */
    public boolean containsMaliciousContent(String input) {
        if (input == null) {
            return false;
        }
        
        // Check for common XSS patterns
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("<script") ||
               lowerInput.contains("javascript:") ||
               lowerInput.contains("onerror=") ||
               lowerInput.contains("onload=") ||
               lowerInput.contains("eval(") ||
               lowerInput.contains("document.cookie") ||
               lowerInput.matches(".*\\bon\\w+\\s*=.*");
    }
}