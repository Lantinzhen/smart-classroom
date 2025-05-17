package com.oh.oh.service;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeepSeekServiceWrapper {

    private String apiKey = "sk-6e51871196344a99ad7b1420209feead";
    private String apiUrl = "https://api.deepseek.com/chat/completions";


    public String generateText(String question) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();

        // 创建 HTTP POST 请求
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + apiKey);

// 构造 JSON 请求体
        String requestBody = String.format(
                "{\"model\": \"deepseek-chat\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"stream\": false}",
                question
        );

// 设置 UTF-8 编码，防止中文变问号
        StringEntity entity = new StringEntity(requestBody, "UTF-8");
        request.setEntity(entity);


        // 发送请求并获取响应
        try (CloseableHttpResponse response = client.execute(request)) {
            // 返回响应内容
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating text";
        }
    }
}

