package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ShowBabyFragment extends Fragment {

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    private LinearLayout container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_baby, container, false);

        // Initializing views and database
        this.container = view.findViewById(R.id.babiesContainer);
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // زر الإضافة
        Button addBabyButton = view.findViewById(R.id.addBabyButton);
        addBabyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                babyFragment babyFragment = new babyFragment(); // اسم كلاس إضافة الطفل
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, babyFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Load all babies when fragment starts
        loadBabies();

        return view;
    }

    private void loadBabies() {
        String userId = fAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("babies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            showBaby(document);
                        }
                    } else {
                        Toast.makeText(getActivity(), "No babies found.", Toast.LENGTH_SHORT).show();
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

        CardView cardView = new CardView(requireContext());
        cardView.setRadius(30f);
        cardView.setCardElevation(16f);
        cardView.setUseCompatPadding(true);

        TextView babyView = new TextView(getActivity());
        babyView.setText("Name: " + name + "\n" +
                "Gender: " + gender + "\n" +
                "Age: " + age + "\n" +
                "ID: " + nationalId);

        babyView.setTextSize(18);
        babyView.setPadding(40, 40, 40, 40);
        babyView.setBackgroundColor(0xFFFFFFFF);

        cardView.addView(babyView);

        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.setMargins(0, 0, 0, 30);

        container.addView(cardView, layout);
    }
}
