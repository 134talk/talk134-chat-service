package kr.co.talk.domain.chatroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class QuestionNoticeResponseDto {
    private RequestDto.ChatRoomEnterResponseDto speaker;
    private List<RequestDto.ChatRoomEnterResponseDto> userList;
    private String topic;

    @Builder
    @Data
    public static class Topic {
        private String keyword;

    }
}
