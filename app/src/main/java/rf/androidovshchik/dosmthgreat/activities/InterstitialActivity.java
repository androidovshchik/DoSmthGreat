package rf.androidovshchik.dosmthgreat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import rf.androidovshchik.dosmthgreat.BuildConfig;

public class InterstitialActivity extends AppCompatActivity {

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3898038055741115/5362686364");
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-8807499760298410/7787004889");
        interstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdClosed() {
                if (BuildConfig.DEBUG) {
                    interstitialAd.loadAd(new AdRequest.Builder()
                        .addTestDevice("BD1C60E379701FB989CE8D2BDBEE9501")
                        .addTestDevice("FAA1BA6958CC85BA6B1B0483BE321991")
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build());
                } else {
                    interstitialAd.loadAd(new AdRequest.Builder()
                        .build());
                }
            }
        });
        if (BuildConfig.DEBUG) {
            interstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("BD1C60E379701FB989CE8D2BDBEE9501")
                .addTestDevice("FAA1BA6958CC85BA6B1B0483BE321991")
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build());
        } else {
            interstitialAd.loadAd(new AdRequest.Builder()
                .build());
        }
    }
}
