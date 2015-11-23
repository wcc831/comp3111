package hk.ust.cse.hunkim.questionroom.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by cc on 9/26/2015.
 */
public class UserInfo {

    public static final int SUPERVISOR = 0;
    public static final int NORMAL_USER = 1;

    public static final String[] roles = {"Supervisor", "Student"};


    public String id;
    public String name;
    public String pictureUrl;
    public String email;
    public File profileImage;
    public int role = -1;
    public boolean hideMessage = false;
    public boolean hideBadword = true;

    private static UserInfo userInfo = new UserInfo();
    private boolean authenticated = false;

    private UserInfo() {}

    public static UserInfo getInstance() { return userInfo; }

    public UserInfo fromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        pictureUrl = obj.has("picture") ? obj.getString("picture") : null;
        name = (obj.has("name")) ? obj.getString("name") : null;
        id = (obj.has("id")) ? obj.getString("id") : null;
        /*
        email = (obj.has("email")) ? obj.getString("email") : null;
        role = (obj.has("role")) ? Integer.parseInt(obj.getString("role")) : -1;
        hideMessage = (obj.has("hideMessage")) ? true : false;
        */

        return this;
    }

    public String toString() {
        return "user name: " + name + "\r\n picture: " + pictureUrl + "\r\n id: " + id;
    }

    public void authenticate() {
        authenticated = true;
    }

    public boolean isAuthenticated() { return authenticated; }

    public String getRole() { return roles[role]; }

    public void logout() {
        authenticated = false;
        role = -1;
        pictureUrl = null;
        id = null;
        email = null;
        profileImage = null;
        hideMessage = false;
        hideBadword = false;
    }
}
