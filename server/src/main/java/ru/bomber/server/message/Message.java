package ru.bomber.server.message;

public class Message {

    //выдает ошибку генерации JSON в поле data
    //возможно стоит попробовать упаковать в List т.к. в  дату передаются и массивы
    private Topic topic;
    private String data;


    public Topic getTopic() {
        return topic;
    }

    public String getData() {
        return data;
    }

    public Message(Topic topic, String data) {
        this.topic = topic;
        this.data = data;
    }

    public Message() {
    }
}
