package kr.co.talk.domain.chatroom.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kr.co.talk.domain.chatroom.model.EmoticonCode;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대화 목록 조회 dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomListDto {
	private long roomId;
	private String roomName;
//	private int userCount;
	private List<Emoticons> emoticons = new ArrayList<>();
	@JsonIgnore
	private List<ChatroomUsers> chatroomUsers = new ArrayList<>();
	private boolean joinFlag;

	@Data
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class Emoticons {
		private String emoticon;
		private int emoticonCount;
	}
}
