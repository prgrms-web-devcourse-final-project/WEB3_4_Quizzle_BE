package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.generation.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizResponseDTO;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.response.RsData;

@RestController
@RequestMapping("/api/v1/quiz")
@Profile("gpt")
public class QuizGenerateController {

	@Autowired
	private GPTQuizService gptQuizService;

	@Autowired
	private RedisQuizAnswerService redisQuizAnswerService;

	@PostMapping("/generate")
	public RsData<QuizResponseDTO> generateQuiz(@RequestBody QuizDTO quizDTO) {
		try {
			QuizGenerationResponseDTO result = gptQuizService.generateQuiz(quizDTO);
            QuizResponseDTO response = new QuizResponseDTO(quizId, result.quizText(), result.answerMap());

			// 모든 필드를 생성자에 전달하여 record 인스턴스 생성
			QuizResponseDTO response = new QuizResponseDTO(quizId, result.quizText(), result.answerMap());

			return RsData.success(HttpStatus.OK, response);
		} catch (Exception e) {
			ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
			return null; // unreachable
		}
	}

}
