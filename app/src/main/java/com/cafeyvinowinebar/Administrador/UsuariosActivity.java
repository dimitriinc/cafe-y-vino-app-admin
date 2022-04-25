package com.cafeyvinowinebar.Administrador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cafeyvinowinebar.Administrador.Adapters.AdapterCustomUsuarios;
import com.cafeyvinowinebar.Administrador.Adapters.AdapterUsuarios;
import com.cafeyvinowinebar.Administrador.Fragments.UserSearcher;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemClickListener;
import com.cafeyvinowinebar.Administrador.Interfaces.OnItemLongClickListener;
import com.cafeyvinowinebar.Administrador.POJOs.Mesa;
import com.cafeyvinowinebar.Administrador.POJOs.Usuario;
import com.cafeyvinowinebar.Administrador.Runnables.MesaInCuentaChanger;
import com.cafeyvinowinebar.Administrador.Runnables.PresenceChanger;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Displays the list of the customers with the 'present' status
 * Admin can change their assigned table of send a message on click
 * Or change their status to 'not present" on long click
 * <p>
 * The sliding panel allows for the search of any user registered with the FirebaseAuth
 * If the user is registered, his ID and Email are displayed
 * On click admin can view the customer's consumption history or send them a message
 */

public class UsuariosActivity extends AppCompatActivity {

    private static final String TAG = "UsuariosActivity";

    private final FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private final FirebaseMessaging fMessaging = FirebaseMessaging.getInstance();

    private RecyclerView recUsarios;
    private RecyclerView recCustomUsuarios;
    private AdapterUsuarios adapterClients;
    private AdapterCustomUsuarios adapterCustom;
    String currentDate;
    private SlidingUpPanelLayout slidingLayout;
    private ImageView imgSlidingUsuarios;
    private MaterialButton btnBuscarUsuarios;
    private EditText edtUserName;
    private UserSearcher fragment;
    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        try {
            init();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }


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

        // sets the recycler view for the users of the client app, that are present in the restaurant
        setupClientsAdapter();

        // sets the recycler view for the custom tables, that are present in the restaurant
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
    private void setupClientsAdapter() {

        boolean isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        Query query = fStore.collection(Utils.USUARIOS)
                .whereEqualTo(Utils.IS_PRESENT, true)
                .orderBy(Utils.KEY_MESA);

        FirestoreRecyclerOptions<Usuario> options = new FirestoreRecyclerOptions.Builder<Usuario>()
                .setQuery(query, Usuario.class)
                .build();

        adapterClients = new AdapterUsuarios(options);
        recUsarios.setAdapter(adapterClients);
        recUsarios.setLayoutManager(new LinearLayoutManager(this));
        adapterClients.setOnItemClickListener((snapshot, position, v) -> {

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
                    snapshot.getReference().update(Utils.KEY_MESA, mesaFormat);

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

            AlertDialog dialog = builder.create();
            dialog.show();

            if (isDarkThemeOn) {
                Button btnPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnPositive.setTextColor(getColor(R.color.white));
                Button btnNegative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnNegative.setTextColor(getColor(R.color.white));
            }
        });

        // we update the customer's status to 'not present'
        adapterClients.setOnItemLongClickListener((snapshot, position, v) -> App.executor.submit(new PresenceChanger(snapshot)));

    }

    private void setupCustomAdapter() {

        Query query = fStore.collection("mesas")
                .whereEqualTo("present", true)
                .orderBy(Utils.KEY_NAME);
        FirestoreRecyclerOptions<Mesa> options = new FirestoreRecyclerOptions.Builder<Mesa>()
                .setQuery(query, Mesa.class)
                .build();
        adapterCustom = new AdapterCustomUsuarios(options, this, mainHandler);
        recCustomUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recCustomUsuarios.setAdapter(adapterCustom);

        adapterCustom.setOnItemLongClickListener((snapshot, position, v) -> snapshot.getReference().update("present", false));

        adapterCustom.setOnItemClickListener((clickedDoc, position, view) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(UsuariosActivity.this);
            View viewDialog = LayoutInflater.from(UsuariosActivity.this).inflate(R.layout.dialog_custom_mesa, null);
            EditText edtNewMesa = viewDialog.findViewById(R.id.edtNewMesa);
            FloatingActionButton fabOkNewMesa = viewDialog.findViewById(R.id.fabOkNewMesa);
            builder.setView(viewDialog);
            AlertDialog dialog = builder.create();

            fabOkNewMesa.setOnClickListener(v2 -> {

                String newMesa = edtNewMesa.getText().toString().trim();
                if (newMesa.isEmpty()) {
                    Toast.makeText(UsuariosActivity.this, R.string.llenar_los_campos, Toast.LENGTH_SHORT).show();
                } else {

                    // check if the new mesa name is already in the mesas collection at the Firestore
                    fStore.collection("mesas").get()
                            .addOnSuccessListener(App.executor, queryDocumentSnapshots -> {

                                boolean isFixed = false;
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                                    if (Objects.equals(snapshot.getString(Utils.KEY_NAME), newMesa)) {

                                        // here we need to check if the new table already has some pedidos or cuenta
                                        // in this case it is present, and can't be chosen
                                        // also check if the new table is blocked, which will mean that it's occupied already
                                        if (snapshot.getBoolean("present") || snapshot.getBoolean("blocked")) {
                                            mainHandler.post(() -> Toast.makeText(UsuariosActivity.this, "La mesa está ocupada", Toast.LENGTH_SHORT).show());
                                            return;
                                        }

                                        // the table of the new name is in the collection, a fixed one, and free to use
                                        snapshot.getReference().update("present", true);
                                        isFixed = true;

                                        // change the mesa value in the pedidos and cuenta in the fStore
                                        App.executor.submit(new MesaInCuentaChanger(
                                                Utils.getCurrentDate(),
                                                clickedDoc.getString(Utils.KEY_NAME),
                                                newMesa));

                                        if (clickedDoc.getBoolean("fixed")) {
                                            // if the table we are changing is one of the fixed ones, we change its presence status
                                            clickedDoc.getReference().update("present", false);
                                        } else {
                                            // the table we are changing was a customized one, we delete it from the collection
                                            clickedDoc.getReference().delete();
                                        }
                                    }
                                }


                                if (!isFixed) {

                                    // we are here because the iteration through the mesas collection didn't find a matching name
                                    // it means that administrator wants to assign a new customized table
                                    // so we add a new one to the collection
                                    fStore.collection("mesas").add(new Mesa(false, false, true, newMesa));

                                    // change the mesa value in the pedidos and cuenta in the fStore
                                    App.executor.submit(new MesaInCuentaChanger(
                                            Utils.getCurrentDate(),
                                            clickedDoc.getString(Utils.KEY_NAME),
                                            newMesa));

                                    if (clickedDoc.getBoolean("fixed")) {

                                        // the old one is one of the fixed, we change its presence status
                                        clickedDoc.getReference().update("present", false);
                                    } else {
                                        // the old one is customized, we can delete it
                                        clickedDoc.getReference().delete();
                                    }
                                }
                            });

                    dialog.dismiss();
                }
            });
            dialog.show();
        });

    }

    private void init() throws ExecutionException, InterruptedException {

        currentDate = Utils.getCurrentDate();
        recUsarios = findViewById(R.id.recUsarios);
        recCustomUsuarios = findViewById(R.id.recCustomUsuarios);
        slidingLayout = findViewById(R.id.slidingUpUsuarios);
        imgSlidingUsuarios = findViewById(R.id.imgSlidingUsuarios);
        btnBuscarUsuarios = findViewById(R.id.btnBuscarUsuarios);
        edtUserName = findViewById(R.id.edtUserName);
        mainHandler = new Handler();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapterClients.startListening();
        adapterCustom.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapterClients.stopListening();
        adapterCustom.stopListening();
    }

}