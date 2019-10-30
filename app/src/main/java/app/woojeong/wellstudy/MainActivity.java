package app.woojeong.wellstudy;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    long backKeyPressedTime = 0;
    InputMethodManager methodManager;
    SharedPreferences preferences;
    String token;
    WebView webView;
    WebView childView;

    String userAgent;

    ValueCallback mFilePathCallback;

    int childcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        methodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        methodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        setContentView(R.layout.activity_main);

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );
        getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;

        preferences = getSharedPreferences("pref", MODE_PRIVATE);
//        token = FirebaseInstanceId.getInstance().getToken();
        getWindow().addFlags(16777216);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setDatabaseEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setAppCachePath(dir.getPath());
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(false);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        webView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "well");
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(getResources().getString(R.string.url));
        }

        webView.loadUrl(getResources().getString(R.string.url));  //원하는 사이트의 주소
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, " onActivityResult ");

        Uri[] results = null;
        if(data != null){
            if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
                mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                mFilePathCallback = null;
            } else if (requestCode == 1) {

                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;

//            mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
//            mFilePathCallback = null;
            } else {
                mFilePathCallback.onReceiveValue(null);
            }
        } else {
            mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            mFilePathCallback = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, " onResume ");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, " onPause ");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, " onBackPressed");
        if (childcnt > 0) {
            childcnt = 0;
            webView.removeAllViews();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(MainActivity.this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                setResult(999);
                finish();
            }
        }
    }

    class MyWebViewClient extends WebViewClient {
        public boolean doFallback(WebView view, Intent parsedIntent) {
            Log.i(TAG, " MyWebViewClient doFallback");
            if (parsedIntent == null) {
                return false;
            }
            String fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url");
            if (fallbackUrl != null) {
                view.loadUrl(fallbackUrl);
                return true;
            }

            final String packageName = parsedIntent.getPackage();
            if (packageName != null) {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                builder.setMessage("설치 후 사용하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                android.support.v7.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, " MyWebViewClient onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i(TAG, " MyWebViewClient onPageFinished" + url);
        }


        @Override
        public void onLoadResource(WebView view, String url) {
            Log.i(TAG, " onLoadResource " + url);
            super.onLoadResource(view, url);
        }



        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, " MyWebViewClient shouldOverrideUrlLoading " + url);
            if (url.startsWith("tel:")) {
                Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(dial);
                return true;
            } else if (url.startsWith("sms:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
                return true;
            } else if (url.startsWith("intent:")) {

                    Intent intent = null;
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        if (webView.canGoBack()) {
                            webView.clearHistory();
                        }
                        return doFallback(view, intent);
                    }

                return true;
            } else if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                view.getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
               Log.i(TAG, "aaaaaaaaaaaaaaaaaa " + url);
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView w) {
            super.onCloseWindow(w);
            Log.i(TAG, " MyWebChromeClient");
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
            Log.i(TAG, " :: MyWebChromeClient : onShowFileChooser" + fileChooserParams.getMode());
            mFilePathCallback = filePathCallback;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(intent, fileChooserParams.getMode());
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.i(TAG, " onConsoleMessage " + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new android.support.v7.app.AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }



        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            Log.i(TAG, " :: MyWebChromeClient : onCreateWindow");
            Log.i(TAG, view.getUrl());

            childView = new WebView(MainActivity.this);

            childView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            WebSettings webSettings = childView.getSettings();
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setLoadWithOverviewMode(true);

            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportMultipleWindows(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
            webSettings.setUseWideViewPort(true);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            childView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onCloseWindow(WebView window) {
                    Log.i(TAG, " :: childView WebChromeClient : onCloseWindow");
                    window.setVisibility(View.GONE);
                    childcnt--;
                    webView.removeView(window);
                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                    Log.i(TAG, " :: childView WebChromeClient : onShowFileChooser");
                    mFilePathCallback = filePathCallback;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 0);
                    return true;
                }
            });
            childView.setWebViewClient(new WebViewClient() {

                public boolean doFallback(WebView view, Intent parsedIntent) {
                    Log.i(TAG, " :: childView WebViewClient : doFallback");
                    if (parsedIntent == null) {
                        return false;
                    }
                    String fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }

                    final String packageName = parsedIntent.getPackage();
                    if (packageName != null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("설치 후 사용하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                    Log.i(TAG, " :: childView WebChromeClient : shouldOverrideUrlLoading");
                    Log.i(TAG, " 222 " + url);
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        webView.removeAllViews();
                        childcnt++;
                        webView.addView(childView);
                        childView.loadUrl(url);
                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);
                                webView.setScrollY(0);
                            }
                        }, 500);
                    } else {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                                intent.setDataAndType(Uri.parse(url), "video/*");
                            }
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            if (url.startsWith("intent:") && webView.canGoBack()) {
                                Intent intent = null;
                                try {
                                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                    startActivity(intent);
                                } catch (Exception e1) {
                                    e.printStackTrace();
                                    if (webView.canGoBack()) {
                                        webView.clearHistory();
                                    }
                                    return doFallback(view, intent);
                                }
                                return true;
                            } else if (url.startsWith("market://details?id=")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } else if (url.contains("twitter") || url.contains("kakao") || url.contains("naver") || url.contains("facebook") || url.contains("band")){
                                Log.i(TAG, "bbbbbbbbbbbbbbbbbbbbbb " + url);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        }
                        if (url.startsWith("tel:")) {
                            Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(dial);
                            return true;
                        }
                    }
                    return true;
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }
    }

    public class WebViewJavaScriptInterface {

        public WebViewJavaScriptInterface(Context context) {
        }

        @JavascriptInterface
        public void print() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doPrint(webView);
                }
            });
        }

        @JavascriptInterface
        public void sns(final String url) {
            Log.i(TAG, " sns" + url);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
    }

    public void doPrint(WebView webView) {
        Log.i(TAG, " createWebPagePrint");
        PrintManager printManager = (PrintManager) MainActivity.this.getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Document";
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }
}
