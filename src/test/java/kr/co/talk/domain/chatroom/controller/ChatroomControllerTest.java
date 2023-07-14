package kr.co.talk.domain.chatroom.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.chatroom.dto.ChatroomListDto;
import kr.co.talk.domain.chatroom.dto.FeedbackDto;
import kr.co.talk.domain.chatroom.dto.FeedbackDto.Feedback;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserNameResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserStatusDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.service.redis.RedisService;

@WebMvcTest(ChatroomController.class)
@ExtendWith(MockitoExtension.class)
public class ChatroomControllerTest {
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserClient userClient;

    @MockBean
    RedisService redisService;

    @MockBean
    ChatRoomService chatRoomService;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setup() {
        // mockMvc 설정
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("[API][GET] 대화방 목록 조회 api test")
    void findChatroomsTest() throws Exception {
        // given
        long userId = 1L;

        List<ChatroomListDto> list = new ArrayList<>();
        list.add(ChatroomListDto.builder()
                .roomId(1L)
                .roomName("test room1")
                .build());

        list.add(ChatroomListDto.builder()
                .roomId(2L)
                .roomName("test room2")
                .build());

        String writeValueAsString = objectMapper.writeValueAsString(list);

        // when
        when(chatRoomService.findChatRooms(userId)).thenReturn(list);

        // then
        mockMvc.perform(get("/chat/find-chatrooms").header("userId", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(writeValueAsString));
    }


    @Test
    @DisplayName("[API][GET] 닉네임 또는 이름으로 대화방 목록 조회 api test")
    void findChatRoomsWithNameTest() throws Exception {
        // given
        String searchName = "석홍";
        long userId = 1L;

        List<ChatroomListDto> list = new ArrayList<>();
        list.add(ChatroomListDto.builder()
                .roomId(1L)
                .roomName("test room1")
                .build());

        list.add(ChatroomListDto.builder()
                .roomId(2L)
                .roomName("test room2")
                .build());

        String writeValueAsString = objectMapper.writeValueAsString(list);

        // when
        when(chatRoomService.findChatRoomsByName(userId, searchName)).thenReturn(list);

        // then
        mockMvc.perform(get("/chat/find-chatrooms-with-name?searchName={searchName}", searchName)
                .header("userId", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(writeValueAsString));
    }

    @Test
    @DisplayName("[API][GET] 대화방 유저 조회 api test")
    void findUsersChatroomTest() throws Exception {
        // given
        long userId = 1L;
        String roomId = "1";

        List<UserNameResponseDto> list = new ArrayList<>();
        list.add(UserNameResponseDto.builder()
                .userId(1L)
                .name("석홍")
                .nickname("석홍_닉네임")
                .build());

        list.add(UserNameResponseDto.builder()
                .userId(1L)
                .name("용현")
                .nickname("용현_닉네임")
                .build());

        list.add(UserNameResponseDto.builder()
                .userId(1L)
                .name("아리")
                .nickname("아리_닉네임")
                .build());

        String writeValueAsString = objectMapper.writeValueAsString(list);

        // when
        when(chatRoomService.findUsersChatroom(userId, Long.valueOf(roomId))).thenReturn(list);

        // then
        mockMvc.perform(get("/chat/find-users/chatroom/{roomId}", roomId)
                .header("userId", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(writeValueAsString));
    }

    @Test
    @DisplayName("[API][POST] 대화방 생성 api test")
    void createChatroomTest() throws Exception {
        // given
        long userId = 1L;
        List<Long> userList = List.of(1L, 2L, 3L);
        String content = objectMapper.writeValueAsString(userList);

        // when

        // then
        mockMvc.perform(post("/chat/create-chatroom")
                .header("userId", userId)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][POST] 피드백 선택형 등록 api test")
    void feedbackCreateOptionalTest() throws Exception {
        // given
        long userId = 1L;
        long roomId = 1L;
        String sentence = "test sentence";

        FeedbackDto feedbackDto = new FeedbackDto();
        List<Feedback> feedbackList = new ArrayList<>();

        FeedbackDto.Feedback feedback1 = new Feedback();
        feedback1.setToUserId(2L);
        feedback1.setReview("2번님 감사합니다.");
        feedbackList.add(feedback1);

        FeedbackDto.Feedback feedback2 = new Feedback();
        feedback2.setToUserId(3L);
        feedback2.setReview("3번님 감사합니다.");
        feedbackList.add(feedback2);

        feedbackDto.setRoomId(roomId);
        feedbackDto.setScore(10);
        feedbackDto.setSentence(sentence);
        feedbackDto.setFeedback(feedbackList);

        String content = objectMapper.writeValueAsString(feedbackDto);

        // when

        // then
        mockMvc.perform(post("/chat/create/feedback/optional")
                .header("userId", userId)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][POST] 피드백 필수형 등록 api test")
    void feedbackCreateTest() throws Exception {
        // given
        long userId = 1L;
        long roomId = 1L;

        FeedbackDto feedbackDto = new FeedbackDto();
        feedbackDto.setRoomId(roomId);
        feedbackDto.setStatusEnergy(8);
        feedbackDto.setStatusRelation(5);
        feedbackDto.setStatusStable(7);
        feedbackDto.setStatusStress(10);

        String content = objectMapper.writeValueAsString(feedbackDto);

        // when

        // then
        mockMvc.perform(post("/chat/create/feedback")
                .header("userId", userId)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[API][GET] 피드백 필수형 조회 api test")
    void findFeedbackTest() throws Exception {
        // given
        long userId = 1L;

        UserStatusDto userStatusDto = UserStatusDto.builder()
                .isToday(false)
                .name("석홍")
                .nickname("석홍_닉네임")
                .statusEnergy(4)
                .statusRelation(5)
                .statusStable(3)
                .statusStress(7)
                .build();

        String writeValueAsString = objectMapper.writeValueAsString(userStatusDto);

        // when
        when(userClient.getUserStaus(userId)).thenReturn(userStatusDto);

        // then
        mockMvc.perform(get("/chat/find/feedback").header("userId", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(writeValueAsString));
    }
}
