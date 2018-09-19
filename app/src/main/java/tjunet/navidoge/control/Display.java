package tjunet.navidoge.control;

import android.content.Context;
import android.widget.TextView;

/**
 * Created by XinNoil on 2018/9/18.
 */

public class Display {
    private TextView[] textViews;
    private int viewNum;
    public Display(TextView[] textViews){
        this.textViews=textViews;
        viewNum=textViews.length;
    }
    public void setString(int type,String string){
        if(type<viewNum){
            textViews[type].setText(string);
        }
    }
    public String getString(int type){
        if(type<viewNum){
            return textViews[type].getText().toString();
        }
        return "";
    }
}
