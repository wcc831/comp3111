package hk.ust.cse.hunkim.questionroom.chatroom;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.List;

import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.question.Question;

/**
 * Created by cc on 10/18/2015.
 */
public class CommentListAdapter extends ArrayAdapter<Question> {

    public static final String TAG = "CommentListAdapter";

    final List<Question> questions;
    Context context;
    Query query;

    public CommentListAdapter(Context context, List<Question> questions) {
        super(context, -1, questions);

        this.context = context;
        this.questions = questions;
    }

    public CommentListAdapter(Query query, Context context, List<Question> questions){
        super(context, -1, questions);

        this.query = query;
        this.context = context;
        this.questions = questions;

        queryOnce();
    }

    private void queryOnce() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    Question q = child.getValue(Question.class);
                    questions.add(q);
                    Log.d("comment loaded from db", q.getWholeMsg());
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void query(){
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Question q = dataSnapshot.getValue(Question.class);
                questions.add(q);
                Log.d("comment loaded from db", q.getWholeMsg());
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    @Override
    public View getView(int pos, View view, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commentView = inflater.inflate(R.layout.comment, parent, false);
        Question comment = getItem(pos);

        String questioner = comment.getQuestioner();
        String message = comment.getWholeMsg();
        if (questioner != null)
            ((TextView) commentView.findViewById(R.id.questioner)).setText(questioner);
        else
            ((TextView) commentView.findViewById(R.id.questioner)).setText("Anonymous");
        ((TextView) commentView.findViewById(R.id.head_desc)).setText(message);

        long time = comment.getTimestamp();
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) commentView.findViewById(R.id.queation_time)).setText(relativeTime);

        /*for (int i = 0; i < questions.size(); i++) {
            Log.d(TAG, getItem(i).getWholeMsg());
        }*/
        Log.d(TAG,Integer.toString(pos) +  " " + comment.getWholeMsg());

        return commentView;
    }
}
