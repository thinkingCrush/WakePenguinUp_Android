package kr.Thinkingcrush.WakePenguinUp.Tool;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DialogButtonClickEffect implements View.OnTouchListener {
    int textColor = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable drawable = null;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                if (v instanceof ImageView){
                    drawable = ((ImageView)v).getDrawable();
                    drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                }else if(v instanceof Button){
                    drawable = v.getBackground();
                    drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                }else if(v instanceof TextView){
                    textColor = ((TextView)v).getCurrentTextColor();
                    ((TextView)v).setTextColor(((textColor & 0x00FFFFFF) | 0x77000000));
                }else if (v instanceof RelativeLayout){
                    v.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                if (v instanceof ImageView){
                    drawable = ((ImageView)v).getDrawable();
                    drawable.clearColorFilter();
                    v.invalidate();
                }else if(v instanceof Button){
                    drawable = ((Button)v).getBackground();
                    drawable.clearColorFilter();
                    v.invalidate();
                }else if(v instanceof TextView){
                    ((TextView)v).setTextColor(textColor);
                }else if (v instanceof RelativeLayout){
                    v.getBackground().clearColorFilter();
                }
                break;
        }
        return false;
    }
}