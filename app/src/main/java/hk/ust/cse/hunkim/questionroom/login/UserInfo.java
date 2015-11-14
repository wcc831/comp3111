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

    public static final int TA = 4;
    public static final int PROFESSOR = 5;

    public String id;
    public String name;
    public String pictureUrl;
    public String email;
    public File profileImage;
    public int role;

    private static UserInfo userInfo = new UserInfo();
    private boolean authenticated = false;

    private UserInfo() {}

    public static UserInfo getInstance() { return userInfo; }

    public UserInfo fromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        pictureUrl = obj.has("picture") ? obj.getString("picture") : null;
        name = (obj.has("name")) ? obj.getString("name") : null;

        return this;
    }

    public String toString() {
        return "user name: " + name + "\r\n picture: " + pictureUrl;
    }

    public void authenticate() {
        authenticated = true;
    }

    public boolean isAuthenticated() { return authenticated; }
}
