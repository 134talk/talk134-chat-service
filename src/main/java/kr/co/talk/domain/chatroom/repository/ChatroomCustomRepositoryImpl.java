package kr.co.talk.domain.chatroom.repository;

import java.util.List;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.talk.domain.chatroom.model.Chatroom;
import static kr.co.talk.domain.chatroomusers.entity.QChatroomUsers.chatroomUsers;
import static kr.co.talk.domain.chatroom.model.QChatroom.chatroom;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatroomCustomRepositoryImpl implements ChatroomCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Chatroom> findByTeamCode(String teamCode) {
        return jpaQueryFactory.selectFrom(chatroom)
                .distinct()
                .leftJoin(chatroom.chatroomUsers, chatroomUsers)
                .fetchJoin()
                .where(chatroom.teamCode.eq(teamCode))
                .fetch();
    }

    @Override
    public List<Chatroom> findByTeamCodeAndName(String teamCode, List<Long> userIds) {
        return jpaQueryFactory.selectFrom(chatroom)
                .where(chatroom.chatroomId
                        .in(JPAExpressions.select(chatroomUsers.chatroom.chatroomId)
                                .distinct()
                                .from(chatroomUsers)
                                .where(chatroomUsers.userId.in(userIds))))
                .fetch();
    }

    @Override
    public List<Long> findByUsersInChatroom(long roomId, long userId) {
        return jpaQueryFactory.select(chatroomUsers.userId)
                .from(chatroomUsers)
                .where(chatroomUsers.chatroom.chatroomId.eq(roomId).and(chatroomUsers.userId.ne(userId)))
                .fetch();
    }

}
