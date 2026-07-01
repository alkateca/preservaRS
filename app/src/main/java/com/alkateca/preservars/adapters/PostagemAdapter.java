package com.alkateca.preservars.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alkateca.preservars.R;
import com.alkateca.preservars.models.Postagem;
import java.util.ArrayList;
import java.util.List;

public class PostagemAdapter extends RecyclerView.Adapter<PostagemAdapter.PostagemViewHolder> implements Filterable {

    private List<Postagem> listaOriginal;
    private List<Postagem> listaFiltrada;
    private OnPostagemClickListener listener;

    public interface OnPostagemClickListener {
        void onEditClick(Postagem postagem);
        void onDeleteClick(Postagem postagem);
    }

    public PostagemAdapter(List<Postagem> listaOriginal, OnPostagemClickListener listener) {
        this.listaOriginal = listaOriginal;
        this.listaFiltrada = new ArrayList<>(listaOriginal);
        this.listener = listener;
    }

    public void atualizarLista(List<Postagem> novaLista) {
        this.listaOriginal = novaLista;
        this.listaFiltrada = new ArrayList<>(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_postagem, parent, false);
        return new PostagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostagemViewHolder holder, int position) {
        Postagem postagem = listaFiltrada.get(position);

        // Configurando os textos
        holder.txtTituloPostagem.setText(postagem.getTitulo());
        holder.txtDescricaoPostagem.setText(postagem.getDescricao());

        if (postagem.getUrlImagem() != null && !postagem.getUrlImagem().isEmpty()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                try {
                    // Vai buscar a imagem à internet em segundo plano
                    InputStream in = new java.net.URL(postagem.getUrlImagem()).openStream();
                    Bitmap bmp = BitmapFactory.decodeStream(in);

                    // Volta à thread principal para desenhar a imagem no ImageView
                    handler.post(() -> {
                        holder.imgPostagem.setImageBitmap(bmp);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    // Em caso de erro, mete a imagem padrão
                    handler.post(() -> holder.imgPostagem.setImageResource(R.mipmap.ic_launcher));
                }
            });
        }
        // Configurando as ações dos botões
        holder.btnEditar.setOnClickListener(v -> listener.onEditClick(postagem));
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(postagem));
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Postagem> filtrados = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtrados.addAll(listaOriginal);
                } else {
                    String padraoFiltro = constraint.toString().toLowerCase().trim();
                    for (Postagem item : listaOriginal) {
                        if (item.getTitulo().toLowerCase().contains(padraoFiltro) ||
                                item.getDescricao().toLowerCase().contains(padraoFiltro)) {
                            filtrados.add(item);
                        }
                    }
                }
                FilterResults resultados = new FilterResults();
                resultados.values = filtrados;
                return resultados;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listaFiltrada.clear();
                listaFiltrada.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    // Mapeamento atualizado dos componentes visuais do seu novo layout XML
    static class PostagemViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloPostagem, txtDescricaoPostagem;
        ImageView imgPostagem;
        ImageButton btnEditar, btnExcluir; // Alterado de Button para ImageButton

        public PostagemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referenciando os exatos mesmos IDs que você declarou no ConstraintLayout
            txtTituloPostagem = itemView.findViewById(R.id.txtTituloPostagem);
            txtDescricaoPostagem = itemView.findViewById(R.id.txtDescricaoPostagem);
            imgPostagem = itemView.findViewById(R.id.imgPostagem);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}