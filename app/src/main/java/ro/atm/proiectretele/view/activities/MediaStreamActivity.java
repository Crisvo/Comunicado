package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ro.atm.proiectretele.R;
import ro.atm.proiectretele.databinding.ActivityMediaStreamBinding;
import ro.atm.proiectretele.utils.app.Constants_Permissions;
import ro.atm.proiectretele.utils.webrtc.CustomPeerConnectionObserver;
import ro.atm.proiectretele.utils.webrtc.SimpleSdpObserver;
import ro.atm.proiectretele.utils.webrtc.VideoTransmissionParameters;
import ro.atm.proiectretele.viewmodel.MediaStreamViewModel;

public class MediaStreamActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MediaStreamActivity";
    //// MEMBERS REGION
    private ActivityMediaStreamBinding binding;
    private MediaStreamViewModel mViewModel;

    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection mLocalPeerConnection;
    private PeerConnection mRemotePeerConnection;
    private VideoTrack mVideoTrackFromCamera;
    private AudioTrack mLocalAudioTrack;
    private MediaConstraints mMediaConstraints;
    private EglBase mEglBase;

    //// OVERRIDE REGION

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_media_stream);
        mViewModel = ViewModelProviders.of(this).get(MediaStreamViewModel.class);
        binding.setViewModel(mViewModel);

        requestPermission();

        initSurfaceViews();
        createPeerConnectionFactory();
        createVideoTrackFromCameraAndShowIt();

        //call();

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been denied
        // ...
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //// METHODS REGION

    private void initSurfaceViews() {
        mEglBase = EglBase.create();

        binding.activityMediaStreamLocalView.init(mEglBase.getEglBaseContext(), null);
        binding.activityMediaStreamLocalView.setEnableHardwareScaler(true);
        //binding.activityMediaStreamLocalView.setZOrderMediaOverlay(true);
        binding.activityMediaStreamLocalView.setMirror(true);

        binding.activityMediaStreamRemotelView.init(mEglBase.getEglBaseContext(), null);
        binding.activityMediaStreamRemotelView.setEnableHardwareScaler(true);
        binding.activityMediaStreamRemotelView.setMirror(true);
    }

    private void createVideoTrackFromCameraAndShowIt() {
        mMediaConstraints = new MediaConstraints();

        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(videoCapturer);

        videoCapturer.startCapture(VideoTransmissionParameters.HD_VIDEO_WIDTH, VideoTransmissionParameters.HD_VIDEO_HEIGHT, VideoTransmissionParameters.VIDEO_FPS_60);

        mVideoTrackFromCamera = mPeerConnectionFactory.createVideoTrack(VideoTransmissionParameters.VIDEO_TRACK_ID, videoSource);
        mVideoTrackFromCamera.setEnabled(true);
        mVideoTrackFromCamera.addSink(binding.activityMediaStreamLocalView);

        AudioSource source = mPeerConnectionFactory.createAudioSource(mMediaConstraints);
        mLocalAudioTrack = mPeerConnectionFactory.createAudioTrack(VideoTransmissionParameters.AUDIO_TRACK_ID, source);
    }

    private void createPeerConnectionFactory() {
        //// Getting local video stream
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options peerConnectionFactoryOptions = new PeerConnectionFactory.Options();
        peerConnectionFactoryOptions.disableEncryption = true;
        peerConnectionFactoryOptions.disableNetworkMonitor = true;

        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(), true, true))
                .setOptions(peerConnectionFactoryOptions)
                .createPeerConnectionFactory();

        mPeerConnectionFactory.setVideoHwAccelerationOptions(mEglBase.getEglBaseContext(), mEglBase.getEglBaseContext());


    }

    @AfterPermissionGranted(Constants_Permissions.RC_WEBRTC)
    public void call() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //PeerConnection.IceServer stunServerList = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        //iceServers.add(stunServerList);

        PeerConnection.RTCConfiguration rtcConfiguration = new PeerConnection.RTCConfiguration(iceServers);
        // mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        mLocalPeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfiguration, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: local");
                mLocalPeerConnection.addIceCandidate(iceCandidate);
            }
        });

        mRemotePeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfiguration, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: remote");
                mRemotePeerConnection.addIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.d(TAG, "onAddStream remote: " + mediaStream.videoTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(binding.activityMediaStreamRemotelView);
            }
        });

        MediaStream mediaStream = mPeerConnectionFactory.createLocalMediaStream("007");
        mediaStream.addTrack(mVideoTrackFromCamera);

        mLocalPeerConnection.addStream(mediaStream);

        mLocalPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Log.d(TAG, "onCreateSuccess local");
                mLocalPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                mRemotePeerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);

                mRemotePeerConnection.createAnswer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        super.onCreateSuccess(sessionDescription);
                        Log.d(TAG, "onCreateSuccess remote");
                        mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);
                        mRemotePeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        super.onCreateFailure(s);
                        Log.d(TAG, "Error to create remote offer!");
                    }
                }, mMediaConstraints);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
                Log.d(TAG, "Error to create local offer!");
            }
        }, mMediaConstraints);


    }


    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "rationale", Constants_Permissions.RC_WEBRTC, perms);
        }

    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // Trying to find main camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Trying to find a front facing camera!
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }


}
