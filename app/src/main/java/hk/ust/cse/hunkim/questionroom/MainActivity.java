package hk.ust.cse.hunkim.questionroom;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Date;

import hk.ust.cse.hunkim.questionroom.db.DBHelper;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;
import hk.ust.cse.hunkim.questionroom.question.Question;
import hk.ust.cse.hunkim.questionroom.question.QuestionListAdapter;

public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

    // TODO: change this to your own Firebase URL
    private static final String FIREBASE_URL = "https://ccwfirebase.firebaseio.com/";

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    private String userEmail = null;
    private String roomName;
    private Firebase mFirebaseRef = new Firebase(FIREBASE_URL);
    private Firebase mChatroomRef;
    private ValueEventListener mConnectedListener;
    private QuestionListAdapter mChatListAdapter;

    private DBUtil dbutil;

    public DBUtil getDbutil() {
        return dbutil;
    }

    private String[] category = new String[] {"No Category", "Final", "Midterm", "Assignment", "Others"};
    private int categoryChoice;

    public void setCategoryButtonText(int choice){
        Button categoryButton = (Button) findViewById(R.id.category);
        categoryButton.setText(category[choice]);
    }

    public void setCategoryChoice(int choice){
        categoryChoice = choice;
    }

    public int getCategoryChoice(){
        return categoryChoice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        //initialized once with an Android context.
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        assert (intent != null);

        // Make it a bit more reliable
        roomName = intent.getStringExtra(JoinActivity.ROOM_NAME);
        userEmail = intent.getStringExtra(JoinActivity.USER_EMAIL);

        if (roomName == null || roomName.length() == 0) {
            roomName = "all";
        }

        setTitle("Room name: " + roomName);

        // Setup our Firebase mFirebaseRef
        mChatroomRef =mFirebaseRef.child("rooms").child(roomName).child("questions");

        // Setup our input methods. Enter key on the keyboard or pushing the send button
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        /*findViewById(R.id.category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View AddCategoryLayout = inflater.inflate(R.layout.add_category_layout, null);
                final TextView categoryContent = (TextView) AddCategoryLayout.findViewById(R.id.category);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(AddCategoryLayout).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });*/

        findViewById(R.id.category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Category");
                builder.setItems(category, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCategoryButtonText(which);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


        //load user profile
        if (userEmail != null)
            JoinActivity.loadPorfile(getFilesDir(),
                    (ImageView) findViewById(R.id.drawer_profileImage),
                    (TextView) findViewById(R.id.drawer_profileEmail),
                    userEmail,
                    findViewById(R.id.loading_icon));

        // get the DB Helper
        DBHelper mDbHelper = new DBHelper(this);
        dbutil = new DBUtil(mDbHelper);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        final ListView listView = getListView();
        // Tell our list adapter that we only want 200 messages at a time
        mChatListAdapter = new QuestionListAdapter(
                mChatroomRef.orderByChild("echo").limitToFirst(200),
                this, R.layout.question, this, mChatroomRef.getRoot().child("comment"));
        mChatListAdapter.setOnTouchListener(
                Generic.getAnimateColorListener(
                        getResources().getColor(R.color.key_up_color),
                        getResources().getColor(R.color.key_down_color)));
        listView.setAdapter(mChatListAdapter);

        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });

        // Finally, a little indication of connection status
        mConnectedListener = mChatroomRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(MainActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatroomRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mChatListAdapter.cleanup();
    }

    private void sendMessage() {
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            // Create our 'model', a Chat object
            Question question = new Question(userEmail, input);
            // Create a new, auto-generated child of that chat location, and save our chat data there
            Firebase pushRef = mChatroomRef.push();
            pushRef.setValue(question);
            String uniqueId = pushRef.getKey();
            inputText.setText("");

            (mChatroomRef.getParent().child("recentQuestion")).setValue(uniqueId);
            (mChatroomRef.getParent().child("activeTime")).setValue(new Date().getTime());


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        Log.d("main", "option menu created");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setTitle(roomName);

        if(userEmail != null)
            menu.findItem(R.id.action_addFavorite).setVisible(true);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color= #ffffff>Search a tag</font>"));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);

        LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.action_search);
        searchBar.setLayoutTransition(new LayoutTransition());
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()){
            case R.id.action_camera:
                Intent cameraIntent = new Intent(this, CameraViewActivity.class);
                cameraIntent.putExtra("action", "takePicture");
                startActivity(cameraIntent);
                break;
            case R.id.action_addFavorite:
                addToFavorite();
                break;
        }

        return true;
    }

    public void updateEcho(String key) {
        if (dbutil.contains(key)) {
            Log.e("Dupkey", "Key is already in the DB!");
            return;
        }

        final Firebase echoRef = mChatroomRef.child(key).child("echo");
        echoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long echoValue = (Long) dataSnapshot.getValue();
                        Log.e("Echo update:", "" + echoValue);

                        echoRef.setValue(echoValue + 1);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                }
        );

        final Firebase orderRef = mChatroomRef.child(key).child("order");
        orderRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long orderValue = (Long) dataSnapshot.getValue();
                        Log.e("Order update:", "" + orderValue);

                        orderRef.setValue(orderValue - 1);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                }
        );

        // Update SQLite DB
        dbutil.put(key);
    }

    public void updateDislike(String dislikeKey) {
        if (dbutil.contains(dislikeKey)) {
            Log.e("Dupkey", "Key is already in the DB!");
            return;
        }

        final Firebase dislikeRef = mChatroomRef.child(dislikeKey).child("dislike");
        dislikeRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long dislikeValue = (Long) dataSnapshot.getValue();
                        Log.e("Dislike update:", "" + dislikeValue);

                        dislikeRef.setValue(dislikeValue + 1);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                }
        );

        // Update SQLite DB
        dbutil.put(dislikeKey);
    }

    public void addToFavorite() {
        mFirebaseRef.child("user").child(parsePath(userEmail)).child("favorite").push().setValue(roomName);
    }

    public String parsePath (String email) {
        return email.replaceAll(".com", "");
    }

    public void Close(View view) {
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        final ListView listView = getListView();
        if (TextUtils.isEmpty(newText)) {
            mChatListAdapter.finishSearch();
        }
        else {
            mChatListAdapter.doSearch(newText);

        }


        return false;
    }
}
