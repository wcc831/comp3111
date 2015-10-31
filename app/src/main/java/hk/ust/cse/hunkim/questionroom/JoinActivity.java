package hk.ust.cse.hunkim.questionroom;

import android.accounts.AccountManager;
import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoomListAdapter;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatroomPagerAdapter;
import hk.ust.cse.hunkim.questionroom.login.GoogleLogin;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

/**
 * A login screen that offers login via email/password.
 */
public class JoinActivity extends FragmentActivity implements SearchView.OnQueryTextListener{
    public static final String ROOM_NAME = "Room_name";
    public static final String USER_EMAIL = "user_email";
    public static Firebase firebaseRef;
    public static Firebase chatroomRef;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    private String userEmail = null;
    private final List<ChatRoom> recentList = new ArrayList<>();
    private final List<ChatRoom> favoriteList = new ArrayList<>();
    private final List<ChatRoom> historyList = new ArrayList<>();
    private ChatRoomListAdapter searchAdapter;

    ViewPager chatroomListPager;
    PagerTabStrip chatroomListTabStrip;
    final Context context = this;
    final ListView[] chatListViews = {null, null, null, null};
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

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://ccwfirebase.firebaseio.com/");
        chatroomRef = firebaseRef.child("rooms");

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

        ChatroomPagerAdapter chatroomPagerAdapter = new ChatroomPagerAdapter(getSupportFragmentManager());

        chatroomPagerAdapter.tabs = getPagerFragments(chatListViews, context);
        chatroomListPager = (ViewPager) findViewById(R.id.chatroom_list_pager);
        chatroomListPager.setAdapter(chatroomPagerAdapter);
        chatroomListTabStrip = (PagerTabStrip) findViewById(R.id.chatroom_list_tab_strip);
        chatroomListTabStrip.setTextColor(Color.WHITE);

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

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color= #ffffff>Type a chat room.</font>"));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        EditText editText = (EditText)searchView.findViewById(getResources().getIdentifier("android:id/search_src_text", null, null));
        editText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});


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
                userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                googleLogin(userEmail);
                Log.d("google auth", userEmail);
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(JoinActivity.this, "canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void login(View view) {

        if (userEmail != null) //logged in
            return;

        findViewById(R.id.loading_icon).setVisibility(View.VISIBLE);
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    public void googleLogin(String email){
        UserInfo userInfo = new UserInfo();
        GoogleLogin login = new GoogleLogin(JoinActivity.this, chatroomRef, email, userInfo);
        login.exceptionCallback = new GoogleLogin.ExceptionHandler() {
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
        login.loginCallback = new GoogleLogin.GoogleLoginCallback() {
            @Override
            public void onLoginSucceed(AuthData userData, String token) {

            }

            @Override
            public void onLoginFailed(FirebaseError error) {

            }

            @Override
            public void onPictureReady() {
                loadPorfile(getFilesDir(),
                        (ImageView) findViewById(R.id.drawer_profileImage),
                        (TextView)findViewById(R.id.drawer_profileEmail),
                        userEmail,
                        findViewById(R.id.loading_icon));
                //getPagerFragments(chatListViews, context);
            }
        };
        login.execute();
    }

    public Fragment[] getPagerFragments(final ListView[] chatListViews, final Context context){
        Fragment[] fragments = new Fragment[4];
        //recent active list fragment
        fragments[0] = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

                if (chatListViews[0] == null) {
                    ChatRoomListAdapter adapter = new ChatRoomListAdapter(
                            chatroomRef.orderByChild("activeTime").limitToFirst(10),
                            context,
                            recentList);
                    adapter.setOnTouchListener(
                            Generic.getAnimateColorListener(
                                    getResources().getColor(R.color.key_up_color),
                                    getResources().getColor(R.color.key_down_color)));
                    adapter.queryRecentList();
                    chatListViews[0] = new ListView(context);
                    chatListViews[0].setAdapter(adapter);

                }
                return chatListViews[0];
            }
        };
        //setup favorite list fragment
        fragments[1] = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

                if (userEmail == null){
                    TextView notLogin = new TextView(context);
                    notLogin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    notLogin.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    notLogin.setTextSize(18);
                    notLogin.setText("Please Login First");
                    return notLogin;
                }

                if (chatListViews[1] == null) {
                    ChatRoomListAdapter adapter = new ChatRoomListAdapter(
                            firebaseRef.child("user").child(userEmail.replaceAll(".com", "")).child("favorite").orderByValue(),
                            context,
                            favoriteList);
                    adapter.queryFavoriteList();
                    adapter.setOnTouchListener(
                            Generic.getAnimateColorListener(
                                    getResources().getColor(R.color.key_up_color),
                                    getResources().getColor(R.color.key_down_color)));
                    chatListViews[1] = new ListView(context);
                    chatListViews[1].setAdapter(adapter);
                }
                return chatListViews[1];
            }
        };
        //setup recently visited lsit fragment
        fragments[2] = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                if (userEmail == null){
                    TextView notLogin = new TextView(context);
                    notLogin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    notLogin.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    notLogin.setTextSize(18);
                    notLogin.setText("Please Login First");
                    return notLogin;
                }

                if (chatListViews[2] == null) {
                    ChatRoomListAdapter adapter = new ChatRoomListAdapter(firebaseRef.child("user").child(userEmail.replaceAll(".com", "")).child("history").orderByValue(),
                            context,
                            historyList);
                    adapter.queryFavoriteList();
                    chatListViews[2] = new ListView(context);
                    adapter.setOnTouchListener(
                            Generic.getAnimateColorListener(
                                    getResources().getColor(R.color.key_up_color),
                                    getResources().getColor(R.color.key_down_color)));
                    chatListViews[2].setAdapter(adapter);
                }
                return chatListViews[2];
            }
        };
        //setup search result fragment
        searchAdapter = new ChatRoomListAdapter(context, firebaseRef, new ArrayList<ChatRoom>());
        searchAdapter.setOnTouchListener(
                Generic.getAnimateColorListener(
                        getResources().getColor(R.color.key_up_color),
                        getResources().getColor(R.color.key_down_color)));
        fragments[3] = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                LinearLayout searchLayout = (LinearLayout)inflater.inflate(R.layout.activity_search_result, container, false);
                ((ListView) searchLayout.findViewById(R.id.chatroom_list)).setAdapter(searchAdapter);

                return searchLayout;
            }
        };
        return fragments;
    }

    /*
    * set account profile to leftMenu
    * */
    public static void loadPorfile(File dir, ImageView profileImage, TextView email, String userEmail, View loading){
        Bitmap bitmap = BitmapFactory.decodeFile(new File(dir, "google/googleProfile.jpg").toString());
        profileImage.setImageDrawable(new RoundImage(bitmap));

        email.setText(userEmail);
        loading.setVisibility(View.INVISIBLE);
    }

    /*
    * Search event listener
    * data input to search field will be handled here
    * */
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("query change", "triggered");

        if (TextUtils.isEmpty(newText) || newText.length() < 1) {
            if (findViewById(R.id.join_chatroom) == null)
                return false;

            ((TextView) findViewById(R.id.join_chatroom)).setText("");
            if (searchAdapter != null)
                searchAdapter.finishSearch();

        } else {
            chatroomListPager.setCurrentItem(3);
            TextView textView = (TextView) findViewById(R.id.join_chatroom);
            textView.setText("Join " + newText);
            textView.setTextSize(20);

            if (searchAdapter != null)
                searchAdapter.searchChatroom(newText);

        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptJoin(View view) {
        // Store values at the time of the login attempt.
        String room_name = "";
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(USER_EMAIL, userEmail);

        //join chatroom through list
        if (view.getId() == R.id.index_chatRoomLayout){
            room_name = (String) ((TextView) view.findViewById(R.id.index_chatRoom)).getText();

        }
        //join chatroom through search
        else if (view.getId() == R.id.join_chatroom){
            room_name = ((TextView)view).getText().toString().substring(5);

            SearchView searchView = (SearchView) findViewById(R.id.action_search);
            searchView.setIconified(true);

        }

        if (room_name.contains(" ")){
            Toast.makeText(this, "room name contain illegal characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail != null)
            firebaseRef.child("user").child(userEmail.replaceAll(".com", "")).child("history").push().setValue(room_name);

        intent.putExtra(ROOM_NAME, room_name);
        startActivity(intent);
    }

    private boolean isEmailValid(String room_name) {
        // http://stackoverflow.com/questions/8248277
        // Make sure alphanumeric characters
        return !room_name.matches("^.*[^a-zA-Z0-9 ].*$");
    }
}

