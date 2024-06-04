package com.saaweel.healthcheckai.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.activities.MainActivity;
import com.saaweel.healthcheckai.adarpters.AllergyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalDataFragment extends Fragment {
    private FirebaseFirestore db;
    private List<String> allergies;

    public PersonalDataFragment() {
        FirebaseApp.initializeApp(this.getContext());

        this.db = FirebaseFirestore.getInstance();

        this.allergies = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        activity.setTabs("app3");

        activity.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                activity.changeFragment(new MainFragment());

                activity.setTabs("home");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_data, container, false);

        Button send_pd = view.findViewById(R.id.save);
        RadioButton pd_male = view.findViewById(R.id.pd_male);
        RadioButton pd_female = view.findViewById(R.id.pd_female);
        EditText pd_age = view.findViewById(R.id.pd_age);
        EditText pd_height = view.findViewById(R.id.pd_height);
        EditText pd_weight = view.findViewById(R.id.pd_weight);

        CheckBox pd_heart_problems = view.findViewById(R.id.pd_heart_problems);
        CheckBox pd_diabetes = view.findViewById(R.id.pd_diabetes);
        EditText add_allergy = view.findViewById(R.id.add_allergy);
        TextView send_add_allergy = view.findViewById(R.id.send_add_allergy);
        RecyclerView allergy_list = view.findViewById(R.id.allergy_list);
        Button send_detailed_pd = view.findViewById(R.id.send_detailed_pd);

        allergy_list.setAdapter(new AllergyAdapter(this.allergies));

        String username = this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", null);

        if (username != null) {
            this.db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String gender = document.getString("gender");
                        String age = document.getString("age");
                        String height = document.getString("height");
                        String weight = document.getString("weight");

                        if (gender != null) {
                            if (gender.equals("male")) {
                                pd_male.setChecked(true);
                            } else {
                                pd_female.setChecked(true);
                            }
                        }

                        if (age != null) {
                            pd_age.setText(age);
                        }

                        if (height != null) {
                            pd_height.setText(height);
                        }

                        if (weight != null) {
                            pd_weight.setText(weight);
                        }

                        Boolean heart_problems = document.getBoolean("heart_problems");
                        Boolean diabetes = document.getBoolean("diabetes");
                        List<String> allergies = (List<String>) document.get("allergies");

                        if (heart_problems != null) {
                            pd_heart_problems.setChecked(heart_problems);
                        }

                        if (diabetes != null) {
                            pd_diabetes.setChecked(diabetes);
                        }

                        if (allergies != null) {
                            this.allergies.addAll(allergies);
                            allergy_list.getAdapter().notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        send_add_allergy.setOnClickListener(v -> {
            String allergy = add_allergy.getText().toString();

            if (!allergy.isEmpty()) {
                this.allergies.add(allergy);
                add_allergy.setText("");
                allergy_list.getAdapter().notifyDataSetChanged();
            }
        });

        send_pd.setOnClickListener(v -> {
            String gender = "";

            if (pd_male.isChecked()) {
                gender = "male";
            } else if (pd_female.isChecked()) {
                gender = "female";
            }


            Map<String, Object> userUpdates = new HashMap<>();

            userUpdates.put("gender", gender);
            userUpdates.put("age", pd_age.getText().toString());
            userUpdates.put("height", pd_height.getText().toString());
            userUpdates.put("weight", pd_weight.getText().toString());

            this.db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    document.getReference().update(userUpdates).addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), getString(R.string.personal_data_updated), Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
            });
        });

        send_detailed_pd.setOnClickListener(v -> {
            Map<String, Object> userUpdates = new HashMap<>();

            userUpdates.put("heart_problems", pd_heart_problems.isChecked());
            userUpdates.put("diabetes", pd_diabetes.isChecked());
            userUpdates.put("allergies", this.allergies);

            this.db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    document.getReference().update(userUpdates).addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), getString(R.string.personal_data_updated), Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(getActivity(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
            });
        });

        return view;
    }
}