package hk.ust.cse.hunkim.questionroom;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by cc on 10/3/2015.
 */
public class Generic {

    public static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String str = reader.readLine(), result = "";
        while(str != null){
            result += str;
            str = reader.readLine();
        }
        return result;
    }

    public static File saveInputStreamToFile(InputStream is, File targetDir){
        try {
            OutputStream os = new FileOutputStream(targetDir);
            byte[] buffer = new byte[1024 * 16];
            int read;
            while ((read = is.read(buffer)) != -1){
                os.write(buffer, 0, read);
            }
            os.flush();
            os.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return targetDir;
    }

    public static byte[] fileToByteArray (File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArr = new byte[(int) file.length()];
        fis.read(byteArr);
        return byteArr;
    }

    public static void animateColor(final View v, final int from, final int to){
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float position = animation.getAnimatedFraction();
                int blended = blendColors(from, to, position);
                v.setBackgroundColor(blended);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private static int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }

    public static View.OnTouchListener getAnimateColorListener(final int from, final int to) {
        return new View.OnTouchListener() {

            float x, y;

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x = event.getX();
                    y = event.getY();
                    animateColor(v, from, to);
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (Math.sqrt(Math.pow(x - event.getX(), 2) + Math.pow(y - event.getY(), 2)) > 10) {
                        animateColor(v, to, from);
                    }
                }
                else{
                    animateColor(v, to, from);
                }
                return false;
            }
        };
    }

}
