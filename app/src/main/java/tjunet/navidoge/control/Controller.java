package tjunet.navidoge.control;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Date;

import tjunet.navidoge.R;
import tjunet.navidoge.collect.Sensors;
import tjunet.navidoge.collect.WiFiScan;

/**
 * Created by XinNoil on 2018/9/18.
 *
 */

public class Controller implements Serializable {
    public Context context;
    public FileSave fileSave;
    private Display display;
    private Handler handler;
    private WiFiScan wiFiScan;
    public Sensors sensors;
    public boolean data_flags[]=new boolean[]{true,false,true,false,false};
    private int datatype_num=data_flags.length;
    private String []setting_strings=new String[5];
    private Vibrator vibrator;
    private long [] pattern = {100,400,100,400};   // this停止 开启 停止 开启
    public boolean logging=false;
    private java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
    public int interval=200;
    public int log_time=60;
    public double log_time_left=log_time;
    private NotificationManager notificationManager;
    private Notification notification;
    private int display_index=0;
    public boolean wifi_async_scan=false;
    /*
    string[0] : data type
     */
    private String[] items;
    /*
    <string-array name="data_type">
        <item>WiFi</item>
        <item>Magnetic</item>
    </string-array>
     */
    public Controller(Context context){
        this.context=context;
        fileSave=new FileSave(context.getExternalFilesDir(null));
        handler=new Handler();
        wiFiScan=new WiFiScan(this);
        sensors=new Sensors(this);
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        items=context.getResources().getStringArray(R.array.data_type);
        notification= new Notification.Builder(context)
                .setContentTitle("通知 ")
                .setContentText("WiFi 记录完成")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon((BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))).getNotification();

        timer();
    }

    public String getSettingString(){
        setting_strings[0]=getDataTypeString();
        setting_strings[1]="FILE     : "+fileSave.getFileName();
        setting_strings[2]="INTERVAL : "+Integer.toString(interval)+" ms";
        setting_strings[3]="LOG TIME : "+Integer.toString(log_time)+" s";
        setting_strings[4]="LEFT TIME: "+df.format(log_time_left)+" s";
        StringBuilder stringBuilder=new StringBuilder();
        for(String string : setting_strings){
            stringBuilder.append(string);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void changeDisplayIndex(){
        int new_index=display_index;
        for (int i=0;i<=datatype_num;i++){
            new_index=(++new_index)%datatype_num;
            if (data_flags[new_index])
                break;
        }
        if(data_flags[new_index])
            display_index=new_index;
        display.setString(1,getDataString());
    }

    private String getDataString(){
        if (!wifi_async_scan){
            wiFiScan.startScan();
            wiFiScan.getScanResult();
        }
        StringBuilder dataStrBuilder=new StringBuilder();
        dataStrBuilder.append(items[display_index]).append(":\n");
        if (data_flags[display_index]){
            switch (display_index){
                case 0:
                    dataStrBuilder.append(wiFiScan.getDisplayOutput());
                    break;
                default:
                    dataStrBuilder.append(sensors.getDisplayOutput(display_index));
                    break;
            }
            return dataStrBuilder.toString();
        }
        else
            return "NO DATA";
    }

    public void setDisplay(Display display){
        this.display=display;
    }

    private void timer(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                //TODO Auto-generated method stub
                handler.postDelayed(this, getInterval());
                if (logging){
                    if (!wifi_async_scan&&data_flags[0]){
                        if(!fileSave.saveData(wiFiScan.getOutput(),fileSave.getFiles(0)))
                            Toast.makeText(context, "FAILED TO SAVE: "+fileSave.getFiles(0).getName(), Toast.LENGTH_LONG).show();
                    }
                    for (int i=1;i<datatype_num;i++){
                        if (data_flags[i]){
                            if(!fileSave.saveData(sensors.getOutput(i),fileSave.getFiles(i)))
                                Toast.makeText(context, "FAILED TO SAVE: "+fileSave.getFiles(i).getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                    log_time_left-=interval*0.001;
                }
                display.setString(0,getSettingString());
                display.setString(1,getDataString());
            }
        };
        handler.postDelayed(runnable, getInterval());
    }

    private Runnable runnable_stopLog=new Runnable(){
        @Override
        public void run() {
            stopLog();
            vibrator.vibrate(pattern, -1);
            startAlarm();
            notificationManager.notify(0, notification);
        }
    };

    public void startLog(){
        if (logging){
            Toast.makeText(context, "CAN NOT START WHEN LOGGING!", Toast.LENGTH_LONG).show();
        }
        if (log_time>0)
            handler.postDelayed(runnable_stopLog, log_time*1000);
        sensors.initOutputBuilders();
        logging=true;
        fileSave.setDir();
        for (int i=0;i<datatype_num;i++){
            if (data_flags[i])
                fileSave.setFiles(i);
        }
        if (wifi_async_scan)
            startAsyncScan();
        Toast.makeText(context, "START", Toast.LENGTH_LONG).show();
    }

    public void stopLog(){
        logging=false;
        log_time_left=log_time;
        wiFiScan.recordNo=0;
        Toast.makeText(context, fileSave.getFileName()+" saved!", Toast.LENGTH_LONG).show();
        fileSave.nextFile();
    }

    private void startAsyncScan(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!wiFiScan.wm.isWifiEnabled()){
                    wiFiScan.wm.setWifiEnabled(true);
                }
                try {
                    while (logging){
                        wiFiScan.wm.startScan();
                        synchronized (wiFiScan.scanned) {
                            try {
                                wiFiScan.scanned.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("WiFi SCAN", "GET RESULT SIZE:"+wiFiScan.wl.size());
                        if(logging){
                            wiFiScan.getScanResult();
                            if(!fileSave.saveData(wiFiScan.getOutput(),fileSave.getFiles(0)))
                                Toast.makeText(context, "FAILED TO SAVE: "+fileSave.getFiles(0).getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startAlarm() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this.context, notification);
        r.play();
    }

    private int getInterval(){
        return interval;
    }

    public String getDataTypeString(){
        String dataTypeString="TYPE: ";
        for (int i = 0; i < data_flags.length; i++) {
            if(data_flags[i]){
                dataTypeString=dataTypeString+items[i]+" ";
            }
        }
        return dataTypeString;
    }
}
