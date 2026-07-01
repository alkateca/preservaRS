package com.alkateca.preservars;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SobreDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(layoutAboutCustom(), null);


        TextView tvNome = view.findViewById(R.id.sobreAutor);
        TextView tvCurso = view.findViewById(R.id.sobreCurso);
        TextView tvObjetivo = view.findViewById(R.id.sobreObjetivo);
        ImageView imgLogo = view.findViewById(R.id.logo);



        builder.setView(view)
                .setPositiveButton("Fechar", (dialog, id) -> dismiss());

        return builder.create();
    }

    private int layoutAboutCustom() {

        return R.layout.dialog_sobre;
    }
}