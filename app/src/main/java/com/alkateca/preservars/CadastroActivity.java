package com.alkateca.preservars;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnNewUser;
    private TextInputEditText txtNewUserEmail;
    private TextInputEditText txtNewUserSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        btnNewUser = findViewById(R.id.btnNewUser);
        txtNewUserEmail = findViewById(R.id.txtNewUserEmail);
        txtNewUserSenha = findViewById(R.id.txtNewUserSenha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        btnNewUser.setOnClickListener(v -> novoUsuario());

    }

    private void novoUsuario(){

        String email = txtNewUserEmail.getText().toString().trim();
        String senha = txtNewUserSenha.getText().toString().trim();
        View rootView = findViewById(android.R.id.content);

        if (email.isEmpty() || senha.isEmpty()){
            Snackbar.make(rootView, "Preencha todos os campos", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("LOGIN", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Snackbar.make(rootView, "Usuário criado com sucesso, redirecionando", Snackbar.LENGTH_SHORT).show();

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                NewPostRedirect();
                            }, 1500);

                        } else {
                            Log.w("LOGIN", "createUserWithEmail:failure",task.getException());
                            Snackbar.make(rootView, "Usuário Erro ao criar o usuário", Snackbar.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    private void NewPostRedirect(){
        Intent intent = new Intent(CadastroActivity.this, NewPostActivity.class);
        startActivity(intent);
    }
}