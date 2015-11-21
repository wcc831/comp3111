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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
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
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import hk.ust.cse.hunkim.questionroom.db.DBHelper;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;
import hk.ust.cse.hunkim.questionroom.question.Question;
import hk.ust.cse.hunkim.questionroom.question.QuestionListAdapter;

public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {
    public static final String ROOM_NAME = "Room_name";

    // TODO: change this to your own Firebase URL
    public static final String FIREBASE_URL = "https://instaquest.firebaseio.com/";
    //public static final String FIREBASE_URL = "https://andyfire.firebaseio.com/";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private UserInfo user = UserInfo.getInstance();

    private String roomName;
    private Firebase mFirebaseRef;
    private Firebase mChatroomRef;
    private ValueEventListener mConnectedListener;
    private QuestionListAdapter mChatListAdapter;

    private String presenceId;

    private DBUtil dbutil;

    public DBUtil getDbutil() {
        return dbutil;
    }

    private String[] category = new String[] {"Midterm", "Final", "Assignment", "Other"};
    private String categoryChoice;

    public void setCategoryButtonText(int choice){
        Button categoryButton = (Button) findViewById(R.id.categoryButton);
        categoryButton.setText(category[choice]);
    }

    public void setCategoryChoice(int choice){
        categoryChoice = category[choice];
    }

    public String getCategoryChoice(){
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
        mFirebaseRef = new Firebase(FIREBASE_URL);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        assert (intent != null);

        roomName = intent.getStringExtra(JoinActivity.ROOM_NAME);
        //if (roomName == null || roomName.length() == 0) {
        if (TextUtils.isEmpty(roomName)){
            roomName = "all";
        }

        setTitle("Room name: " + roomName);

        // Setup our Firebase mFirebaseRef
        mChatroomRef =mFirebaseRef.child("rooms").child(roomName).child("questions");

        if (user.hideMessage){
            //set on off button
            ((ImageView)findViewById(R.id.hide_message)).
                    setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.on));
        }
        else{
            ((ImageView)findViewById(R.id.hide_message)).
                    setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.off));
        }



        // Setup our input methods. Enter key on the keyboard or pushing the send button
        /*
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage(null);
                }
                return true;
            }
        });
        */
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(null);
            }
        });

        findViewById(R.id.pollButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent();
                intent.putExtra(ROOM_NAME, roomName);
                intent.setClass(MainActivity.this, PollingActivity.class);
                startActivity(intent);
            }
        });





        //record users in chatroom
        Firebase pushPresence = mChatroomRef.getParent().child("presence").push();
        pushPresence.setValue(true);
        presenceId = pushPresence.getKey();

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

        findViewById(R.id.categoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Category");
                builder.setItems(category, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCategoryChoice(which);
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
        try {
            JoinActivity.loadProfile((ImageView) findViewById(R.id.drawer_profileImage),
                    (TextView) findViewById(R.id.drawer_profileEmail),
                    (TextView) findViewById(R.id.drawer_userRole));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // get the DB Helper
        DBHelper mDbHelper = new DBHelper(this);
        dbutil = new DBUtil(mDbHelper);

        countOnlineUser();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        final ListView listView = getListView();
        // Tell our list adapter that we only want 200 messages at a time
        mChatListAdapter = new QuestionListAdapter(
                mChatroomRef.orderByChild("echo").limitToFirst(200),
                this, R.layout.question, this, mChatroomRef.getParent().child("comment"));
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

        //remove presence id
        mChatroomRef.getParent().child("presence").child(presenceId).removeValue();
    }

    private void sendMessage(Bitmap photo) {
        EditText inputText = (EditText) findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {


            // Create our 'model', a Chat object
            Question question = new Question(user.email, input, categoryChoice);

            if (photo != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();

                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                encodedImage = "data:image/jpg;base64," + encodedImage;
                Log.d("attachment", encodedImage);
                question.setAttachment(encodedImage);
            }


            // Create a new, auto-generated child of that chat location, and save our chat data there

            if (user.getInstance().role == UserInfo.SUPERVISOR) {    question.setHighlight(2);}
            else { question.setHighlight(0);}

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
        inflater.inflate(R.menu.action_bar_main, menu);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setTitle(roomName);

        if(user.isAuthenticated())
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

        setCategoryChoice(3);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            sendMessage(imageBitmap);
            //ImageView mImageView = (ImageView) findViewById(R.id.cameraImageView);
            //mImageView.setImageBitmap(imageBitmap);


        }
    }

    public void updateEcho(String key) {
        if (dbutil.contains(key)) {
            Log.e("Dupkey", "Key is already in the DB!");
            return;
        }

        final Firebase echoRef = mChatroomRef.child(key).child("like");
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

    public void addToFavorite(MenuItem item) {
        mFirebaseRef.child("userRecord").child(user.id).child("favorite").push().setValue(roomName);

    }

    public void Close(View view) {
        finish();
    }

    public void countOnlineUser(){
        mChatroomRef.getParent().child("presence").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int numUser = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    numUser++;
                }

                TextView textView = new TextView(MainActivity.this);
                textView.setId(R.id.drawer_onlineUsers);
                textView.setText("online users: " + Integer.toString(numUser));
                textView.setPadding(5, 5, 5, 5);

                LinearLayout linearLayout = ((LinearLayout) findViewById(R.id.drawer_menu));
                if (linearLayout.findViewById(R.id.drawer_onlineUsers) == null)
                    linearLayout.addView(textView);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(final String newText) {

        final ListView listView = getListView();
        if (TextUtils.isEmpty(newText)) {
            mChatListAdapter.finishSearch();
        }
        else {
            mChatListAdapter.doSearch(newText);

            //mChatListAdapter.getFilter().filter(newText);
        }


        return false;
    }

    public void login(View v){

    }

    public boolean refresh(MenuItem item){
        Toast.makeText(this, "refresh!", Toast.LENGTH_LONG).show();
        onStop();
        onStart();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public void changeHideMessage(View view) {
        user.hideMessage = !user.hideMessage;
        if(user.hideMessage) {
            ((ImageView) view).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.on));

        }
        else {
            ((ImageView) view).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.off));

        }

        mChatListAdapter.refersh();

    }

    public void startCamera(MenuItem item) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void setUser(boolean b) {
        user.hideMessage = b;
    }

}
