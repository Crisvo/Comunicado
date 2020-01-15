package ro.atm.proiectretele.utils.webrtc;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ro.atm.proiectretele.utils.webrtc.pojo.TurnServer;

/**
 * Webrtc_Step3
 * Created by vivek-3102 on 11/03/17.
 */

public class Utils {

    public static final String API_ENDPOINT = "https://global.xirsys.net";
    static Utils instance;
    private Retrofit retrofitInstance;

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public TurnServer getRetrofitInstance() {
        if (retrofitInstance == null) {
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance.create(TurnServer.class);
    }
}
