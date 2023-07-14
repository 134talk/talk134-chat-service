package kr.co.talk.domain.chatroom.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.co.talk.domain.chatroom.dto.FeedbackDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatroomController {

	private final UserClient userClient;
	private final ChatRoomService chatRoomService;
	private final RedisService redisService;

	/**
	 * 대화방 목록 조회 api
	 * 
	 * @param userId
	 * @param teamCode
	 * @return
	 */
	@GetMapping("/find-chatrooms")
	public ResponseEntity<?> findChatRooms(@RequestHeader(value = "userId") Long userId) {
		return ResponseEntity.ok(chatRoomService.findChatRooms(userId));
	}

	/**
	 * 닉네임 또는 이름으로 대화방 목록 조회 api
	 * 
	 * @param userId
	 * @param teamCode
	 * @param name
	 * @return
	 */
	@GetMapping("/find-chatrooms-with-name")
	public ResponseEntity<?> findChatRoomsWithName(@RequestHeader(value = "userId") Long userId, String searchName) {
		return ResponseEntity.ok(chatRoomService.findChatRoomsByName(userId, searchName));
	}

	/**
	 * 대화방 유저 조회 api
	 * 
	 * @param userId
	 * @param roomId
	 * @return
	 */
	@GetMapping("/find-users/chatroom/{roomId}")
	public ResponseEntity<?> findUsersChatroom(@RequestHeader(value = "userId") Long userId, @PathVariable(name = "roomId") String roomId){
        return ResponseEntity.ok(chatRoomService.findUsersChatroom(Long.valueOf(roomId), userId));
	}
	
	/**
	 * 대화방 생성 api
	 * 
	 * @param teamCode
	 * @param userList
	 * @return
	 */
	@PostMapping("/create-chatroom")
	public ResponseEntity<?> createChatroom(@RequestHeader(value = "userId") Long userId,
			@RequestBody List<Long> userList) {
		chatRoomService.createChatroom(userId, userList);
		return ResponseEntity.ok().build();
	}

	/**
	 * 피드백 선택형 등록 api
	 */
	@PostMapping("/create/feedback/optional")
	public ResponseEntity<?> feedbackCreateOptional(@RequestHeader(value = "userId") Long userId, @RequestBody FeedbackDto feedbackDto) {
		log.info("feedbackOptionalDto::::" + feedbackDto);
		chatRoomService.saveFeedbackOptionalToRedis(feedbackDto, userId);
		return ResponseEntity.ok().build();
	}

	/**
	 * 피드백 필수형 등록 api
	 */
	@PostMapping("/create/feedback")
	public ResponseEntity<?> feedbackCreate(@RequestHeader(value = "userId") Long userId,
			@RequestBody FeedbackDto feedbackDto) {
		log.info("feedbackOptionalDto::::" + feedbackDto);

		Map<String, Object> entry = redisService.getEntry(RedisConstants.FEEDBACK_ + feedbackDto.getRoomId(),
				FeedbackDto.class);

		chatRoomService.saveFeedbackToRedis(userId, feedbackDto, entry);
		return ResponseEntity.ok().build();
	}

	/**
	 * 피드백 필수형 조회
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/find/feedback")
	public ResponseEntity<?> findFeedback(@RequestHeader(value = "userId") Long userId) {
		return ResponseEntity.ok(userClient.getUserStaus(userId));
	}
}
