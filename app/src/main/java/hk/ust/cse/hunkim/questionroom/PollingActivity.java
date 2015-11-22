package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.ChildEventListener;

import java.util.ArrayList;

import hk.ust.cse.hunkim.questionroom.chatroom.CommentListAdapter;
import hk.ust.cse.hunkim.questionroom.db.DBHelper;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;
import hk.ust.cse.hunkim.questionroom.polling.PollingListAdapter;
import hk.ust.cse.hunkim.questionroom.polling.Polling;
import hk.ust.cse.hunkim.questionroom.question.Question;


/**
 * Created by onzzz on 20/11/2015.
 */
public class PollingActivity extends Activity {
    public static final String ROOM_NAME = "Room_name";
    private String roomName;

    Firebase fireRef;
    Firebase pollingRef;
    private DBUtil dbutil;

    public DBUtil getDbutil() {
        return dbutil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_polling);

        Intent intent = getIntent();
        assert (intent != null);

        roomName = intent.getStringExtra(MainActivity.ROOM_NAME);
        if (roomName == null || roomName.length() == 0) {
            roomName = "all";
        }

        setTitle(roomName);

        fireRef = new Firebase(MainActivity.FIREBASE_URL);
        pollingRef = fireRef.child("rooms").child(roomName).child("polling");

        findViewById(R.id.create_poll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(ROOM_NAME, roomName);
                intent.setClass(PollingActivity.this, AddPollingActivity.class);
                startActivity(intent);
            }
        });

        PollingListAdapter adapter = new PollingListAdapter(fireRef.child("rooms").child(roomName)
                .child("polling").orderByChild("timestamp"),
                this, R.layout.poll, this, pollingRef);
        ((ListView) findViewById(R.id.pollList)).setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
