package hk.ust.cse.hunkim.questionroom;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

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

    String questionKey;
    String roomName;

    DBUtil dbUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Firebase.setAndroidContext(this);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("room");
        questionKey = intent.getStringExtra("key");


        fireRef = new Firebase(MainActivity.FIREBASE_URL);
        questionRef = fireRef.child("rooms").child(roomName).child("questions").child(questionKey);

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

                TextView category = (TextView) findViewById(R.id.category);
                TextView msg = (TextView) findViewById(R.id.head_desc);
                final TextView questionerView = (TextView) findViewById(R.id.questioner);
                category.setText(q.getCategory());
                msg.setText(q.getWholeMsg());
                Typeface helvetica = Typeface.createFromAsset(getAssets(), "font/Helvetica_Neue.ttf");
                if (q.getHighlight() == 2) {
                    msg.setTypeface(helvetica);
                    category.setTypeface(helvetica, Typeface.BOLD);
                    msg.setTextColor((0xFF2DAAF3));
                    category.setTextColor((0xFF2DAAF3));
                    questionerView.setTypeface(helvetica, Typeface.BOLD);
                    questionerView.setTextColor((0xFF2DAAF3));

                }
                else if (q.getHighlight() == 1) {
                    msg.setTypeface(helvetica);
                    category.setTypeface(helvetica, Typeface.BOLD);
                    msg.setTextColor((0xFFF28D09));
                    category.setTextColor((0xFFF28D09));
                    questionerView.setTypeface(helvetica, Typeface.BOLD);
                    questionerView.setTextColor((0xFFF28D09));

                }

                //set attachment
                String encodedImage = q.getAttachment();
                if (!TextUtils.isEmpty(encodedImage)
                        && encodedImage.indexOf(',') > 0) {
                    encodedImage = encodedImage.substring(encodedImage.indexOf(','));
                    byte[] imageAsBytes = Base64.decode(encodedImage.getBytes(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                    ImageView image = (ImageView) findViewById(R.id.attachment);
                    image.setLayoutParams(new LinearLayout.LayoutParams(bitmap.getWidth() * 5, bitmap.getHeight() * 5));
                    image.setImageBitmap(bitmap);
                    image.setPadding(0, 15, 0, 15);
                }



                Button likeButton = ((Button) findViewById(R.id.echo));
                likeButton.setText(Integer.toString(q.getLike()));

                Button dislikeButton = ((Button) findViewById(R.id.dislike));
                dislikeButton.setText(Integer.toString(q.getDislike()));

                boolean clickable = !dbUtil.contains(questionKey);
                likeButton.setClickable(clickable);
                likeButton.setEnabled(clickable);

                dislikeButton.setClickable(clickable);
                dislikeButton.setEnabled(clickable);

                Log.d(TAG, Boolean.toString(dbUtil.contains(questionKey)));

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        setUpdateListener(((Button) findViewById(R.id.echo)), "like", 1);
        setUpdateListener(((Button) findViewById(R.id.dislike)), "dislike", -1);
        //load comments
        CommentListAdapter adapter = new CommentListAdapter(fireRef.child("rooms").child(roomName).child("comment").child(questionKey).orderByChild("timestamp"),
                this, new ArrayList<Question>());

        ((ListView) findViewById(R.id.question_comments)).setAdapter(adapter);


        dbUtil = new DBUtil(new DBHelper(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_question, menu);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("InstaQuest");

        menu.findItem(R.id.action_giveReword).setVisible(UserInfo.getInstance().role == UserInfo.SUPERVISOR);
        menu.findItem(R.id.action_highlight).setVisible(UserInfo.getInstance().role == UserInfo.SUPERVISOR);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

                dbUtil.put(questionKey);

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

    public void highlight(MenuItem item) {
        fireRef.child("rooms").child(roomName).child("questions").child(questionKey).child("highlight").setValue(1);
    }

    public void giveReward (MenuItem item) {
        String email = (String) ((TextView) findViewById(R.id.questioner)).getText();
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

    public void addToFavorite(MenuItem item) {

    }

    public void giveComment(View view) {

        final Firebase commentRef = fireRef.child("rooms").child(roomName).child("comment").child(questionKey).push();

        LayoutInflater inflater = getLayoutInflater();
        final View AddCommentLayout = inflater.inflate(R.layout.add_comment_layout, null);
        final TextView comentContent = (TextView) AddCommentLayout.findViewById(R.id.add_comment);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(AddCommentLayout).setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (comentContent.getText().length() < 1){
                    Toast.makeText(QuestionActivity.this, "comment cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Question question = new Question(comentContent.getText().toString());
                question.setQusetioner(UserInfo.getInstance().email);
                if (UserInfo.getInstance().role == UserInfo.SUPERVISOR)
                    question.setHighlight(2);
                commentRef.setValue(question);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}
