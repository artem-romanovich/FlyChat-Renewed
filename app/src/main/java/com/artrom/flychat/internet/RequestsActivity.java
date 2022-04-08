package com.artrom.flychat.internet;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.artrom.flychat.R;
import com.artrom.flychat.adapters.RequestsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.artrom.flychat.MySystem.LINK;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.readFile;
import static com.artrom.flychat.MySystem.thisUser;
import static com.artrom.flychat.MySystem.thisUserInfo;
import static com.artrom.flychat.internet.InternetFragment.recycle_requests;
import static com.artrom.flychat.internet.InternetFragment.requests_adapter;

public class RequestsActivity extends AppCompatActivity {

    Button btn_invite;
    ImageButton btn_back;
    LinearLayout requests, no_requests;
    TextView requests_title;

    ArrayList<String> Requests_nick = new ArrayList<>();
    ArrayList<String> Requests_name = new ArrayList<>();
    ArrayList<String> Requests_status = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }

        //ActionBar actionBar = getSupportActionBar();
        //if (actionBar != null) {
        //    actionBar.setTitle((Html.fromHtml("<font color=\"#ffffff\">Список запросов</font>")));
        //    actionBar.setIcon(R.drawable.ic_account_circle);
        //}

        String user_nick = readFile(getApplicationContext(), file_nick);

        checkForRequest(user_nick);

        btn_invite = findViewById(R.id.btn_invite);
        recycle_requests = findViewById(R.id.recycle_requests);
        requests = findViewById(R.id.requests);
        no_requests = findViewById(R.id.no_requests);
        requests_title = findViewById(R.id.requests_title);
        btn_back = findViewById(R.id.btn_back);

        requests_title.setText("Список запросов");

        recycle_requests.addItemDecoration(new DividerItemDecoration(RequestsActivity.this, DividerItemDecoration.VERTICAL));
        requests_adapter = new RequestsAdapter(RequestsActivity.this, Requests_nick, Requests_name, Requests_status, user_nick);
        recycle_requests.setAdapter(requests_adapter);

        requests.setVisibility(View.GONE);
        no_requests.setVisibility(View.VISIBLE);

        btn_invite.setOnClickListener(view1 -> LINK.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot1) {
                if (dataSnapshot1.exists()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", dataSnapshot1.getValue(String.class));
                    clipboard.setPrimaryClip(clip);
                    MyToast(getApplicationContext(), "Ссылка скопирована", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        }));

        btn_back.setOnClickListener(view -> {
            finish();
        });
    }

    private void checkForRequest(String user_nick) {
        thisUserInfo("Request/" + user_nick).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String new_user_nick = null;
                    String new_user_status = null;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        new_user_status = postSnapshot.getValue(String.class);
                        new_user_nick = postSnapshot.getKey();
                    }
                    Log.wtf("new_user_nick", new_user_nick);
                    Log.wtf("new_user_status", new_user_status);

                    try {
                        String finalNew_user_nick = new_user_nick;
                        String finalNew_user_status = new_user_status;
                        thisUser(new_user_nick + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    String new_user_name = snapshot.getValue(String.class);

                                    Requests_name.add(new_user_name);
                                    Requests_nick.add(finalNew_user_nick);
                                    Requests_status.add(finalNew_user_status);

                                    no_requests.setVisibility(View.GONE);
                                    requests.setVisibility(View.VISIBLE);

                                        /*AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle("Новый запрос");
                                        alert.setMessage("Пользователь " + new_user_name + " (под ником " + finalNew_user_nick + ") " +
                                                "желает добавить вас в свой список контактов. Принять запрос?");
                                        alert.setPositiveButton("Принять", (dialog, which) -> {

                                            if (!CONTACTS.contains(finalNew_user_nick)) {

                                                contacts.add(new MyContacts(new_user_name, ""));
                                                CONTACTS.add(finalNew_user_nick);

                                                addContactToBase(user_nick, finalNew_user_nick);

                                            } else {
                                                MyToast(getActivity(), "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT);
                                            }

                                            thisUserInfo("Request/" + user_nick + "/" + finalNew_user_nick).setValue("ok");

                                            //changeRequest(user_nick, "ok");
                                        });
                                        alert.setNegativeButton("Отклонить", (dialog, which) -> {
                                            dialog.dismiss();
                                            changeRequest(user_nick, "ok");
                                        });
                                        alert.show();*/
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }
}