package com.oh.oh.controller;

import com.oh.oh.service.DeepSeekServiceWrapper;
import com.oh.oh.service.MarkdownToPrettyText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@Slf4j
public class DeepSeekController {

    @Autowired
    private DeepSeekServiceWrapper deepSeekServiceWrapper;

    @Autowired
    MarkdownToPrettyText markdownToPrettyText;

    @GetMapping("/askDeepSeek")
    public String askDeepSeek(@RequestParam String question) {

        log.info("AI模型调用询问的问题是:{}", question);

        // 返回响应内容
        try {
            String response= deepSeekServiceWrapper.generateText(question);
            log.info("返回deepseek结果");
            return markdownToPrettyText.renderPretty(extractAnswer(response));
        } catch (IOException e) {
            e.printStackTrace();
            return "Error askDeepSeek question";
        }
    }

    public String extractAnswer(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        return root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }


}

