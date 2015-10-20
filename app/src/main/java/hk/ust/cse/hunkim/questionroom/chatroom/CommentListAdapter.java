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

import java.util.List;

import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.question.Question;

/**
 * Created by cc on 10/18/2015.
 */
public class CommentListAdapter extends ArrayAdapter<Question> {

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

        query();
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
    public View getView(int pos, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commentView = inflater.inflate(R.layout.question, parent, false);
        Question comment = getItem(pos);

        ((TextView) commentView.findViewById(R.id.head_desc)).setText(comment.getWholeMsg());
        Log.d("comment added to view", comment.getWholeMsg());

        long time = comment.getTimestamp();
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) commentView.findViewById(R.id.queation_time)).setText(relativeTime);

        return commentView;
    }
}
