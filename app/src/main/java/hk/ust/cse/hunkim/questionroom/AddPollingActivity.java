package hk.ust.cse.hunkim.questionroom;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.Firebase;

import hk.ust.cse.hunkim.questionroom.polling.Polling;

/**
 * Created by onzzz on 20/11/2015.
 */
public class AddPollingActivity extends ListActivity {

    public static final String FIREBASE_URL = "https://instaquest.firebaseio.com/";


    private String roomName;
    private Firebase mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.add_polling);

        Intent intent = getIntent();
        assert (intent != null);

        roomName = intent.getStringExtra(PollingActivity.ROOM_NAME);
        if (roomName == null || roomName.length() == 0) {
            roomName = "all";
        }

        mFirebaseRef = new Firebase(FIREBASE_URL).child("rooms").child(roomName).child("polling");

        findViewById(R.id.send_polling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pollingTitleInputText = (EditText) findViewById(R.id.poll_title_input);
                String pollingTitleInput = pollingTitleInputText.getText().toString();

                String option[] = new String[10];
                EditText pollingAnswerInputText1 = (EditText) findViewById(R.id.poll_answer_input1);
                String pollingAnswerInput1 = pollingAnswerInputText1.getText().toString();
                option[0] = pollingAnswerInput1;
                EditText pollingAnswerInputText2 = (EditText) findViewById(R.id.poll_answer_input2);
                String pollingAnswerInput2 = pollingAnswerInputText2.getText().toString();
                option[1] = pollingAnswerInput2;
                EditText pollingAnswerInputText3 = (EditText) findViewById(R.id.poll_answer_input3);
                String pollingAnswerInput3 = pollingAnswerInputText3.getText().toString();
                option[2] = pollingAnswerInput3;
                EditText pollingAnswerInputText4 = (EditText) findViewById(R.id.poll_answer_input4);
                String pollingAnswerInput4 = pollingAnswerInputText4.getText().toString();
                option[3] = pollingAnswerInput4;
                EditText pollingAnswerInputText5 = (EditText) findViewById(R.id.poll_answer_input5);
                String pollingAnswerInput5 = pollingAnswerInputText5.getText().toString();
                option[4] = pollingAnswerInput5;
                EditText pollingAnswerInputText6 = (EditText) findViewById(R.id.poll_answer_input6);
                String pollingAnswerInput6 = pollingAnswerInputText6.getText().toString();
                option[5] = pollingAnswerInput6;
                EditText pollingAnswerInputText7 = (EditText) findViewById(R.id.poll_answer_input7);
                String pollingAnswerInput7 = pollingAnswerInputText7.getText().toString();
                option[6] = pollingAnswerInput7;
                EditText pollingAnswerInputText8 = (EditText) findViewById(R.id.poll_answer_input8);
                String pollingAnswerInput8 = pollingAnswerInputText8.getText().toString();
                option[7] = pollingAnswerInput8;
                EditText pollingAnswerInputText9 = (EditText) findViewById(R.id.poll_answer_input9);
                String pollingAnswerInput9 = pollingAnswerInputText9.getText().toString();
                option[8] = pollingAnswerInput9;
                EditText pollingAnswerInputText10 = (EditText) findViewById(R.id.poll_answer_input10);
                String pollingAnswerInput10 = pollingAnswerInputText10.getText().toString();
                option[9] = pollingAnswerInput10;

                int numOfOption = 0;

                for (int i=0; i<10; i++){
                    if (!option[i].equals(""))
                        numOfOption++;
                }

                if (!pollingTitleInput.equals("") && numOfOption!=0) {
                    // Create our 'model', a Chat object
                    Polling polling = new Polling(pollingTitleInput, option, numOfOption);
                    // Create a new, auto-generated child of that chat location, and save our chat data there
                    mFirebaseRef.push().setValue(polling);
                }

                pollingTitleInputText.setText("");
                pollingAnswerInputText1.setText("");
                pollingAnswerInputText2.setText("");
                pollingAnswerInputText3.setText("");
                pollingAnswerInputText4.setText("");
                pollingAnswerInputText5.setText("");
                pollingAnswerInputText6.setText("");
                pollingAnswerInputText7.setText("");
                pollingAnswerInputText8.setText("");
                pollingAnswerInputText9.setText("");
                pollingAnswerInputText10.setText("");
            }
        });
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
