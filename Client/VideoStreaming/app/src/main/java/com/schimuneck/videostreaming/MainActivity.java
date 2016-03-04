package com.schimuneck.videostreaming;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,  MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener{

    //String server = "http://10.42.0.52:12345/videos%2F";
    String server;
    String[] videos = {"h265.mp4"};
    String[] files;
    long prepared;
    long play;
    long rederingStart;
    long bufferBegin;
    long bufferEnd;
    long stop;
    double batteryBegin;
    double batteryEnd;
    String vidAddress;
    VideoView vidView;
//    Intent batteryStatus;
    Context context;
    String freezes = "";
    String serverBegin = "";
    String serverEnd = "";
    //String dstAddress ="192.168.44.1";
    String dstAddress ="10.42.0.1";
    int dstPort = 80;
    String msgToServer = "get";
    String clientXBegin;
    String clientXEnd;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        server = "http://"+dstAddress+"/videos/";

//        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        vidView = (VideoView)findViewById(R.id.myVideo);

//        String time = getTimesTamp();
//        files = new String[videos.length];
//        for(int i = 0; i<videos.length;i++){
//            String str = videos[i];
//            //files[i] = str.replaceAll("\\.mp4", "")+time+".log";
//            files[i] = str.replaceAll("\\.mp4", "")+".log";
//            salvarEmArquivo(context, files[i], "TIMESTART,PREPARED,TIMEBUFFEREND,TIMERENDERINGSTART,TIMESTOP,BATTERYSTART,BATTERYSTOP,FREEZES,RXBEGIN,TXBEGIN,RXEND,TXEND,SERVERBATERRYBEGIN,SERVERXBEGIN,SERVERTXBEGIN,SERVERBATERRYEND,SERVERXEND,SERVERTXEND");
//        }

        vidView.setOnCompletionListener(this);
        vidView.setOnPreparedListener(this);
        vidView.setOnInfoListener(this);

        playVideo(videos[0]);
    }


    public void playVideo(String v){
//        MyClientTask myClientTask = new MyClientTask();
//        myClientTask.execute();
        clientXBegin = getRxTxData();
        vidAddress = server+v;
        freezes = "[";
        bufferEnd = 0;

        Uri vidUri = Uri.parse(vidAddress);

        vidView.setVideoURI(vidUri);

//        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        float batteryPct = level / (float)scale;
//        batteryBegin = getBatteryCapacity()*batteryPct;
//        Log.i("BATERIA: ", String.valueOf(batteryBegin));
        play = System.currentTimeMillis();
        Log.i("TIME", "Start at " + play);

        vidView.start();
    }

    private String getRxTxData() {
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int UID = 0;
        //loop through the list of installed packages and see if the selected
        //app is in the list
        for (ApplicationInfo packageInfo : packages)
        {
            if(packageInfo.packageName.equals("com.schimuneck.videostreaming"))
            {
                //get the UID for the selected app
                UID = packageInfo.uid;
            }
            //Do whatever with the UID
            //Log.i("Check UID", "UID is: " + UID);
        }

        long rx = TrafficStats.getUidRxBytes(UID);
        long tx = TrafficStats.getUidTxBytes(UID);
        //Log.v(UserProfile.class.getName(), "Rx : "+rx+" Tx : "+tx);

        Log.v("TAG", "Rec. Bytes : " + rx / 1000 + " KB");
        Log.v("TAG", "Sent Bytes : " + tx / 1000 + " KB");

        return rx +","+tx;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public double getBatteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            return batteryCapacity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("TIME", "OnCompletionListener at " + System.currentTimeMillis());
//        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        float batteryPct = level / (float)scale;
//        batteryEnd = getBatteryCapacity()*batteryPct;
//        stop = System.currentTimeMillis();
//        freezes = freezes + "]";
//        freezes = freezes.replaceAll("],]", "]]");
//        clientXEnd = getRxTxData();
//        MyClientTask myClientTask = new MyClientTask();
//        myClientTask.execute();
    }

    public void saveDataAndRecurVideo(){
//        for(int i = 0; i<videos.length;i++){
//            int size = fileSize(context, files[i]);
//            if(size<32){
//                salvarEmArquivo(context, files[i], play + "," + prepared + "," + bufferEnd + "," + rederingStart + "," + stop + "," + batteryBegin + "," + batteryEnd + "," + freezes + "," + clientXBegin + "," + clientXEnd + "," + serverBegin + "," + serverEnd);
//                clearVariables();
//                try {
//                    trimCache(this);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    //e.printStackTrace();
//                }
//                if(size==31){
//                    if(i<videos.length-1){
//                        playVideo(videos[i+1]);
//                    }
//                } else {
//                    playVideo(videos[i]);
//                }
//                break;
//            }
//        }
    }

    private void clearVariables() {
        prepared = 0;
        play = 0;
        rederingStart = 0;
        bufferBegin = 0;
        bufferEnd = 0;
        stop = 0;
        batteryBegin = 0;
        batteryEnd = 0;
        vidAddress = "";
        freezes = "";
        serverBegin = "";
        serverEnd = "";
        clientXBegin= "";
        clientXEnd = "";
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        Log.i("TIME", "OnInfoListener " + what + " at " + System.currentTimeMillis());
        if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
            freezes = freezes + "["+System.currentTimeMillis()+",";
            Log.i("TIME", "MEDIA_INFO_BUFFERING_START " + System.currentTimeMillis());
        } else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
            if(bufferEnd>0){
                freezes = freezes + System.currentTimeMillis()+"],";
            } else {
                bufferEnd = System.currentTimeMillis();
            }
            Log.i("TIME", "MEDIA_INFO_BUFFERING_END " + System.currentTimeMillis());
        } else if(what == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING){
            Log.i("TIME", "MEDIA_INFO_BAD_INTERLEAVING " + System.currentTimeMillis());
        } else if(what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
            rederingStart = System.currentTimeMillis();
            Log.i("TIME", "MEDIA_INFO_VIDEO_RENDERING_START " + rederingStart);
        } else if(what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING){
            Log.i("TIME", "MEDIA_INFO_VIDEO_TRACK_LAGGING " + System.currentTimeMillis());
        } else if (what == 972){
            bufferBegin = System.currentTimeMillis();
            Log.i("TIME", "MEDIA_INFO_972 " + System.currentTimeMillis());
        }

        return false;

    }


    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static String getTimesTamp(){
        Calendar c = Calendar.getInstance();
        int dia = c.get(Calendar.DAY_OF_MONTH);
        int mes = c.get(Calendar.MONTH);
        int ano = c.get(Calendar.YEAR);
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        String timesTamp = dia + "-" + (mes+1) + "-" + ano + " " + hora + ":" + min + ":"
                + seconds;
        return timesTamp;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = System.currentTimeMillis();
        Log.i("TIME", "OnPreparedListener at " + System.currentTimeMillis());
    }


    public static void salvarEmArquivo(Context myContext, String arquivo, String valor){
        //System.out.println("GRAVANDO DADOS NO ARQUIVO = " + valor);
        try {
            File file = new File(myContext
                    .getExternalFilesDir(null), arquivo);
            FileOutputStream out = new FileOutputStream(
                    file, true);
            out.write(valor.getBytes());
            out.write("\n".getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int fileSize(Context myContext, String arquivo){
        //System.out.println("GRAVANDO DADOS NO ARQUIVO = " + valor);
        int lines = 0;
        try {
            File file = new File(myContext
                    .getExternalFilesDir(null), arquivo);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.readLine() != null) lines++;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }


    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        //String dstAddress;
        //int dstPort;
        String response = "";
        //String msgToServer;

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                if(msgToServer != null){
                    dataOutputStream.writeUTF(msgToServer);
                }

                response = dataInputStream.readUTF();
                //return response;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            if(serverBegin.equals("")){
                serverBegin = response;
            } else {
                serverEnd = response;
                saveDataAndRecurVideo();
            }
            super.onPostExecute(result);
        }

    }

}