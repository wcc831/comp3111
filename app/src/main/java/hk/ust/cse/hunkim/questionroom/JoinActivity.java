package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoomListAdapter;
import hk.ust.cse.hunkim.questionroom.login.GoogleLogin;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

/**
 * A login screen that offers login via email/password.
 */
public class JoinActivity extends Activity implements SearchView.OnQueryTextListener{
    public static final String ROOM_NAME = "Room_name";
    public static Firebase firebaseRef;
    public static Firebase chatroomRef;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    private String mEmail = null;
    private MenuItem selectedItem = null;

    private final List<ChatRoom> chatRoomList = new ArrayList<>();
    private ChatRoomListAdapter roomAdapter;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private TextView roomNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        setContentView(R.layout.activity_join);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())){
            Log.d("joinActivity", "received search action");
            handlSearch(getIntent().getStringExtra(SearchManager.QUERY));
            return;
        }

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://ccwfirebase.firebaseio.com/");
        chatroomRef = firebaseRef.child("chatroom");

        /*
        // Set up join chatroom.
        roomNameView = (TextView) findViewById(R.id.room_name);
        roomNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d("onEditorAcito", "called");
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("onEditorAcito", "attemptJoin");
                    attemptJoin(textView);
                }
                return true;
            }
        });
        */
        //set up most recnet active chatroom

        roomAdapter = new ChatRoomListAdapter(chatroomRef.orderByChild("activeTime").limitToFirst(10), this, chatRoomList);
        ((ListView) findViewById(R.id.recent_chatRoom)).setAdapter(roomAdapter);

        //setup drawer
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.join_mainLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.leftMenu_open, R.string.leftMenu_close){
            @Override
            public void onDrawerClosed(View view){

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View view){
                Log.d("main", "onDrawerOpened");
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setTitle("InstaQuest");


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color= #ffffff>Type a chat room.</font>"));
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
            case R.id.action_login:
                break;
            case R.id.action_camera:
                Intent cameraIntent = new Intent(this, CameraViewActivity.class);
                startActivity(cameraIntent);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //request google account
        if(requestCode == REQUEST_CODE_PICK_ACCOUNT){
            if (resultCode == RESULT_OK){
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleLogin(mEmail, selectedItem);
                Log.d("google auth", mEmail);
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(JoinActivity.this, "canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void pickUserAccount(MenuItem item) {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    public void googleLogin(String email, final MenuItem item){
        UserInfo userInfo = new UserInfo();
        GoogleLogin login = new GoogleLogin(JoinActivity.this, chatroomRef, email, userInfo);
        login.ExceptionCallback = new GoogleLogin.ExceptionHandler() {
            @Override
            public void handleException(final UserRecoverableAuthException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof GooglePlayServicesAvailabilityException) {
                            int statusCode = ((GooglePlayServicesAvailabilityException) e)
                                    .getConnectionStatusCode();
                            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                                    JoinActivity.this,
                                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                            dialog.show();
                        } else if (e instanceof UserRecoverableAuthException) {
                            Intent intent = ((UserRecoverableAuthException) e).getIntent();
                            startActivityForResult(intent,
                                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                        }
                    }
                });
            }
        };
        login.LoginCallback = new GoogleLogin.GoogleLoginCallback() {
            @Override
            public void onLoginSucceed(AuthData userData, String token) {
                item.setVisible(false);
            }

            @Override
            public void onLoginFailed(FirebaseError error) {

            }

            @Override
            public void onPictureReady() {
                loadPorfile();
            }
        };
        login.execute();
    }
    /*
    * set account profile to leftMenu
    * */
    public void loadPorfile(){
        ImageView profileImage = (ImageView) findViewById(R.id.drawer_profileImage);
        Bitmap bitmap = BitmapFactory.decodeFile(new File(getFilesDir(), "google/googleProfile.jpg").toString());
        profileImage.setImageBitmap(bitmap);

        TextView email = (TextView) findViewById(R.id.drawer_profileEmail);
        email.setText(mEmail);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            ((TextView) findViewById(R.id.join_chatroom)).setText("");
            ((ListView) findViewById(R.id.recent_chatRoom)).setAdapter(roomAdapter);

        } else {

            TextView textView = (TextView) findViewById(R.id.join_chatroom);
            textView.setText("Join " + newText);
            textView.setTextSize(20);
            //ArrayAdapter adapter = new ArrayAdapter(this, R.layout.question);

            List<ChatRoom> chatroomSearchResultList = new ArrayList<>();
            ChatRoomListAdapter adapter = new ChatRoomListAdapter(this, chatroomSearchResultList);
            ((ListView) findViewById(R.id.recent_chatRoom)).setAdapter(adapter);

            searchChatroom(newText, chatroomSearchResultList, adapter);

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /*
    * search request will be handled by this function
    * */

    public void handlSearch(String query){
        TextView joinChatroom = (TextView) findViewById(R.id.join_chatroom);
        joinChatroom.setText("Join " + query);
    }

    private List<ChatRoom> searchChatroom(final String queryStr, final List<ChatRoom> chatroomList, final ArrayAdapter chatroomAdapter){

        chatroomRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String roomName = dataSnapshot.getKey();

                Log.d("search result", roomName);

                if (roomName.contains(queryStr)) {
                    try {
                        String latestQuestionId = dataSnapshot.child("recentQuestion").getValue().toString();
                        String latestQuestion = dataSnapshot.child("questions").child(latestQuestionId).child("head").getValue().toString();
                        String activeTime = dataSnapshot.child("questions").child(latestQuestionId).child("timestamp").getValue().toString();

                        chatroomList.add(0, new ChatRoom(roomName,
                                latestQuestion,
                                Long.parseLong(activeTime)));

                        chatroomAdapter.notifyDataSetChanged();
                    }
                    catch (NullPointerException npe){
                        return;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {         }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {            }
        });

        return chatroomList;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptJoin(View view) {
        // Reset errors.
        //roomNameView.setError(null);

        // Store values at the time of the login attempt.

        String room_name = null;
        if (view.getId() == R.id.index_chatRoomLayout){
            room_name = (String) ((TextView) view.findViewById(R.id.index_chatRoom)).getText();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(ROOM_NAME, room_name);
            startActivity(intent);

            return;
        }
        else if (view.getId() == R.id.join_chatroom){
            room_name = ((TextView)view).getText().toString().substring(5);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(ROOM_NAME, room_name);

            SearchView searchView = (SearchView) findViewById(R.id.action_search);
            searchView.setIconified(true);

            startActivity(intent);

            return;
        }


        room_name = roomNameView.getText().toString();
        boolean cancel = false;

        // Check for a valid email address.
        if (TextUtils.isEmpty(room_name)) {
            roomNameView.setError(getString(R.string.error_field_required));

            cancel = true;
        } else if (!isEmailValid(room_name)) {
            roomNameView.setError(getString(R.string.error_invalid_room_name));
            cancel = true;
        }

        if (cancel) {
            roomNameView.setText("");
            roomNameView.requestFocus();
        } else {
            // Start main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(ROOM_NAME, room_name);
            startActivity(intent);
        }
    }

    private boolean isEmailValid(String room_name) {
        // http://stackoverflow.com/questions/8248277
        // Make sure alphanumeric characters
        return !room_name.matches("^.*[^a-zA-Z0-9 ].*$");
    }
}

