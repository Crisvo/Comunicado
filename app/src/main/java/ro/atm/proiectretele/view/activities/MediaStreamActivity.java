package ro.atm.proiectretele.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ro.atm.proiectretele.R;
import ro.atm.proiectretele.data.Constants;
import ro.atm.proiectretele.data.firestore_models.UserModel;
import ro.atm.proiectretele.databinding.ActivityMediaStreamBinding;
import ro.atm.proiectretele.utils.app.constant.Constants_Permissions;
import ro.atm.proiectretele.utils.app.login.LogedInUser;
import ro.atm.proiectretele.utils.webrtc.CustomPeerConnectionObserver;
import ro.atm.proiectretele.utils.webrtc.SignallingClient;
import ro.atm.proiectretele.utils.webrtc.SimpleSdpObserver;
import ro.atm.proiectretele.utils.webrtc.VideoTransmissionParameters;
import ro.atm.proiectretele.viewmodel.MediaStreamViewModel;

public class MediaStreamActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MediaStreamActivity";
    //region Members
    private ActivityMediaStreamBinding binding;
    private MediaStreamViewModel mViewModel;

    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnection mLocalPeerConnection;
    private PeerConnection mRemotePeerConnection;
    private SimpleSdpObserver mLocalSdp;
    private SimpleSdpObserver mRemoteSdp;

    private VideoTrack mVideoTrackFromCamera;
    private AudioTrack mLocalAudioTrack;

    private MediaConstraints mMediaConstraints;
    private EglBase mEglBase;

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private DocumentReference sdpRef = mDatabase.collection(LogedInUser.getInstance().getEmail()).document(Constants.DOCUMENT_SDP);
    private CollectionReference colRef = mDatabase.collection(LogedInUser.getInstance().getEmail());
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
        createVideoTrackFromCameraAndShowIt();

        //P2Pcall();
        onConnectionStart();

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

    @Override
    protected void onStart() {
        super.onStart();
        colRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String docId = documentSnapshot.getId();
                    if (!docId.equals(Constants.DOCUMENT_SDP) && !docId.equals(Constants.DOCUMENT_FROM)) {
                        String candidate = documentSnapshot.getString("candidate");
                        String id = documentSnapshot.getString("id");
                        int label = Objects.requireNonNull(documentSnapshot.getLong("label")).intValue();
                        String type = documentSnapshot.getString("type");
                        mLocalPeerConnection.addIceCandidate(new IceCandidate(id, label, candidate));
                    }
                }
            }
        });
        sdpRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null)
                    return;
                String str;
                if (documentSnapshot.exists()) {
                    str = documentSnapshot.getString("sdp");
                    if (str != null) { // is a sdp
                        String type = documentSnapshot.getString("type");
                        try {
                            if (type.equals("offer")) {
                                mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(),
                                        new SessionDescription(SessionDescription.Type.fromCanonicalForm(documentSnapshot.getString("type").toLowerCase()), documentSnapshot.getString("sdp")));
                                doAnswer();
                            }
                            if (type.equals("answer")) {
                                mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(),
                                        new SessionDescription(SessionDescription.Type.fromCanonicalForm(documentSnapshot.getString("type").toLowerCase()), documentSnapshot.getString("sdp")));
                            }
                        } catch (NullPointerException exc) {
                            //TODO: fix this
                        }

                    }
                }
            }
        });
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

    private void onConnectionStart() {
        if (SignallingClient.getInstance().hasPandingOffer && !SignallingClient.getInstance().isStarted) {
            SignallingClient.getInstance().isStarted = true;
            createPeerConnection();
            mDatabase.collection(LogedInUser.getInstance().getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                        for(DocumentSnapshot documentSnapshot : myListOfDocuments){
                            if(documentSnapshot.getId().equals(Constants.DOCUMENT_SDP)){
                                mLocalPeerConnection.setRemoteDescription(new SimpleSdpObserver(),
                                        new SessionDescription(SessionDescription.Type.fromCanonicalForm(documentSnapshot.getString("type").toLowerCase()), documentSnapshot.getString("sdp")));
                                doAnswer();
                            }
                            if(documentSnapshot.getId().equals(Constants.DOCUMENT_FROM)){
                                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                CollectionReference colRef = mDatabase.collection(userModel.getEmail());
                                SignallingClient.getInstance().setDatabaseReference(colRef);
                            }
                        }
                    }
                }
            });

        }
        if (!SignallingClient.getInstance().isStarted && SignallingClient.getInstance().getChannelState()) {
            createPeerConnection();
            SignallingClient.getInstance().isStarted = true;
            if (SignallingClient.getInstance().isInitiator) {
                doCall();
            }
        }
    }

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
                //mRemotePeerConnection.addIceCandidate(iceCandidate);
                SignallingClient.getInstance().emitIceCandidate(iceCandidate);
                // send to remote peer connection
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                Log.d(TAG, "onAddStream: local");
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(binding.activityMediaStreamRemotelView);
            }
        });

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
                Log.d("onCreateSuccess", "SignallingClient emit ");
                SignallingClient.getInstance().emitMessage(sessionDescription);
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
                SignallingClient.getInstance().emitMessage(sessionDescription);
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
                SignallingClient.getInstance().emitIceCandidate(iceCandidate);
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
        mediaStream.addTrack(mVideoTrackFromCamera);

        mLocalPeerConnection.addStream(mediaStream);

        mLocalPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Log.d(TAG, "onCreateSuccess local");
                mLocalPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                mRemotePeerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);
                SignallingClient.getInstance().emitMessage(sessionDescription);
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


}
