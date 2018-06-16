package cn.edu.zjut.jinkai.supertelecontroller;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Spinner spinner;
    private List<String> dataList;
    private ConsumerIrManager mCIR;
    private ArrayAdapter<String> adapter;
    private TextView textView;
    boolean web_flag;
    private TextView log_textview;
    private LinearLayout home_layout;
    private android.support.v7.widget.GridLayout dashboard_layout;
    private WebView webView_layout;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("InlinedApi")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    home_layout.setVisibility(View.VISIBLE);
                    dashboard_layout.setVisibility(View.GONE);
                    webView_layout.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    dashboard_layout.setVisibility(View.VISIBLE);
                    webView_layout.setVisibility(View.GONE);
                    home_layout.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_ssh:
                    //mTextMessage.setText(R.string.title_ssh);
                    webView_layout.setVisibility(View.VISIBLE);
                    dashboard_layout.setVisibility(View.GONE);
                    home_layout.setVisibility(View.GONE);
                    if(web_flag==false) {
                        web_flag=true;
                        webView_layout.getSettings().setUseWideViewPort(true);
                        webView_layout.getSettings().setJavaScriptEnabled(true);
                        webView_layout.getSettings().setSupportZoom(true); //设置可以支持缩放
                        webView_layout.loadUrl("https://www.baidu.com");
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCIR=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        log_textview=(TextView)findViewById(R.id.log_textview);
        log_textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        web_flag=false;
        textView=(TextView) findViewById(R.id.textView);
        spinner =(Spinner)findViewById(R.id.spinner);
        home_layout=(LinearLayout)findViewById(R.id.home_layout);
        dashboard_layout=(android.support.v7.widget.GridLayout)findViewById(R.id.dashboard_layout);
        webView_layout=(WebView)findViewById(R.id.webview_layout);
        dataList=new ArrayList<String>();
        dataList.add("红外线");
        dataList.add("wifi");
        dataList.add("远程");
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dataList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textView.setText("您当前选择的是："+adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                textView.setText("请选择控制方式");

            }
        });
        mTextMessage = (TextView) findViewById(R.id.message);
        initViewsAndEvents();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private void initViewsAndEvents() {
        findViewById(R.id.reboot).setOnClickListener(mSendClickListener);
        findViewById(R.id.turnoff).setOnClickListener(mOnClickListener);
        mTextMessage = (TextView) findViewById(R.id.textView);
    }
    View.OnClickListener mSendClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onClick(View v) {
            if (!mCIR.hasIrEmitter()) {
                //Log.e(TAG, "未找到红外发身器！");
                return;
            }
            // 一种交替的载波序列模式，通过毫秒测量
            int[] pattern = { 1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625,
                    442, 625, 442, 625, 468, 625, 442, 625, 494, 572, 1614,
                    625, 1588, 625, 1614, 625, 494, 572, 442, 651, 442, 625,
                    442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442,
                    625, 494, 598, 442, 625, 442, 625, 520, 572, 442, 625, 442,
                    625, 442, 651, 1588, 625, 1614, 625, 1588, 625, 1614, 625,
                    1588, 625, 48958 };
            // 在38.4KHz条件下进行模式转换
            mCIR.transmit(38400, pattern);
        }
    };
    @SuppressLint("NewApi")
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            StringBuilder b = new StringBuilder();
            if (!mCIR.hasIrEmitter()) {
               mTextMessage.setText("未找到红外发身器！");
                return;
            }
            // 获得可用的载波频率范围
            ConsumerIrManager.CarrierFrequencyRange[] freqs = mCIR
                    .getCarrierFrequencies();
            b.append("IR Carrier Frequencies:\n");// 红外载波频率
            // 边里获取频率段
            for (ConsumerIrManager.CarrierFrequencyRange range : freqs) {
                b.append(String.format("  %d - %d\n",
                        range.getMinFrequency(), range.getMaxFrequency()));
            }
            mTextMessage.setText(b.toString());// 显示结果
        }
    };
}
