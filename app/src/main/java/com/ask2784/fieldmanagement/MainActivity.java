package com.ask2784.fieldmanagement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ask2784.fieldmanagement.databases.adapters.FieldsAdapter;
import com.ask2784.fieldmanagement.databases.listeners.OnClickListener;
import com.ask2784.fieldmanagement.databases.models.Fields;
import com.ask2784.fieldmanagement.databinding.ActivityMainBinding;
import com.ask2784.fieldmanagement.databinding.AddFieldsBinding;
import com.ask2784.fieldmanagement.databinding.AddYearBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements FirebaseAuth.AuthStateListener, OnClickListener {
    private ArrayList<Fields> fieldsList;
    private ArrayList<String> fieldsIdList;
    private long pressedTime;
    private String uId;
    private ActivityMainBinding binding;
    private ListenerRegistration listenerRegistration;
    private CollectionReference collectionReference;
    private EventListener<QuerySnapshot> eventListener;
    private FieldsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.include.toolbar);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            uId = mAuth.getCurrentUser().getUid();
            mainMethod();
            addField();
        }
    }

    private void mainMethod() {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        fieldsList = new ArrayList<>();
        fieldsIdList = new ArrayList<>();
        initRecyclerView();
        collectionReference = fireStore.collection("Fields");
        getFirestoreData();
    }

    private void initRecyclerView() {
        binding.mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FieldsAdapter(fieldsList, this);
        binding.mainRecyclerView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getFirestoreData() {
        eventListener = (value, error) -> {
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
                fieldsIdList.clear();
                fieldsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    if (snapshot != null) {
                        fieldsIdList.add(snapshot.getId());
                        Fields fields = snapshot.toObject(Fields.class);
                        fieldsList.add(fields);
                    } else Toast.makeText(this, "Data Not Available", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
                binding.mainRecyclerView.setVisibility(fieldsList.isEmpty() ? View.GONE : View.VISIBLE);
                binding.empty.setVisibility(fieldsList.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
            }
        };

        if (listenerRegistration == null) {
            setCollectionReference();
        }
    }

    private void setCollectionReference() {
        listenerRegistration = collectionReference
                .whereEqualTo("uid", uId)
                .orderBy("name")
                .addSnapshotListener(eventListener);
    }

    private void addField() {
        binding.addFab.setOnClickListener(v -> {

            AddFieldsBinding addFieldsBinding;
            addFieldsBinding = AddFieldsBinding.inflate(getLayoutInflater());
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (addFieldsBinding.addFiledName.isFocused()) {
                        addFieldsBinding.tInputLayout.setError(null);
                    }
                    if (addFieldsBinding.addFieldArea.isFocused()) {
                        addFieldsBinding.tInputLayout1.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };

            addFieldsBinding.addFiledName.addTextChangedListener(textWatcher);
            addFieldsBinding.addFieldArea.addTextChangedListener(textWatcher);

            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder
                    .setTitle("Add New Field")
                    .setView(addFieldsBinding.getRoot())
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(dialogInterface -> {
                String[] fieldType = {"Bigha", "Hectare", "Acer"};
                ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fieldType);
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addFieldsBinding.fieldSpinner.setAdapter(typeAdapter);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            if (addFieldsBinding.addFiledName.getText() != null && addFieldsBinding.addFiledName.getText().toString().isEmpty()) {
                                addFieldsBinding.tInputLayout.setError("Enter Field Name");
//                                addFieldsBinding.addFiledName.setError("Enter Field Name");
                                addFieldsBinding.addFiledName.requestFocus();
                            } else if (addFieldsBinding.addFieldArea.getText() != null && addFieldsBinding.addFieldArea.getText().toString().isEmpty()) {
                                addFieldsBinding.tInputLayout1.setError("Enter Field Area");
//                                addFieldsBinding.addFieldArea.setError("Enter Field Area");
                                addFieldsBinding.addFieldArea.requestFocus();
                            } else {
                                Fields fields = new Fields(uId,
                                        addFieldsBinding.addFiledName.getText().toString().trim(),
                                        addFieldsBinding.addFieldArea.getText().toString().trim() + " " + addFieldsBinding.fieldSpinner.getSelectedItem().toString());
                                collectionReference.add(fields).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    if (task.isCanceled()) {
                                        Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                alertDialog.dismiss();
                            }
                        });
            });
            alertDialog.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthUI.getInstance().signOut(this);
            return true;
        } else if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            System.exit(0);
        } else {
            Toast.makeText(MainActivity.this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
        setCollectionReference();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onViewClick(int position, View view) {
        AddYearBinding addYearBinding = AddYearBinding.inflate(getLayoutInflater());
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.add_and_select, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.addYear) {

                MaterialAlertDialogBuilder addBuilder = new MaterialAlertDialogBuilder(this);
                ArrayList<String> years = new ArrayList<>();
                int thisYear = Calendar.getInstance().get(Calendar.YEAR);
                for (int i = 1990; i <= thisYear; thisYear--) {
                    years.add(String.valueOf(thisYear));
                }
                ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
                yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addYearBinding.addYearSpinner.setAdapter(yearsAdapter);
                addBuilder.setTitle("Add Year")
                        .setView(addYearBinding.getRoot())
                        .setPositiveButton("Okay", (dialogInterface, i) -> {
//                            FieldDetails fieldDetails = new FieldDetails(addYearBinding.addYearSpinner.getSelectedItem().toString());
//                            List<String> list = new ArrayList<>();
//                            for (FieldDetails d : fieldDetailsList) {
//                                list.add(d.getYear());
//                            }
//
//                            if (list.contains(fieldDetails.getYear())) {
//                                Snackbar.make(binding.getRoot(), fieldDetails.getYear() + " Already Exist", Snackbar.LENGTH_SHORT).show();
//                            } else {
//                                collectionReference
//                                        .document(fieldId)
//                                        .collection("FieldDetails")
//                                        .add(fieldDetails)
//                                        .addOnCompleteListener(task -> {
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(FieldDetailsActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
//                                            }
//                                            if (task.isCanceled()) {
//                                                Toast.makeText(FieldDetailsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
//                                            }
//                                            dialogInterface.dismiss();
//                                        });
//                            }
                            Snackbar.make(binding.getRoot(), addYearBinding.addYearSpinner.getSelectedItem() + " Year Selected", Snackbar.LENGTH_SHORT).show();

                            dialogInterface.dismiss();
                        })
                        .create()
                        .show();
                return true;
            } else if (item.getItemId() == R.id.selectYear) {
                Snackbar.make(binding.getRoot(), "Select Year", Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

//        Intent intent = new Intent(MainActivity.this, FieldDetailsActivity.class);
//        intent.putExtra("fieldId", fieldsIdList.get(position));
//        intent.putExtra("fieldData",
//                fieldsList.get(position));
//        startActivity(intent);
    }
}
