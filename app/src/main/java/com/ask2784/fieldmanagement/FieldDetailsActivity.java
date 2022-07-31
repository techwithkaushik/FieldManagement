package com.ask2784.fieldmanagement;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ask2784.fieldmanagement.databases.adapters.FieldDetailsAdapter;
import com.ask2784.fieldmanagement.databases.listeners.OnClickListener;
import com.ask2784.fieldmanagement.databases.listeners.OnLongClickListener;
import com.ask2784.fieldmanagement.databases.models.FieldDetails;
import com.ask2784.fieldmanagement.databases.models.Fields;
import com.ask2784.fieldmanagement.databinding.ActivityFieldDetailsBinding;
import com.ask2784.fieldmanagement.databinding.AddFieldsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class FieldDetailsActivity
        extends AppCompatActivity
        implements OnClickListener, OnLongClickListener {
    private CollectionReference collectionReference;
    private String fieldId;
    private Fields fieldData;
    private ActivityFieldDetailsBinding binding;
    private ArrayList<FieldDetails> fieldDetailsList;
    private ArrayList<String> fieldDetailsIdList;
    private FieldDetailsAdapter fieldDetailsAdapter;
    private ListenerRegistration detailsListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFieldDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.include1.toolbar);
        fieldData = (Fields) getIntent().getSerializableExtra("fieldData");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(fieldData.getName());
        }
        mainMethod();
    }

    private void mainMethod() {
        fieldDetailsList = new ArrayList<>();
        fieldDetailsIdList = new ArrayList<>();
        fieldId = getIntent().getStringExtra("fieldId");
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        collectionReference = fireStore
                .collection("Fields");
        initRecyclerView();
        getFieldData();
        addDetails();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getFieldData() {
        EventListener<QuerySnapshot> detailsEventListener = (value, error) -> {
            if (error != null) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                SpannableString spannableString = new SpannableString(error.getLocalizedMessage());
                Linkify.addLinks(spannableString, Linkify.ALL);
                TextView errorView = new TextView(this);
                errorView.setPadding(40, 0, 40, 0);
                errorView.setMovementMethod(LinkMovementMethod.getInstance());
                errorView.setText(spannableString);
                builder.setTitle("Getting Error on Data")
                        .setView(errorView)
                        .setPositiveButton("Okay", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
            if (value != null) {
                fieldDetailsIdList.clear();
                fieldDetailsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    if (snapshot != null) {
                        fieldDetailsIdList.add(snapshot.getId());
                        FieldDetails details = snapshot.toObject(FieldDetails.class);
                        fieldDetailsList.add(details);
                    } else Toast.makeText(this, "Data Not Available", Toast.LENGTH_SHORT).show();
                }
                fieldDetailsAdapter.notifyDataSetChanged();
                binding.detailsRecyclerView.setVisibility(fieldDetailsList.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyYear.setVisibility(fieldDetailsList.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
            }
        };
        if (detailsListenerRegistration == null) {
            setDetailsCollectionReference(detailsEventListener);
        }
    }

    private void setDetailsCollectionReference(EventListener<QuerySnapshot> detailsEventListener) {
        detailsListenerRegistration = collectionReference
                .document(fieldId)
                .collection("FieldDetails")
                .orderBy("year", Query.Direction.DESCENDING)
                .addSnapshotListener(detailsEventListener);
    }

    private void initRecyclerView() {
        binding.detailsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        fieldDetailsAdapter = new FieldDetailsAdapter(this, this, fieldDetailsList);
        binding.detailsRecyclerView.setAdapter(fieldDetailsAdapter);
    }

    private void addDetails() {
        binding.addFabDetails.setOnClickListener(v -> {
            MaterialAlertDialogBuilder addBuilder = new MaterialAlertDialogBuilder(this);
            Spinner yearsSpinner = new AppCompatSpinner(this);
            ArrayList<String> years = new ArrayList<>();
            int thisYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = 1900; i <= thisYear; thisYear--) {
                years.add(String.valueOf(thisYear));
            }
            ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
            yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            yearsSpinner.setAdapter(yearsAdapter);
            addBuilder.setTitle("Years")
                    .setView(yearsSpinner)
                    .setPositiveButton("Okay", (dialogInterface, i) -> {
                        FieldDetails fieldDetails = new FieldDetails(yearsSpinner.getSelectedItem().toString());
                        List<String> list = new ArrayList<>();
                        for (FieldDetails d : fieldDetailsList) {
                            list.add(d.getYear());
                        }

                        if (list.contains(fieldDetails.getYear())) {
                            Snackbar.make(binding.getRoot(), fieldDetails.getYear() + " Already Exist", Snackbar.LENGTH_SHORT).show();
                        } else {
                            collectionReference
                                    .document(fieldId)
                                    .collection("FieldDetails")
                                    .add(fieldDetails)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FieldDetailsActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        if (task.isCanceled()) {
                                            Toast.makeText(FieldDetailsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                                        }
                                        dialogInterface.dismiss();
                                    });
                        }
                    })
                    .create()
                    .show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_field) {
            editField();
            return true;
        }
        if (item.getItemId() == R.id.delete_field) {
            deleteField();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editField() {
        AddFieldsBinding editFieldsBinding;
        editFieldsBinding = AddFieldsBinding.inflate(getLayoutInflater());

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder
                .setTitle("Edit Field")
                .setView(editFieldsBinding.getRoot())
                .setPositiveButton("Done", null)
                .setNegativeButton("Cancel", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            String[] fieldType = {"Bigha", "Hectare", "Acer"};
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fieldType);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editFieldsBinding.fieldSpinner.setAdapter(typeAdapter);
            String[] area = fieldData.getArea().split("(?<=\\d)(\\s+)(?=\\D)");

            editFieldsBinding.addFiledName.setText(fieldData.getName());
            editFieldsBinding.addFieldArea.setText(area[0]);
            editFieldsBinding.fieldSpinner.setSelection(typeAdapter.getPosition(area[1]));

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(view -> {
                        if (editFieldsBinding.addFiledName.getText() != null && editFieldsBinding.addFiledName.getText().toString().isEmpty()) {
                            editFieldsBinding.addFiledName.setError("Enter Field Name");
                            editFieldsBinding.addFiledName.requestFocus();
                        } else if (editFieldsBinding.addFieldArea.getText() != null && editFieldsBinding.addFieldArea.getText().toString().isEmpty()) {
                            editFieldsBinding.addFieldArea.setError("Enter Field Area");
                            editFieldsBinding.addFieldArea.requestFocus();
                        } else {
                            Fields fields = new Fields(fieldData.getUId(),
                                    editFieldsBinding.addFiledName.getText().toString().trim(),
                                    editFieldsBinding.addFieldArea.getText().toString().trim() + " " + editFieldsBinding.fieldSpinner.getSelectedItem().toString());
                            collectionReference.document(fieldId)
                                    .set(fields).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            fieldData = fields;
                                            Objects.requireNonNull(getSupportActionBar()).setTitle(fieldData.getName());
                                            Toast.makeText(FieldDetailsActivity.this, "Edited Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        if (task.isCanceled()) {
                                            Toast.makeText(FieldDetailsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            alertDialog.dismiss();
                        }
                    });
        });
        alertDialog.show();
    }

    private void deleteField() {
        if (fieldDetailsList.size() == 0) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setTitle("Delete")
                    .setMessage("Do You want to delete `" + fieldData.getName() + "` Field?")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            collectionReference.document(fieldId)
                                    .delete().addOnCompleteListener(task -> {
                                        if (task.isCanceled()) {
                                            Toast.makeText(this, "Canceled " + task.getResult(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (task.isComplete()) {
                                            Toast.makeText(this, "Deleted ", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        dialogInterface.dismiss();
                                    }))
                    .setNegativeButton("No", null);
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        } else {
            Snackbar.make(binding.getRoot(), "First Delete All Years", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void deleteYear(int position) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Delete")
                .setMessage("Do You want to delete `" + fieldDetailsList.get(position).getYear() + "` Field?")
                .setPositiveButton("Yes", (dialogInterface, i) ->
                        collectionReference.document(fieldId).collection("FieldDetails")
                                .document(fieldDetailsIdList.get(position))
                                .delete().addOnCompleteListener(task -> {
                                    if (task.isCanceled()) {
                                        Snackbar.make(binding.getRoot(), "Cancel" + position, Snackbar.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (task.isComplete()) {
                                        Snackbar.make(binding.getRoot(), "Deleted" + position, Snackbar.LENGTH_SHORT).show();
                                    }
                                    dialogInterface.dismiss();
                                }))
                .setNegativeButton("No", null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onViewClick(int position,View view) {
        Snackbar.make(binding.getRoot(), "Clicked on " + position, Snackbar.LENGTH_SHORT).show();
    }


    @Override
    public boolean onViewLongClick(int position) {
        deleteYear(position);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFieldData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (detailsListenerRegistration != null)
            detailsListenerRegistration.remove();
    }
}
