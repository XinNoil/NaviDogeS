package tjunet.navidoge.collect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import tjunet.navidoge.control.Controller;

/**
 * Created by XinNoil on 2018/9/18.
 *
 */

public class WiFiScan {
    Controller controller;
    public WifiManager wm;
    public List<ScanResult> wl;
    public int recordNo=0;
    private WifiScanResultReceiver wifiScanResultReceiver;
    public final Boolean scanned = false;

    public  WiFiScan(Controller controller) {
        this.controller=controller;
        Context context=controller.context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiScanResultReceiver = new WifiScanResultReceiver();
        context.registerReceiver(wifiScanResultReceiver, intentFilter);
        wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        OpenWifi();
    }

    private void OpenWifi() {
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
    }

    public void startScan(){
        wm.startScan();
    }

    public void getScanResult(){
        wl=wm.getScanResults();
    }

    public String getDisplayOutput(){
        wl=wm.getScanResults();
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("Length:").append(wl.size()).append("\n");

        for (ScanResult result: wl){
            if(result.level>=-90){
                stringBuilder.append(result.BSSID).append(" ").append(result.level).append(" ").append(result.frequency).append(" ").append(result.SSID).append("\n");
            }
        }

        return stringBuilder.toString();
    }

    public String getOutput(){
        StringBuilder output=new StringBuilder();
        MyComparator mc = new MyComparator();
        Collections.sort(wl, mc);
        long timestamp = new Date().getTime();
        for (ScanResult result : wl) {
            output.append((recordNo)).append(" ").append(result.BSSID).append(" ").append(result.level).append(" ").append(result.frequency).append(" ").append(timestamp).append("\n");
        }
        recordNo++;
        return output.toString();
    }

    public class WifiScanResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            Log.i("WiFi SCAN", "RESULT RETURN:"+new Date().getTime());
            synchronized (scanned) {
                scanned.notify();
            }
            //throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
