package com.alkateca.preservars;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button btnLogin;
    private Button btnCadastro;
    private TextInputEditText txtEmail;
    private TextInputEditText txtSenha;


    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            MainPageRedirect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        btnLogin = findViewById(R.id.btnLogin);
        btnCadastro = findViewById(R.id.btnCadastro);

        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> login());
        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });


    }

    private void login(){

        String email = txtEmail.getText().toString().trim();
        String senha = txtSenha.getText().toString().trim();
        View rootView = findViewById(android.R.id.content);

        if (email.isEmpty() || senha.isEmpty()){
            Snackbar.make(rootView, "Preencha todos os campos", Snackbar.LENGTH_SHORT).show();
            return;
        }


        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("AUTH", "Login com sucesso");
                        Snackbar.make(rootView, "Login realizado com sucesso", Snackbar.LENGTH_SHORT).show();

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            MainPageRedirect();
                        }, 1500);

                    } else {
                        Log.w("AUTH", "Falha no login", task.getException());
                        Snackbar.make(rootView,  "Erro de Autenticação", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void MainPageRedirect(){
        Intent intent = new Intent(MainActivity.this, TimeLineActivity.class);
        startActivity(intent);
    }

}