package tjunet.navidoge.collect;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * Created by XinNoil on 2018/9/18.
 */

public class MyComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2)
    {
        ScanResult r1= (ScanResult )o1;
        ScanResult r2= (ScanResult )o2;
        return r1.BSSID.compareTo(r2.BSSID);
    }
}
