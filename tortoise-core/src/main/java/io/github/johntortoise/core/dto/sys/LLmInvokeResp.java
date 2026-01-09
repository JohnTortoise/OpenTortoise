package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LLmInvokeResp {
    private String lastResp;

    private List<AllChat> allChatList;

    private List<Event> eventLists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AllChat{
        String req;

        String resp;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Event{
        String eventType;
        String content;
    }
}
