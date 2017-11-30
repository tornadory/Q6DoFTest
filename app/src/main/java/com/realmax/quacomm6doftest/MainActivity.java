package com.realmax.quacomm6doftest;

import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Button updateBtn;
    TextView tv;
    WebView webView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surface_id_0);
        surfaceView.setMinimumHeight(1);
        surfaceView.setMinimumWidth(1);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int eventAction = motionEvent.getAction();
                Log.d("6dof", "touch event " + eventAction);
                return false;
            }
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                //try to initialize the svr service
                //surfaceHolder = surfaceHolder;
                //tv.setText(stringFromJNI(surfaceHolder.getSurface()));

                initSVR(surfaceHolder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });

        tv = findViewById(R.id.sample_text);
        tv.setVisibility(View.INVISIBLE);

        updateBtn = findViewById(R.id.updatebtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //try to update 6-dof data
                tv.setText(stringFromJNI(null));
            }
        });
        updateBtn.setVisibility(View.INVISIBLE);

        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setDisplayZoomControls(true);

        webView.setWebViewClient(new SSLTolerentWebViewClient());

        //webView.getSettings().setSafeBrowsingEnabled(false);
        QualComm6DofObject qob = new QualComm6DofObject();
        webView.addJavascriptInterface(qob, "sdof");

        //webView.loadUrl("file:///android_asset/www/demo0.html"); //use local file, due to can not access the external URL, maybe related with WiFi or App permission
        webView.loadUrl("https://139.196.195.57/pub/test/demo.html");
    }

    private class SSLTolerentWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }


    public String Reset6Dof(){
        //return stringFromJNI(surfaceHolder.getSurface());
        return stringFromJNI(surfaceView.getHolder().getSurface());
    }




    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(Surface surface);
    public native boolean initSVR(Surface surface1);
    public native String getPose();
    public native boolean exitSVR();
    public native boolean resetSVR();  //TBD

    @Override
    protected void onStop() {
        super.onStop();
        exitSVR();
    }

    class QualComm6DofObject{
        @android.webkit.JavascriptInterface
        public String get6Dof(){
            return getPose();
            //return stringFromJNI(null);
            //return "try to get 6dof data from QualComm6DofObject";
        }

        @android.webkit.JavascriptInterface
        public boolean reset6Dof(){
            return resetSVR(); //TBD
            //return stringFromJNI(surfaceHolder.getSurface());
            //return Reset6Dof(); //error
            //eglCreateWindowSurface() returned error 12291
            //E/libEGL: eglCreateWindowSurface:481 error 3003 (EGL_BAD_ALLOC)
            //E/libEGL: eglCreateWindowSurface: native_window_api_connect (win=0xd24ff008) failed (0xffffffea) (already connected to another API?)
            //--> so need exitVR first, then re-enter VR mode?
        }
    }
}
