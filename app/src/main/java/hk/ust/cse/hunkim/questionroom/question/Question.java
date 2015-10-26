package hk.ust.cse.hunkim.questionroom.question;

import java.util.Date;
import java.util.List;

/**
 * Created by hunkim on 7/16/15.
 */
public class Question implements Comparable<Question> {

    /**
     * Must be synced with firebase JSON structure
     * Each must have getters
     */
    private String key;
    private String wholeMsg;
    private String head;
    private String headLastChar;
    private String desc;
    private String linkedDesc;
    private String questioner;
    private boolean completed;
    private long timestamp;
    private String tags;
    private int echo;
    private int order;
    private boolean newQuestion;

    public String getDateString() {
        return dateString;
    }

    private String dateString;

    public String getTrustedDesc() {
        return trustedDesc;
    }

    private String trustedDesc;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private Question() {
    }

    public Question(String questioner, String message){
        this(message);
        this.questioner = questioner;
    }

    /**
     * Set question from a String message
     * @param message string message
     */
    public Question(String message) {
        message = badWordFilter(message);
        this.wholeMsg = message;
        this.echo = 0;
        this.head = getFirstSentence(message).trim();
        this.desc = "";
        if (this.head.length() < message.length()) {
            this.desc = message.substring(this.head.length());
        }

        // get the last char
        this.headLastChar = head.substring(head.length() - 1);

        this.timestamp = new Date().getTime();
    }

    /**
     * Get first sentence from a message
     * @param message
     * @return
     */
    public static String getFirstSentence(String message) {
        String[] tokens = {". ", "? ", "! "};

        int index = -1;

        for (String token : tokens) {
            int i = message.indexOf(token);
            if (i == -1) {
                continue;
            }

            if (index == -1) {
                index = i;
            } else {
                index = Math.min(i, index);
            }
        }

        if (index == -1) {
            return message;
        }

        return message.substring(0, index+1);
    }

    /* -------------------- Getters ------------------- */
    public String getHead() {
        return head;
    }

    public String getDesc() {
        return desc;
    }

    public int getEcho() {
        return echo;
    }

    public String getWholeMsg() {
        return wholeMsg;
    }

    public String getHeadLastChar() {
        return headLastChar;
    }

    public String getLinkedDesc() {
        return linkedDesc;
    }

    public String getQuestioner() { return questioner; }

    public boolean isCompleted() {
        return completed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTags() {
        return tags;
    }

    public int getOrder() { return order; }

    public boolean isNewQuestion() {
        return newQuestion;
    }

    public void updateNewQuestion() {
        newQuestion = this.timestamp > new Date().getTime() - 180000;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String badWordFilter(String message){
        message = message.toLowerCase();
        String filter;
        filter = message.replace("fuck", "love").replace("fuxk","support").replace("fxck","support").replace("fxxk","great").replace("on9","yeah")
                .replace("on 9","oh yeah").replace("diu","Auntie").replace("chi lan sin","HaHa").replace("on lun","HiHi").replace("asshole","javascript")
                .replace("ass hole","javascript").replace("ass","java").replace("bitch","friend").replace("suck","nice").replace("popkai","lucky")
                .replace("pop kai","lucky").replace("seven head","HelloWorld").replace("sevenhead","HelloWorld").replace("7head","HelloWorld")
                .replace("7 head","HelloWorld");
        return filter;
    }

    /**
     * New one/high echo goes bottom
     * @param other other chat
     * @return order
     */
    @Override
    public int compareTo(Question other) {
        // Push new on top
        other.updateNewQuestion(); // update NEW button
        this.updateNewQuestion();

        if (this.newQuestion != other.newQuestion) {
            return this.newQuestion ? 1 : -1; // this is the winner
        }


        if (this.echo == other.echo) {
            if (other.timestamp == this.timestamp) {
                return 0;
            }
            return other.timestamp > this.timestamp ? -1 : 1;
        }
        return this.echo - other.echo;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Question)) {
            return false;
        }
        Question other = (Question)o;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
