package com.example.gotix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gotix.model.User;
import com.example.gotix.store.UserStore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private RadioGroup roleGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        roleGroup = findViewById(R.id.role_group);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvCreateAccount = findViewById(R.id.tv_create_account);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int selectedId = roleGroup.getCheckedRadioButtonId();
        
        String role = (selectedId == R.id.radio_admin) ? "admin" : "end_user";

        User user = UserStore.findUser(username, password, role);

        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials for " + role, Toast.LENGTH_SHORT).show();
        }
    }
}
