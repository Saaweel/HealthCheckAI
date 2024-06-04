package com.saaweel.healthcheckai.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.activities.MainActivity;

public class ToolsFragment extends Fragment {
    private FirebaseFirestore db;

    public ToolsFragment() {
        FirebaseApp.initializeApp(this.getContext());

        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        activity.setTabs("app2");

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
        View view = inflater.inflate(R.layout.fragment_tools, container, false);

        EditText imc_height = view.findViewById(R.id.imc_height);
        EditText imc_weight = view.findViewById(R.id.imc_weight);

        RadioButton kcal_male = view.findViewById(R.id.kcal_male);
        RadioButton kcal_female = view.findViewById(R.id.kcal_female);
        EditText kcal_age = view.findViewById(R.id.kcal_age);
        EditText kcal_height = view.findViewById(R.id.kcal_height);
        EditText kcal_weight = view.findViewById(R.id.kcal_weight);
        Spinner kcal_activity = view.findViewById(R.id.kcal_activity);

        kcal_activity.setAdapter(new ArrayAdapter<>(this.getContext(), R.layout.spinner_item, getResources().getStringArray(R.array.activity)));

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
                                kcal_male.setChecked(true);
                            } else {
                                kcal_female.setChecked(true);
                            }
                        }

                        if (age != null) {
                            kcal_age.setText(age);
                        }

                        if (height != null) {
                            imc_height.setText(height);
                            kcal_height.setText(height);
                        }

                        if (weight != null) {
                            imc_weight.setText(weight);
                            kcal_weight.setText(weight);
                        }
                    }
                }
            });
        }

        view.findViewById(R.id.send_imc).setOnClickListener(v -> {
            imc_height.setError(null);
            imc_weight.setError(null);

            String imcHeight = imc_height.getText().toString();
            float height = Float.parseFloat(imcHeight.isEmpty() ? "-1" : imcHeight);
            String imcWeight = imc_weight.getText().toString();
            Integer weight = Integer.parseInt(imcWeight.isEmpty() ? "-1" : imcWeight);

            if (height < 0) {
                imc_height.setError(this.getString(R.string.error_invalid_height));
            } else if (weight < 0) {
                imc_weight.setError(this.getString(R.string.error_invalid_weight));
            } else {
                height /= 100;
                float imc = weight / (height * height);
                String status = this.getImcStatus(imc);

                TextView result = view.findViewById(R.id.imc_result);

                result.setText(String.format("%.2f", imc) + " (" + status + ")");
                result.setVisibility(View.VISIBLE);
            }
        });

        view.findViewById(R.id.send_kcal).setOnClickListener(v -> {
            kcal_male.setError(null);
            kcal_female.setError(null);
            kcal_age.setError(null);
            kcal_height.setError(null);
            kcal_weight.setError(null);

            String gender = kcal_male.isChecked() ? "male" : "female";
            String kcalAge = kcal_age.getText().toString();
            Integer age = Integer.parseInt(kcalAge.isEmpty() ? "-1" : kcalAge);
            String kcalHeight = kcal_height.getText().toString();
            float height = Float.parseFloat(kcalHeight.isEmpty() ? "-1" : kcalHeight);
            String kcalWeight = kcal_weight.getText().toString();
            Integer weight = Integer.parseInt(kcalWeight.isEmpty() ? "-1" : kcalWeight);
            Integer activity = kcal_activity.getSelectedItemPosition();

            if (gender.equals("female") && !kcal_female.isChecked()) {
                kcal_male.setError(this.getString(R.string.error_invalid_gender));
                kcal_female.setError(this.getString(R.string.error_invalid_gender));
            } else if (age < 0) {
                kcal_age.setError(this.getString(R.string.error_invalid_age));
            } else if (height < 0) {
                kcal_height.setError(this.getString(R.string.error_invalid_height));
            } else if (weight < 0) {
                kcal_weight.setError(this.getString(R.string.error_invalid_weight));
            } else if (activity < 0) {
                kcal_activity.requestFocus();
            } else {
                double bmr = this.calculateBMR(gender, weight, height, age);
                double tdee = this.calculateTDEE(bmr, activity);

                TextView result = view.findViewById(R.id.kcal_result);

                result.setText(String.format("%.2f", tdee) + " " + this.getString(R.string.cal_day));
                result.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private String getImcStatus(float imc) {
        String imcStatus;

        if (imc < 18.5) {
            imcStatus = this.getString(R.string.imc_status_underweight);
        } else if (imc >= 18.5 && imc < 25) {
            imcStatus = this.getString(R.string.imc_status_normal);
        } else if (imc >= 25 && imc < 30) {
            imcStatus = this.getString(R.string.imc_status_overweight);
        } else {
            imcStatus = this.getString(R.string.imc_status_obesity);
        }

        return imcStatus;
    }

    public static double calculateBMR(String gender, double weight, double height, int age) {
        if (gender.equalsIgnoreCase("male")) {
            return 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else if (gender.equalsIgnoreCase("female")) {
            return 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        } else {
            throw new IllegalArgumentException("Género no reconocido. Por favor, usa 'male' o 'female'.");
        }
    }

    public static double calculateTDEE(double bmr, Integer activityLevel) {
        switch (activityLevel) {
            case 0:
                return bmr * 1.2;
            case 1:
                return bmr * 1.375;
            case 2:
                return bmr * 1.55;
            case 3:
                return bmr * 1.725;
            case 4:
                return bmr * 1.9;
            default:
                throw new IllegalArgumentException("Nivel de actividad no reconocido. Usa 'sedentary', 'lightly active', 'moderately active', 'very active' o 'extra active'.");
        }
    }
}