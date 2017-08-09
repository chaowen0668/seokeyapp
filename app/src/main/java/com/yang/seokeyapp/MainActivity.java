package com.yang.seokeyapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    WebView webview;
    TextView contentTv;
    EditText et;
    EditText targetEt;
    Button searchBtn ;
    int page = 1;
    String baiduSearchUrl = null;
    boolean isFind = false;
    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        initView();
    }

    private void initView(){
        et = (EditText)findViewById(R.id.et);
        targetEt = (EditText)findViewById(R.id.targetEt);
        searchBtn = (Button)findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(this);
        contentTv = (TextView)findViewById(R.id.content);
        webview = (WebView) findViewById(R.id.webview);
        //ProxyUtils.setProxy(webview,"180.102.153.224",8118);
        // 设置允许加载混合内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.getSettings().setDefaultTextEncodingName("utf-8");
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (error.getPrimaryError() == SslError.SSL_INVALID) {
                    handler.proceed();
                } else {
                    handler.cancel();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
               /* if(isFind){
                    contentTv.setText("打开目标网站完成，等待下一次打开");
                }else{
                    contentTv.setText("找不到目标网站，请重新搜索");
                }*/

                super.onPageFinished(view, url);
            }
        });
       // connetedVpn();

        //webview.loadUrl("http://www.ip138.com");
    }

    private void cleanCache(){
        webview.clearCache(true);
        webview.clearHistory();
        webview.clearFormData();
        CookieSyncManager cookieSyncManager =  CookieSyncManager.createInstance(webview.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.searchBtn:
                webview.loadUrl(null);
                page =1;
                isFind = false;
                final String key = et.getText().toString();
                String target = targetEt.getText().toString();
                if(TextUtils.isEmpty(key)){
                    return;
                }
               // webview.loadData(content, "text/html; charset=UTF-8", null);
               webview.loadUrl("https://www.baidu.com/s?wd="+key);
                parseData(key,target);
                break;
        }
    }

    /**
     *
     * @param key  搜索关键字
     * @param target  要匹配的内容
     */
    public void parseData(final String key, String target){
        if(TextUtils.isEmpty(target)){
            return;
        }
        if(page == 1){
            baiduSearchUrl = "https://www.baidu.com/s?wd="+key;
        }

        try {
            Document doc = Jsoup.connect(baiduSearchUrl).get();
            String content = doc.html().toString();
            Elements elements = doc.select("div.f13");

            for (int i = 0; i < elements.size(); i++) {
                contentTv.setText("正在搜索第"+page+"页");
                //打印 <a>标签里面的title
                String url = elements.get(i).select("a.c-showurl").text();
                final String baiduurl = elements.get(i).select("a.c-showurl").attr("href");
                if(url.contains(target)){
                    isFind = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contentTv.setText("找到目标，正在打开");
                            webview.loadUrl(null);
                            webview.loadUrl(baiduurl);
                        }
                    });

                    break;
                }
                Log.i("mytag",url);
                Log.i("mytag",baiduurl);
            }

            if(!isFind){

                ++page;
                if(page <=10){
                    Element pageElements = doc.getElementById("page");
                    Elements aelemens = pageElements.select("a");
                    for (int i = 0; i < aelemens.size(); i++) {
                        String nextPage = aelemens.get(i).text();
                        if(nextPage.equals(page+"")){
                            baiduSearchUrl = "https://www.baidu.com"+aelemens.get(i).attr("href");
                            webview.loadUrl(null);
                            webview.loadUrl(baiduSearchUrl);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    parseData(key,targetEt.getText().toString());
                                }
                            },3000);

                            break;
                        }
                        Log.i("mytag",aelemens.get(i).text());

                    }
                }else{
                        //结束搜索
                        contentTv.setText("找不到目标网站");
                        webview.loadUrl("https://www.baidu.com");
                }



            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 连接
     */
    public void connetedVpn(){
        int vpnOperateType = 2;
        String province = "所有";
        String city = "";
        Intent intent = new Intent("com.chuangdian.ipjlsdk.VPN_OPERATE");
        intent.putExtra("username", "mctk");        //IP精灵登录账号名
        intent.putExtra("password", "a5291314");     //IP精灵账号密码
        intent.putExtra("opertype", vpnOperateType);
        intent.putExtra("province", province);
        intent.putExtra("city", city);
        sendBroadcast(intent);
    }

    /**
     * 断开连接
     */
    public void closeIpConnet(){
      //1 - 关闭当前动态IP加速器连接; 2 - 切换当前动态IP加速器连接IP
        int vpnOperateType = 2;
        Intent intent = new Intent("com.chuangdian.ipjlsdk.VPN_OPERATE");
        intent.putExtra("opertype", vpnOperateType);
        sendBroadcast(intent);
    }


    class VpnOperateResultBcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int operateResult = intent.getIntExtra("operresult", 0);
            //operateResult的值列表如下
            // 0 -无结果
            // 1 - 完成关闭
            // 2 - 连接成功
            // 3 - 连接失败
            // 4 - 当前指定区域没有线路
            // 5 - 异常断开
            // 6 - 恢复连接
            // 7 - 需要点击信任对话框
            // 8 - App退出
            // 9 - IP精灵用户未登录
            //……根据返回的结果类型，指定自己的代码逻辑
            if(operateResult == 0){
                contentTv.setText("无结果");
            }else if(operateResult == 1){
                contentTv.setText("完成关闭");
            }else if(operateResult == 2){
                contentTv.setText("连接成功,可以开始搜索");
            }else if(operateResult == 3){
                contentTv.setText("连接失败");
            }else if(operateResult == 4){
                contentTv.setText("当前指定区域没有线路");
            }else if(operateResult == 5){
                contentTv.setText("异常断开");
            }else if(operateResult == 6){
                contentTv.setText("恢复连接");
            }else if(operateResult == 9){
                contentTv.setText("IP精灵用户未登录");
            }
        }
    }
}
