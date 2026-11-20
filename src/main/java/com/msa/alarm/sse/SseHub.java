package com.msa.alarm.sse;

import com.msa.alarm.dto.NotificationResponseDto;
import org.springframework.scheduling.annotation.Scheduled;
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
        // 목록 만들기와 추가를 한 번에(compute) 한다. removeEmitter 가 빈 목록의 키를 지우므로,
        // computeIfAbsent(...).add() 로 나눠 하면 그 사이에 키가 지워져 emitter 가 맵 밖 목록에 들어갈 수 있다.
        emitters.compute(userId, (k, list) -> {
            CopyOnWriteArrayList<SseEmitter> l = (list != null) ? list : new CopyOnWriteArrayList<>();
            l.add(emitter);
            return l;
        });

        // 연결 종료시 자원 정리
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        // 연결 직후 주석(comment) 이벤트 전송 (초기 연결 확인 및 503 에러 방지)
        try {
            emitter.send(SseEmitter.event()
                    .comment("connected"));

        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }
        return emitter;
    }
    @Scheduled(fixedRate = 15000)
    public void ping(){
        emitters.forEach((userId, userEmitters) -> {
                    for(SseEmitter emitter : userEmitters){
                        try{
                            emitter.send(SseEmitter.event().comment("ping"));
                        }catch (Exception e){
                            // 끊긴 연결은 목록에서만 뺀다. 여기서 complete() 를 부르면 이미 끊긴 요청을
                            // 스프링이 오류로 다시 처리해 GlobalExceptionHandler 로그가 남는다 (2026-11-21 확인)
                            removeEmitter(userId, emitter);
                        }
                    }
                }
        );
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
    // 목록이 비면 키까지 지운다 (null 을 돌려주면 ConcurrentHashMap 이 그 키를 제거). 키 단위로 원자적이다
    private void removeEmitter(Long userId, SseEmitter emitter){
        emitters.computeIfPresent(userId, (k, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}
