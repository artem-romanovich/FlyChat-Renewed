package com.artrom.flychat.system;

public class MyMessage {
    private String name;
    private String text;
    private String time;

    public MyMessage(String name, String text, String time) {
        this.name = name;
        this.text = text;
        this.time = time;
    }

    public static boolean equalMessages(MyMessage message1, MyMessage message2) {
        return message1.getName().equals(message2.getName()) &&
                message1.getText().equals(message2.getText()) &&
                message1.getTime().equals(message2.getTime());
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }
}
