package hk.ust.cse.hunkim.questionroom.chatroom;

/**
 * Created by cc on 10/11/2015.
 */
public class ChatRoom {
    public String roomName;
    public String question = "test";
    public long activeTime;


    public ChatRoom() {}

    public ChatRoom(String roomName, String question, long activeTime) {
        this.roomName = roomName;
        this.question = question;
        this.activeTime = activeTime;
    }
}
