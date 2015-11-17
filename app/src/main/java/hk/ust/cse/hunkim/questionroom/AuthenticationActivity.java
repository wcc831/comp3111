package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import hk.ust.cse.hunkim.questionroom.animation.AnimationFactory;
import hk.ust.cse.hunkim.questionroom.server.ServerConfig;
import hk.ust.cse.hunkim.questionroom.server.ServerConnection;

public class AuthenticationActivity extends Activity {

    private static final String TAG = "AuthActicity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        String token = getIntent().getStringExtra("token");

        final ServerConfig config = new ServerConfig();
        try{
            config.auth(token);
            config.setServerResultCallBack(new ServerConfig.ServerResultCallBack() {
                @Override
                public void onResult(InputStream is, String result) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String auth = null;
                        if (obj.has("auth"))
                            auth = obj.getString("auth");

                        if (auth == null) {
                            //TODO something wrong, need to handle the error
                            return;
                        }

                        if (auth.equals("1")){
                            //auth succeed
                            AnimationFactory.crossFade(findViewById(R.id.auth_loading_container),
                                    findViewById(R.id.auth_authInfo), 400);

                        }
                        else{
                            //auth failed

                            Log.d(TAG, "auth failed");
                            ((ImageView) findViewById(R.id.auth_image)).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bad));
                            ((TextView) findViewById(R.id.auth_word)).setText("he is trying to trick you!!");

                            AnimationFactory.crossFade(findViewById(R.id.auth_loading_container),
                                    findViewById(R.id.auth_authInfo), 400);
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            ServerConnection con = new ServerConnection(config);
            con.execute();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
