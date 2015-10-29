package hk.ust.cse.hunkim.questionroom.question;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.ust.cse.hunkim.questionroom.FirebaseListAdapter;
import hk.ust.cse.hunkim.questionroom.MainActivity;
import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.chatroom.CommentListAdapter;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;

/**
 * @author greg
 * @since 6/21/13
 * <p/>
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class QuestionListAdapter extends FirebaseListAdapter<Question> {

    // The mUsername for this client. We use this to indicate which messages originated from this user
    MainActivity activity;
    Context context;
    Query query;
    int layout;
    Firebase commentRef;
    List<Question> searchResult;
    Map<String, Question> searchHashMap;
    List<Question> tmpQuestionList;
    Map<String, Question> tmpHashMap;

    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {

            //set a longClickListener to popup menu for entering comment
            LayoutInflater inflater = activity.getLayoutInflater();
            final View AddCommentLayout = inflater.inflate(R.layout.add_comment_layout, null);
            final TextView comentContent = (TextView) AddCommentLayout.findViewById(R.id.add_comment);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(AddCommentLayout).setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Firebase commentPushRef = commentRef.child(v.getTag().toString()).push();
                    Question question = new Question(comentContent.getText().toString());
                    commentPushRef.setValue(question);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

            return false;
        }
    };

    public QuestionListAdapter(Query ref, Activity activity, int layout, Context context, Firebase commentRef) {
        super(ref, Question.class, layout, activity);

        // Must be MainActivity
        assert (activity instanceof MainActivity);

        this.query = ref;
        this.activity = (MainActivity) activity;
        this.context = context;
        this.layout = layout;
        this.commentRef = commentRef;
    }

    public void doSearch(String keyword){

        if (searchResult == null) {
            searchResult = new ArrayList<>();
            searchHashMap = new HashMap<>();
            tmpQuestionList = getModels();
            tmpHashMap = getModelMap();
            super.attachList(searchResult, searchHashMap);
            super.detachChildEventlistener();
        }

        searchResult.clear();
        searchHashMap.clear();

        for (Question q : tmpQuestionList) {
            boolean containTag = false;
            for (String tag : q.getTags()) {
                if (tag.contains(keyword))
                    containTag = true;
            }

            if (containTag) {
                searchHashMap.put(q.getKey(), q);
                setKey(q.getKey(), q);
                searchResult.add(q);

            }
        }
        notifyDataSetChanged();
    }

    public void finishSearch () {
        if (searchResult != null){
            searchResult.clear();
            searchResult = null;
            searchHashMap.clear();
            searchHashMap = null;
        }
        if (tmpQuestionList != null && tmpHashMap != null)
            attachList(tmpQuestionList, tmpHashMap);

        initChildEventlistener();
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view     A view instance corresponding to the layout we passed to the constructor.
     * @param question An instance representing the current state of a chat message
     */
    @Override
    protected void populateView(View view, Question question) {
        DBUtil dbUtil = activity.getDbutil();

        // Map a Chat object to an entry in our listview
        int echo = question.getEcho();
        Button echoButton = (Button) view.findViewById(R.id.echo);
        echoButton.setText("" + echo);
        echoButton.setTextColor(Color.BLUE);


        echoButton.setTag(question.getKey()); // Set tag for button

        echoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity m = (MainActivity) view.getContext();
                        m.updateEcho((String) view.getTag());
                    }
                }

        );

        int dislike = question.getDislike();
        Button dislikeButton = (Button) view.findViewById(R.id.dislike);
        dislikeButton.setText("" + dislike);
        dislikeButton.setTextColor(Color.RED);


        dislikeButton.setTag(question.getKey()); // Set tag for button

        dislikeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity m = (MainActivity) view.getContext();
                        m.updateDislike((String) view.getTag());
                    }
                }

        );



        String msgString = "";

        question.updateNewQuestion();
        if (question.isNewQuestion()) {
            msgString += "<font color=red>NEW </font>";
        }

        msgString += "<B>" + question.getHead() + "</B>" + question.getDesc();
        Log.d("comments", msgString);

        ((TextView) view.findViewById(R.id.head_desc)).setText(Html.fromHtml(msgString));
        view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        MainActivity m = (MainActivity) view.getContext();
                                        m.updateEcho((String) view.getTag());
                                        m.updateDislike((String) view.getTag());
                                    }
                                }
        );

        long time = question.getTimestamp();
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) view.findViewById(R.id.queation_time)).setText(relativeTime);

        if (question.getQuestioner() != null)
            ((TextView) view.findViewById(R.id.questioner)).setText(question.getQuestioner());
        else
            ((TextView) view.findViewById(R.id.questioner)).setText("Anonymouse");

        // check if we already clicked
        boolean clickable = !dbUtil.contains(question.getKey());

        echoButton.setClickable(clickable);
        echoButton.setEnabled(clickable);
        view.setClickable(clickable);


        // http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
        // grey out our button
        if (clickable) {
            echoButton.getBackground().setColorFilter(null);
        } else {
            echoButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        }


        // check if we already clicked
        boolean dislikeclickable = !dbUtil.contains(question.getKey());

        dislikeButton.setClickable(dislikeclickable);
        dislikeButton.setEnabled(dislikeclickable);
        view.setClickable(dislikeclickable);


        // http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
        // grey out our button
        if (dislikeclickable) {
            dislikeButton.getBackground().setColorFilter(null);
        } else {
            dislikeButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }




        view.setTag(question.getKey());  // store key in the view

        view.setOnLongClickListener(longClickListener);
/*        QuestionListAdapter commentsAdapter = new QuestionListAdapter(commentRef.child(question.getKey()).orderByChild("timestamp"),
                activity, layout, context, commentRef);

        ((ListView) view.findViewById(R.id.question_comments)).setAdapter(commentsAdapter);
*/
        final CommentListAdapter commentListAdapter = new CommentListAdapter(
                commentRef.child(question.getKey()).orderByChild("timestamp"),
                context,
                new ArrayList<Question>());
        ((ListView) view.findViewById(R.id.question_comments)).setAdapter(commentListAdapter);
    }

    @Override
    protected void sortModels(List<Question> mModels) {
        Collections.sort(mModels);
    }

    @Override
    protected void setKey(String key, Question model) {
        model.setKey(key);
    }
}
