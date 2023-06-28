package kr.co.talk.domain.questionnotice.service;

import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeManagementRedisDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto.Topic;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionNoticeService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRepository chatroomUsersRepository;
    private final RedisService redisService;
    private final UserClient userClient;
    private final QuestionRepository questionRepository;
    private final KeywordService keywordService;

    @Transactional(readOnly = true)
    public QuestionNoticeResponseDto getQuestionNotice(long roomId, int questionNumber, long senderId) {
        // redis에서 현재 진행상황 있는지 조회
        QuestionNoticeManagementRedisDto dto = redisService.getCurrentQuestionNoticeDto(getQuestionKey(roomId));

        // 없으면 새로 만들기
        if (dto == null) {
            Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(roomId);
            var chatroomUsersList = chatroomUsersRepository.findChatroomUsersByChatroom(chatroom);
            if (chatroomUsersList.isEmpty()) {
                throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
            }
            var userList = userClient.requiredEnterInfo(0, chatroomUsersList.stream()
                    .map(ChatroomUsers::getUserId)
                    .collect(Collectors.toList()));

            List<QuestionNoticeManagementRedisDto.QuestionUserMap> tempUserMaps = new ArrayList<>();

            userList.forEach(enterInfo -> {
                List<Long> questionCodes = redisService.findQuestionCode(roomId, enterInfo.getUserId());
                questionCodes.forEach(code -> tempUserMaps.add(QuestionNoticeManagementRedisDto.QuestionUserMap.builder()
                        .userId(enterInfo.getUserId())
                        .questionCode(code)
                        .build()));
            });

            List<QuestionNoticeManagementRedisDto.QuestionUserMap> finalUserMaps = new ArrayList<>();

            // 랜덤하게 섞는 로직
            do {
                int randomIndex = RandomUtils.nextInt(tempUserMaps.size());
                var randomPickedObject = tempUserMaps.get(randomIndex);
                // 랜덤하게 뽑은 질문의 유저가 방금 추가한 유저와 같을 때는 skip. 다시.
                if (!finalUserMaps.isEmpty()
                        && finalUserMaps.get(finalUserMaps.size() - 1).getUserId() == randomPickedObject.getUserId()) {
                    continue;
                }
                finalUserMaps.add(randomPickedObject);
                tempUserMaps.remove(randomPickedObject);
            } while (!CollectionUtils.isEmpty(tempUserMaps));

            dto = QuestionNoticeManagementRedisDto.builder()
                    .userList(userList)
                    .questionList(finalUserMaps)
                    .build();
            saveCurrentQuestionStatus(roomId, dto);
        }

        int questionIndex = questionNumber - 1;

        var currentUserId = dto.getQuestionList().get(questionIndex).getUserId();
        var speaker = dto.getUserList().stream()
                .filter(enterInfo -> enterInfo.getUserId() == currentUserId)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Redis data validation error.\n" +
                            "Current User Id: {}\n", currentUserId);
                    return new CustomException(CustomError.SERVER_ERROR);
                });

        Question currentQuestion = questionRepository.findById(dto.getQuestionList().get(questionIndex).getQuestionCode())
                .orElseThrow(CustomException::new);

        return QuestionNoticeResponseDto.builder()
                .speaker(speaker)
                .userList(dto.getUserList())
                .topic(Topic.builder()
                        .keyword(currentQuestion.getKeyword().getName())
                        .questionCode(currentQuestion.getQuestionId())
                        .questionName(currentQuestion.getContent())
                        .depth(keywordService.convertIdIntoDepth(currentQuestion.getQuestionId()))
                        .questionGuide(currentQuestion.getGuideList())
                        .build())
                .metadata(QuestionNoticeResponseDto.QuestionNoticeMetadata.builder()
                        .senderId(senderId)
                        .questionNumber(questionNumber)
                        .finalQuestionNumber(dto.getQuestionList().size())
                        .build())
                .build();
    }

    private String getQuestionKey(long roomId) {
        return String.format("%s_%s", roomId, RedisConstants.QUESTION_NOTICE);
    }

    private void saveCurrentQuestionStatus(long roomId, QuestionNoticeManagementRedisDto dto) {
        redisService.saveObject(getQuestionKey(roomId), dto, Duration.ofDays(1));
    }
}
