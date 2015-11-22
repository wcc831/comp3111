package hk.ust.cse.hunkim.questionroom.polling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.Collections;
import java.util.List;

import hk.ust.cse.hunkim.questionroom.FirebaseListAdapter;
import hk.ust.cse.hunkim.questionroom.MainActivity;
import hk.ust.cse.hunkim.questionroom.PollingActivity;
import hk.ust.cse.hunkim.questionroom.R;
import hk.ust.cse.hunkim.questionroom.db.DBUtil;

/**
 * Created by onzzz on 22/11/2015.
 */


public class PollingListAdapter extends FirebaseListAdapter<Polling> {
    private String roomName;
    PollingActivity activity;
    Context context;
    Query query;
    int layout;
    Firebase pollingRef;

    Firebase fireRef;
    Firebase mAddVote;

    List<Polling> polling;
    List<Polling> tmpPollingList;

    public PollingListAdapter(Query ref, Activity activity, int layout, Context context, Firebase pollingRef){
        super(ref, Polling.class, layout, activity);

        assert (activity instanceof MainActivity);

        this.query = ref;
        this.activity = (PollingActivity) activity;
        this.context = context;
        this.layout = layout;
        this.pollingRef = pollingRef;

        this.polling = getModels();
    }

    @Override
    protected void populateView(View view, final Polling polling){

        final String pollTitle = polling.getName();
        ((TextView) view.findViewById(R.id.pollTitle)).setText(pollTitle);

        ((Button) view.findViewById(R.id.pollOption1)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption2)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption3)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption4)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption5)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption6)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption7)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption8)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption9)).setVisibility(View.GONE);
        ((Button) view.findViewById(R.id.pollOption10)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote1)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote2)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote3)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote4)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote5)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote6)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote7)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote8)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote9)).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.pollVote10)).setVisibility(View.GONE);


        String pollOption[][] = polling.getOptions();
        int numOfOption = 0;
        if (numOfOption < pollOption.length){
            ((Button) view.findViewById(R.id.pollOption1)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption1)).setText(pollOption[0][0]);
            ((TextView) view.findViewById(R.id.pollVote1)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote1)).setText(pollOption[0][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length){
            ((Button) view.findViewById(R.id.pollOption2)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption2)).setText(pollOption[1][0]);
            ((TextView) view.findViewById(R.id.pollVote2)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote2)).setText(pollOption[1][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption3)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption3)).setText(pollOption[2][0]);
            ((TextView) view.findViewById(R.id.pollVote3)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote3)).setText(pollOption[2][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption4)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption4)).setText(pollOption[3][0]);
            ((TextView) view.findViewById(R.id.pollVote4)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote4)).setText(pollOption[3][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption5)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption5)).setText(pollOption[4][0]);
            ((TextView) view.findViewById(R.id.pollVote5)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote5)).setText(pollOption[4][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption6)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption6)).setText(pollOption[5][0]);
            ((TextView) view.findViewById(R.id.pollVote6)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote6)).setText(pollOption[5][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption7)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption7)).setText(pollOption[6][0]);
            ((TextView) view.findViewById(R.id.pollVote7)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote7)).setText(pollOption[6][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption8)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption8)).setText(pollOption[7][0]);
            ((TextView) view.findViewById(R.id.pollVote8)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote8)).setText(pollOption[7][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption9)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption9)).setText(pollOption[8][0]);
            ((TextView) view.findViewById(R.id.pollVote9)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote9)).setText(pollOption[8][1]);
        }
        numOfOption++;
        if (numOfOption < pollOption.length) {
            ((Button) view.findViewById(R.id.pollOption10)).setVisibility(View.VISIBLE);
            ((Button) view.findViewById(R.id.pollOption10)).setText(pollOption[9][0]);
            ((TextView) view.findViewById(R.id.pollVote10)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.pollVote10)).setText(pollOption[9][1]);
        }
        numOfOption++;

        ((Button) view.findViewById(R.id.pollOption1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(0));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("0").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption1).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(1));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("1").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption2).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(2));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("2").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption3).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(3));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("3").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption4).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption5)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(4));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("4").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption5).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption6)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(5));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("5").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption6).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption7)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(6));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("6").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption7).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption8)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(7));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("7").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption8).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption9)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(8));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("8").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption9).setEnabled(false);
            }
        });
        ((Button) view.findViewById(R.id.pollOption10)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int intVote = Integer.parseInt(polling.getVote(9));
                String stringVote = Integer.toString(++intVote);
                pollingRef.child(polling.getKey()).child("options").child("9").child("1").setValue(stringVote);
                view.findViewById(R.id.pollOption10).setEnabled(false);
            }
        });

        long pollTime = polling.getTimestamp();
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context, pollTime, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        ((TextView) view.findViewById(R.id.pollTime)).setText(relativeTime);
    }

    @Override
    protected void sortModels(List<Polling> mModels) {
        Collections.sort(mModels);
    }
    @Override
    protected void setKey(String key, Polling model) {
        model.setKey(key);
    }
}
