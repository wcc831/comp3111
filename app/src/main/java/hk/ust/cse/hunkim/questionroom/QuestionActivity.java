package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import hk.ust.cse.hunkim.questionroom.chatroom.CommentListAdapter;
import hk.ust.cse.hunkim.questionroom.question.Question;

public class QuestionActivity extends Activity {

    Firebase fireRef;
    Firebase questionRef;

    String questitionKey;
    String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        roomName = getIntent().getStringExtra("room");
        questitionKey = getIntent().getStringExtra("key");

        fireRef = new Firebase(MainActivity.FIREBASE_URL);
        questionRef = fireRef.child("rooms").child(roomName).child("questions").child(questitionKey);

        //load question to layout
        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Question q = dataSnapshot.getValue(Question.class);

                if (q.getQuestioner() != null)
                    ((TextView) findViewById(R.id.questioner)).setText(q.getQuestioner());

                long time = q.getTimestamp();
                String relativeTime = (String) DateUtils.getRelativeDateTimeString(QuestionActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                ((TextView) findViewById(R.id.queation_time)).setText(relativeTime);

                ((TextView)findViewById(R.id.category)).setText(q.getCategory());
                ((TextView)findViewById(R.id.head_desc)).setText(q.getWholeMsg());

                ((Button)findViewById(R.id.echo)).setText(Integer.toString(q.getLike()));
                ((Button)findViewById(R.id.dislike)).setText(Integer.toString(q.getDislike()));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        //load comments
        CommentListAdapter adapter = new CommentListAdapter(fireRef.child("rooms").child(roomName).child("comment").child(questitionKey).orderByChild("timestamp"),
                this, new ArrayList<Question>());

        ((ListView) findViewById(R.id.question_comments)).setAdapter(adapter);
    }
}
