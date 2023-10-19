package com.example.chatgptassistantapi.controller;


import com.example.chatgptassistantapi.model.ChatgptRequest;
import com.example.chatgptassistantapi.model.ChatgptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;



@RestController
@RequestMapping("/api/v1")
public class ChatgptController {

    @Value("${chatgpt.model}")
    private String model;

    @Value("${chatgpt.api.url}")
    private String apiUrl;


    @Value("${chatgpt.api.key}")
    private String apiKey;


    private static RestTemplate restTemplate = new RestTemplate();


    @RequestMapping(value = "/ask" , method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String ask(@RequestParam String query) {
        ChatgptRequest request = new ChatgptRequest(model, query);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);

        int maxRetries = 3; // Define your maximum number of retries.
        int retryDelayMillis = 1000; // Define the initial delay in milliseconds.

        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                ChatgptResponse chatgptResponse = restTemplate.postForObject(
                        apiUrl,
                        new HttpEntity<>(request, headers),
                        ChatgptResponse.class
                );
                return chatgptResponse.getChoices().get(0).getMessage().getContent();
            } catch (HttpClientErrorException.TooManyRequests e) {
                // 429 Too Many Requests received. Implement backoff and retry.
                if (retryCount < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    retryDelayMillis *= 2; // Exponential backoff for each retry.
                } else {
                    // Maximum retries reached, handle this situation accordingly.
                    // You can log an error or return an appropriate response to the client.
                    return "Error: Too many requests. Please try again later.";
                }
            }
        }

        // If the loop finishes without a successful response, handle this situation accordingly.
        return "Error: Unable to get a response from the service.";
    }

}
