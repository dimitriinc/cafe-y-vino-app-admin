package com.cafeyvinowinebar.Administrador.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.cafeyvinowinebar.Administrador.App;
import com.cafeyvinowinebar.Administrador.MainActivity;
import com.cafeyvinowinebar.Administrador.R;
import com.cafeyvinowinebar.Administrador.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Registration extends DialogFragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final Handler handler;

    public Registration(Handler handler) {
        this.handler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        EditText edtEmail = view.findViewById(R.id.edtEmailRegister);
        EditText edtPassword = view.findViewById(R.id.edtPasswordRegister);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(getContext(), getString(R.string.password_length), Toast.LENGTH_SHORT).show();
                return;
            }

            // we create a collection of admins and store in their documents data:
            // email as identifier
            // FirestoreMessaging token to send them messages from the client app
            fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(App.executor, task -> {
                if (task.isSuccessful()) {

                    fMessaging.getToken().addOnSuccessListener(App.executor, s -> {

                        String adminId = fAuth.getUid();
                        Map<String, Object> admin = new HashMap<>();
                        admin.put(Utils.KEY_TOKEN, s);
                        admin.put(Utils.KEY_EMAIL, email);
                        assert adminId != null;
                        fStore.collection(Utils.ADMINS).document(adminId).set(admin);

                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(new Intent(getContext(), MainActivity.class));
                    });

                } else {
                    handler.post(() -> Toast.makeText(getContext(), getString(R.string.eror,
                            Objects.requireNonNull(task.getException()).getMessage()),
                            Toast.LENGTH_SHORT).show());
                }
            });
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
