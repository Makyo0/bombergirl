package ru.bomber.server.network;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.bomber.server.service.GameService;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Scope("singleton")
public class ConnectionHandler extends TextWebSocketHandler implements WebSocketHandler {

    @Override
    public synchronized void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(session.getUri(), StandardCharsets.UTF_8);
        int gameId = Integer.parseInt(nameValuePairList.get(0).getValue());
        GameService.connect(gameId, session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public synchronized void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        super.afterConnectionClosed(session, closeStatus);
    }

    @Override
    protected synchronized void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //System.out.println("Recieved message from " + session + " message:" + message.getPayload());
        GameService.handleMessage(session, message);
    }
}
