package cn.edu.zjut.jinkai.supertelecontroller;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.webkit.SslErrorHandler;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Spinner spinner;
    private List<String> dataList;
    private ConsumerIrManager mCIR;
    private ArrayAdapter<String> adapter;
    private StringBuffer log_sb;
    private TextView textView;
    private int[] memInfo;
    private int tmp;
    private Handler handler;
    private TextView cpuText;
    private TextView memText;
    private TextView temText;
    private TextView flagText;
    private Button conn_button;
    private TextView isconn_text;
    private EditText ip_text;
    private String tmps;
    private String cpu;
    private String mem;
    private Button time_button;
    private String tem;
    private String flag;
    private Socket socket;
    boolean web_flag;
    private TextView log_textview;
    private LinearLayout home_layout;
    private android.support.v7.widget.GridLayout dashboard_layout;
    private WebView webView_layout;
    private LinearLayout webview;
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
                    webview.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    dashboard_layout.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.GONE);
                    home_layout.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_ssh:
                    //mTextMessage.setText(R.string.title_ssh);
                    webview.setVisibility(View.VISIBLE);
                    dashboard_layout.setVisibility(View.GONE);
                    home_layout.setVisibility(View.GONE);
                    if(web_flag==false) {
                        web_flag=true;
                        webView_layout.getSettings().setUseWideViewPort(true);
                        webView_layout.getSettings().setSupportZoom(true); //设置可以支持缩放
                        webView_layout.getSettings().setJavaScriptEnabled(true);
                        webView_layout.loadUrl("http://123.206.224.40:5080");

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
        webview=(LinearLayout)findViewById(R.id.webview_layout);
        log_sb=new StringBuffer("");
        textView=(TextView) findViewById(R.id.textView);
        spinner =(Spinner)findViewById(R.id.spinner);
        home_layout=(LinearLayout)findViewById(R.id.home_layout);
        dashboard_layout=(android.support.v7.widget.GridLayout)findViewById(R.id.dashboard_layout);
        webView_layout=(WebView)findViewById(R.id.webView);
        dataList=new ArrayList<String>();
        dataList.add("红外线");
        dataList.add("wifi");
        dataList.add("远程");
        time_button=(Button)findViewById(R.id.time_button);
        memText=(TextView) findViewById(R.id.mem_text);
        cpuText=(TextView) findViewById(R.id.cpu_text);
        temText=(TextView) findViewById(R.id.tem_text);
        flagText=(TextView) findViewById(R.id.flag_text);
        handler=new Handler();
        mem="0";
        cpu="0";
        tem="0";
        flag="0";
        conn_button=(Button)findViewById(R.id.conn_button);
        isconn_text=(TextView)findViewById(R.id.isconn_text);
        ip_text=(EditText)findViewById(R.id.ip_text);
        tmp=0;
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
        findViewById(R.id.LEDblue).setOnClickListener(mSendClickListener);
        findViewById(R.id.LEDgreen).setOnClickListener(mSendClickListener);
        findViewById(R.id.LEDred).setOnClickListener(mSendClickListener);
        findViewById(R.id.update_button).setOnClickListener(mSendSocketClickListener);
        findViewById(R.id.openview_buton).setOnClickListener(openView);
        conn_button.setOnClickListener(mConnClickListener);
        mTextMessage = (TextView) findViewById(R.id.textView);
        time_button.setOnClickListener(timeDelay);
    }
    View.OnClickListener mConnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            conn(v);
        }
    };
    View.OnClickListener timeDelay=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            delay(v);
            //findViewById(R.id.update_button).performClick();
        }
    };
    View.OnClickListener openView=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                       Intent intent = new Intent();
                        //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("http://123.206.224.40:5080");
                        intent.setData(content_url);
                        startActivity(intent);
        }
    };

    View.OnClickListener mSendSocketClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            send(v);
        }
    };
    View.OnClickListener mSendClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void onClick(View v) {
            if (!mCIR.hasIrEmitter()) {
                //Log.e(TAG, "未找到红外发身器！");
                log_text("发送红外信号失败，未找到红外发身器");
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
            //日志输出
            log_text("发送红外信号成功");
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

    public void conn(View v) {
        new Thread() {

            @Override
            public void run() {
                if(ip_text.getText().toString().length()<7)
                {
                    tmps="ip为空";
                    handler.post(runnableUi);
                    return;
                }
                try {

                    socket = new Socket(ip_text.getText().toString(), 9050);
                    handler.post(isconn);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    log_text("连接失败");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(socket!=null)
                {
                    tmps="连接成功"+socket;
                    handler.post(runnableUi);
                }
//                try {
//                    DataInputStream in=new DataInputStream(socket.getInputStream());
//                    tmps=in.readUTF();
//                    handler.post(runnableUi);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }.start();
    }
    public void send(View v) {
        new Thread() {
            @Override
            public void run() {
                try {
                    socket.getOutputStream();
                    DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                    writer.writeUTF("132");

                    //writer.close();
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    mem = in.readUTF();
                    System.out.println("client:" + mem);
                    cpu = in.readUTF();
                    System.out.println("client:" + cpu);
                    tem = in.readUTF();
                    System.out.println("client:" + tem);
                    flag = in.readUTF();
                    System.out.println("client:" + flag);
                    handler.post(setmem);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                tmps="获取成功";
                   handler.post(runnableUi);
            }
        }.start();
    }
    public void delay(View v)
    {
        new Thread() {
            @Override
            public void run() {
                Timer timer = new Timer();

                TimerTask task = new TimerTask() {
                    public void run() {
                        handler.post(updateClick);
                    }
                };
                timer.schedule(task,0,3000);
            }
        }.start();
    }
    public void log_text(String s)
    {
        tmp++;
        log_sb.insert(0,tmp+":"+s+"\n");
        log_textview.setText(log_sb);
    }
    Runnable runnableUi=new Runnable() {
        @Override
        public void run() {
            log_text(tmps);
        }
    };
    Runnable isconn=new Runnable() {
        @Override
        public void run() {
            isconn_text.setText("已连接");

        }
    };
    Runnable setmem=new Runnable() {
        @Override
        public void run() {
            memText.setText("MEM："+mem);
            if(cpu.length()>3)
            cpuText.setText("CPU："+cpu.substring(0,4));
            else
                cpuText.setText("CPU："+cpu);
            temText.setText("TEM："+tem);
            flagText.setText("FLAG："+flag);

        }
    };
    Runnable updateClick=new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.update_button).performClick();//每次需要执行的代码放到这里面。

        }
    };
}
