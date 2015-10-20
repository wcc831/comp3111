package hk.ust.cse.hunkim.questionroom;

import android.util.Log;

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
}
