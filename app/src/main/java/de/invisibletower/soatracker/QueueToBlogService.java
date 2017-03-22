package de.invisibletower.soatracker;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daniel on 21.03.17.
 */

public class QueueToBlogService extends IntentService {

    public static void doRepeating(Context context) {
        boolean alarmUp = (PendingIntent.getService(context, 0,
                new Intent(context, QueueToBlogService.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Log.d("MARKER", "Alarm is already active");
        } else {
            Log.d("MARKER", "Set Alarm");
            long interval = 1000*60*15; // 15 min
            interval = 1000 * 30;

            PendingIntent pi = PendingIntent.getService(context, 0,
                    new Intent(context, QueueToBlogService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pi);

        }
    }

    public QueueToBlogService() {
        super("QueueToBlogService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDb = new PinQueue.PinQueueDbHelper(this);
        sharedPref =  this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Log.i("MARKER", "Q2Blog onCreate");
    }

    PinQueue.PinQueueDbHelper mDb;
    private SharedPreferences sharedPref;

    @Override
    protected void onHandleIntent(Intent workIntent) {


        while(true) {

            int lastreqid = sharedPref.getInt(getString(R.string.pref_lastcommittedreqid), 0);
            Log.i("MARKER", "Q2Blog lastCommited " + String.valueOf(lastreqid));

            PinInfo pi = mDb.getNext(lastreqid + 1);
            if (pi != null){
                Log.i("MARKER", "Q2Blog got One");

                try {
                    String query = "reqid="+pi.reqid+
                            "&lat="+pi.lat+
                            "&lon="+pi.lon+
                            "&markername="+pi.descr+
                            "&kml_timestamp="+pi.ts+
                            "&icon_hidden="+pi.icon+
                            "&popuptext="+
                            "&address="
                            ;

                    URL url = new URL("https://feuchtundschwitzig.de/wp-admin/admin-ajax.php");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //Set to POST
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(20000);
                    Writer writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(query);
                    writer.flush();
                    writer.close();

                    if (connection.getResponseCode() == 200)
                    {
                        String msg = connection.getResponseMessage();
                        Log.i("MARKER", "Resp online " + msg);
                        try {

                            JSONObject json = new JSONObject(msg);
                            if (json.getBoolean("success")) {
                                Log.i("MARKER", "SUCCESS");
                                int newlastreqid = json.getInt("last");
                                {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt(getString(R.string.pref_lastcommittedreqid), newlastreqid);
                                    Log.i("MARKER", "Update lastreqid: " + String.valueOf(newlastreqid));
                                    editor.commit();
                                }
                            } else {
                                Log.i("MARKER", "ERROR " + json.getString("reason"));
                                int newlastreqid = json.getInt("last");
                                {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt(getString(R.string.pref_lastcommittedreqid), newlastreqid);
                                    Log.i("MARKER", "Update lastreqid: " + String.valueOf(newlastreqid));
                                    editor.commit();
                                }
                            }
                        }
                        catch (JSONException e) {
                            Log.e("MARKER", "JSON DEcode " + e.toString());
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.e("N", e.toString());
                }

            } else {
                Log.i("MARKER", "Q2Blog got none");

                return;
            }

        }

    }


}
