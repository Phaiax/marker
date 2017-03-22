package de.invisibletower.soatracker;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by daniel on 21.03.17.
 */

public class PushLocationToQueueService extends IntentService implements Runnable {

    private SharedPreferences sharedPref;

    PinQueue.PinQueueDbHelper mDb;

    LinkedList<PinInfo> mRequests = new LinkedList<PinInfo>();
    MyLocationService mLocationService;
    long mLastReqTime = 0;

    public PushLocationToQueueService() {
        super("PushLocationToQueueService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationService = MyLocationService.getLocationManager(this);

        mDb = new PinQueue.PinQueueDbHelper(this);
        sharedPref =  this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        Log.i("MARKER", "In Push Loc2Que Service");

        PinInfo i = new PinInfo();
        i.descr = workIntent.getStringExtra("descr");
        i.icon = workIntent.getStringExtra("icon");
        i.ts = System.currentTimeMillis();
        mRequests.add(i);
        mLastReqTime = System.currentTimeMillis();
        mLocationService.startLocating(this, this);
    }

    @Override
    public void run() {
        PinInfo nextreq = mRequests.peek();
        if (nextreq == null) {
            Log.i("MARKER", "No Reqs for locs");
            mLocationService.stop(this);
            return;
        }


        if (mLocationService.location != null
                && mLocationService.location.getTime() >= nextreq.ts
                && mLocationService.location.hasAccuracy()
                && mLocationService.location.getAccuracy() < 100) {
            Log.i("MARKER", "Got Good Loc");
            if (mRequests.peek() == null) {
                mLocationService.stop(this);
            }
            send(mLocationService.location);
        }
        if (mLastReqTime + 1000 * 60 * 3 < System.currentTimeMillis()) {
            Log.i("MARKER", "Timeout at Loc Wait");
            mLocationService.stop(this);
            return;
        }
    }


    void send(Location l) {
        PinInfo i = mRequests.poll();
        if (i != null) {

            i.lon = String.valueOf(l.getLongitude());
            i.lat = String.valueOf(l.getLatitude());
            i.ts = l.getTime();
            i.reqid = sharedPref.getInt(getString(R.string.pref_nextreqid), 1);
            {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.pref_nextreqid), i.reqid + 1);
                Log.i("MARKER", "Update nextreqid: " + String.valueOf(i.reqid + 1));
                editor.commit();
            }
            Log.i("MARKER", "Insert row");
            mDb.insert(i);
        }
    }





}
