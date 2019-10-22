package kr.Tcrush.WakePenguinUp.View;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Objects;

import im.delight.android.webview.AdvancedWebView;
import kr.Tcrush.WakePenguinUp.Data.UrlArray;
import kr.Tcrush.WakePenguinUp.MainActivity;
import kr.Tcrush.WakePenguinUp.R;
import kr.Tcrush.WakePenguinUp.Tool.ChromeClientController;
import kr.Tcrush.WakePenguinUp.Tool.DialogSupport;
import kr.Tcrush.WakePenguinUp.Tool.Dlog;
import kr.Tcrush.WakePenguinUp.Tool.SharedWPU;
import kr.Tcrush.WakePenguinUp.View.Floating.FloatingService;

public class WebViewFragment extends Fragment implements View.OnClickListener, AdvancedWebView.Listener {
    static EditText et_url;
    ImageView iv_star,iv_list;
    LinearLayout ll_webToolbar;
    static ImageView iv_noneWebView ;
    TextView tv_error_message ;


    static AdvancedWebView wv_webview;
    //WebView wv_webview;

    InputMethodManager inputMethodManager;

    Handler viewHandler ;

    RelativeLayout rl_webview_error ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview,container,false);
        MainActivity.setPageNum(PageNumber.WebViewFragment.ordinal());
        initView(view);
        initHandler();

        return view;
    }
    private void initView (View view){
        //TEST
        et_url=view.findViewById(R.id.et_url);
        et_url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH){
                    try{
                        String inputData =String.valueOf(textView.getText()) ;
                        inputData = checkUrlText(inputData);
                        loadUrl(inputData);
                        //키보드 숨기기
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(et_url.getWindowToken(),0);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        iv_star = view.findViewById(R.id.iv_star);
        iv_list = view.findViewById(R.id.iv_list);
        iv_noneWebView = view.findViewById(R.id.iv_noneWebView);
        rl_webview_error = view.findViewById(R.id.rl_webview_error);
        tv_error_message = view.findViewById(R.id.tv_error_message);

        initImageViewHandler();


        iv_star.setOnClickListener(this);
        iv_list.setOnClickListener(new MainActivity.DrawerClickListener(getContext()));

        wv_webview = view.findViewById(R.id.wv_webview);
        wv_webview.getSettings().setJavaScriptEnabled(true);
        wv_webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv_webview.getSettings().setSupportMultipleWindows(true);
        wv_webview.addJavascriptInterface(new MyJavaScriptInterface(),
                "android");


        wv_webview.setWebChromeClient(new ChromeClientController(getActivity()));

        WebViewClient mWebViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
            };
        };
        wv_webview.setWebViewClient(mWebViewClient);

        ArrayList<UrlArray> urlArrays = new SharedWPU().getUrlArrayList(getContext());
        try{
            if(urlArrays != null && !urlArrays.isEmpty()){
                MainActivity.startFloating(getContext());
                loadUrl(urlArrays.get(0).url);
            }else{
                //내용이 없음
                urlUnknownError();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        ll_webToolbar = view.findViewById(R.id.ll_webToolbar);

        new SharedWPU().setFirstUser(Objects.requireNonNull(getContext()));
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(et_url.getWindowToken(),0);
        }

        /*if(!MainActivity.FloatingStart){
            try{
                Context context = getContext();
                if(context != null){
                    Intent intent = MainActivity.intent;
                    if(intent == null){
                        intent = new Intent(context, FloatingService.class);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Objects.requireNonNull(context).startForegroundService(intent);
                    }else {
                        Objects.requireNonNull(context).startService(intent);
                    }

                }
                MainActivity.FloatingStart = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/



    }

    private static Handler viewImageHandler = null;
    private final int WebViewFlag = 1;
    private final int ImageUnknownFlag =2;
    private final int ImageErrorFlag = 3;
    private void initImageViewHandler(){
        try{
            viewImageHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {

                    switch (msg.what){
                        case WebViewFlag :
                            Dlog.e("test 1111 WebViewFlag");
                            MainActivity.startFloating(MainActivity.mainContext);
                            wv_webview.setVisibility(View.VISIBLE);
                            rl_webview_error.setVisibility(View.GONE);
                            tv_error_message.setVisibility(View.GONE);
                            iv_noneWebView.setVisibility(View.GONE);
                            break;
                        case ImageUnknownFlag :
                            Dlog.e("test 2222 ImageUnknownFlag");
                            wv_webview.setVisibility(View.GONE);
                            rl_webview_error.setVisibility(View.VISIBLE);
                            tv_error_message.setVisibility(View.VISIBLE);
                            iv_noneWebView.setVisibility(View.VISIBLE);
                            iv_noneWebView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_findfail_url,null));
                            tv_error_message.setText("바로가기 항목이 비어있습니다.");
                            break;
                        case ImageErrorFlag :
                            Dlog.e("test 3333 ImageErrorFlag");
                            MainActivity.stopFloating(MainActivity.mainContext);
                            wv_webview.setVisibility(View.GONE);
                            rl_webview_error.setVisibility(View.VISIBLE);
                            tv_error_message.setVisibility(View.VISIBLE);
                            iv_noneWebView.setVisibility(View.VISIBLE);
                            iv_noneWebView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.img_unknown_url,null));
                            tv_error_message.setText("URL 주소를 확인 해 주세요.");
                            break;
                    }

                    return true;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void webViewVisible(){
        if(viewImageHandler != null){
            viewImageHandler.obtainMessage(WebViewFlag,null).sendToTarget();
        }
    }
    public void urlUnknownError(){
        if(viewImageHandler != null){
            viewImageHandler.obtainMessage(ImageUnknownFlag,null).sendToTarget();
        }
    }
    public void urlFindFailError(){
        if(viewImageHandler != null){
            viewImageHandler.obtainMessage(ImageErrorFlag,null).sendToTarget();
        }
    }

    private String checkUrlText(String data){
        //URL 이 어떻게 이상한지 체크해야하는데,??
        try{
            data = data.replace("\r","");
            data = data.replace("\n","");

            if(data.length()>=5 && !data.substring(0,5).contains("http")){
                data = "https://"+data;
            }


            return data;
        }catch (Exception e){
            e.printStackTrace();
        }

        return data;
    }

    private final int viewStarOn = 1;
    private final int viewStarOff = 2;
    private void initHandler (){
        try{
            if(viewHandler == null){
                viewHandler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        switch (message.what){
                            case viewStarOn :
                                iv_star.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.icon_star,null));
                                break;
                            case viewStarOff :
                                iv_star.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.icon_star_off,null));
                                break;

                        }
                        return true;
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean canGoback (){
        if(wv_webview != null){
            return wv_webview.canGoBack();
        }
        return false;
    }
    public void goBack (){
        if(wv_webview != null){
            wv_webview.goBack();
        }

    }




    @Override
    public void onPause() {
        super.onPause();
    }




    @Override
    public void onResume() {
        super.onResume();
        try{
            if(wv_webview!=null){
                wv_webview.onResume();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void loadUrl (String url){
        try{
            if(wv_webview!=null){
                wv_webview.setVisibility(View.VISIBLE);
                iv_noneWebView.setVisibility(View.GONE);
                wv_webview.loadUrl(url);
                et_url.setText(url);
            }
        }catch (Exception e){
            //URL 잘못됨
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_star :

                new DialogSupport().addItemDialog(getContext(),String.valueOf(et_url.getText()));
                break;
        }
    }

    private boolean checkStar (String webViewUrl){
        try{
            String currentUrl = webViewUrl;
            if(currentUrl != null){
                ArrayList<UrlArray> urlArrays = new SharedWPU().getUrlArrayList(getContext());
                for(UrlArray urlArray : urlArrays){
                    String url = urlArray.url;
                    currentUrl = currentUrl.replace("https://www.","");
                    currentUrl = currentUrl.replace("http://www.","");
                    currentUrl = currentUrl.replace("https://m.","");
                    currentUrl = currentUrl.replace("http://m.","");
                    url = url.replace("https://www.","");
                    url = url.replace("http://www.","");
                    url = url.replace("https://m.","");
                    url = url.replace("http://m.","");

                    if(currentUrl.contains(url)||url.contains(currentUrl)){
                        return true;
                    }
                }
            }else{
                Dlog.e("currentUrl = null");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            try{
                Dlog.e("url : " + url);
                if(et_url!=null && !url.equals("about:blank")) {
                    et_url.setText(url);
                    if (checkStar(url)) {
                        iv_star.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_star, null));
                    } else {
                        iv_star.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_star_off, null));
                    }
                    webViewVisible();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
