//package com.ll.quizzle.domain.quiz.service;
//
//import com.ll.quizzle.domain.quiz.client.OpenAIClient;
//import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
//import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
//import com.ll.quizzle.domain.quiz.parser.QuizResponseParser;
//import com.ll.quizzle.domain.quiz.util.QuizPromptBuilder;
//import com.ll.quizzle.global.config.OpenAIProperties;
//import org.springframework.stereotype.Service;
//
//@Service
//public class GPTQuizService {
//
//    private final OpenAIClient openAIClient;
//    private final QuizResponseParser quizResponseParser;
//
//    public GPTQuizService(OpenAIProperties openAIProperties) {
//        this.openAIClient = new OpenAIClient(openAIProperties);
//        this.quizResponseParser = new QuizResponseParser();
//    }
//
//    public QuizGenerationResponse generateQuiz(QuizGenerationRequest request) {
//        String systemPrompt = QuizPromptBuilder.buildPrompt(request);
//        String responseBody = openAIClient.sendRequest(systemPrompt, "퀴즈 생성");
//        return quizResponseParser.parse(responseBody);
//    }
//}


package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.domain.quiz.client.OpenAIClient;
import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
import com.ll.quizzle.domain.quiz.parser.QuizResponseParser;
import com.ll.quizzle.domain.quiz.util.QuizPromptBuilder;
import com.ll.quizzle.global.config.OpenAIProperties;
import org.springframework.stereotype.Service;

@Service
public class GPTQuizService {

    private final OpenAIClient openAIClient;
    private final QuizResponseParser quizResponseParser;

    public GPTQuizService(OpenAIProperties openAIProperties) {
        this.openAIClient = new OpenAIClient(openAIProperties);
        this.quizResponseParser = new QuizResponseParser();
    }

    public QuizGenerationResponse generateQuiz(QuizGenerationRequest request) {
        String systemPrompt = QuizPromptBuilder.buildPrompt(request);
        String responseBody = openAIClient.sendRequest(systemPrompt, "퀴즈 생성");
        return quizResponseParser.parse(responseBody);
    }
}
