package com.example.study_mdns;

import java.net.InetSocketAddress;

public enum SocketConnectionState {
    CONNECTED("CONNECTED"),                                 // 소켓이 정상적으로 연결이 되었을 경우
    DISCONNECTED("DISCONNECTED"),                           // 소켓이 끊어졌을 경우
    DISCONNECTED_BY_FORCE("DISCONNECTED_BY_FORCE"),
    UNAVAILABLE("UNAVAILABLE"),                             // 소켓 연결 실패
    RETRY("RETRY");                                         // 세트 쪽에서 소켓을 끊었을 때, 다시 재 연결 하는 시나리오 예외 처리

    SocketConnectionState(String description) {
        this.description = description;
    }

    private final String description;
    public InetSocketAddress address;

    @Override
    public String toString() {
        return "SocketConnectionState{" + description + '}';
    }
}
