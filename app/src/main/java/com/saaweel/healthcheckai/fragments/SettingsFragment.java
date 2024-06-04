package com.saaweel.healthcheckai.fragments;

import static android.content.Context.MODE_PRIVATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.saaweel.healthcheckai.R;
import com.saaweel.healthcheckai.activities.LoginActivity;
import com.saaweel.healthcheckai.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private ImageView avatar;
    private Uri imageUri;

    private FirebaseFirestore db;
    private String newAvatar;

    public SettingsFragment() {
        FirebaseApp.initializeApp(this.getContext());

        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        activity.setTabs("app4");

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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ActivityResultLauncher<Intent> imageResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();

                if (data != null) {
                    Bundle extras = data.getExtras();

                    if (extras != null) {

                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        avatar.setImageBitmap(imageBitmap);

                        uploadImageToFirebase(imageBitmap);
                    } else {
                        imageUri = data.getData();

                        try {
                            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);

                            avatar.setImageBitmap(imageBitmap);

                            uploadImageToFirebase(imageBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        avatar = view.findViewById(R.id.avatar);
        EditText oldPassword = view.findViewById(R.id.oldPassword);
        EditText password = view.findViewById(R.id.password);
        EditText confirmPassword = view.findViewById(R.id.confirmPassword);
        Button save = view.findViewById(R.id.save);
        Button logout = view.findViewById(R.id.logout);
        Button clearChat = view.findViewById(R.id.clearChat);

        this.db.collection("users").whereEqualTo("username", this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", "")).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                String avatarUrl = task.getResult().getDocuments().get(0).getString("avatar");

                if (avatarUrl != null) {
                    Picasso.get().load(avatarUrl).into(avatar);
                }
            }
        });

        avatar.setOnClickListener(v -> {
            showImagePickerDialog(imageResult);
        });

        save.setOnClickListener(v -> {
            String oldPasswordText = oldPassword.getText().toString();
            String passwordText = password.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (!passwordText.isEmpty()) {
                if (passwordText.length() < 8) {
                    password.setError(getString(R.string.password_must_be_at_least_8_characters));
                    return;
                }

                if (!passwordText.matches(".*\\d.*") || !passwordText.matches(".*[a-zA-Z].*") || !passwordText.matches(".*[^a-zA-Z0-9].*")) {
                    password.setError(getString(R.string.password_must_contain_number_letter_special));
                    return;
                }

                if (passwordText.equals(confirmPasswordText)) {
                    if (!oldPasswordText.isEmpty()) {
                        this.db.collection("users").whereEqualTo("username", this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", "")).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                                if (task.getResult().getDocuments().get(0).get("password").equals(LoginActivity.hashPassword(oldPasswordText))) {
                                    this.db.collection("users").document(task.getResult().getDocuments().get(0).getId()).update("password", LoginActivity.hashPassword(passwordText));
                                    oldPassword.setText("");
                                    password.setText("");
                                    confirmPassword.setText("");
                                    Toast.makeText(getActivity(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                } else {
                                    oldPassword.setError(getString(R.string.incorrect_password));
                                }
                            } else {
                                oldPassword.setError(getString(R.string.unexpected_error));
                            }
                        });
                    } else {
                        oldPassword.setError(getString(R.string.enter_old_password));
                    }
                } else {
                    confirmPassword.setError(getString(R.string.passwords_do_not_match));
                }
            }

            if (newAvatar != null) {
                this.db.collection("users").whereEqualTo("username", this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", "")).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                        this.db.collection("users").document(task.getResult().getDocuments().get(0).getId()).update("avatar", newAvatar);
                        Toast.makeText(getActivity(), getString(R.string.avatar_updated), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        logout.setOnClickListener(v -> {
            this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().remove("LOGIN_USERNAME").remove("LOGIN_PASSWORD").apply();

            Intent intent = new Intent(this.getContext(), LoginActivity.class);
            startActivity(intent);
        });

        clearChat.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle(getString(R.string.clear_chat));
            builder.setMessage(getString(R.string.are_you_sure_you_want_to_clear_chat));

            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                this.db.collection("messages").whereEqualTo("chat", this.getActivity().getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("LOGIN_USERNAME", "")).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                            this.db.collection("messages").document(task.getResult().getDocuments().get(i).getId()).delete();
                        }
                    }
                });

                dialog.dismiss();

                Toast.makeText(getActivity(), getString(R.string.chat_cleared), Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
                dialog.dismiss();
            });

            builder.show();
        });

        return view;
    }

    private void showImagePickerDialog(ActivityResultLauncher<Intent> imageResult) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(getString(R.string.select_image));
        builder.setItems(new CharSequence[]{getString(R.string.take_picture), getString(R.string.select_from_gallery)}, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (this.hasPermissions(this.getContext(), new String[]{android.Manifest.permission.CAMERA})) {
                        imageResult.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                    } else {
                        ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                    }
                    break;
                case 1:
                    imageResult.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    break;
            }
        });
        builder.show();
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        if (bitmap != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("uploads/" + System.currentTimeMillis() + ".png");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            storageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    newAvatar = uri.toString();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), getString(R.string.upload_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

}