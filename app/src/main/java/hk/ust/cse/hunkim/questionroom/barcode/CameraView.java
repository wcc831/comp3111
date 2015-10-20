package hk.ust.cse.hunkim.questionroom.barcode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by cc on 10/8/2015.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraView";

    private SurfaceHolder holder=null;
    private Camera mCamera;
    private byte[] frame;
    private int width;
    private int height;
    private static int scale=1;

    public CameraView(Context context, Camera camera){
        super(context);

        this.mCamera = camera;
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mCamera = Camera.open();
        if (mCamera == null) {
            Log.d(TAG, "fail to open camera");
            return;
        }

        Log.d(TAG, "camera view created");

        try{
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
        }
        catch (IOException e){
            mCamera.release();
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");


        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(800, 480);
        params.setPictureFormat(PixelFormat.JPEG);
        params.setFocusMode("auto");

        mCamera.setParameters(params);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mCamera.setDisplayOrientation(90);
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera=null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        frame = data;
        width = camera.getParameters().getPreviewSize().width;
        height = camera.getParameters().getPreviewSize().height;
    }

    public byte[] getFrame(){
        return frame;
    }

    public int width() { return width; }

    public int height() { return height; }
}
