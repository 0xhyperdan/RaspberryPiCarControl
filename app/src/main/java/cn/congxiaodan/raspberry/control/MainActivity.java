package cn.congxiaodan.raspberry.control;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements OnTouchListener {

    private static final String HOST = "10.0.0.1";
    private static final int CONTROL_PORT = 7891;
    private static final int CAMERA_PORT = 8081;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FORWARD = 119;
    private static final int BACK = 115;
    private static final int LEFT = 97;
    private static final int RIGHT = 100;
    private static final int STOP = 32;
    private WebView mWebView;
    private RelativeLayout mRoot;
    private TextView tvDistance;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton btnForward = findViewById(R.id.forward);
        ImageButton btnBack = findViewById(R.id.back);
        ImageButton btnLeft = findViewById(R.id.left);
        ImageButton btnRight = findViewById(R.id.right);
        ImageButton btnStop = findViewById(R.id.stop);
        mWebView = findViewById(R.id.web_camera);
        tvDistance = findViewById(R.id.distance);
        mRoot = findViewById(R.id.root);

        btnForward.setOnTouchListener(this);
        btnBack.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);
        btnStop.setOnTouchListener(this);

        mWebView.loadUrl(HOST + ":" + CAMERA_PORT);
        mWebView.setWebViewClient(new WebViewClient());
    }

    private void send(int comm) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(HOST, CONTROL_PORT);
                OutputStream out = socket.getOutputStream();
                out.write(comm);
                out.flush();
                InputStream in = socket.getInputStream();
                int read;
                while ((read = in.read()) != -1) {
                    Log.i(TAG, "data: " + (char) read);
                    tvDistance.setText(getString(R.string.fmt_distance, read));
                }
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.forward:
                    send(FORWARD);
                    break;
                case R.id.back:
                    send(BACK);
                    break;
                case R.id.left:
                    send(LEFT);
                    break;
                case R.id.right:
                    send(RIGHT);
                    break;
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            send(STOP);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
            mRoot.removeView(mWebView);
            mWebView = null;
        }
    }
}
