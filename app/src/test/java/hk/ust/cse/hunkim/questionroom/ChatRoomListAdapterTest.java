package hk.ust.cse.hunkim.questionroom;

import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.firebase.client.Firebase;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoomListAdapter;

/**
 * Created by cc on 11/1/2015.
 */
public class ChatRoomListAdapterTest extends AndroidTestCase {

    //Firebase ref = new Firebase("https://testfirebasewcc.firebaseio.com/");
    List<ChatRoom> chatroomList;
    ChatRoomListAdapter adapter;

    public ChatRoomListAdapterTest(){
        chatroomList = new ArrayList<>();
    }
    protected void setup() throws Exception{
        super.setUp();
    }

}
