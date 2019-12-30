package ro.atm.proiectretele.utils.webrtc;

import android.util.Log;

import org.webrtc.CameraVideoCapturer;

public class CustomCameraEventsHandler implements CameraVideoCapturer.CameraEventsHandler {

    private String TAG = this.getClass().getCanonicalName();


    @Override
    public void onCameraError(String s) {
        Log.d(TAG, "onCameraError() called with: s = [" + s + "]");
    }

    @Override
    public void onCameraDisconnected() {
        Log.d(TAG, "onCameraDisconnected");

    }

    @Override
    public void onCameraFreezed(String s) {
        Log.d(TAG, "onCameraFreezed() called with: s = [" + s + "]");
    }

    @Override
    public void onCameraOpening(String s) {
        Log.d(TAG, "onCameraOpening() called with: s = [" + s + "]");
    }
    

    @Override
    public void onFirstFrameAvailable() {
        Log.d(TAG, "onFirstFrameAvailable() called");
    }

    @Override
    public void onCameraClosed() {
        Log.d(TAG, "onCameraClosed() called");
    }
}