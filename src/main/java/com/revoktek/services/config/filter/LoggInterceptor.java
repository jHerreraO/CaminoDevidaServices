package com.revoktek.services.config.filter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;


@Component
@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggInterceptor extends OncePerRequestFilter {

    private static final int MAX_LOG_LENGTH = 100; // Adjust as needed
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getSanitizedRequestBody(String requestBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(requestBody);
        sanitizeJsonNode(jsonNode);
        return objectMapper.writeValueAsString(jsonNode);
    }

    private void sanitizeJsonNode(JsonNode jsonNode) {
        jsonNode.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            if (value.isTextual() && value.asText().length() > MAX_LOG_LENGTH) {
                ((ObjectNode) jsonNode).put(entry.getKey(), "[CONTENT OMITTED]");
            } else if (value.isObject()) {
                sanitizeJsonNode(value);
            }
        });
    }

    private String getHeadersInfo(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        // List of headers to log
        String[] headersToLog = {"host", "content-type", "content-length"};

        for (String header : headersToLog) {
            String headerValue = request.getHeader(header);
            if (headerValue != null) {
                headers.append(header).append(": ").append(headerValue).append(", ");
            }
        }
        // Remove the trailing comma and space
        if (!headers.isEmpty()) {
            headers.setLength(headers.length() - 2);
        }
        return headers.toString();
    }

    private String getResponseHeadersInfo(HttpServletResponse response) {
        StringBuilder headers = new StringBuilder();
        response.getHeaderNames().forEach(headerName -> {
            if (!headerName.startsWith("Vary")) {
                headers.append(headerName).append(": ").append(response.getHeader(headerName)).append(", ");
            }
        });
        // Remove the trailing comma and space
        if (!headers.isEmpty()) {
            headers.setLength(headers.length() - 2);
        }
        return headers.toString();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isMultipart(request)) {
            log.info("🟡 Es multipart file");

            // Use MultipartResolver to convert the request
            StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
            MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);

            StringBuilder multipartLog = new StringBuilder();

            // Log parameters
            multipartRequest.getParameterMap().forEach((key, values) -> {

                if(!key.equals("password")){
                    multipartLog.append(key).append(": ");
                    for (String value : values) {
                        multipartLog.append(value).append(", ");
                    }
                    multipartLog.setLength(multipartLog.length() - 2); // Remove the trailing comma and space
                    multipartLog.append("; ");
                }

            });

            // Log files
            multipartRequest.getFileMap().forEach((key, file) ->
                multipartLog.append("File [").append(key).append("]: ")
                        .append(file.getOriginalFilename()).append(", size: ")
                        .append(file.getSize()).append(" bytes; ")
            );

            log.info("Multipart Request Parameters and Files: {}", multipartLog.toString());

            // Continue with the filter chain
            filterChain.doFilter(multipartRequest, response);
            return;
        }

        CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
        long time = System.currentTimeMillis();
        try {
            filterChain.doFilter(cachedBodyHttpServletRequest, response);
        } finally {
            String headersInfo = getHeadersInfo(request);
            String headerResponse = getResponseHeadersInfo(response);
            time = System.currentTimeMillis() - time;
            log.info("Request Method: {} , Request URI: {} ,Request Headers: {} ", request.getMethod(), request.getRequestURI(), headersInfo);
            log.info("Response Status: {} , Response Headers: {} , Request Time: {}ms", response.getStatus(), headerResponse, time);
        }
    }

    private boolean isMultipart(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }
}