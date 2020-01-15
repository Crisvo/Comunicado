package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ro.atm.proiectretele.R;
import ro.atm.proiectretele.data.CloudFirestoreRepository;
import ro.atm.proiectretele.data.Constants;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.databinding.ActivityMediaStreamBinding;
import ro.atm.proiectretele.utils.app.constant.Constants_Permissions;
import ro.atm.proiectretele.utils.app.login.LogedInUser;
import ro.atm.proiectretele.utils.webrtc.CustomPeerConnectionObserver;
import ro.atm.proiectretele.utils.webrtc.pojo.IceServer;
import ro.atm.proiectretele.utils.webrtc.signaling.SignallingClientSocket;
import ro.atm.proiectretele.utils.webrtc.signaling.SignallingInterface;
import ro.atm.proiectretele.utils.webrtc.SimpleSdpObserver;
import ro.atm.proiectretele.utils.webrtc.pojo.TurnServerPojo;
import ro.atm.proiectretele.utils.webrtc.Utils;
import ro.atm.proiectretele.utils.webrtc.VideoTransmissionParameters;
import ro.atm.proiectretele.viewmodel.MediaStreamViewModel;

public class MediaStreamActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, SignallingInterface {
    private static final String TAG = "MediaStreamActivity";
    //region Members
    private ActivityMediaStreamBinding binding;
    private MediaStreamViewModel mViewModel;

    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection mLocalPeerConnection;
    private PeerConnection mRemotePeerConnection;
    private SimpleSdpObserver mLocalSdp;
    private SimpleSdpObserver mRemoteSdp;

    List<IceServer> iceServers;
    private AudioTrack mLocalAudioTrack;

    private MediaConstraints mMediaConstraints;
    List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();
    private VideoTrack mLocalVideoTrack;
    private MediaConstraints mSdpConstraints;
    private EglBase mEglBase;
    private MediaConstraints mVideoConstraints;

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private DocumentReference sdpRef = mDatabase.collection(LogedInUser.getInstance().getEmail()).document(Constants.DOCUMENT_SDP);
    private CollectionReference colRef = mDatabase.collection(LogedInUser.getInstance().getEmail());
    private MediaConstraints mAudioConstraints;
    private boolean gotUserMedia;
    //endregion

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_media_stream);
        mViewModel = ViewModelProviders.of(this).get(MediaStreamViewModel.class);
        binding.setViewModel(mViewModel);

        requestPermission();

        initSurfaceViews();
        createPeerConnectionFactory();
        getIceServers();
        createVideoTrackFromCameraAndShowIt();

        //P2Pcall();

    }

    //region Permissions
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

    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "rationale", Constants_Permissions.RC_WEBRTC, perms);
        }

    }

    //endregion
    private void getIceServers() {
        //get Ice servers using xirsys
        byte[] data = new byte[0];
        try {
            data = ("<xirsys_ident>:<xirsys_secret>").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String authToken = "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
        Utils.getInstance().getRetrofitInstance().getIceCandidates(authToken).enqueue(new Callback<TurnServerPojo>() {
            @Override
            public void onResponse(@NonNull Call<TurnServerPojo> call, @NonNull Response<TurnServerPojo> response) {
                TurnServerPojo body = response.body();
                if (body != null) {
                    iceServers = body.iceServerList.iceServers;
                }
                for (IceServer iceServer : iceServers) {
                    if (iceServer.credential == null) {
                        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url).createIceServer();
                        peerIceServers.add(peerIceServer);
                    } else {
                        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url)
                                .setUsername(iceServer.username)
                                .setPassword(iceServer.credential)
                                .createIceServer();
                        peerIceServers.add(peerIceServer);
                    }
                }
                Log.d("onApiResponse", "IceServers\n" + iceServers.toString());
            }

            @Override
            public void onFailure(@NonNull Call<TurnServerPojo> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //region WebRTC initialisations
    private void initSurfaceViews() {
        mEglBase = EglBase.create();

        binding.activityMediaStreamLocalView.init(mEglBase.getEglBaseContext(), null);
        binding.activityMediaStreamLocalView.setEnableHardwareScaler(true);
        binding.activityMediaStreamLocalView.setZOrderMediaOverlay(true);
        binding.activityMediaStreamLocalView.setMirror(true);

        binding.activityMediaStreamRemotelView.init(mEglBase.getEglBaseContext(), null);
        binding.activityMediaStreamRemotelView.setEnableHardwareScaler(true);
        binding.activityMediaStreamLocalView.setZOrderMediaOverlay(true);
        binding.activityMediaStreamRemotelView.setMirror(true);
    }

    private void createVideoTrackFromCameraAndShowIt() {
        mMediaConstraints = new MediaConstraints();

        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(videoCapturer);

        videoCapturer.startCapture(VideoTransmissionParameters.HD_VIDEO_WIDTH, VideoTransmissionParameters.HD_VIDEO_HEIGHT, VideoTransmissionParameters.VIDEO_FPS_60);

        mLocalVideoTrack = mPeerConnectionFactory.createVideoTrack(VideoTransmissionParameters.VIDEO_TRACK_ID, videoSource);
        mLocalVideoTrack.setEnabled(true);
        mLocalVideoTrack.addSink(binding.activityMediaStreamLocalView);

        AudioSource source = mPeerConnectionFactory.createAudioSource(mMediaConstraints);
        mLocalAudioTrack = mPeerConnectionFactory.createAudioTrack(VideoTransmissionParameters.AUDIO_TRACK_ID, source);

        gotUserMedia = true;

        if (SignallingClientSocket.getInstance().isInitiator) {
            onTryToStart();
        }
    }

    private void createPeerConnectionFactory() {
        //// Getting local video stream
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        SignallingClientSocket.getInstance().init(this);

        PeerConnectionFactory.Options peerConnectionFactoryOptions = new PeerConnectionFactory.Options();
        //peerConnectionFactoryOptions.disableEncryption = true;
        //peerConnectionFactoryOptions.disableNetworkMonitor = true;

        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(), true, true))
                .setOptions(peerConnectionFactoryOptions)
                .createPeerConnectionFactory();

        mPeerConnectionFactory.setVideoHwAccelerationOptions(mEglBase.getEglBaseContext(), mEglBase.getEglBaseContext());
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
    //endregion

    /**
     * Creating local peer connection instance
     */
    private void createPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stunServerList = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(stunServerList);

        PeerConnection.RTCConfiguration rtcConfiguration = new PeerConnection.RTCConfiguration(iceServers);

        rtcConfiguration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfiguration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfiguration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfiguration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfiguration.keyType = PeerConnection.KeyType.ECDSA;

        mLocalPeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfiguration, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: local");
                SignallingClientSocket.getInstance().emitIceCandidate(iceCandidate);
                // send to remote peer connection
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.d(TAG, "onAddStream: local");
                runOnUiThread(() -> {
                    try {
                        VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                        //remoteVideoTrack.setEnabled(true);
                        remoteVideoTrack.addSink(binding.activityMediaStreamRemotelView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        MediaStream mediaStream = mPeerConnectionFactory.createLocalMediaStream("007");
        mediaStream.addTrack(mLocalVideoTrack);
        mediaStream.addTrack(mLocalAudioTrack);

        mLocalPeerConnection.addStream(mediaStream);

    }

    @AfterPermissionGranted(Constants_Permissions.RC_WEBRTC)
    private void doCall() {
        mMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        mLocalPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                mLocalPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d("onCreateSuccess", "SignallingClientFirestore emit ");
                SignallingClientSocket.getInstance().emitMessage(sessionDescription);
                // send sdp to the other peer

            }
        }, mMediaConstraints);
    }

    @AfterPermissionGranted(Constants_Permissions.RC_WEBRTC)
    private void doAnswer() {
        mLocalPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                mLocalPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                //SignallingClientFirestore.getInstance().emitMessage(sessionDescription);
                SignallingClientSocket.getInstance().emitMessage(sessionDescription);
                // send sdp to the other peer
            }
        }, new MediaConstraints());
    }

    @AfterPermissionGranted(Constants_Permissions.RC_WEBRTC)
    public void P2Pcall() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stunServerList = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        iceServers.add(stunServerList);

        PeerConnection.RTCConfiguration rtcConfiguration = new PeerConnection.RTCConfiguration(iceServers);
        // mMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        mLocalPeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfiguration, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: local");
                //SignallingClientFirestore.getInstance().emitIceCandidate(iceCandidate);
                mRemotePeerConnection.addIceCandidate(iceCandidate);
            }
        });

        mRemotePeerConnection = mPeerConnectionFactory.createPeerConnection(rtcConfiguration, new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: remote");
                mLocalPeerConnection.addIceCandidate(iceCandidate);
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
        mediaStream.addTrack(mLocalVideoTrack);

        mLocalPeerConnection.addStream(mediaStream);

        mLocalPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Log.d(TAG, "onCreateSuccess local");
                mLocalPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                mRemotePeerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);
                //SignallingClientFirestore.getInstance().emitMessage(sessionDescription);
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

    /**
     * Util Methods
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    private void updateVideoViews(final boolean remoteVisible) {
        runOnUiThread(() -> {
            ViewGroup.LayoutParams params = binding.activityMediaStreamLocalView.getLayoutParams();
            if (remoteVisible) {
                params.height = dpToPx(100);
                params.width = dpToPx(100);
            } else {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            binding.activityMediaStreamLocalView.setLayoutParams(params);
        });

    }

    private void hangUp() {
        try {
            mLocalPeerConnection.close();
            mLocalPeerConnection = null;
            SignallingClientSocket.getInstance().close();
            updateVideoViews(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(MediaStreamActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRemoteHangUp(String msg) {
        showToast("Remote Peer hungup");
        runOnUiThread(this::hangUp);
    }

    @Override
    public void onOfferReceived(JSONObject data) {
        showToast("Received Offer");
        runOnUiThread(() -> {
            if (!SignallingClientSocket.getInstance().isInitiator && !SignallingClientSocket.getInstance().isStarted) {
                onTryToStart();
            }

            try {
                mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                doAnswer();
                updateVideoViews(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Remote sdp (answer) received
     */
    @Override
    public void onAnswerReceived(JSONObject data) {
        showToast("Received Answer");
        try {
            mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
            //updateVideoViews(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote ice candidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        try {
            mLocalPeerConnection.addIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTryToStart() {
        runOnUiThread(() -> {
            if (!SignallingClientSocket.getInstance().isStarted && mLocalVideoTrack != null && SignallingClientSocket.getInstance().isChannelReady) {
                createPeerConnection();
                SignallingClientSocket.getInstance().isStarted = true;
                if (SignallingClientSocket.getInstance().isInitiator) {
                    doCall();
                }
            }
        });
    }

    @Override
    public void onCreatedRoom() {
        showToast("You created the room " + gotUserMedia);
        if (gotUserMedia) {
            SignallingClientSocket.getInstance().emitMessage("got user media");
        }
    }

    @Override
    public void onJoinedRoom() {
        showToast("You joined the room " + gotUserMedia);
        if (gotUserMedia) {
            SignallingClientSocket.getInstance().emitMessage("got user media");
        }
    }

    @Override
    public void onNewPeerJoined() {
        showToast("Remote Peer Joined");
    }

    @Override
    protected void onDestroy() {
        SignallingClientSocket.getInstance().close();

        CloudFirestoreRepository.create().onUserSignOut(new UserModel(LogedInUser.getInstance()));

        super.onDestroy();
    }
}
