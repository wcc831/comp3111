package hk.ust.cse.hunkim.questionroom.login;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc on 9/26/2015.
 */
public class UserInfo {
    public int id;
    public String name;
    public String pictureUrl;
    public boolean infoReady = false;

    public UserInfo fromJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        pictureUrl = obj.has("picture") ? obj.getString("picture") : null;
        name = (obj.has("name")) ? obj.getString("name") : null;

        return this;
    }

    public String toString() {
        return "user name: " + name + "\r\n picture: " + pictureUrl;
    }
}
