package com.example.gotix;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotix.server.LocalApiServer;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private LocalApiServer server;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole != null) {
            Toast.makeText(this, "Logged in as: " + userRole, Toast.LENGTH_SHORT).show();
        }

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        server = new LocalApiServer(this, 8080);
        try {
            server.start();
            webView.loadUrl("http://127.0.0.1:8080/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
    }
}
