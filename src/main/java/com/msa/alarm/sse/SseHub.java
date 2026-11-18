package com.msa.alarm.sse;

import com.msa.alarm.common.BusinessException;
import com.msa.alarm.dto.NotificationResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseHub {
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters
            = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId){
        SseEmitter emitter = new SseEmitter(60 * 1000L * 30);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 연결 종료시 자원 정리
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        return emitter;
    }
    // 유저에게 정보 전송
    public Integer sendToUser(Long userId, String eventName, NotificationResponseDto dto){
        List<SseEmitter> userEmitters = emitters.get(userId);
        if(userEmitters == null || userEmitters.isEmpty()){
            return 0;
        }
        Integer deliverCount = 0;
        for(SseEmitter emitter : userEmitters){
            if(sendToClient(emitter, userId, eventName, dto)){
                deliverCount++;
            }
        }
        return deliverCount;
    }
    // 왜 private?
    // 허브 내에서만 쓰이는 도우미 메서드이므로 타 클래스가 알 필요 없음
    private Boolean sendToClient(SseEmitter emitter, Long userId, String name, Object data){
        try{
            emitter.send(SseEmitter.event().name(name).data(data));
            return true;
        }catch(Exception e){
            emitter.complete();
            // 완료처리하고 즉시 제거
            removeEmitter(userId, emitter);
        }
        return false;
    }

    // emitter 삭제
    private void removeEmitter(Long userId, SseEmitter emitter){
        List<SseEmitter> userEmitters = emitters.get(userId);
        if(userEmitters != null){
            userEmitters.remove(emitter);
        }
    }
}
