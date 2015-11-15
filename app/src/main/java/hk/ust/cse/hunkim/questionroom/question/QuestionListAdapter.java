package hk.ust.cse.hunkim.questionroom.question;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import hk.ust.cse.hunkim.questionroom.QuestionActivity;
import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoomListAdapter;
import hk.ust.cse.hunkim.questionroom.chatroom.CommentListAdapter;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;

/**
 * @author greg
 * @since 6/21/13
 * <p/>
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class QuestionListAdapter extends FirebaseListAdapter<Question> implements Filterable{
    private class QuestionFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0){

                List<Question> filterResult = new ArrayList<>();

                for (Question q: tmpQuestionList){

                    if (constraint.charAt(0) == '#'){
                        //search by tag
                        boolean containTag = false;
                        if (q.getTags() == null)
                            continue;

                        for (String tag : q.getTags()) {
                            if (tag.toLowerCase().contains(constraint.toString().toLowerCase()))
                                containTag = true;
                        }

                        if (containTag) {
                            filterResult.add(q);
                        }
                    }
                    else if (constraint.charAt(0) == '@'){

                        String catagory = constraint.subSequence(1, constraint.length()).toString();

                        //search by catagory
                        if (q.getCategory() != null
                                && q.getCategory().toLowerCase().equals(
                                catagory)){
                            filterResult.add(q);
                        }
                    }
                    else if (q.getWholeMsg().contains(constraint)){
                        //search by whole message
                        filterResult.add(q);
                    }
                }

                results.count = filterResult.size();
                results.values = filterResult;
            }
            else {
                results.count = tmpQuestionList.size();
                results.values = tmpQuestionList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setModels((List<Question>)results.values);
            Log.d("publish dta size", Integer.toString(questions.size()));

            notifyDataSetChanged();
        }
    }

    // The mUsername for this client. We use this to indicate which messages originated from this user
    MainActivity activity;
    Context context;
    Query query;
    int layout;
    Firebase commentRef;

    QuestionFilter questionFilter;

    List<Question> questions;
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
                     if (comentContent.getText().length() < 1){
                         Toast.makeText(context, "comment cannot be empty.", Toast.LENGTH_SHORT).show();
                         return;
                     }

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

    private View.OnTouchListener touchListener;

    public QuestionListAdapter(Query ref, Activity activity, int layout, Context context, Firebase commentRef) {
        super(ref, Question.class, layout, activity);

        // Must be MainActivity
        assert (activity instanceof MainActivity);

        this.query = ref;
        this.activity = (MainActivity) activity;
        this.context = context;
        this.layout = layout;
        this.commentRef = commentRef;

        this.questions = getModels();
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        this.touchListener = listener;
    }

    public void setOnLongClickListener (View.OnLongClickListener listener) {
        longClickListener = listener;
    }

    public void setList(List<Question> questions) {
        super.setModels(questions);
    }

    public void doSearch(String keyword){
        if (tmpQuestionList == null){
            tmpQuestionList = getModels();
        }
        getFilter().filter(keyword);
    }

    public void finishSearch () {
        setModels(tmpQuestionList);
        notifyDataSetChanged();

    }

    public String badWordFilter(String message){
        return message.replaceAll("(?i)fuck", "love").replaceAll("(?i)fuxk", "support").replaceAll("(?i)fxck", "support").replaceAll("(?i)fxxk", "great").replaceAll("(?i)on9", "clever")
                .replaceAll("(?i)on 9", "clever").replaceAll("(?i)diu", "Auntie").replaceAll("(?i)chi lan sin", "HaHa").replaceAll("(?i)on lun", "HiHi").replaceAll("(?i)asshole", "javascript")
                .replaceAll("(?i)ass hole", "javascript").replaceAll("(?i)ass ", "java ").replaceAll("(?i)bitch", "friend").replaceAll("(?i)suck", "good").replaceAll("(?i)popkai","lucky")
                .replaceAll("(?i)pop kai", "lucky").replaceAll("(?i)seven head", "handsome").replaceAll("(?i)sevenhead", "handsome").replaceAll("(?i)7head", "handsome")
                .replaceAll("(?i)7 head", "handsome").replaceAll("(?i)shit", "nice").replaceAll("(?i)sxit", "nice").replaceAll("(?i)shxt", "nice").replaceAll("(?i)sh!t", "nice")
                .replaceAll("(?i)damn", "god");
    }

    /**
     *
     *
    * */
    @Override
    public Filter getFilter() {
        if (questionFilter == null)
            questionFilter = new QuestionFilter();
        return questionFilter;
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
    protected void populateView(final View view, Question question) {

        DBUtil dbUtil = activity.getDbutil();

        // Map a Chat object to an entry in our listview
        int like = question.getLike();
        Button liekButton = (Button) view.findViewById(R.id.echo);
        liekButton.setText("" + like);
        //liekButton.setTextColor(Color.BLUE);


        liekButton.setTag(question.getKey()); // Set tag for button

        liekButton.setOnClickListener(
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
        //dislikeButton.setTextColor(Color.RED);


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

        //msgString += "<B>" + question.getHead() + "</B>" + question.getDesc();
        msgString = question.getWholeMsg();

        Log.d("comments", msgString);

        ((TextView) view.findViewById(R.id.head_desc)).setText(badWordFilter(msgString));
        /*view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        MainActivity m = (MainActivity) view.getContext();
                                        m.updateEcho((String) view.getTag());
                                        m.updateDislike((String) view.getTag());
                                    }
                                }
        );*/

        long time = question.getTimestamp();
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) view.findViewById(R.id.queation_time)).setText(relativeTime);

        if (question.getQuestioner() != null)
            ((TextView) view.findViewById(R.id.questioner)).setText(question.getQuestioner());
        else
            ((TextView) view.findViewById(R.id.questioner)).setText("Anonymouse");

        // To display the category
        ((TextView) view.findViewById(R.id.category)).setText(question.getCategory());

        // check if we already clicked
        boolean clickable = !dbUtil.contains(question.getKey());

        liekButton.setClickable(clickable);
        liekButton.setEnabled(clickable);
        view.setClickable(clickable);


        // http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
        // grey out our button
        /*
        if (clickable) {
            liekButton.getBackground().setColorFilter(null);
        } else {
            liekButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        }
*/

        // check if we already clicked
        boolean dislikeclickable = !dbUtil.contains(question.getKey());

        dislikeButton.setClickable(dislikeclickable);
        dislikeButton.setEnabled(dislikeclickable);
        view.setClickable(dislikeclickable);


        // http://stackoverflow.com/questions/8743120/how-to-grey-out-a-button
        // grey out our button
        /*
        if (dislikeclickable) {
            dislikeButton.getBackground().setColorFilter(null);
        } else {
            dislikeButton.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }
*/



        view.setTag(question.getKey());  // store key in the view

        view.setOnLongClickListener(longClickListener);
        view.setOnTouchListener(touchListener);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putExtra("room", query.getRef().getParent().getKey());
                intent.putExtra("key", (String) v.getTag());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
/*        QuestionListAdapter commentsAdapter = new QuestionListAdapter(commentRef.child(question.getKey()).orderByChild("timestamp"),
                activity, layout, context, commentRef);

        ((ListView) view.findViewById(R.id.question_comments)).setAdapter(commentsAdapter);
*/
        /*final CommentListAdapter commentListAdapter = new CommentListAdapter(
                commentRef.child(question.getKey()).orderByChild("timestamp"),
                context,
                new ArrayList<Question>());
        ((ListView) view.findViewById(R.id.question_comments)).setAdapter(commentListAdapter);*/

        commentRef.child(question.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    ((TextView) view.findViewById(R.id.more_comment)).setText("click to view more comment");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
