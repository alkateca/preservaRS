package com.alkateca.preservars;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.alkateca.preservars.models.Postagem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeLineActivity extends AppCompatActivity {

    private LinearLayout containerPostagens;
    private DatabaseReference databaseRef;
    private String uidUsuarioLogado;
    private View rootView;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        rootView = findViewById(android.R.id.content);
        containerPostagens = findViewById(R.id.containerPostagens);

        uidUsuarioLogado = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("postagens");

        recuperarPostagens();
    }

    private void recuperarPostagens() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpa a tela antes de recarregar
                containerPostagens.removeAllViews();

                List<Postagem> listaTemporaria = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Postagem postagem = ds.getValue(Postagem.class);
                    if (postagem != null) listaTemporaria.add(postagem);
                }


                Collections.sort(listaTemporaria, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));


                for (Postagem p : listaTemporaria) {
                    desenharPostagemNaTela(p);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(rootView, "Erro ao carregar a timeline", Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    private void desenharPostagemNaTela(Postagem postagem) {

        View viewDaPostagem = LayoutInflater.from(this).inflate(R.layout.item_postagem, containerPostagens, false);

        TextView titulo = viewDaPostagem.findViewById(R.id.txtTituloPostagem);
        TextView descricao = viewDaPostagem.findViewById(R.id.txtDescricaoPostagem);
        ImageView imagem = viewDaPostagem.findViewById(R.id.imgPostagem);
        ImageButton btnEditar = viewDaPostagem.findViewById(R.id.btnEditar);
        ImageButton btnExcluir = viewDaPostagem.findViewById(R.id.btnExcluir);

        titulo.setText(postagem.getTitulo());
        descricao.setText(postagem.getDescricao());


        carregarImagemNativamente(postagem.getUrlImagem(), imagem);


        if (postagem.getIdUser().equals(uidUsuarioLogado)) {
            btnEditar.setVisibility(View.VISIBLE);
            btnExcluir.setVisibility(View.VISIBLE);
            btnEditar.setOnClickListener(v -> editarPostagem(postagem));
            btnExcluir.setOnClickListener(v -> excluirPostagem(postagem));
        } else {
            btnEditar.setVisibility(View.GONE);
            btnExcluir.setVisibility(View.GONE);
        }


        containerPostagens.addView(viewDaPostagem);
    }


    private void editarPostagem(Postagem postagem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Postagem");

        View viewDialog = LayoutInflater.from(this).inflate(R.layout.activity_new_post, null);
        viewDialog.findViewById(R.id.btnTirarFoto).setVisibility(View.GONE);
        viewDialog.findViewById(R.id.imgPreview).setVisibility(View.GONE);
        viewDialog.findViewById(R.id.btnPublicar).setVisibility(View.GONE);

        EditText edtTitulo = viewDialog.findViewById(R.id.txtTitulo);
        EditText edtDescricao = viewDialog.findViewById(R.id.txtDescricao);

        edtTitulo.setText(postagem.getTitulo());
        edtDescricao.setText(postagem.getDescricao());

        builder.setView(viewDialog);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoTitulo = edtTitulo.getText().toString().trim();
            String novaDescricao = edtDescricao.getText().toString().trim();

            if (!novoTitulo.isEmpty() && !novaDescricao.isEmpty()) {
                postagem.setTitulo(novoTitulo);
                postagem.setDescricao(novaDescricao);

                databaseRef.child(postagem.getIdPost()).setValue(postagem)
                        .addOnSuccessListener(aVoid -> Snackbar.make(rootView, "Atualizada!", Snackbar.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void excluirPostagem(Postagem postagem) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Deseja apagar esta publicação?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    databaseRef.child(postagem.getIdPost()).removeValue()
                            .addOnSuccessListener(aVoid -> Snackbar.make(rootView, "Removida", Snackbar.LENGTH_SHORT).show());
                })
                .setNegativeButton("Não", null)
                .show();
    }


    private void carregarImagemNativamente(String urlImagem, ImageView imageView) {
        imageView.setTag(urlImagem);
        imageView.setImageBitmap(null);

        executor.execute(() -> {
            try {
                URL url = new URL(urlImagem);
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setDoInput(true);
                conexao.connect();

                InputStream input = conexao.getInputStream();
                Bitmap bitmapDecodificado = BitmapFactory.decodeStream(input);

                mainHandler.post(() -> {
                    if (imageView.getTag() != null && imageView.getTag().equals(urlImagem)) {
                        imageView.setImageBitmap(bitmapDecodificado);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}