package hk.ust.cse.hunkim.questionroom.barcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import hk.ust.cse.hunkim.questionroom.AuthenticationActivity;
import hk.ust.cse.hunkim.questionroom.CameraViewActivity;

/**
 * Created by cc on 10/8/2015.
 */
public class BarcodeScanner  extends Thread{
    public interface TrackingCallback {
        void track();
    }

    public TrackingCallback callback;
    private ImageScanner scanner;
    private byte[] bytedata = null;
    private String Tag = "onTracking";
    private int count = 0;
    private long time = 0;
    private int width, height;
    private static final String TAG = "CopyDemo:Log";
    Activity activity;

    {
        //markerDectection = new MarkerDectection();
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    public BarcodeScanner(Activity activity){
        this.activity = activity;
    }

    public void run () {
        Log.d(TAG, "start onTracking thread");
        try {
            while (true)
            {
                //Log.d(Tag,"loop start");
                getPic();
                while (bytedata==null) {
                    sleep(1000);
                    Log.d(Tag,"stop here");
                    getPic();
                }
                count++;

                Image barcode = new Image(width, height, "Y800");
                barcode.setData(bytedata);
                int result = scanner.scanImage(barcode);
                if (result != 0) {
                    SymbolSet syms = scanner.getResults();
                    for (final Symbol sym : syms) {
                        Log.d(Tag, "barcode result " + sym.getData());

                        Intent intent = new Intent(activity, AuthenticationActivity.class);
                        intent.putExtra("token", sym.getData());

                        activity.startActivity(intent);
                        activity.finish();
                        return;
                        /*activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, sym.getData(), Toast.LENGTH_SHORT).show();
                            }
                        });*/

                        //str = sym.getData();
                    }
                }
                else
                    //str = "";
                //Log.d("FPS","Tracking:"+count)
                if (count>60){
                    float now= SystemClock.elapsedRealtime()-time;
                    //Log.d(Tag,"Tracking:"+1000.0f/(now/count));
                    count = 0;
                    time = SystemClock.elapsedRealtime();
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    get data from the CameraView
    * */
    private void getPic() {
        // TODO Auto-generated method stub
        callback.track();
    }



    // here lets change it to the object
    // object (<---data) with specific data type


    public void setdata(byte[] bytedata, int width, int height) {
        // TODO Auto-generated method stub
        this.bytedata = bytedata;
        this.width = width;
        this.height = height;

    }
}
