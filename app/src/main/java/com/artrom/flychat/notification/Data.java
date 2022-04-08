package com.artrom.flychat.notification;

public class Data {
    private String Title;
    private String Message;
    private int Icon;
    private String User;
    private String Sented;

    public Data(String title, String User, int Icon, String Sented, String Message) {
        this.Title = title;
        this.Message = Message;
        this.Icon = Icon;
        this.User = User;
        this.Sented = Sented;
    }

    public Data() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getIcon() {
        return Icon;
    }

    public void setIcon(int icon) {
        Icon = icon;
    }

    public String getSented() {
        return Sented;
    }

    public void setSented(String sented) {
        Sented = sented;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
