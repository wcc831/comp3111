package hk.ust.cse.hunkim.questionroom.question;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hunkim on 7/16/15.
 */
public class Question implements Comparable<Question> {

    public boolean isLoad = false;

    /**
     * Must be synced with firebase JSON structure
     * Each must have getters
     */
    private String key;
    private String wholeMsg;
    //private String head;
    //private String headLastChar;
    //private String desc;
    //private String linkedDesc;
    private String questioner;
    private boolean completed;
    private long timestamp;
    private String[] tags;
    private int like;
    private int dislike;
    private String dislikeKey;
    private int order;
    private boolean newQuestion;
    private String category;
    private String attachment;
    private int highlight;
    // is professfor ask, set hightlight = 2
    // 0= nothing, 1 = prof like , 2= prof ask

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

    public Question(String questioner, String message, String category){
        this(message);
        this.questioner = questioner;
        this.category = category;
    }

    /**
     * Set question from a String message
     * @param message string message
     */
    public Question(String message) {
        List<String> tmp = new ArrayList<>();
        extractTag(message, 0, tmp);
        tags = new String[tmp.size()];
        tmp.toArray(tags);

        for(String tag : tags){
            Log.d("print tags", tag);
        }

        this.wholeMsg = message;
        this.like = 0;
        this.dislike = 0;
        /*
        this.head = getFirstSentence(message).trim();
        this.desc = "";
        if (this.head.length() < message.length()) {
            this.desc = message.substring(this.head.length());
        }

        // get the last char
        this.headLastChar = head.substring(head.length() - 1);
        */
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

            if (separateTitle(message, i)){
                if (index == -1) {
                    index = i;
                } else {
                    index = Math.min(i, index);
                }
            }

        }

        if (index == -1) {
            return message;
        }

        return message.substring(0, index+1);
    }

    /* -------------------- Getters ------------------- */
    //public String getHead() { return head; }

//    public String getDesc() { return desc; }

    public int getLike() {
        return like;
    }

    public int getDislike() {
        return dislike;
    }

    public String getWholeMsg() {
        return wholeMsg;
    }

    //public String getHeadLastChar() { return headLastChar; }

    //public String getLinkedDesc() { return linkedDesc; }

    public String getQuestioner() { return questioner; }

    public boolean isCompleted() {
        return completed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String[] getTags() {
        return tags;
    }

    public int getOrder() { return order; }

    public String getAttachment(){ return attachment; };

    public boolean isNewQuestion() {
        return newQuestion;
    }

    public void updateNewQuestion() {
        newQuestion = this.timestamp > new Date().getTime() - 180000;
    }

    public String getKey() {
        return key;
    }

    public String getDislikeKey(){ return dislikeKey;}

    public void setQusetioner(String questioner) { this.questioner = questioner; }

    public void setAttachment(String attachment) { this.attachment = attachment; }

    public void setTimestamp(long time) { this.timestamp = time; }

    public void setLike(int like) { this.like = like; }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDislikeKey(String key) { this.dislikeKey = dislikeKey;}

    public String getCategory(){return category;}

    public static boolean separateTitle(String m, int i){

        // if meet conditions(e.g. Dr.), return false
        if ((m.substring(i-2, i+1).equalsIgnoreCase("Dr.")) ||  (m.substring(i-2, i+1).equalsIgnoreCase("Mr."))
                || (m.substring(i-2, i+1).equalsIgnoreCase("Ms."))){
            return false;
        }
        else if (m.substring(i-3, i+1).equalsIgnoreCase("Mrs.")){
            return false;
        }
        else if ((m.substring(i-4, i+1).equalsIgnoreCase("Prof.")) || (m.substring(i-4, i+1).equalsIgnoreCase("Para."))){
            return false;
        }
        // else return true
        else {
            return true;
        }
    }

    public static List<String> extractTag(String message, int index, List<String> tags){
        int hashIndex = message.indexOf('#', index);
        int spaceIndex;
        if (hashIndex == -1)
            return tags;
        spaceIndex = message.indexOf(" ", hashIndex);
        if (spaceIndex == -1)
            spaceIndex = message.length();

        tags.add(message.substring(hashIndex, spaceIndex));

        return extractTag(message, spaceIndex, tags);
    }
    /**
     * New one/high like goes bottom
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


        if (this.like == other.like) {
            if (other.timestamp == this.timestamp) {
                return 0;
            }
            return other.timestamp > this.timestamp ? -1 : 1;
        }
        return this.like - other.like;
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

    public int getHighlight() { return highlight;}

    public void setHighlight(int num) {
        highlight = num;}

}