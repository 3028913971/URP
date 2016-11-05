package com.jin123d.urp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jin123d.Interface.GetNetDataListener;
import com.jin123d.Interface.UrpUserListener;
import com.jin123d.util.HttpUtil;
import com.jin123d.util.JsoupUtil;
import com.jin123d.util.UrpSp;
import com.jin123d.util.UrpUrl;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GetNetDataListener {
    private EditText et_user, et_pwd, et_yzm;
    private ImageView img_yzm;
    private ProgressBar pgb_yzm;
    private Button btn_login;
    private HttpClient httpClient;
    private String cookie;
    private Bitmap bitmap;
    private String zjh;
    private String mm;
    private String tv;
    private ProgressDialog progressDialog;
    private CheckBox chb_mm;
    private CheckBox chk_auto;
    private Toolbar toolbar;
    private LinearLayout lin_main;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UrpUrl.DATA_FAIL:
                    progressDialog.dismiss();
                    // shapeLoadingDialog.dismiss();
                    Snackbar.make(lin_main, getText(R.string.getDataTimeOut), Snackbar.LENGTH_SHORT)
                            .show();
                    break;
                case UrpUrl.YZM_SUCCESS:
                    img_yzm.setImageBitmap(bitmap);
                    pgb_yzm.setVisibility(View.INVISIBLE);
                    /*Toast.makeText(LoginActivity.this, cookie, Toast.LENGTH_SHORT)
                            .show();*/
                    break;
                case UrpUrl.DATA_SUCCESS:
                    progressDialog.dismiss();
                    JsoupUtil.isLogin(LoginActivity.this, tv, new UrpUserListener.UserStateListener() {
                        @Override
                        public void loginSuccess(Document document) {
                            if (chb_mm.isChecked()) {
                                UrpSp.setZjh(zjh);
                                UrpSp.setMm(mm);
                                UrpSp.setRememberMm(true);
                            } else {
                                UrpSp.setZjh(null);
                                UrpSp.setMm(null);
                            }
                            if (chk_auto.isChecked()) {
                                UrpSp.setAuto(true);
                            }
                            // UrpSp.setCookie(cookie);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void loginFailed(String errorMsg) {
                            Snackbar.make(lin_main, errorMsg, Snackbar.LENGTH_SHORT)
                                    .show();
                            getCode();
                        }
                    });
                    break;
                case UrpUrl.YZM_FAIL:
                    Snackbar.make(lin_main, getText(R.string.getYzmFail), Snackbar.LENGTH_SHORT)
                            .show();
                    pgb_yzm.setVisibility(View.INVISIBLE);
                    break;
                case UrpUrl.SESSION:
                    Toast.makeText(LoginActivity.this, getString(R.string.loginFail), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.login));
        if (UrpSp.getAuto()) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            httpClient = new DefaultHttpClient();
            initView();
            getCode();
        }
    }

    private void initView() {
        chb_mm = (CheckBox) findViewById(R.id.chk_mm);
        chk_auto = (CheckBox) findViewById(R.id.chk_auto);
        et_user = (EditText) findViewById(R.id.et_user);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        pgb_yzm = (ProgressBar) findViewById(R.id.pgb_yzm);
        btn_login = (Button) findViewById(R.id.btn_login);
        img_yzm = (ImageView) findViewById(R.id.img_yzm);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        lin_main = (LinearLayout) findViewById(R.id.lin_main);
        //--滑动视图开始
        // ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.list);
        //  scrollView.setScrollViewCallbacks(this);
        //---滑动视图结束
        setSupportActionBar(toolbar);
        pgb_yzm.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getText(R.string.logining));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        //shapeLoadingDialog =new ShapeLoadingDialog(this);
        //shapeLoadingDialog.setLoadingText("加载中...");
        btn_login.setOnClickListener(this);
        img_yzm.setOnClickListener(this);
        chk_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    chb_mm.setChecked(true);
                }
            }
        });
        //记住密码是否开启
        if (UrpSp.getRememberMM()) {
            et_user.setText(UrpSp.getZjh());
            et_pwd.setText(UrpSp.getMm());
            chb_mm.setChecked(true);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_yzm:
                getCode();
                break;
            case R.id.btn_login:
                if (et_yzm.getText().toString().length() == 0) {
                    Snackbar.make(lin_main, "验证码为空", Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    login();
                    progressDialog.show();
                    //shapeLoadingDialog.show();
                }
                break;
        }
    }

    private void getCode() {
        pgb_yzm.setVisibility(View.VISIBLE);
        et_yzm.setFocusable(true);
        et_yzm.setText(null);
        new Thread() {
            public void run() {
                bitmap = getcode();
                if (bitmap == null) {
                    handler.sendEmptyMessage(UrpUrl.YZM_FAIL);
                } else {
                    handler.sendEmptyMessage(UrpUrl.YZM_SUCCESS);
                }
            }
        }.start();
    }

    private void login() {
        new Thread() {
            public void run() {
                zjh = et_user.getText().toString();
                mm = et_pwd.getText().toString();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("zjh", zjh));
                params.add(new BasicNameValuePair("mm", mm));
                params.add(new BasicNameValuePair("v_yzm", et_yzm.getText()
                        .toString()));
                HttpUtil.doPost(
                        UrpUrl.URL + UrpUrl.URL_LOGIN, params, LoginActivity.this);
            }
        }.start();
    }


    //获取验证码
    public Bitmap getcode() {
        HttpPost httpPost = new HttpPost(
                UrpUrl.URL + UrpUrl.URL_YZM);
        HttpResponse httpResponse = null;
        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000); //设置连接超时
            HttpConnectionParams.setSoTimeout(params, 10000); //设置请求超时
            httpPost.setParams(params);
            httpResponse = httpClient.execute(httpPost);
            cookie = ((AbstractHttpClient) httpClient).getCookieStore()
                    .getCookies().get(0).getValue();
            UrpSp.setCookie(cookie);
            byte[] bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(UrpUrl.YZM_FAIL);
        }
        return bitmap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                String str = UrpUrl.URL;
                Snackbar.make(lin_main, str, Snackbar.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_about:
                startActivity(new Intent(this, WebActivity.class));
                break;
        }

        return true;
    }


    @Override
    public void getDataSuccess(String Data) {
        tv = Data;
        handler.sendEmptyMessage(UrpUrl.DATA_SUCCESS);
    }

    @Override
    public void getDataFail() {
        handler.sendEmptyMessage(UrpUrl.DATA_FAIL);
    }

    @Override
    public void getDataSession() {
        handler.sendEmptyMessage(UrpUrl.SESSION);
    }
}
