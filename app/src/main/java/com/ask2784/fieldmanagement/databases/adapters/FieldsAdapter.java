package com.ask2784.fieldmanagement.databases.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ask2784.fieldmanagement.databases.listeners.OnClickListener;
import com.ask2784.fieldmanagement.databases.models.Fields;
import com.ask2784.fieldmanagement.databinding.FieldsItemsBinding;

import java.util.ArrayList;

public class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.FieldsViewHolder> {

    private static final String TAG = "FieldsAdapter";
    private final OnClickListener clickListener;
    private final ArrayList<Fields> fields;

    public FieldsAdapter(ArrayList<Fields> fields, OnClickListener onClickListener) {
        this.fields = fields;
        this.clickListener = onClickListener;
    }

    @NonNull
    @Override
    public FieldsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FieldsViewHolder(FieldsItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(FieldsViewHolder holder, int position) {
        Fields fields1 = fields.get(position);
        holder.binding.setFields(fields1);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }


    public class FieldsViewHolder extends RecyclerView.ViewHolder {
        FieldsItemsBinding binding;

        public FieldsViewHolder(FieldsItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.addSelYear.setOnClickListener(view -> {
                clickListener.onViewClick(getAdapterPosition(), binding.addSelYear);
                Log.d(TAG, "FieldsViewHolder: " + getAdapterPosition());
            });
        }
    }
}
