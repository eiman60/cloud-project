package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText nameEditText, fnameEditText, phoneEditText, phone2EditText, emailEditText;
    private FirebaseAuth fAuth;
    private Button deleteAccountBtn;

    public profileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment profileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Initialize views
        nameEditText = view.findViewById(R.id.signup_name);
        fnameEditText = view.findViewById(R.id.signup_fname);
        phoneEditText = view.findViewById(R.id.signup_phone1);
        phone2EditText = view.findViewById(R.id.signup_phone2);
        emailEditText = view.findViewById(R.id.signup_email);
        fAuth = FirebaseAuth.getInstance();

        // Load user data
        loadUserData();

        deleteAccountBtn = view.findViewById(R.id.delete_account_btn);
        deleteAccountBtn.setOnClickListener(v -> showDeleteConfirmationDialog());

        return view;
        //return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("حذف الحساب")
                .setMessage("هل أنت متأكد أنك تريد حذف حسابك؟ لا يمكن التراجع عن هذا الإجراء.")
                .setPositiveButton("حذف", (dialog, which) -> deleteAccount())
                .setNegativeButton("إلغاء", null)
                .show();
    }
    private void deleteAccount() {
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            // First delete Firestore data
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Then delete the auth account
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show();
                                        navigateToLogin();
                                    } else {
                                        Toast.makeText(getContext(), "فشل الحذف: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "فشل حذف البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void navigateToLogin() {
        fAuth.signOut();
        startActivity(new Intent(requireActivity(), LoginActivity.class));
        requireActivity().finish();
    }
    private void loadUserData() {
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            emailEditText.setText(user.getEmail());

            // Load additional profile data from Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            nameEditText.setText(documentSnapshot.getString("name"));
                            fnameEditText.setText(documentSnapshot.getString("fname"));
                            phoneEditText.setText(documentSnapshot.getString("phone"));
                            phone2EditText.setText(documentSnapshot.getString("phone2"));
                        }
                    });
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.save_profile_btn).setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String fname = fnameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String phone2 = phone2EditText.getText().toString().trim();

            // Save to Firestore
            FirebaseUser user = fAuth.getCurrentUser();
            if (user != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("name", name);
                profile.put("fname", fname);
                profile.put("phone", phone);
                profile.put("phone2", phone2);
                profile.put("email", user.getEmail());

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .set(profile)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(getContext(), "تم حفظ البيانات", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "خطأ في الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

}