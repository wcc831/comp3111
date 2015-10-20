package hk.ust.cse.hunkim.questionroom;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SearchResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d("searchActivity", "handle intent");
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            boolean cancel = false;

            // Check for a valid email address.
            if (TextUtils.isEmpty(query)) {
                //roomNameView.setError(getString(R.string.error_field_required));
                Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();

                cancel = true;
            } else if (!isEmailValid(query)) {
                //roomNameView.setError(getString(R.string.error_invalid_room_name));
                Toast.makeText(this, R.string.error_invalid_room_name, Toast.LENGTH_SHORT).show();
                cancel = true;
            }

            if (cancel) {
                //roomNameView.setText("");
                //roomNameView.requestFocus();
            } else {
                // Start main activity
                Intent searchRoomIntent = new Intent(this, MainActivity.class);
                intent.putExtra("Room_name", query);
                startActivity(intent);
            }
            //use the query to search your data somehow
        }
    }

    private boolean isEmailValid(String room_name) {
        // http://stackoverflow.com/questions/8248277
        // Make sure alphanumeric characters
        return !room_name.matches("^.*[^a-zA-Z0-9 ].*$");
    }
}
