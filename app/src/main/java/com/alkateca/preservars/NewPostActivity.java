package com.alkateca.preservars;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alkateca.preservars.models.Postagem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class NewPostActivity extends AppCompatActivity {

    private Bitmap imagemSelecionada;
    private View rootView;

    private EditText txtTitulo;
    private EditText txtDescricao;
    private ImageView imgPreview;

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.TakePicturePreview(),
            result -> {
            if (result != null){
                imagemSelecionada = result;
                imgPreview.setImageBitmap(imagemSelecionada);
            } else {
                Snackbar.make(rootView, "Captura de imagem cancelada", Snackbar.LENGTH_SHORT).show();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_post);

        rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTitulo = findViewById(R.id.txtTitulo);
        txtDescricao = findViewById(R.id.txtDescricao);
        imgPreview = findViewById(R.id.imgPreview);
        Button btnTirarFoto = findViewById(R.id.btnTirarFoto);
        Button btnPublicar = findViewById(R.id.btnPublicar);

        btnTirarFoto.setOnClickListener(v -> cameraLauncher.launch(null));
        btnPublicar.setOnClickListener(v -> publicarPostagem());

    }

    private void publicarPostagem() {
        String titulo = txtTitulo.getText().toString().trim();
        String descricao = txtDescricao.getText().toString().trim();


        if (titulo.isEmpty() || descricao.isEmpty()) {
            Snackbar.make(rootView, "Preencha o título e a descrição", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (imagemSelecionada == null) {
            Snackbar.make(rootView, "Tire uma foto primeiro", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Snackbar.make(rootView, "Publicando postagem, aguarde...", Snackbar.LENGTH_INDEFINITE).show();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemSelecionada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dadosImagem = baos.toByteArray();


        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String nomeArquivoImagem = uidUsuario + "_" + System.currentTimeMillis() + ".jpg";


        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("postagens")
                .child(nomeArquivoImagem);

        storageRef.putBytes(dadosImagem)
                .addOnSuccessListener(taskSnapshot -> {

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String urlImagem = uri.toString();
                        salvarDadosNoDatabase(titulo, descricao, urlImagem, uidUsuario);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UPLOAD", "Erro no Storage", e);
                    Snackbar.make(rootView, "Erro ao enviar a imagem", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void salvarDadosNoDatabase(String titulo, String descricao, String urlImagem, String uidUsuario) {


        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("postagens");


        String idPostagem = databaseRef.push().getKey();


        Postagem novaPostagem = new Postagem(
                idPostagem,
                titulo,
                descricao,
                urlImagem,
                uidUsuario,
                System.currentTimeMillis()
        );

        if (idPostagem != null) {

            databaseRef.child(idPostagem).setValue(novaPostagem)
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(rootView, "Postagem publicada com sucesso!", Snackbar.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DATABASE", "Erro no Banco", e);
                        Snackbar.make(rootView, "Erro ao salvar postagem", Snackbar.LENGTH_SHORT).show();
                    });
        }
    }
}