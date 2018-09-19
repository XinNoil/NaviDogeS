package tjunet.navidoge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private int buttonNum=1;
    private Button[] buttons=new Button[buttonNum];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtons(buttonNum);
    }
    public void setButtons(int buttonNum){
        buttons[0]=findViewById(R.id.button_collect);
        ButtonListener buttonListener=new ButtonListener();
        for (int i=0;i<buttonNum;i++){
            buttons[i].setOnClickListener(buttonListener);
        }
    }

    class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            Intent intent;
            switch (v.getId()){
                case R.id.button_collect:
                    intent=new Intent(MainActivity.this,DataActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }
}
