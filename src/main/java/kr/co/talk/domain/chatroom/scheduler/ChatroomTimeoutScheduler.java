package kr.co.talk.domain.chatroom.scheduler;

import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * chatroom의 timeout을 걸고, 대화 마감을 해주기 위한 scheduler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatroomTimeoutScheduler {
    private final RedisService redisService;

    private static final long NOTICE_5MINUTE = Duration.ofMinutes(5).toMillis();

    @Scheduled(fixedRate = 3000)
    public void scheduleNoticeTask() {
//        log.debug("fixed rate task - {}", System.currentTimeMillis() / 1000);

        // 채팅방 timeout check
        Map<String, ChatroomNoticeDto> chatroomNoticeEntry = redisService.getChatroomNoticeEntry();

//        log.info("현재 시간 :: " + System.currentTimeMillis());

        chatroomNoticeEntry.entrySet().forEach(entry -> {
            String roomId = entry.getKey();
            ChatroomNoticeDto cn = entry.getValue();

            log.info("createTime:::" + cn.getCreateTime());
            if (cn.getCreateTime() + cn.getTimeout() > System.currentTimeMillis()
                    && cn.getCreateTime() + cn.getTimeout() <= System.currentTimeMillis()
                            + NOTICE_5MINUTE
                    && !cn.isNotice()) {
                // TODO 종료 5분전이면 socket으로 알림
                log.info("채팅방 종료 5분전, CHAT ROOM ID ::", cn.getRoomId());
                cn.setNotice(true); // 5분전 공지 flag
                redisService.pushNoticeMap(roomId, cn);
            } else if (cn.getCreateTime() + cn.getTimeout() <= System.currentTimeMillis()) {
                // TODO 채팅방 종료 알림 SOCKET
                log.info("채팅방 종료 , CHAT ROOM ID ::", cn.getRoomId());
                redisService.deleteChatroomNotice(roomId);
            }
        });

    }

}
