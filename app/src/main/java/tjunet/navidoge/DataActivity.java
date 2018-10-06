package tjunet.navidoge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import tjunet.navidoge.control.Controller;
import tjunet.navidoge.control.Display;

public class DataActivity extends AppCompatActivity {
    private int buttonNum=4;
    private Button[] buttons=new Button[buttonNum];
    private final static int DATA_TYPE=1;
    private final static int FILE_SET=2;
    private final static int OTHER_SET=3;

    public TextView[] textViews;
    Controller controller;
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        setTextViews();
        setButtons();
        display=new Display(textViews);
        controller=new Controller(this);
        controller.setDisplay(display);
    }

    public void setTextViews(){
        textViews=new TextView[2];
        textViews[0]=findViewById(R.id.setting_view);
        textViews[1]=findViewById(R.id.data_view);
        textViews[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.changeDisplayIndex();
            }
        });
    }

    public void setButtons(){
        buttons[0]=findViewById(R.id.button_type);
        buttons[1]=findViewById(R.id.button_file);
        buttons[2]=findViewById(R.id.button_log);
        buttons[3]=findViewById(R.id.button_set);
        ButtonListener buttonListener=new ButtonListener();
        for (int i=0;i<buttonNum;i++){
            buttons[i].setOnClickListener(buttonListener);
        }
    }

    class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.button_type:
                    showDialog(DATA_TYPE);
                    break;
                case R.id.button_file:
                    showDialog(FILE_SET);
                    break;
                case R.id.button_log:
                    if (controller.log_time>0){
                            controller.startLog();
                    }
                    else{
                        if (controller.logging){
                            controller.stopLog();
                            buttons[2].setText(R.string.button_log_start);
                        }
                        else{
                            controller.startLog();
                            buttons[2].setText(R.string.button_log_stop);
                        }
                    }
                    break;
                case R.id.button_set:
                    showDialog(OTHER_SET);
                    break;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        View view;
        switch (id) {
            case DATA_TYPE:
                builder.setTitle("DATA TYPE");
                builder.setMultiChoiceItems(R.array.data_type, controller.data_flags, new DialogInterface.OnMultiChoiceClickListener(){
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        controller.data_flags[which]=isChecked;
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        controller.saveSettings(Controller.SettingType.data_flags);
                        controller.loadSettings(Controller.SettingType.data_flags);
                        Toast.makeText(DataActivity.this, controller.getDataTypeString(), Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case FILE_SET:
                builder.setTitle("FILE SET");
                view = LayoutInflater.from(DataActivity.this).inflate(R.layout.dialog_file, null);
                builder.setView(view);
                final TextView textView=view.findViewById(R.id.textView);
                final EditText editText=view.findViewById(R.id.editText);
                final EditText editText2=view.findViewById(R.id.editText2);
                final Button button_next=view.findViewById(R.id.button_next);
                textView.setText(controller.fileSave.getFileName());
                editText.setText(String.valueOf(controller.fileSave.getDirNum()));
                editText2.setText(String.valueOf(controller.fileSave.getFileNum()));
                button_next.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        switch (v.getId()){
                            case R.id.button_next:
                                controller.fileSave.nextDir();
                                textView.setText(controller.fileSave.getFileName());
                                editText.setText(String.valueOf(controller.fileSave.getDirNum()));
                                editText2.setText(String.valueOf(controller.fileSave.getFileNum()));
                                break;
                        }
                    }
                });
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            controller.fileSave.setDirNum(editText.getText().toString());
                            controller.fileSave.setFileNum(editText2.getText().toString());
                            controller.saveSettings(Controller.SettingType.file);
                            controller.loadSettings(Controller.SettingType.file);
                            Toast.makeText(DataActivity.this, controller.fileSave.getFileName(), Toast.LENGTH_LONG).show();
                        } catch (Exception e){
                            Toast.makeText(DataActivity.this, "FAILED!", Toast.LENGTH_LONG).show();
                        }

                    }
                });
                break;
            case OTHER_SET:
                builder.setTitle("SETTING");
                view = LayoutInflater.from(DataActivity.this).inflate(R.layout.dialog_set, null);
                builder.setView(view);
                final TextView textView2=view.findViewById(R.id.textView2);
                final EditText editText3=view.findViewById(R.id.editText3);
                final Button button_interval=view.findViewById(R.id.button_interval);
                final Button button_time=view.findViewById(R.id.button_time);
                final ToggleButton toggleButton=view.findViewById(R.id.toggleButton);
                final ToggleButton toggleButton2=view.findViewById(R.id.toggleButton2);
                // set default value
                textView2.setText(controller.getSettingString());
                toggleButton.setChecked(controller.sensors.eventMode);
                toggleButton2.setChecked(controller.wifi_async_scan);

                button_interval.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try{
                            controller.interval=Integer.valueOf(editText3.getText().toString());
                            textView2.setText(controller.getSettingString());
                        } catch (Exception e){
                            Toast.makeText(DataActivity.this, "INVALID INPUT: "+editText3.getText().toString(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                button_time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try{
                            controller.log_time=Integer.valueOf(editText3.getText().toString());
                            controller.log_time_left=controller.log_time;
                            textView2.setText(controller.getSettingString());
                        } catch (Exception e){
                            Toast.makeText(DataActivity.this, "INVALID INPUT: "+editText3.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                        controller.sensors.eventMode=isChecked;
                    }
                });
                toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        controller.wifi_async_scan=isChecked;
                    }
                });
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.saveSettings(Controller.SettingType.other_settings);
                        controller.loadSettings(Controller.SettingType.other_settings);
                    }
                });
                break;
        }
        dialog=builder.create();
        return dialog;

    }
}
