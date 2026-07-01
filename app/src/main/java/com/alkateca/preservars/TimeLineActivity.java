package com.alkateca.preservars;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alkateca.preservars.adapters.PostagemAdapter;
import com.alkateca.preservars.models.Postagem;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeLineActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PostagemAdapter.OnPostagemClickListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private PostagemAdapter adapter;
    private List<Postagem> listaPostagens = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mAuth = FirebaseAuth.getInstance();
        // Inicializa a referência do nó "postagens" no Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("postagens");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.menu_sobre, R.string.menu_sobre);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostagemAdapter(listaPostagens, this);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        carregarDadosDoFirebase();
    }

    private void carregarDadosDoFirebase() {
        // addValueEventListener escuta as mudanças no banco de dados em tempo real
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPostagens.clear(); // Limpa a lista antes de popular para não duplicar dados
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Postagem postagem = dataSnapshot.getValue(Postagem.class);
                    if (postagem != null) {
                        // Salva a Key do Firebase como ID do objeto para podermos editar/deletar depois
                        postagem.setId(dataSnapshot.getKey());
                        listaPostagens.add(postagem);
                    }
                }
                adapter.atualizarLista(listaPostagens);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TimeLineActivity.this, "Erro ao carregar dados do Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_postagens) {
            // Já estamos na timeline
        } else if (id == R.id.nav_nova_postagem) {
            startActivity(new Intent(this, NewPostActivity.class));
        } else if (id == R.id.nav_sobre) {
            abrirDialogSobre();
        } else if (id == R.id.nav_deslogar) {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            abrirDialogSobre();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirDialogSobre() {
        SobreDialogFragment dialog = new SobreDialogFragment();
        dialog.show(getSupportFragmentManager(), "SobreDialog");
    }

    @Override
    public void onEditClick(Postagem postagem) {
        // Cria um AlertDialog customizado com campos de texto para edição
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Postagem");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText editTitulo = new EditText(this);
        editTitulo.setText(postagem.getTitulo());
        editTitulo.setHint("Título");
        layout.addView(editTitulo);

        final EditText editDescricao = new EditText(this);
        editDescricao.setText(postagem.getDescricao());
        editDescricao.setHint("Descrição");
        layout.addView(editDescricao);

        builder.setView(layout);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoTitulo = editTitulo.getText().toString().trim();
            String novaDesc = editDescricao.getText().toString().trim();

            if (!novoTitulo.isEmpty() && !novaDesc.isEmpty()) {
                postagem.setTitulo(novoTitulo);
                postagem.setDescricao(novaDesc);

                // Atualiza o nó no Firebase
                if (postagem.getId() != null) {
                    databaseReference.child(postagem.getId()).setValue(postagem)
                            .addOnSuccessListener(aVoid -> Snackbar.make(recyclerView, "Postagem atualizada com sucesso!", Snackbar.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(TimeLineActivity.this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(this, "Os campos não podem estar vazios.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onDeleteClick(Postagem postagem) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir Postagem")
                .setMessage("Tem certeza que deseja excluir esta postagem permanentemente?")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> {
                    if (postagem.getId() != null) {

                        databaseReference.child(postagem.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Snackbar.make(recyclerView, "Postagem excluída!", Snackbar.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Toast.makeText(TimeLineActivity.this, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}