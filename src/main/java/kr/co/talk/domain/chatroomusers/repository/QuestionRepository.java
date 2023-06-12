package kr.co.talk.domain.chatroomusers.repository;

import kr.co.talk.domain.chatroomusers.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Question findByQuestionIdAndKeyword_KeywordId(Long questionId, Long keywordId);
    List<Question> findByKeyword_KeywordId(Long keywordId);
}