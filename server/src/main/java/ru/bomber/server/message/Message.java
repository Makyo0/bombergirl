package ru.bomber.server.message;

public class Message {

    private Topic topic;
    private String data;

    public Message(Topic topic, String data) {
        this.topic = topic;
        this.data = data;
    }

    public Message() {
    }
}
