package hk.ust.cse.hunkim.questionroom;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.question.Question;


/**
 * Created by hunkim on 7/15/15.
 */

public class QuestionTest  extends TestCase {
    Question q;



    protected void setUp() throws Exception {
        super.setUp();

        q = new Question("Hello? This is very fucking shit");
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
    public void testgetWholeMsg() { assertEquals("WholeMsg", "Hello? This is very loveing nice", q.getWholeMsg()); }

    @SmallTest
    public void testWordFilterTest() { assertEquals("filter", true, !q.getWholeMsg().contains("fuck")); }

    @SmallTest
    public void testExtractTag() {
        List<String> tags = new ArrayList<>();
        Question.extractTag("#tag1 #tag2 #tag3", 0, tags);

        assertEquals("tag", "#tag1", tags.get(0));
        assertEquals("tag", "#tag2", tags.get(1));
        assertEquals("tag", "#tag3", tags.get(2));

    }
}
