package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class babyFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private EditText nameEditText, ageEditText, nationalIdEditText;
    private RadioGroup genderRadioGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baby, container, false);

        // Initialize views
        nameEditText = view.findViewById(R.id.nameEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        nationalIdEditText = view.findViewById(R.id.nationalIdEditText);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancel);



        // Initialize Firestore and Authentication
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        saveButton.setOnClickListener(v -> saveBabyData());
        cancelButton.setOnClickListener(v -> navigateToShowBaby());

        return view;
    }

    private void saveBabyData() {
        // Get input values
        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String nationalId = nationalIdEditText.getText().toString().trim();
        String gender = getSelectedGender();

        // Validate inputs
        if (!validateInputs(name, age, gender, nationalId)) {
            return;
        }

        // Create baby object
        Map<String, Object> baby = new HashMap<>();
        baby.put("name", name);
        baby.put("age", age);
        baby.put("gender", gender);
        baby.put("nationalId", nationalId);

        String userId = fAuth.getCurrentUser().getUid(); // Get current user ID

        db.collection("users").document(userId)
                .collection("babies").document(nationalId)
                .set(baby)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Baby saved successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToShowBaby() {
        // Use FragmentManager to replace current fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, new ShowBabyFragment()) // Replace with your container ID
                .commit();
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.maleRadioButton) return "Male";
        if (selectedId == R.id.femaleRadioButton) return "Female";
        return "";
    }

    private boolean validateInputs(String name, String age, String gender, String nationalId) {
        if (name.isEmpty()) {
            nameEditText.setError("Name required");
            return false;
        }
        if (age.isEmpty()) {
            ageEditText.setError("Age required");
            return false;
        }
        if (gender.isEmpty()) {
            Toast.makeText(requireContext(), "Select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (nationalId.isEmpty()) {
            nationalIdEditText.setError("ID required");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nameEditText.setText("");
        ageEditText.setText("");
        nationalIdEditText.setText("");
        genderRadioGroup.clearCheck();
    }
}