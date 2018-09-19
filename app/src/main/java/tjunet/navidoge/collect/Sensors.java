package tjunet.navidoge.collect;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import Jama.Matrix;
import tjunet.navidoge.control.Controller;

/**
 * Created by XinNoil on 2018/9/19.
 */

public class Sensors extends Activity implements SensorEventListener {
    Controller controller;
    final SensorManager sensorManager;
    final Sensor accelerateSensor;
    final Sensor magneticFieldSensor;
    final Sensor gyroscopeSensor;
    final Sensor gravitySensor;
    public boolean haveGravity=false;
    private int sensorNum=4;
    private final int INDEX_ACCELEROMETER=1;
    private final int INDEX_MAGNETIC_FIELD=2;
    private final int INDEX_GYROSCOPE=3;
    private final int INDEX_GRAVITY=4;
    static final double NS2S = 1.0f / 1000000000.0f;
    public float time_constant = 0.01f;
    public float alpha = 0.8f;
    double[][] values = new double[sensorNum+1][3];
    public float[] gra=new float[3];
    public float [] Rm = new float[9];
    long[] timestamps=new long[sensorNum+1];
    public int recordMode=1;
    public Gyroscope gyroscope=new Gyroscope();
    public Compass compass=new Compass();
    public Gravity gravity=new Gravity();
    public boolean eventMode=false;
    public boolean data_flags[];
    private StringBuilder [] outputBuilders=new StringBuilder[sensorNum+1];
    java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
    public Sensors(Controller controller){
        this.controller=controller;
        Context context=controller.context;
        initialize();
        initOutputBuilders();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, accelerateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        haveGravity=sensorManager.getSensorList(Sensor.TYPE_GRAVITY).size()>0;
    }

    public void initialize(){
        for (int i = 1; i <= sensorNum; i++) {
            values[i][0]=0;
            values[i][1]=0;
            values[i][2]=0;
            outputBuilders[i]=new StringBuilder();
        }
        gra[0]=0;
        gra[1]=0;
        gra[2]=0;
        for (int i=0;i<9;i++){
            Rm[i]=0;
        }
    }

    public void initOutputBuilders(){
        for (int i = 1; i <= sensorNum; i++) {
            outputBuilders[i]=new StringBuilder();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        int SensorINDEX=getSensorIndex(event.sensor.getType());
        double dT=getDt(event.timestamp,SensorINDEX);
        timestamps[SensorINDEX]=event.timestamp;
        for (int i=0;i<3;i++)
            values[SensorINDEX][i] = event.values[i];
        if(controller.logging){
            if(eventMode&&controller.data_flags[SensorINDEX]){
                outputBuilders[SensorINDEX].append(getCurrentOutput(SensorINDEX));
            }
        }
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if(!haveGravity){ // Isolate the force of gravity with the low-pass filter.
                    alpha = time_constant/(time_constant+(float)dT);
                    values[getSensorIndex(Sensor.TYPE_GRAVITY)][0] = gra[0] = alpha * gra[0] + (1 - alpha) * event.values[0];
                    values[getSensorIndex(Sensor.TYPE_GRAVITY)][1] = gra[1] = alpha * gra[1] + (1 - alpha) * event.values[1];
                    values[getSensorIndex(Sensor.TYPE_GRAVITY)][2] = gra[2] = alpha * gra[2] + (1 - alpha) * event.values[2];
                    gravity.calElements(gra);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                float[] acc = toFloat(values[1]);
                SensorManager.getRotationMatrix(Rm, null, acc, event.values);
                compass.setR(Rm);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscope.updateGyroscope(new Matrix(values[3], 3), dT);
                break;
            case Sensor.TYPE_GRAVITY:
                gra[0]=event.values[0];
                gra[1]=event.values[1];
                gra[2]=event.values[2];
                gravity.calElements(gra);
                break;
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
        //sensorManager.unregisterListener(this);
    }

    public String getDisplayOutput(int index){
        StringBuilder str=new StringBuilder();
        str.append("Body:").append(formatV(values[index],3)).append("\n");
        switch (index){
            case INDEX_ACCELEROMETER:
                break;
            case INDEX_MAGNETIC_FIELD:
                double hori[]=gravity.getTransform(values[index]);
                double value_hori=Math.sqrt(hori[0] * hori[0] + hori[1] * hori[1]);
                double magnitude=Math.sqrt(value_hori*value_hori+hori[2]*hori[2]);
                str.append("Magnitude:").append(df.format(magnitude)).append("\n");
                str.append("Hori:").append(df.format(value_hori)).append(" Vert:").append(df.format(hori[2])).append("\n");
                str.append("Rc:\n").append(formatR(compass.getR(),false));
                str.append("vc: ").append(formatV(compass.getValues(),3)).append("\n");
                break;
            case INDEX_GYROSCOPE:
                str.append("Rg:\n").append(formatR(gyroscope.getR(),false));
                str.append("vg: ").append(formatV(gyroscope.getValues(),3));
                break;
            case INDEX_GRAVITY:
                str.append("Hori :").append(formatV(gravity.getTransform(values[index]),3)).append("\n");
                if (haveGravity)
                    str.append("Hard sensor\n");
                else
                    str.append("Soft sensor\n");
                break;
        }
        str.append("\n");
        return str.toString();
    }

    public String getCurrentOutput(int index){
        StringBuilder outputBuilder=new StringBuilder();
        outputBuilder.append(index).append(" ").append(timestamps[index]).append(" ");
        outputBuilder.append(values[index][0]).append(" ").append(values[index][1]).append(" ").append(values[index][2]).append(" ");
        switch (index){
            case INDEX_MAGNETIC_FIELD:
                outputBuilder.append(formatR(gyroscope.getR(),true)).append(" ");
                outputBuilder.append(formatV(values[4],3)).append(" ");
                outputBuilder.append(formatV(gravity.getTransform(values[index]),3)).append(" ");
                break;
        }
        outputBuilder.append("\n");
        return outputBuilder.toString();
    }

    public String getOutput(int index){
        String output;
        if(eventMode){
            output=outputBuilders[index].toString();
            outputBuilders[index]=new StringBuilder();
        } else{
            output=getCurrentOutput(index);
        }
        return output;
    }

    public int getSensorIndex(int SensorTYPE){
        switch (SensorTYPE){
            case Sensor.TYPE_ACCELEROMETER:
                return INDEX_ACCELEROMETER;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return INDEX_MAGNETIC_FIELD;
            case Sensor.TYPE_GYROSCOPE:
                return INDEX_GYROSCOPE;
            case Sensor.TYPE_GRAVITY:
                return INDEX_GRAVITY;
        }
        return 0;
    }

    private double getDt(long timestamp,int SensorINDEX){
        if (timestamps[SensorINDEX]>0)
            return (timestamp - timestamps[SensorINDEX]) * NS2S;
        else
            return 0;
    }

    public String formatR(double [][]M,boolean row){
        StringBuilder r=new StringBuilder();
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                r=r.append(df.format(M[i][j])+" ");
            }
            if(!row)
                r=r.append("\n");
        }
        return r.toString();
    }

    public String formatV(double []V,int n){
        StringBuilder r=new StringBuilder();
        for (int j=0;j<n;j++){
            r=r.append(df.format(V[j])+"  ");
        }
        return r.toString();
    }

    public float [] toFloat(double [] value){
        float [] v=new float[3];
        for (int i=0;i<value.length;i++){
            v[i]=(float)value[i];
        }
        return v;
    }
}
