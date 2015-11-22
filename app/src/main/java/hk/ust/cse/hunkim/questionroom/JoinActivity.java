package hk.ust.cse.hunkim.questionroom;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoom;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatRoomListAdapter;
import hk.ust.cse.hunkim.questionroom.chatroom.ChatroomPagerAdapter;
import hk.ust.cse.hunkim.questionroom.login.UserInfo;

/**
 * A login screen that offers login via email/password.
 */
public class JoinActivity extends FragmentActivity implements SearchView.OnQueryTextListener{
    public static final String ROOM_NAME = "Room_name";
    public static Firebase firebaseRef;
    public static Firebase chatroomRef;

    private UserInfo user = UserInfo.getInstance();
    private final List<ChatRoom> recentList = new ArrayList<>();
    private final List<ChatRoom> favoriteList = new ArrayList<>();
    private ChatRoomListAdapter searchAdapter;

    ChatroomPagerAdapter chatroomPagerAdapter;
    ViewPager chatroomListPager;
    PagerTabStrip chatroomListTabStrip;
    final Context context = this;
    final ListView[] chatListViews = {null, null, null, null};
    File root;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            logout(null);
        }

        //set status bar color
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.action_bar_red));

        setContentView(R.layout.activity_join);

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(MainActivity.FIREBASE_URL);
        chatroomRef = firebaseRef.child("rooms");

        root = new File(getFilesDir(), "instaquest");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        try {
            loadProfile((ImageView) findViewById(R.id.drawer_profileImage),
                    (TextView) findViewById(R.id.drawer_profileEmail),
                    (TextView) findViewById(R.id.drawer_userRole));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (user.hideMessage){
            //set on off button
            ((ImageView)findViewById(R.id.hide_message)).
                    setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.on));
        }
        else{
            ((ImageView)findViewById(R.id.hide_message)).
                    setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.off));
        }

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
    protected void onResume (){
        super.onResume();

        chatroomPagerAdapter = new ChatroomPagerAdapter(getSupportFragmentManager());

        chatroomPagerAdapter.tabs = getPagerFragments(chatListViews, context);
        chatroomListPager = ((ViewPager) findViewById(R.id.chatroom_list_pager));
        chatroomListPager.setAdapter(chatroomPagerAdapter);
        chatroomListTabStrip = ((PagerTabStrip) findViewById(R.id.chatroom_list_tab_strip));
        chatroomListTabStrip.setTextColor(Color.WHITE);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public  void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_join, menu);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("InstaQuest");

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color= #ffffff>Type a chat room.</font>"));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        EditText editText = (EditText)searchView.findViewById(getResources().getIdentifier("android:id/search_src_text", null, null));
        editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        LinearLayout searchBar = (LinearLayout) searchView.findViewById(R.id.action_search);
        searchBar.setLayoutTransition(new LayoutTransition());
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        if (user.isAuthenticated()) {
            ImageView imageView = (ImageView) findViewById(R.id.drawer_logout);
            imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logout));
            //imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        }

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public Fragment[] getPagerFragments(final ListView[] chatListViews, final Context context){
        Fragment[] fragments = new Fragment[3];
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

                if (!UserInfo.getInstance().isAuthenticated()){
                    TextView notLogin = new TextView(context);
                    notLogin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    notLogin.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    notLogin.setTextSize(18);
                    notLogin.setText("Please Login First");
                    return notLogin;
                }

                if (chatListViews[1] == null) {
                    ChatRoomListAdapter adapter = new ChatRoomListAdapter(
                            firebaseRef.child("userRecord").child(user.id).child("favorite").orderByValue(),
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
        //setup search result fragment
        fragments[2] = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

                searchAdapter = new ChatRoomListAdapter(context, firebaseRef, new ArrayList<ChatRoom>());
                searchAdapter.setOnTouchListener(
                        Generic.getAnimateColorListener(
                                getResources().getColor(R.color.key_up_color),
                                getResources().getColor(R.color.key_down_color)));

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
    public static void loadProfile(ImageView profileImage, TextView email, TextView userRole){

        UserInfo user = UserInfo.getInstance();

        if (user.profileImage != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(user.profileImage.toString());
            profileImage.setImageDrawable(new RoundImage(bitmap));
        }
        if (user.email != null) {
            email.setText(user.email);
        }
        if (user.role != -1) {
            userRole.setText(user.getRole());
        }

    }

    /*
    * Search event listener
    * data input to search field will be handled here
    * */
    @Override
    public boolean onQueryTextChange(String newText) {

        if (TextUtils.isEmpty(newText)) {
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
        attemptJoin(findViewById(R.id.join_chatroom));

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

        //join chatroom through list
        if (view.getId() == R.id.index_chatRoomLayout){
            room_name = (String) ((TextView) view.findViewById(R.id.index_chatRoom)).getText();

        }
        //join chatroom through search
        else /*if (view.getId() == R.id.join_chatroom)*/{

            room_name = ((TextView)view).getText().toString().substring(5);

            SearchView searchView = (SearchView) findViewById(R.id.action_search);
            searchView.setIconified(true);
        }

        if (room_name.contains(" ")){
            Toast.makeText(this, "room name contain illegal characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra(ROOM_NAME, room_name);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    /*public void refresh(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Category");
        builder.show();
    }*/

    public boolean refresh(MenuItem item){
        Toast.makeText(this, "refresh!", Toast.LENGTH_LONG).show();
        onPause();
        onResume();
        return false;
    }

    public void changeHideMessage(View view) {
        user.hideMessage = !user.hideMessage;
        if(user.hideMessage)
            ((ImageView)view).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.on));
        else
            ((ImageView)view).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.off));
    }

    public void setUser(boolean b) {
        user.hideMessage = b;
    }

    public void setUser() {


        user.email = " ";
        user.role = 1;
        user.profileImage = new File(root, "visitor.jpg");
    }

    public void removeAdapter() {
        this.searchAdapter = null;
    }

    public void startCamera(MenuItem item) {
        Intent cameraIntent = new Intent(this, CameraViewActivity.class);
        startActivity(cameraIntent);
    }

    public void logout(View view) {
        user.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }
}

