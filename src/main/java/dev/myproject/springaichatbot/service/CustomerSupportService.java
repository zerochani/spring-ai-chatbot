package dev.myproject.springaichatbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerSupportService {
    private final ChatClient chatClient;

    public CustomerSupportService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    //사용자별 대화 기록을 저장하는 맵
    private final Map<String, List<Message>> chatHistory = new ConcurrentHashMap<String, List<Message>>();

    //챗봇의 역할을 정의 하는 시스템 프롬프트
    private final String systemPrompt = """
            당신은 "Awesome Tech"라는 회사의 E-커머스 고객 지원 챗봇입니다.
            항상 친절하고 명확하게 답변해야 합니다. 사용자가 상품에 대해서 물으면 아래 정보를 기반으로 답해주세요.
            
            - 상품명: 갤럭시 AI 북
            - 가격: 1,500,000원
            - 특징: 최신 AI 기능이 탑재된 고성능 노트북, 가볍고 배터리가 오래간다.
            - 재고: 현재 구매 가능
            
            - 상품명: AI 스마트 워치
            - 가격: 350,000원
            - 특징: 건강 모니터링이 가능하고, 스마트폰과 연동 기능을 제공, 방수 기능을 포함
            - 재고: 품절(5일 후 재입고 예정)
            
            내부에 없는 정보일 경우, 정중히 안내하면서도 일반적인 정보가 유사한 내용을 최대한 제공해 주세요.
            """;


    public String getChatResponse(String userId, String userMessage) {
        List<Message> history = chatHistory.computeIfAbsent(userId, k -> new ArrayList<>());

        //시스템 메시지와 사용자 메시지를 포함한 전체 대화 생성
        List<Message> conversion = new ArrayList<Message>();
        conversion.add(new SystemPromptTemplate(systemPrompt).createMessage());
        conversion.addAll(history);
        conversion.add(new UserMessage(userMessage));

        //ChatClient를 사용하여 AI 모델 요청
        Prompt prompt = new Prompt(conversion);
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        //Null 체크
        if(response == null || response.getResult() == null || response.getResult().getOutput() == null){
            System.err.println("AI 응답이 유효하지 않습니다.");
            return "현재 AI 응답을 받을 수 없습니다. 잠시 후 다시 시도해 주세요";
        }

        //대화 기록에 대한 현재 질문과 답변 추가
        history.add(new UserMessage(userMessage));
        history.add(response.getResult().getOutput());

        //대화 기록이 너무 길어 지지 않도록 관리
        if(history.size() > 10){
            history.subList(0,history.size()-10).clear();
        }

        System.out.println(">>> 사용자 :" + userId + "의 대화기록");
        System.out.println("===================================");

        List<Message> messages = chatHistory.get(userId);
        for(int i=0; i<messages.size(); i+=2){
            Message question = messages.get(i);
            System.out.println("Q :" + question.getText());

            if(i+1 < messages.size()){
                Message answer = messages.get(i+1);
                System.out.println("A :" + answer.getText());
            }
            System.out.println("==============================");
        }
        return response.getResult().getOutput().getText();
    }
}
