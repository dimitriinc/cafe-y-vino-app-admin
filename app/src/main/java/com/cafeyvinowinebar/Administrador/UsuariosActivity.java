package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCustomUsuarios;
import com.cafeyvinowinebar.Administrador.Adapters.AdapterUsuarios;
import com.cafeyvinowinebar.Administrador.Fragments.UserSearcher;
import com.cafeyvinowinebar.Administrador.POJOs.Usuario;
import com.cafeyvinowinebar.Administrador.Runnables.MesaInCuentaChanger;
import com.cafeyvinowinebar.Administrador.Runnables.MesaSetter;
import com.cafeyvinowinebar.Administrador.Runnables.PresenceChanger;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.concurrent.ExecutionException;

/**
 * Displays the list of the customers with the 'present' status
 * Admin can change their assigned table of send a message on click
 * Or change their status to 'not present" on long click
 *
 * The sliding panel allows for the search of any user registered with the FirebaseAuth
 * If the user is registered, his ID and Email are displayed
 * On click admin can view the customer's consumption history or send them a message
 */

public class UsuariosActivity extends AppCompatActivity {

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private RecyclerView recUsarios;
    private RecyclerView recCustomUsuarios;
    private AdapterUsuarios adapter;
    String currentDate;
    private SlidingUpPanelLayout slidingLayout;
    private ImageView imgSlidingUsuarios;
    private MaterialButton btnBuscarUsuarios;
    private EditText edtUserName;
    private UserSearcher fragment;
    private MesasViewModel mesasViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        init();

        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                imgSlidingUsuarios.setAlpha(1 - slideOffset);
            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });
        if (getResources().getConfiguration().smallestScreenWidthDp == 600) {
            slidingLayout.setPanelHeight(45);
        } else {
            slidingLayout.setPanelHeight(90);
        }

        // sets the recycler view on the main screen
        setupAdapter();

        // sets the recycler view for the custom tables
        setupCustomAdapter();

        // searches customers on the sliding panel
        btnBuscarUsuarios.setOnClickListener(v -> {
            String name = edtUserName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(UsuariosActivity.this, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
            } else {

                // shows a dialog fragment with personal data of the customer
                FragmentManager manager = getSupportFragmentManager();
                fragment = UserSearcher.newInstance(name);
                fragment.show(manager, Utils.TAG);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void setupAdapter() {

        Query query = fStore.collection(Utils.USUARIOS)
                .whereEqualTo(Utils.IS_PRESENT, true)
                .orderBy(Utils.KEY_MESA);

        FirestoreRecyclerOptions<Usuario> options = new FirestoreRecyclerOptions.Builder<Usuario>()
                .setQuery(query, Usuario.class)
                .build();

        adapter = new AdapterUsuarios(options);
        recUsarios.setAdapter(adapter);
        recUsarios.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener((snapshot, position, v) -> {

            String token = snapshot.getString(Utils.KEY_TOKEN);

            // displays an alert dialog with options of changing the user's table or sending him a message
            AlertDialog.Builder builder = new AlertDialog.Builder(UsuariosActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_usuarios_mesa_msg, null);

            // two edit texts for a new table and a message
            EditText etMesa = view.findViewById(R.id.edt_mesa_usario);
            EditText etMsg = view.findViewById(R.id.edtMsg);

            builder.setView(view);

            builder.setPositiveButton(getString(R.string.cambiar), (dialog, which) -> {

                // updating the table value
                String mesa = etMesa.getText().toString().trim();
                if (mesa.isEmpty()) {
                    Toast.makeText(getBaseContext(), getString(R.string.usarios_no_mesa), Toast.LENGTH_SHORT).show();
                } else {

                    // table value is stored as a string with a certain format
                    // so we convert the string received from the edit text to a long and then back to string
                    // to make sure that it's of the correct format
                    long mesaLong = Long.parseLong(mesa);
                    String mesaFormat = String.format("%02d", mesaLong);

                    // we get the customer's id to look for the open orders and bills assigned to him
                    String userId = snapshot.getId();

                    // updates the mesa field in the user's doc
                    App.executor.submit(new MesaSetter(snapshot, mesaFormat));

                    // looks for the customer's orders and bill to updates the mesa field on those docs as well
                    App.executor.submit(new MesaInCuentaChanger(currentDate, userId, mesaFormat));
                }
            });

            builder.setNegativeButton(getString(R.string.enviar_mensaje), (dialog, which) -> {

                // we get the message from its edit text camp, and send it to the customer's app
                String msg = etMsg.getText().toString();
                if (TextUtils.equals(msg, "")) {
                    Toast.makeText(UsuariosActivity.this, getString(R.string.llenar_los_campos), Toast.LENGTH_SHORT).show();
                } else {
                    fMessaging.send(new RemoteMessage.Builder(getString(R.string.fcm_id, App.SENDER_ID))
                            .setMessageId(Utils.getMessageId())
                            .addData(Utils.KEY_TOKEN, token)
                            .addData(Utils.KEY_ACTION, Utils.ACTION_MSG)
                            .addData(Utils.KEY_TYPE, Utils.TO_CLIENT)
                            .addData(Utils.ACTION_MSG, msg)
                            .build());
                }
            });

            builder.create().show();
        });

        // we update the customer's status to 'not present'
        adapter.setOnItemLongClickListener((snapshot, position, v) -> App.executor.submit(new PresenceChanger(snapshot)));

    }

    private void setupCustomAdapter() throws ExecutionException, InterruptedException {

        recCustomUsuarios.setLayoutManager(new LinearLayoutManager(this));
        AdapterCustomUsuarios adapter = new AdapterCustomUsuarios(this);
        recCustomUsuarios.setAdapter(adapter);
        mesasViewModel.getPresentMesas().observe(this, adapter::submitList);
    }

    private void init() {

        currentDate = Utils.getCurrentDate();
        recUsarios = findViewById(R.id.recUsarios);
        recCustomUsuarios = findViewById(R.id.recCustomUsuarios);
        slidingLayout = findViewById(R.id.slidingUpUsuarios);
        imgSlidingUsuarios = findViewById(R.id.imgSlidingUsuarios);
        btnBuscarUsuarios = findViewById(R.id.btnBuscarUsuarios);
        edtUserName = findViewById(R.id.edtUserName);
        mesasViewModel = new ViewModelProvider(this).get(MesasViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}