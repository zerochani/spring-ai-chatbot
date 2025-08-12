package dev.myproject.springaichatbot.controller;

import dev.myproject.springaichatbot.dto.ChatRequestDto;
import dev.myproject.springaichatbot.dto.ChatResponseDto;
import dev.myproject.springaichatbot.service.CustomerSupportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final CustomerSupportService service;

    public ChatController(CustomerSupportService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(@RequestBody ChatRequestDto request) {
        System.out.println("요청된 request 값 :" + request);
        String responseMessage = service.getChatResponse(request.getUserId(),request.getMessage());
        return new ChatResponseDto(responseMessage);
    }
}
