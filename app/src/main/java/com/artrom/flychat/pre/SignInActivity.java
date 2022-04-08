package com.artrom.flychat.pre;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import static com.artrom.flychat.MySystem.Auth;
import static com.artrom.flychat.MySystem.CHAT_NUMBER;
import static com.artrom.flychat.MySystem.CONTACTS;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.clearStringsStr;
import static com.artrom.flychat.MySystem.contacts;
import static com.artrom.flychat.MySystem.file_email;
import static com.artrom.flychat.MySystem.file_name;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.file_password;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.thisUser;

public class SignInActivity extends AppCompatActivity {

    Button btn_sign_in;
    EditText set_password, set_nick;

    String user_nick;
    String user_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_sign_in);

        findViews();

        setListeners();
    }

    private void setListeners() {
        btn_sign_in.setOnClickListener(view -> {

            boolean hasConnection = hasConnection(SignInActivity.this);

            if (!hasConnection) {

                MyToast(SignInActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG);

            } else {
                checkFieldsCorrect();
            }
        });
    }

    private void checkFieldsCorrect() {
        user_password = set_password.getText().toString();
        user_nick = set_nick.getText().toString();

        user_password = clearStringsStr(user_password);
        user_nick = clearStringsStr(user_nick);

        if (!hasConnection(SignInActivity.this)) {
            MyToast(SignInActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);

        } else {
            if (notBlankSpaceEnter(user_nick) && notBlankSpaceEnter(user_password)) {

                signIn(user_password, user_nick);

            } else {
                MyToast(SignInActivity.this, "Пустое поле ввода", Toast.LENGTH_SHORT);

            }
        }
    }

    private void signIn(String user_password, String user_nick) {

        final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setTitle("Проверка...");
        progressDialog.show();

        try {
            CONTACTS.clear();
            CHAT_NUMBER.clear();
            contacts.clear();
        } catch (Exception ignored) {
        }

        thisUser(user_nick).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    thisUser(user_nick + "/password/").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                            String base_password = dataSnapshot.getValue(String.class);

                            if (base_password != null && base_password.equals(user_password)) {

                                thisUser(user_nick + "/email/").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                        String base_email = dataSnapshot.getValue(String.class);

                                        if (base_email != null) {

                                            Auth.signInWithEmailAndPassword(base_email, base_password)
                                                    .addOnCompleteListener(SignInActivity.this, task -> {
                                                        if (task.isSuccessful()) {
                                                            loadUsersData(user_nick, user_password, progressDialog);
                                                        } else progressDialog.dismiss();
                                                    });
                                            //loadUsersData(user_nick, user_password);

                                        } else {
                                            progressDialog.dismiss();
                                            MyToast(SignInActivity.this, "Произошла ошибка", Toast.LENGTH_SHORT);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                        progressDialog.dismiss();
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                MyToast(SignInActivity.this, "Неверный пароль", Toast.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void onCancelled(@NotNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    MyToast(SignInActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void loadUsersData(String base_nick, String base_password, ProgressDialog progressDialog) {
        thisUser(user_nick + "/name/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                String base_name = dataSnapshot.getValue(String.class);

                thisUser(user_nick + "/email/").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                        String base_email = dataSnapshot.getValue(String.class);

                        if (base_email != null && base_name != null) {
                            if (notBlankSpaceEnter(base_email) && notBlankSpaceEnter(base_nick) &&
                                    notBlankSpaceEnter(base_password) && notBlankSpaceEnter(base_name)) {

                                progressDialog.dismiss();

                                //MyToast(SignInActivity.this, "Авторизация успешна", Toast.LENGTH_SHORT);
                                Log.wtf("UserParameters", base_nick + " " + base_name + " " + base_email + " " + base_password);

                                try {
                                    BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                            openFileOutput(file_nick, MODE_PRIVATE)));
                                    bnn.write(base_nick);
                                    bnn.close();
                                    BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                            openFileOutput(file_name + base_nick, MODE_PRIVATE)));
                                    bn.write(base_name);
                                    bn.close();
                                    BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                            openFileOutput(file_password + base_nick, MODE_PRIVATE)));
                                    bp.write(base_password);
                                    bp.close();
                                    BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                            openFileOutput(file_email + base_nick, MODE_PRIVATE)));
                                    be.write(base_email);
                                    be.close();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(SignInActivity.this, WrapActivity.class);

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);

                            } else {
                                progressDialog.dismiss();
                                MyToast(SignInActivity.this, "Произошла ошибка", Toast.LENGTH_SHORT);
                            }
                        } else {
                            progressDialog.dismiss();
                            MyToast(SignInActivity.this, "Произошла ошибка", Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void findViews() {
        set_password = findViewById(R.id.sign_password);
        btn_sign_in = findViewById(R.id.sign_btn);
        set_nick = findViewById(R.id.sign_nick);
    }
}