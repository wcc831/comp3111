package hk.ust.cse.hunkim.questionroom.chatroom;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.FirebaseListAdapter;
import hk.ust.cse.hunkim.questionroom.Generic;
import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.question.Question;

/**
 * Created by cc on 10/11/2015.
 */
public class ChatRoomListAdapter extends ArrayAdapter<ChatRoom> {

    final Context context;
    Query query;
    List<ChatRoom> chatrooms;
    Firebase firebaseRef;

    public static View.OnTouchListener onTouchListener;/*( = new View.OnTouchListener() {

        float x, y;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                x = event.getX();
                y = event.getY();

                Generic.animateColor(v, Color.argb(255, 238, 238, 238), Color.argb(255, 200, 200, 200));
            } else if (event.getAction() == MotionEvent.ACTION_UP)
                Generic.animateColor(v, Color.argb(255, 200, 200, 200), Color.argb(255, 238, 238, 238));
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(y - event.getY(), 2)) > 10) {
                    Generic.animateColor(v, Color.argb(255, 200, 200, 200), Color.argb(255, 238, 238, 238));
                }
            }
            else{
                Generic.animateColor(v, Color.argb(255, 200, 200, 200), Color.argb(255, 238, 238, 238));
            }
            return false;
        }
    };*/

    public ChatRoomListAdapter(Context context, Firebase firebaseRef, List<ChatRoom> list){
        super(context, -1, list);

        this.context = context;
        this.chatrooms = list;
        this.firebaseRef = firebaseRef;
    }

    public ChatRoomListAdapter(Context context, List<ChatRoom> list) {
        super(context, -1, list);

        this.context = context;
        this.chatrooms = list;
    }

    public ChatRoomListAdapter(Query query, Context context, List<ChatRoom> list) {
        super(context, -1, list);

        this.query = query;
        this.context = context;
        this.chatrooms = list;
    }


    public void queryRecentList() {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.child("recentQuestion").getValue() != null) {

                    String roomName = dataSnapshot.getKey();
                    String latestQuestionId = dataSnapshot.child("recentQuestion").getValue().toString();
                    String latestQuestion = dataSnapshot.child("questions").child(latestQuestionId).child("head").getValue().toString();
                    String activeTime = dataSnapshot.child("questions").child(latestQuestionId).child("timestamp").getValue().toString();

                    chatrooms.add(0, new ChatRoom(roomName,
                            latestQuestion,
                            Long.parseLong(activeTime)));

                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String modifiedRoom = dataSnapshot.getKey();
                String latestQuestionId = dataSnapshot.child("recentQuestion").getValue().toString();
                String latestQuestion = dataSnapshot.child("questions").child(latestQuestionId).child("head").getValue().toString();
                String activeTime = dataSnapshot.child("questions").child(latestQuestionId).child("timestamp").getValue().toString();

                for (int i = 0; i < chatrooms.size(); i++) {
                    if (chatrooms.get(i).roomName.equals(modifiedRoom)) {
                        chatrooms.get(i).activeTime = Long.parseLong(activeTime);
                        chatrooms.get(i).question = latestQuestion;
                        Collections.swap(chatrooms, 0, i);

                        notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void queryFavoriteList() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String roomName = child.getValue().toString();
                    query.getRef().getRoot().child("chatroom").child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String roomName = dataSnapshot.getKey();
                                String latestQuestionId = dataSnapshot.child("recentQuestion").getValue().toString();
                                String latestQuestion = dataSnapshot.child("questions").child(latestQuestionId).child("head").getValue().toString();
                                String activeTime = dataSnapshot.child("questions").child(latestQuestionId).child("timestamp").getValue().toString();

                                chatrooms.add(0, new ChatRoom(roomName,
                                        latestQuestion,
                                        Long.parseLong(activeTime)));

                                notifyDataSetChanged();
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void queryVisitedList() {}

    public void searchChatroom(final String searchKey){

        chatrooms.clear();

        firebaseRef.child("chatroom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String roomName = child.getKey();
                    Log.d("room name", roomName);
                    if (roomName.contains(searchKey)) {
                        try {
                            String latestQuestionId = child.child("recentQuestion").getValue().toString();
                            String latestQuestion = child.child("questions").child(latestQuestionId).child("head").getValue().toString();
                            String activeTime = child.child("questions").child(latestQuestionId).child("timestamp").getValue().toString();

                            chatrooms.add(0, new ChatRoom(roomName,
                                    latestQuestion,
                                    Long.parseLong(activeTime)));

                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void finishSearch() {
        chatrooms.clear();
        notifyDataSetChanged();
    }

    public void setOnTouchListener(View.OnTouchListener listener){
        this.onTouchListener = listener;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View chatRoomView = inflater.inflate(R.layout.chatroom, parent, false);
        ChatRoom room = getItem(pos);

        ((TextView) chatRoomView.findViewById(R.id.index_chatRoom)).setText(room.roomName);
        ((TextView) chatRoomView.findViewById(R.id.index_question)).setText(room.question);

        long time = room.activeTime;
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) chatRoomView.findViewById(R.id.index_activeTime)).setText(relativeTime);


        chatRoomView.setOnTouchListener(onTouchListener);

        return chatRoomView;
    }

}
