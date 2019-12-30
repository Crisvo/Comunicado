package ro.atm.proiectretele.utils.webrtc;

/**
 * Transmission parameters constants
 * @author Cristian VOICU
 * @since 4 nov 2019
 * */
public class VideoTransmissionParameters {
    // IDS
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    // HD resolution
    public static final int HD_VIDEO_HEIGHT = 1280;
    public static final int HD_VIDEO_WIDTH = 720;
    // FULL HD resolution
    public static final int FULL_HD_VIDEO_HEIGHT = 1920;
    public static final int FULL_HD_VIDEO_WIDTH = 1080;
    // FPS
    public static final int VIDEO_FPS_60 = 60;
    public static final int VIDEO_FPS_30 = 30;
    // Codecs
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
}
