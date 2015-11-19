package hk.ust.cse.hunkim.questionroom;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import hk.ust.cse.hunkim.questionroom.chatroom.CommentListAdapter;
import hk.ust.cse.hunkim.questionroom.db.DBHelper;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;
import hk.ust.cse.hunkim.questionroom.question.Question;
import hk.ust.cse.hunkim.questionroom.server.ServerConfig;
import hk.ust.cse.hunkim.questionroom.server.ServerConnection;

public class QuestionActivity extends Activity {

    private static final String TAG = "QuestionActivity";

    Firebase fireRef;
    Firebase questionRef;

    String questitionKey;
    String roomName;

    DBUtil dbUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        roomName = getIntent().getStringExtra("room");
        questitionKey = getIntent().getStringExtra("key");

        fireRef = new Firebase(MainActivity.FIREBASE_URL);
        questionRef = fireRef.child("rooms").child(roomName).child("questions").child(questitionKey);

        //load question to layout
        questionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Question q = dataSnapshot.getValue(Question.class);

                if (q.getQuestioner() != null)
                    ((TextView) findViewById(R.id.questioner)).setText(q.getQuestioner());

                long time = q.getTimestamp();
                String relativeTime = (String) DateUtils.getRelativeDateTimeString(QuestionActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                ((TextView) findViewById(R.id.queation_time)).setText(relativeTime);

                ((TextView) findViewById(R.id.category)).setText(q.getCategory());
                ((TextView) findViewById(R.id.head_desc)).setText(q.getWholeMsg());

                //set attachment
                String encodedImage = q.getAttachment();
                if (encodedImage != null) {
                    byte[] imageAsBytes = Base64.decode(encodedImage.getBytes(), Base64.DEFAULT);
                    ImageView image = (ImageView) findViewById(R.id.attachment);
                    image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
                    image.setImageBitmap(
                            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                    );
                }

                Button likeButton = ((Button) findViewById(R.id.echo));
                likeButton.setText(Integer.toString(q.getLike()));

                Button dislikeButton = ((Button) findViewById(R.id.dislike));
                dislikeButton.setText(Integer.toString(q.getDislike()));

                boolean clickable = !dbUtil.contains(questitionKey);
                likeButton.setClickable(clickable);
                likeButton.setEnabled(clickable);

                dislikeButton.setClickable(clickable);
                dislikeButton.setEnabled(clickable);

                Log.d(TAG, Boolean.toString(dbUtil.contains(questitionKey)));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        setUpdateListener(((Button) findViewById(R.id.echo)), "like", 1);
        setUpdateListener(((Button) findViewById(R.id.dislike)), "dislike", -1);
        //load comments
        CommentListAdapter adapter = new CommentListAdapter(fireRef.child("rooms").child(roomName).child("comment").child(questitionKey).orderByChild("timestamp"),
                this, new ArrayList<Question>());

        ((ListView) findViewById(R.id.question_comments)).setAdapter(adapter);


        dbUtil = new DBUtil(new DBHelper(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("InstaQuest");
        }

        if (UserInfo.getInstance().role == UserInfo.SUPERVISOR) {
            menu.findItem(R.id.action_giveReword).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_giveReword:
                giveReword((String) ((TextView) findViewById(R.id.questioner)).getText());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpdateListener(final Button b, final String child, final int i) {

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "clicked: ");

                final Firebase echo = questionRef.child(child);
                echo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long value = (Long) dataSnapshot.getValue();
                        echo.setValue(value + i);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

                final Firebase order = questionRef.child(child);
                order.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long value = (Long) dataSnapshot.getValue();
                        order.setValue(value + i);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

                dbUtil.put(questitionKey);

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public boolean refresh(MenuItem item) {
        return false;
    }

    public void giveReword (String email) {
        if (!email.contains(".com") && !email.contains("@")) {
            Toast.makeText(QuestionActivity.this, "The email provide by this user is not valid", Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            ServerConfig config = new ServerConfig();
            config.sendEmail(UserInfo.getInstance().email, email, email, 1);
            new ServerConnection(config).execute();

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}
