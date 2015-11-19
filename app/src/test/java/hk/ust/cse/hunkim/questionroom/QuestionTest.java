package hk.ust.cse.hunkim.questionroom;

import android.test.suitebuilder.annotation.SmallTest;

import com.google.android.gms.games.quest.Quest;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;
import hk.ust.cse.hunkim.questionroom.question.Question;


/**
 * Created by hunkim on 7/15/15.
 */

public class QuestionTest  extends TestCase {
    Question q;
    String message = "#tah3 tag2 #tag3 Hello? #tag3 This is very fucking shit";


    protected void setUp() throws Exception {
        super.setUp();

        q = new Question("me", message, "other");
    }


    @SmallTest
    public void testChatFirstString() {
        String[] strHead = {
                "Hello? This is very nice", "Hello?",
                "This is cool! Really?", "This is cool!",
                "How.about.this? Cool", "How.about.this?"
        };

        for (int i=0; i<strHead.length; i+=2) {
            String head = q.getFirstSentence(strHead[i]);
            assertEquals("Chat.getFirstSentence", strHead[i+1], head);
        }
    }


    @SmallTest
    public void testEcho() { assertEquals("like", 0, q.getLike()); }

    @SmallTest
    public void testDislike() {
        assertEquals("dislike", 0, q.getDislike());
    }

    @SmallTest
    public void testgetWholeMsg() {
//        assertTrue("wholeMsg", !q.getWholeMsg().equals(message));
//        assertEquals("WholeMsg", "#tah3 tag2 #tag3 Hello? #tag3 This is very loveing nice", q.getWholeMsg());
    }


    @SmallTest
    public void testExtractTag() {
        List<String> tags = new ArrayList<>();
        Question.extractTag("#tag1 #tag2 #tag3", 0, tags);

        assertEquals("tag", "#tag1", tags.get(0));
        assertEquals("tag", "#tag2", tags.get(1));
        assertEquals("tag", "#tag3", tags.get(2));

    }

    @SmallTest
    public void testCatagorry(){
        //assertTrue(q.getCategory().equals("other"));
    }

    @SmallTest
    public void testQuestioner() {
        assertEquals("questioner", q.getQuestioner(), "me");
    }

    @SmallTest
    public void testTag(){
        assertEquals("tag", "#tah3", q.getTags()[0]);
        assertEquals("tag", "#tag3", q.getTags()[1]);
        assertEquals("tag", "#tag3", q.getTags()[2]);
    }

    @SmallTest
    public void testGetSeperateTitle() {
        boolean title = Question.separateTitle("Dr. Tam", 2);
        assertTrue("title", !title);

        title = Question.separateTitle("prof. Tam", 4);
        assertTrue("title", !title);

        title = Question.separateTitle("Mr. Tam", 2);
        assertTrue("title", !title);

        title = Question.separateTitle("Ms. Tam", 2);
        assertTrue("title", !title);
    }

    @SmallTest
    public void testGetFirstSentence(){
        String str = "Prof. ta!! test";
        String str2 = "Mr. ta!! test";
        String str3 = "Ms. ta!! test";
        assertTrue(Question.getFirstSentence(str).equals("Prof. ta!!"));
        assertTrue(Question.getFirstSentence(str2).equals("Mr. ta!!"));
        assertTrue(Question.getFirstSentence(str3).equals("Ms. ta!!"));
    }

    @SmallTest
    public void testCompareTo(){
        Question q1 = new Question("msg1");
        Question q2 = new Question("msg2");

        q1.compareTo(q1);

        q2.setTimestamp(0);
        q1.compareTo(q2);
        q2.compareTo(q1);

        Question q3 = new Question("msg3");
        Question q4 = new Question("msg3");
        Question q5 = new Question("msg3");

        q4.setLike(74);
        q3.compareTo(q3);
        q3.compareTo(q4);
        q4.compareTo(q3);

        q4.setTimestamp(75);
        q5.setTimestamp(15583);
    }

    @SmallTest
    public void testEquals() throws Exception {
        Question q1 = new Question("msg1");
        Question q2 = new Question("msg2");
        ChatRoom c = new ChatRoom("", "", 12);
        q1.setKey("");
        q2.setKey("sdfadfsdf");
        q1.equals(q1);
        q2.equals(q1);
        q2.equals(c);
    }
}
