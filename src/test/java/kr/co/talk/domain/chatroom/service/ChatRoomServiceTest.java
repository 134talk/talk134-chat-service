package kr.co.talk.domain.chatroom.service;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.FindChatroomResponseDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import static kr.co.talk.domain.chatroom.model.QChatroom.chatroom;
import static kr.co.talk.domain.chatroomusers.entity.QChatroomUsers.chatroomUsers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.global.client.UserClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles(profiles = "test")
@EmbeddedKafka(partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class ChatRoomServiceTest {
    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @MockBean
    UserClient userClient;


    @Test
    @DisplayName("[Service] 채팅방 잘 만들어지는지 확인")
    @Transactional
    public void createChatroom() {
        // given
        String teamCode = "teamCode_test";
        String chatroomName = "석홍, 용현, 아리";

        long createUserId = 1L;
        List<Long> userList = List.of(1L, 2L, 3L);

        FindChatroomResponseDto mockChatroomResponseDto = new FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode(teamCode);
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        CreateChatroomResponseDto mockCreateChatroomResponseDto = new CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode(teamCode);
        mockCreateChatroomResponseDto.setChatroomName(chatroomName);


        // when
        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());


        chatRoomService.createChatroom(createUserId, userList);

        Chatroom firstChatroom =
                jpaQueryFactory.selectFrom(chatroom).leftJoin(chatroom.chatroomUsers, chatroomUsers)
                        .fetchJoin().where(chatroom.teamCode.eq(teamCode)).fetchFirst();

        List<ChatroomUsers> findChatroomUsers = jpaQueryFactory.selectFrom(chatroomUsers)
                .where(chatroomUsers.chatroom.eq(firstChatroom)).fetch();

        // then
        assertNotNull(firstChatroom);
        assertEquals(findChatroomUsers.size(), userList.size());

    }

    @Test
    @Transactional
    @DisplayName("[Service] 대화방 목록 조회")
    public void findChatRoomsTest() {
        // given
        long userId = 2L;
        String teamCode = "teamCode_test";
        
        FindChatroomResponseDto findChatroomResponseDto = new FindChatroomResponseDto();
        
        // when
//        when(userClient.findChatroomInfo(userId)).thenReturn(null)
        

        // redis에 이모티콘 저장
//        RoomEmoticon roomEmoticon1 =
//                RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP1).fromUserId(user1)
//                        .toUserId(user2).roomId(1L).build();
//
//        RoomEmoticon roomEmoticon2 =
//                RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP1).fromUserId(user1)
//                        .toUserId(user3).roomId(1L).build();
//
//        RoomEmoticon roomEmoticon3 =
//                RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP2).fromUserId(user2)
//                        .toUserId(user3).roomId(1L).build();
//
//        RoomEmoticon roomEmoticon4 =
//                RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP2).fromUserId(user1)
//                        .toUserId(user1).roomId(3424L).build();
//
//        RoomEmoticon roomEmoticon5 =
//                RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP4).fromUserId(user2)
//                        .toUserId(user3).roomId(1L).build();
//
//        redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon1);
//        redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon2);
//        redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon3);
//        redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon4);
//        redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon5);

        // when
        // List<ChatroomListDto> findChatRooms = chatRoomService.findChatRooms(userId, teamCode);

        // log.info("findChatRooms::::::" + findChatRooms);
        // // then
        // assertEquals(findChatRooms.get(0).getEmoticons().size(), 3);
        // assertTrue(findChatRooms.get(0).isJoinFlag());
    }

}
