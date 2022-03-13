package com.cafeyvinowinebar.Administrador.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class Login extends DialogFragment {

    private final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();
    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final Handler handler;

    public Login(Handler handler) {
        this.handler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText edtEmail = view.findViewById(R.id.edtEmailLogin);
        EditText edtPassword = view.findViewById(R.id.edtPasswordLogin);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                return;
            }

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(App.executor, task -> {
                if (task.isSuccessful()) {

                    // before directing the admin to the MainActivity
                    // we check if the token is up-to-date
                    fMessaging.getToken().addOnSuccessListener(App.executor, s -> {
                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        assert user != null;
                        String userId = user.getUid();
                        fStore.collection("administradores").document(userId).get()
                                .addOnSuccessListener(App.executor, snapshot -> {
                                    String token = snapshot.getString(Utils.KEY_TOKEN);
                                    assert token != null;
                                    if (!token.equals(s)) {
                                        snapshot.getReference().update(Utils.KEY_TOKEN, s);
                                    }
                                });
                    });

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else {
                    handler.post(() -> Toast.makeText(getContext(),
                            getString(R.string.eror, Objects.requireNonNull(task.getException()).getMessage()),
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
