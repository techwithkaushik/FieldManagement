package com.ask2784.fieldmanagement.databases.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ask2784.fieldmanagement.databases.listeners.OnClickListener;
import com.ask2784.fieldmanagement.databases.listeners.OnLongClickListener;
import com.ask2784.fieldmanagement.databases.models.FieldDetails;
import com.ask2784.fieldmanagement.databinding.FieldDetailsBinding;

import java.util.ArrayList;

public class FieldDetailsAdapter
        extends RecyclerView.Adapter<FieldDetailsAdapter.FieldDetailsViewHolder> {
    private final OnClickListener onClickListener;
    private final OnLongClickListener onLongClickListener;
    private final ArrayList<FieldDetails> fieldDetailsList;

    public FieldDetailsAdapter(OnClickListener onClickListener, OnLongClickListener onLongClickListener, ArrayList<FieldDetails> fieldDetailsList) {
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.fieldDetailsList = fieldDetailsList;
    }

    @NonNull
    @Override
    public FieldDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FieldDetailsViewHolder(FieldDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FieldDetailsViewHolder holder, int position) {
        FieldDetails fieldDetails = fieldDetailsList.get(position);
        holder.fieldDetailsBinding.setFieldDetails(fieldDetails);
        holder.fieldDetailsBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return fieldDetailsList.size();
    }

    public class FieldDetailsViewHolder extends RecyclerView.ViewHolder {
        FieldDetailsBinding fieldDetailsBinding;

        public FieldDetailsViewHolder(@NonNull FieldDetailsBinding fieldDetailsBinding) {
            super(fieldDetailsBinding.getRoot());
            this.fieldDetailsBinding = fieldDetailsBinding;
//            fieldDetailsBinding.getRoot().setOnClickListener(view -> onClickListener.onViewClick(binding.getRoot(), getAdapterPosition()));
            fieldDetailsBinding.getRoot().setOnLongClickListener(v -> onLongClickListener.onViewLongClick(getAdapterPosition()));
        }
    }
}
