package com.example.financetracker.telegram;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserState {
    @Getter
    private final State state;
    private final Map<String, String> data;
    private final List<Integer> messageIds;

    public UserState(State state) {
        this(state, new HashMap<>(), new ArrayList<>());
    }

    public UserState(State state, Map<String, String> data) {
        this(state, data, new ArrayList<>());
    }

    public UserState(State state, Map<String, String> data, List<Integer> messageIds) {
        this.state = state;
        this.data = data;
        this.messageIds = messageIds;
    }

    public Map<String, String> getAllData() {
        return new HashMap<>(data);
    }

    public void addMessageId(int messageId) {
        this.messageIds.add(messageId);
    }

    public List<Integer> getMessageIds() {
        return new ArrayList<>(messageIds);
    }
}

enum State {
    AWAITING_LOGIN,
    AWAITING_PASSWORD,
    AWAITING_REGISTRATION_USERNAME,
    AWAITING_REGISTRATION_PASSWORD,
    AWAITING_REGISTRATION_CONFIRM_PASSWORD,
    AWAITING_REGISTRATION_EMAIL,

}