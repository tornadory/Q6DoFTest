package com.RMSR.quacomm6doftest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final String TAG = "Q6DoFTest";
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Button updateBtn;
    TextView tv;
    WebView webView;

    boolean isRunning = false;

    boolean isWebReady = false;

    Timer timer;
    TimerTask timerTask;

    //begin BT
    private BluetoothAdapter mBluetoothAdapter;
    String btControllerData = "";

    public static final String WXYF000_SERVICE_NORMAL = "0000fe55-0000-1000-8000-00805f9b34fb";
    public static final String WXYF001_CHAR_NORMAL_NOTIFY = "00000001-1000-1000-8000-00805f9b34fb";
    public static final String WXYF002_CHAR_NORMAL_WRITE = "00000001-1000-1000-8000-00805f9b34fb";
    public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private class GattConnectCallback extends BluetoothGattCallback
    {
        private BluetoothGatt mGatt;
        private List<BluetoothGattService> mServices;

        public void onConnectionStateChange (BluetoothGatt gatt,
                                             int status,
                                             int newState)
        {
            Log.d(TAG, "New Status "+ String.valueOf(newState));
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                gatt.discoverServices();
            }
        }

        private void UpdateToast(String toast)
        {
            final String v = toast;
        }

        public void onServicesDiscovered (BluetoothGatt gatt, int status)
        {
            mGatt = gatt;
            mServices = mGatt.getServices();
            Log.d(TAG, "onServicesDiscovered");
            for(BluetoothGattService s :mServices)
            {
                String uuid_s = s.getUuid().toString();
                Log.d(TAG, "mServices"+ uuid_s.toString());
                if(uuid_s.equals(WXYF000_SERVICE_NORMAL))
                {
                    UpdateToast("Find TargetService");
                    Log.d(TAG, "Find TargetService");
                    for(BluetoothGattCharacteristic c : s.getCharacteristics())
                    {
                        if(c.getUuid().toString().equals(WXYF001_CHAR_NORMAL_NOTIFY))
                        {
                            mGatt.setCharacteristicNotification(c, true);
                            BluetoothGattDescriptor descriptor = c.getDescriptor(
                                    CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);

                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            Boolean Passed = mGatt.writeDescriptor(descriptor);
                            UpdateToast("Notification Enable" + String.valueOf(Passed));
                        }
                    }
                }

                Log.d(TAG, uuid_s);
            }
        }

        class J2JhelperBT implements Runnable{

            String data;
            J2JhelperBT(String s){
                //String call = "javascript:updateController(" + data + ")";
                data = s;
            }

            @Override
            public void run() {
                Log.d(TAG, "J2JhelperBT run with " + data); //data is OK
                //Log.d(7)
                //data = "{\"buttons\": 0, \"rotation\":{\"yaw\": -14.10,\"pitch\":-2.54,\"roll\":06.49}}";
                webView.loadUrl("javascript:updateController(\'"+ data + "\')");
            }
        }

        public void onCharacteristicChanged (BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic)
        {
            final byte[] data = characteristic.getValue();

            if(true)
            {
                ProtocolXWYF000Normal protocol = new ProtocolXWYF000Normal(data);
                //UpdateToast(protocol.toString());

//                webView.post(new J2JhelperBT(protocol.toJSON()));
                btControllerData = protocol.toJSON();

                int button = protocol.ioKeyBytes;
                if(button == 2){
                    //reset
                    Log.e(TAG, "button 2");
                    resetApp();
                }
                if(button == 4){
                    //quit
                    Log.e(TAG, "button 4");
                    gatt.disconnect();
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.disable();
                    finishAndRemoveTask();

//                    finish();
//                    System.exit(0);
                }
            }
            else
            {
                Log.d(TAG, "onCharacteristicChanged " + byteArray2String(data));
            }
        }

        private String byteArray2String(byte[] data)
        {
            final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0; i<data.length; i++){
                stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
                stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
                if(i < data.length-1)
                    stringBuilder.append(" ");
            }
            return stringBuilder.toString();
        }
    }
    //end BT

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                String data = getPose();
//                //webView.loadUrl("javascript:updateController(\""+ data + "\")");
//                webView.post(new J2Jhelper(data));
//            }
//        };

        surfaceView = findViewById(R.id.surface_id_0);
        surfaceView.setMinimumHeight(1);
        surfaceView.setMinimumWidth(1);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int eventAction = motionEvent.getAction();
                Log.d(TAG, "touch event " + eventAction);
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
                Log.e(TAG, "surface changed");

                initSVR(surfaceHolder.getSurface());
                isRunning = true;
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
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);

        webView.setWebViewClient(new SSLTolerentWebViewClient());

        //webView.getSettings().setSafeBrowsingEnabled(false);
        QualComm6DofObject qob = new QualComm6DofObject();
        webView.addJavascriptInterface(qob, "sdof");

//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Fcubes-with-controller.scene.json");

//        webView.loadUrl("https://139.196.195.57/pub/test/demo.html");
//        webView.loadUrl("file:///android_asset/www/demo.html");
        webView.loadUrl("file:///android_asset/www/demo.html"); //use local file, due to can not access the external URL, maybe related with WiFi or App permission
//        webView.loadUrl("file:///android_asset/www/demo0.html");
//        webView.loadUrl("file:///android_asset/www/demo00.html");

//        webView.loadUrl("https://139.196.195.57/pub/test/demo.html");


//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Fcubes-creator.scene.json");
//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Ftest222.scene.json");
//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Ftest6dofar.scene.json");
//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Fcube6.scene.json");
//        webView.loadUrl("https://139.196.195.57/realstudio/editor/player.html?url=..%2Fliteserver%2Fsrc%2Ffiles%2Fjohnnyz%2Fprojects%2Fcube6.scene.json");


//        webView.loadUrl("https://192.168.1.116/projects/published-project?url=../../projects/chEpiDcgYo3KXiQy/_main.scene.json");

//        webView.loadUrl("https://192.168.1.116/projects/published-project?url=../../projects/8Z0hWxIXhovbRZ3W/_main.scene.json");
        //
        //https://192.168.100.11/pub/realcenter/frontend/web/realstudio/editor/player.html?url=..%2F..%2Fprojects%2F7THKkZszyLThPRxs%2F_main.scene.json

//        webView.loadUrl("https://realcenter.realmax.com/projects/published-project?url=..%2F..%2Fprojects%2FcogkdTZ2qQPuqYuu%2F_main.scene.json"); //still alive project

    }

//    class J2Jhelper implements Runnable{
//
//        String data;
//        J2Jhelper(String s){
//            data = s;
//        }
//
//        @Override
//        public void run() {
//            //Log.d("Qualcomm6DoF", "J2JHelper run with " + data); //data is OK
//            data = data.replace("\"", "\\\""); //OK
//            Log.d(TAG, "J2JHelper change to " + data); //data is OK
//            webView.loadUrl("javascript:updateController(\""+ data + "\")");
//            //webView.loadUrl("javascript:updateController(\"{\"position\":{ \"x\":246.54, \"y\":6.11, \"z\":708.23},  \"rotation\": {\"x\":0.00, \"y\":-0.07, \"z\":1.00, \"w\":0.00}}\")");
//        }
//    }

    private class SSLTolerentWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "page loading finished - " + url);
            isWebReady = true;
            //timer.schedule(timerTask, 0, 200); // 200 is OK
        }
    }


    public String Reset6Dof(){
        //return stringFromJNI(surfaceHolder.getSurface());
        return stringFromJNI(surfaceView.getHolder().getSurface());
    }

    public void resetApp(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
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
    protected void onPause() {
        super.onPause();
        exitSVR();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isWebReady = false;
        isRunning = false;

        //begin BT
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.enable();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();


        for(BluetoothDevice d:devices)
        {
            Log.d(TAG, d.getName());

            d.connectGatt(this, true, new GattConnectCallback());
        }
        //end BT
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    class QualComm6DofObject{
        @android.webkit.JavascriptInterface
        public String get3dofController(){
            return btControllerData;
        }

        @android.webkit.JavascriptInterface
        public String get6Dof(){
            if(isRunning)
                return getPose();
            else
                return "";
            //return stringFromJNI(null);
            //return "try to get 6dof data from QualComm6DofObject";
        }

        @android.webkit.JavascriptInterface
        public void reset6Dof(){
            resetApp();
//            if(isRunning){
////                finish();
////                startActivity(getIntent());
//                finish();
//                overridePendingTransition(0, 0);
//                startActivity(getIntent());
//                overridePendingTransition(0, 0);
//
//            }
//            if(isRunning)
//                return resetSVR(); //TBD
//            else
//                return resetSVR();
            //return stringFromJNI(surfaceHolder.getSurface());
            //return Reset6Dof(); //error
            //eglCreateWindowSurface() returned error 12291
            //E/libEGL: eglCreateWindowSurface:481 error 3003 (EGL_BAD_ALLOC)
            //E/libEGL: eglCreateWindowSurface: native_window_api_connect (win=0xd24ff008) failed (0xffffffea) (already connected to another API?)
            //--> so need exitVR first, then re-enter VR mode?
        }
    }
}
