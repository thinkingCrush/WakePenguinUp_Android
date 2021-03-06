package kr.Thinkingcrush.WakePenguinUp.View.Floating;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;

import kr.Thinkingcrush.WakePenguinUp.MainActivity;
import kr.Thinkingcrush.WakePenguinUp.R;
import kr.Thinkingcrush.WakePenguinUp.Tool.Dlog;

public class FloatingService extends Service implements View.OnClickListener, View.OnTouchListener {

    private WindowManager.LayoutParams mParams;
    private WindowManager.LayoutParams gifParams;
    private WindowManager.LayoutParams timerParams;
    private WindowManager mWindowManager;
    private float START_X, START_Y;
    private int PREV_X, PREV_Y;
    private int MAX_X = -1, MAX_Y = -1;

    private static FloatingGauge fg_outGauge;
    private static TextView tv_floating_count;
    private static LinearLayout ll_floating;
    private static ImageView iv_floating_lock;
    private static RelativeLayout rl_outFloatingLayout;
    private static ImageView iv_gifImage;
    private static TextView tv_timerText;

    private static CoordinatorLayout out_coordinatorLayout;
    private static CoordinatorLayout out_gifLayout;
    private static CoordinatorLayout out_timerLayout;
    public static android.app.NotificationManager notification_Manager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initCreate();
        initForgroundService();
    }
    SharedPreferences settingXY;
    SharedPreferences.Editor settingXYEditor ;
    @SuppressLint({"CommitPrefEdits", "ClickableViewAccessibility"})
    private void initCreate(){
        try{

            //View
            settingXY = getSharedPreferences("FloatingXY",0);
            settingXYEditor = settingXY.edit();

            ContextThemeWrapper ctx = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_NoActionBar);
            out_coordinatorLayout = (CoordinatorLayout) LayoutInflater
                    .from(ctx)
                    .inflate(R.layout.service_floating, null);
            out_coordinatorLayout.getViewTreeObserver().addOnWindowFocusChangeListener(onWindowFocusChangeListener);
            out_coordinatorLayout.setStatusBarBackgroundColor(getBaseContext().getResources().getColor(R.color.blank,null));

            out_gifLayout = (CoordinatorLayout) LayoutInflater
                    .from(ctx)
                    .inflate(R.layout.service_gif, null);
            out_gifLayout.getViewTreeObserver().addOnWindowFocusChangeListener(onWindowFocusChangeListener);
            out_gifLayout.setStatusBarBackgroundColor(getBaseContext().getResources().getColor(R.color.blank,null));

            out_timerLayout = (CoordinatorLayout) LayoutInflater
                    .from(ctx)
                    .inflate(R.layout.service_timer, null);
            out_timerLayout.getViewTreeObserver().addOnWindowFocusChangeListener(onWindowFocusChangeListener);
            out_timerLayout.setStatusBarBackgroundColor(getBaseContext().getResources().getColor(R.color.blank,null));


            fg_outGauge = out_coordinatorLayout.findViewById(R.id.fg_floating_count);
            tv_floating_count = out_coordinatorLayout.findViewById(R.id.tv_floating_count);
            ll_floating = out_coordinatorLayout.findViewById(R.id.ll_floating);
            iv_floating_lock = out_coordinatorLayout.findViewById(R.id.iv_floating_lock);
            rl_outFloatingLayout = out_coordinatorLayout.findViewById(R.id.rl_outfloatingLayout);
            iv_gifImage = out_gifLayout.findViewById(R.id.iv_gifImage);
            tv_timerText = out_timerLayout.findViewById(R.id.tv_timerText);
            rl_outFloatingLayout.setOnClickListener(this);
            rl_outFloatingLayout.setOnTouchListener(this);

            if(MainActivity.TouchLockFlag){
                fg_outGauge.setVisibility(View.GONE);
                tv_floating_count.setVisibility(View.GONE);
                ll_floating.setVisibility(View.GONE);
                iv_floating_lock.setVisibility(View.GONE);
                rl_outFloatingLayout.setVisibility(View.GONE);
            }

            new FloatingViewController().initHandler(getBaseContext(),
                    tv_floating_count,ll_floating,iv_floating_lock,fg_outGauge, rl_outFloatingLayout,iv_gifImage,tv_timerText);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }else {
                mParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }
            mParams.gravity = Gravity.LEFT | Gravity.TOP;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gifParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }else {
                gifParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }
            gifParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timerParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }else {
                timerParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
            }
            timerParams.gravity = Gravity.CENTER | Gravity.TOP;

            int settingX = settingXY.getInt("settingX",-1);
            int settingY = settingXY.getInt("settingY",-1);
            if(settingX==-1 && settingY==-1){
                try{
                    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    Display display = null;
                    if (window != null) {
                        display = window.getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        display.getMetrics(displayMetrics);
                        settingX = ((size.x/2)-(dpToPx(getBaseContext(),88)/2));
                        settingY = (int) (size.y-(dpToPx(getBaseContext(),88)*1.2));
                    }
                }catch (Exception e){
                    settingX = 50;
                    settingY = 200;
                    e.printStackTrace();
                }

            }
            mParams.x = settingX;
            mParams.y = settingY;

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (mWindowManager != null) {
                mWindowManager.addView(out_gifLayout,gifParams);
                mWindowManager.addView(out_coordinatorLayout, mParams);
                mWindowManager.addView(out_timerLayout,timerParams);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int dpToPx(Context context, float dp){
        try{
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.getResources().getDisplayMetrics());
            return px;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 100;
    }


    @Override
    public void onDestroy() {

        if(mWindowManager != null) {
            if(out_coordinatorLayout != null) mWindowManager.removeView(out_coordinatorLayout);
        }
        try{
            stopForeground(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    ViewTreeObserver.OnWindowFocusChangeListener onWindowFocusChangeListener = new ViewTreeObserver.OnWindowFocusChangeListener() {
        @Override
        public void onWindowFocusChanged(boolean hasFocus) {

            setMaxPosition();
        }
    };

    private void setMaxPosition() {

        try {
            Display display = mWindowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            float viewSize = dpToPx(getBaseContext(),88);
            MAX_X = (int)(size.x-viewSize);
            MAX_Y = (int)(size.y-viewSize);

        }catch (Exception e){
            MAX_X = 800;
            MAX_Y = 600;
        }


    }

    private static boolean FloatingClicked = false;
    public static void setFloatingClicked (boolean clicked){
        FloatingClicked = clicked;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_outfloatingLayout :
                if(!FloatingClicked){
                    new FloatingViewController().screenLock(getBaseContext());
                    setFloatingClicked(true);
                }else{
                    //취소
                    setFloatingClicked(false);
                    new FloatingViewController().screenLockCancel();
                }

                break;
        }
    }

    private boolean moveFAB = false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                try {
                    moveFAB = false;
                    if(MAX_X == -1) {
                        setMaxPosition();
                    }
                    START_X = event.getRawX();
                    START_Y = event.getRawY();
                    PREV_X = mParams.x;
                    PREV_Y = mParams.y;
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                try {
                    int x = (int)(event.getRawX() - START_X);
                    int y = (int)(event.getRawY() - START_Y);
                    mParams.x = PREV_X + x;
                    mParams.y = PREV_Y + y;
                    optimizePosition();
                    mWindowManager.updateViewLayout(out_coordinatorLayout, mParams);
                    if(((x <= 30)&&(x>=-30))&&((y <= 30)&&(y>=-30))){
                        moveFAB = false;
                    }else {
                        moveFAB = true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;

            case MotionEvent.ACTION_UP :
                try {
                    settingXYEditor.putInt("settingX",mParams.x);
                    settingXYEditor.putInt("settingY",mParams.y);
                    settingXYEditor.commit();

                    if(rl_outFloatingLayout != null){
                        rl_outFloatingLayout.setPressed(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                break;
        }


        return moveFAB;
    }


    private void optimizePosition() {
        float px = convertDptoPixel(25,getBaseContext());

        if(mParams.x > MAX_X) mParams.x = MAX_X;
        if(mParams.y > MAX_Y) mParams.y = MAX_Y;
        if(mParams.x < 0) mParams.x = 0;
        if(mParams.y < px) mParams.y = (int)px;
    }
    private float convertDptoPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi/160f);
    }

    private void initForgroundService(){
        int smallIcon = R.drawable.icon_phone;
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channelMessage = new NotificationChannel("GPS", getBaseContext().getResources().getString(R.string.service_tag), NotificationManager.IMPORTANCE_LOW);
                channelMessage.setDescription(getBaseContext().getResources().getString(R.string.service_message));
                channelMessage.setShowBadge(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channelMessage);
                }
                PendingIntent pendingIntent = null;
                Intent backIntent = new Intent(getApplicationContext(),MainActivity.class);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                pendingIntent = PendingIntent.getActivity(getBaseContext(),0,backIntent,0);
                Notification notification_oreo = new Notification.Builder(getBaseContext(),"GPS")
                        .setContentTitle(getBaseContext().getResources().getString(R.string.service_title))
                        .setContentText(getBaseContext().getResources().getString(R.string.service_title))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(smallIcon).build();
                notification_oreo.flags = 16;
                startForeground(smallIcon,notification_oreo);

            }else {
                if (notification_Manager != null) {
                    notification_Manager.cancelAll();
                }
                notification_Manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = null;
                Intent backIntent = new Intent(getApplicationContext(), MainActivity.class);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                pendingIntent = PendingIntent.getActivity(getBaseContext(),0,backIntent,0);
                Notification notification = new NotificationCompat.Builder(this,"GPS")
                        .setContentTitle(getBaseContext().getResources().getString(R.string.service_title))
                        .setContentText(getBaseContext().getResources().getString(R.string.service_title))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(smallIcon).build();
                Dlog.e("startForeground 2222");
                startForeground(smallIcon,notification);

            }
        }catch (Exception e){
            e.printStackTrace();
            //new ErrorLogManager().saveErrorLog(e);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(mParams != null){
            int x = mParams.x;
            int y = mParams.y;
            int preMaxX = MAX_X;
            int preMaxY = MAX_Y;

            int percentX = (x * MAX_X)/preMaxX;
            int percentY = (y * MAX_Y)/preMaxY;
            mParams.x = percentX;
            mParams.y = percentY;

            setMaxPosition();
            optimizePosition();

            mWindowManager.updateViewLayout(out_coordinatorLayout, mParams);
        }
    }
}
