package com.oussama.ppuruntime;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {
    private WebView web;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebView.setWebContentsDebuggingEnabled(false);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new android.webkit.WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.addJavascriptInterface(new NativeBridge(), "PPUNative");
        web.addJavascriptInterface(new ShareBridge(), "PPUShare");
        web.loadUrl("https://appassets.androidplatform.net/assets/ppu_runtime.html");
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) handleIncoming(getIntent().getData());
    }

    private void handleIncoming(Uri uri) {
        if (uri == null) return;
        new Thread(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                if (in == null) throw new IllegalStateException("Cannot open file");
                byte[] bytes = readAll(in);
                String json = PPUCore.inspectBytes(bytes);
                runOnUiThread(() -> web.evaluateJavascript("window.ppuIncoming&&window.ppuIncoming("+jsQuote(json)+")", null));
            } catch(Exception e) {
                String msg=e.getMessage()==null?"carrier inspection failed":e.getMessage();
                runOnUiThread(() -> web.evaluateJavascript("window.ppuIncoming&&window.ppuIncoming("+jsQuote(msg)+")", null));
            }
        }).start();
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i); setIntent(i);
        if (Intent.ACTION_VIEW.equals(i.getAction())) handleIncoming(i.getData());
    }

    private static String jsQuote(String s) {
        String x=s==null?"":s;
        return "'" + x.replace("\\","\\\\").replace("'","\\'").replace("\n","\\n").replace("\r","\\r") + "'";
    }

    private static byte[] readAll(InputStream in) throws Exception {
        byte[] buf=new byte[8192]; java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream(); int n;
        while((n=in.read(buf))!=-1) out.write(buf,0,n); return out.toByteArray();
    }

    private final class NativeBridge {
        @JavascriptInterface public String platform(){return "Android native PPU host";}
        @JavascriptInterface public String inspectCarrier(){return PPUCore.bundledReport(MainActivity.this);}
        @JavascriptInterface public String inspectJpegBase64(String b64){
            try{return PPUCore.inspectBytes(Base64.decode(b64,Base64.DEFAULT));}
            catch(Exception e){return "{\"error\":\"invalid carrier data\"}";}
        }
        @JavascriptInterface public String runMonteCarlo(int n){return PPUCore.monteCarlo(0x4F555353414D4131L,n);}
        @JavascriptInterface public String runPrimeKernel(int n,int rounds){return PPUCore.primeKernel(n,rounds);}
        @JavascriptInterface public String selfTest(){
            try{return new org.json.JSONObject().put("platform",platform())
                .put("carrier",inspectCarrier())
                .put("monteCarlo",new org.json.JSONObject(runMonteCarlo(250000)))
                .put("primeKernel",new org.json.JSONObject(runPrimeKernel(5000,2))).toString();}
            catch(Exception e){return "{\"error\":\"self test\"}";}
        }
        @JavascriptInterface public void toast(String s){runOnUiThread(()->Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show());}
        @JavascriptInterface public void settings(){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}
    }

    private final class ShareBridge {
        @JavascriptInterface public void shareText(String text){
            Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT,text);
            startActivity(Intent.createChooser(i,"Share PPU result"));
        }
        @JavascriptInterface public void shareJpeg(String b64){
            try{
                byte[] bytes=Base64.decode(b64,Base64.DEFAULT); File f=new File(getCacheDir(),"ppu-frame.jpg");
                try(FileOutputStream o=new FileOutputStream(f)){o.write(bytes);}
                Uri u=FileProvider.getUriForFile(MainActivity.this,getPackageName()+".fileprovider",f);
                Intent i=new Intent(Intent.ACTION_SEND); i.setType("image/jpeg"); i.putExtra(Intent.EXTRA_STREAM,u);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(i,"Share PPU frame"));
            }catch(Exception e){Toast.makeText(MainActivity.this,"JPEG sharing failed",Toast.LENGTH_SHORT).show();}
        }
    }
}
