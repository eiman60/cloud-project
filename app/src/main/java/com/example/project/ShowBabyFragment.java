package com.example.project;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ShowBabyFragment extends Fragment {

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    private LinearLayout container;
    private ImageView noBabiesImage;
    private Button addBabyButton;
    private ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_baby, container, false);

        // Initialize views
        this.container = view.findViewById(R.id.babiesContainer);
        noBabiesImage = view.findViewById(R.id.noBabiesImage);
        addBabyButton = view.findViewById(R.id.addBabyButton);
        backButton = view.findViewById(R.id.left3);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set click listener
        addBabyButton.setOnClickListener(v -> showAddBabyDialog());
        backButton.setOnClickListener(v -> navigateToHome());

        // Load babies
        loadBabies();

        return view;
    }

    private void navigateToHome() {
        // Navigate back to home fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, new homeFragment())
                .addToBackStack(null)
                .commit();
    }

    private void showAddBabyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_baby, null);
        builder.setView(dialogView);

        // Initialize views
        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText ageEditText = dialogView.findViewById(R.id.ageEditText);
        EditText nationalIdEditText = dialogView.findViewById(R.id.nationalIdEditText);
        RadioGroup genderRadioGroup = dialogView.findViewById(R.id.genderRadioGroup);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set button click listeners
        btnSave.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            String nationalId = nationalIdEditText.getText().toString().trim();
            String gender = getSelectedGender(genderRadioGroup);

            if (validateBabyInputs(name, age, gender, nationalId)) {
                saveBabyData(name, age, nationalId, gender);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "الرجاء ملء جميع الحقول المطلوبة", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Adjust dialog window size
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }


    private String getSelectedGender(RadioGroup genderRadioGroup) {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.maleRadioButton) return "Male";
        if (selectedId == R.id.femaleRadioButton) return "Female";
        return "";
    }

    private boolean validateBabyInputs(String name, String age, String gender, String nationalId) {
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (age.isEmpty()) {
            Toast.makeText(requireContext(), "Age required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (gender.isEmpty()) {
            Toast.makeText(requireContext(), "Select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (nationalId.isEmpty()) {
            Toast.makeText(requireContext(), "ID required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveBabyData(String name, String age, String nationalId, String gender) {
        Map<String, Object> baby = new HashMap<>();
        baby.put("name", name);
        baby.put("age", age);
        baby.put("gender", gender);
        baby.put("nationalId", nationalId);

        String userId = fAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("babies").document(nationalId)
                .set(baby)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "تم حفظ بيانات الطفل بنجاح", Toast.LENGTH_SHORT).show();
                    loadBabies();
                    addBabyButton.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addBabyButton.setVisibility(View.VISIBLE);
                });
    }

    private void loadBabies() {
        String userId = fAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("babies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    container.removeAllViews();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            showBaby(document);
                        }
                        noBabiesImage.setVisibility(View.GONE);
                    } else {
                        noBabiesImage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showBaby(DocumentSnapshot document) {
        String name = document.getString("name");
        String gender = document.getString("gender");
        String age = document.getString("age");
        String nationalId = document.getString("nationalId");
        String babyId = document.getId(); // Get the document ID

        // Create card container
        CardView cardView = new CardView(requireContext());
        cardView.setRadius(30f);
        cardView.setCardElevation(8f);
        cardView.setUseCompatPadding(true);
        cardView.setCardBackgroundColor(Color.WHITE);

        // Create main layout for card content
        LinearLayout cardContent = new LinearLayout(requireContext());
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(40, 40, 40, 40);

        // Add baby info
        TextView babyInfo = new TextView(requireContext());
        babyInfo.setText("الاسم: " + name + "\n" +
                "الجنس: " + gender + "\n" +
                "العمر: " + age + "\n" +
                "رقم الهوية: " + nationalId);
        babyInfo.setTextSize(18);
        babyInfo.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.almara_regular));
        babyInfo.setTextColor(Color.BLACK);

        // Add delete button
        Button deleteButton = new Button(requireContext());
        deleteButton.setText("حذف الطفل");
        deleteButton.setBackgroundResource(R.drawable.baby_blue_boarder);
        deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.baby_blue));
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(babyId));


        // Add views to card
        cardContent.addView(babyInfo);
        cardContent.addView(deleteButton);
        cardView.addView(cardContent);

        // Add card to container
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 30);
        container.addView(cardView, layoutParams);
    }

    private void showDeleteConfirmationDialog(String babyId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("حذف الطفل")
                .setMessage("هل أنت متأكد أنك تريد حذف هذا الطفل؟ لا يمكن التراجع عن هذا الإجراء.")
                .setPositiveButton("حذف", (dialog, which) -> deleteBaby(babyId))
                .setNegativeButton("إلغاء", null)
                .show();
    }
    private void deleteBaby(String babyId) {
        String userId = fAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("babies").document(babyId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "تم حذف الطفل بنجاح", Toast.LENGTH_SHORT).show();
                    loadBabies(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "خطأ في الحذف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}