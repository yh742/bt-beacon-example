package com.example.seanhsu.mylilhealthbot;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.RemoteException;

import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.Identifier;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, MonitorNotifier {

    private Button mVendButton;
    private TextView mTextLog;
    private BeaconManager mBeaconManager;
    private String mMacAddress;
    private String mIpAddress;
    private final String TAG = "mylilhealthbot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Beacon
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        mBeaconManager.getBeaconParsers().clear();
        // Detect the main identifier (UID) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.bind(this);

        // Setup Button
        mVendButton = (Button)findViewById(R.id.button2);
        mVendButton.setEnabled(false);
        mVendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVendButton.isEnabled()){
                    // TODO: Send GET request to the server
                }
            }
        });

        // Setup TextView log
        mTextLog = (TextView)findViewById(R.id.textView2);
        logToDisplay("No HealthBot Found Nearby!", true);

        // Read External Config Files
        String storagePath = getApplicationContext().getFilesDir().getAbsolutePath();
        File file = new File(storagePath + File.separator + "lilhealthbot.txt");
        //Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            mMacAddress = br.readLine();
            mIpAddress = br.readLine();
            br.close();
        } catch (IOException e){
            Log.d("blah", e.getMessage());
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mBeaconManager.unbind(this);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);

        mBeaconManager.addMonitorNotifier(this);
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                if (url.equals("https://bit.ly/2x")){
                    double distance = Math.round((beacon.getDistance()/6.0)*100)/100.0;
                    logToDisplay("I see a HealthBot" +
                            " approximately " + distance + " meters away.", true);

                    if (distance < 10 && !mVendButton.isEnabled()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVendButton.setEnabled(true);
                            }
                        });
                    }
                    else if (distance > 10 && mVendButton.isEnabled()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVendButton.setEnabled(false);
                            }
                        });
                    }
                }
            }
        }
    }

    public void didEnterRegion(Region region) {
        Log.d(TAG, "Entered the region");
    }

    public void didExitRegion(Region region) {
        logToDisplay("The HealthBot is not in range", true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVendButton.setEnabled(false);
            }
        });
    }

    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG, "State Changed");
    }

    public void logToDisplay(final String line, final boolean clear){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(clear){
                    mTextLog.setText("");
                }
                mTextLog.append(line);
            }
        });
    }
}
