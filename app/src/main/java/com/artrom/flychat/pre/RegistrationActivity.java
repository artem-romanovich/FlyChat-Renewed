package com.artrom.flychat.pre;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artrom.flychat.BuildConfig;
import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static com.artrom.flychat.MySystem.Auth;
import static com.artrom.flychat.MySystem.CAPABLE;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.VERSION;
import static com.artrom.flychat.MySystem.checkForRegStarted;
import static com.artrom.flychat.MySystem.clearStringsStr;
import static com.artrom.flychat.MySystem.count;
import static com.artrom.flychat.MySystem.createAnimBundle;
import static com.artrom.flychat.MySystem.file_email;
import static com.artrom.flychat.MySystem.file_name;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.file_password;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.hideKeyboard;
import static com.artrom.flychat.MySystem.max_name_len;
import static com.artrom.flychat.MySystem.max_nick_len;
import static com.artrom.flychat.MySystem.min_password_len;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.thisUser;

public class RegistrationActivity extends AppCompatActivity {

    Button btn_view_password, btn_cancel, btn_register, btn_sign_in;
    EditText set_password, set_email, set_name, set_nick;
    TextView replacement_register, have_account, alaalarm;

    String user_nick;
    String user_name;
    String user_password;
    String user_email;

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_registration);

        findViews();

        prepareViews();

        setListeners();

        if (checkForRegStarted(RegistrationActivity.this)) {
            takeTheRegStartedForm();
        }
    }

    private void takeTheRegStartedForm() {
        set_email.setEnabled(false);
        set_name.setEnabled(false);
        set_nick.setEnabled(false);
        set_password.setEnabled(false);

        btn_view_password.setEnabled(false);

        hideKeyboard(RegistrationActivity.this, btn_sign_in);

        btn_sign_in.setEnabled(true);
        btn_sign_in.setBackgroundResource(R.drawable.btn_selector);

        btn_register.setVisibility(View.GONE);
        replacement_register.setVisibility(View.VISIBLE);
        alaalarm.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {


        have_account.setOnClickListener(view -> {
            Intent intent = new Intent(RegistrationActivity.this, SignInActivity.class);
            startActivity(intent, createAnimBundle(RegistrationActivity.this));
        });


        int two_digits = (int) (Math.random() * 100);
        View.OnTouchListener touch_listen = (v, event) -> {
            try {
                if (set_email != null) {
                    String idea_nick = set_email.getText().toString();
                    if (!idea_nick.equals("")) {
                        int indexet = idea_nick.indexOf("@");
                        if (indexet != -1) {
                            idea_nick = idea_nick.substring(0, indexet);
                        }
                        String finalIdea_nick = idea_nick;
                        finalIdea_nick = finalIdea_nick.toLowerCase().replaceAll("\\d", "");

                        try {
                            set_nick.setHint("Пример: " + toTranslit(finalIdea_nick) + two_digits);
                        } catch (Exception e) {
                            e.printStackTrace();
                            set_nick.setHint("Пример: " + finalIdea_nick + two_digits);
                        }
                    } else {
                        set_nick.setHint("Придумайте никнейм");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        };
        set_nick.setOnTouchListener(touch_listen);

        btn_view_password.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_UP:
                    set_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    set_password.setTypeface(Typeface.createFromAsset(getAssets(), "font/montserrat_medium.ttf"));
                    break;

                case MotionEvent.ACTION_DOWN:
                    set_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    set_password.setTypeface(Typeface.createFromAsset(getAssets(), "font/montserrat_medium.ttf"));
                    break;

            }
            return true;
        });


        btn_cancel.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(RegistrationActivity.this);
            alert.setTitle(R.string.if_sure);
            alert.setMessage("Вы точно хотите прервать создание вашего аккаунта? Вы сможете заново создать его в будущем");
            alert.setPositiveButton(R.string.yes, (dialog, which) -> {

                String email = null;
                String password = null;

                try {
                    BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                    BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                    email = br_e.readLine();
                    password = br_p.readLine();
                    br_e.close();
                    br_p.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (email == null || password == null) {

                    email = set_email.getText().toString();
                    password = set_password.getText().toString();

                    email = clearStringsStr(email);
                    password = clearStringsStr(password);

                }

                if (!email.equals("") && email != null && !password.equals("") && password != null) {

                    String finalEmail = email;
                    String finalPassword = password;
                    Auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegistrationActivity.this, task_a -> {
                                if (task_a.isSuccessful()) {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    AuthCredential credential = EmailAuthProvider.getCredential(finalEmail, finalPassword);

                                    if (user != null) {
                                        user.reauthenticate(credential).addOnCompleteListener(
                                                task -> user.delete().addOnCompleteListener(Task::isSuccessful));
                                    }

                                    try {
                                        BufferedWriter brnn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_nick", MODE_PRIVATE)));
                                        brnn.flush();
                                        brnn.close();
                                        BufferedWriter brn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                        brn.flush();
                                        brn.close();
                                        BufferedWriter brp = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                        brp.flush();
                                        brp.close();
                                        BufferedWriter bre = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                        bre.flush();
                                        bre.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }

                finish();
            });

            alert.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
            alert.show();
        });

        btn_register.setOnClickListener(view -> {

            boolean hasConnection = hasConnection(RegistrationActivity.this);

            if (!hasConnection) {

                MyToast(RegistrationActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG);

            } else {

                checkFieldsCorrect();

            }
        });

        btn_sign_in.setOnClickListener(view -> {
            try {
                BufferedReader br_tmp_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                BufferedReader br_tmp_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                String email = br_tmp_e.readLine();
                String password = br_tmp_p.readLine();
                br_tmp_e.close();
                br_tmp_p.close();

                Auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this, task_a -> {
                            if (task_a.isSuccessful()) {

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if (user != null) {
                                    if (user.isEmailVerified()) {
                                        try {
                                            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_nick")));
                                            user_nick = br_nn.readLine();
                                            br_nn.close();
                                            BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_name")));
                                            user_name = br_n.readLine();
                                            br_n.close();
                                            BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                                            user_password = br_p.readLine();
                                            br_p.close();
                                            BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                                            user_email = br_e.readLine();
                                            br_e.close();

                                            thisUser(user_nick).child("name").setValue(user_name);
                                            thisUser(user_nick).child("nick").setValue(user_nick);
                                            thisUser(user_nick).child("email").setValue(user_email);
                                            thisUser(user_nick).child("password").setValue(user_password);
                                            //addContactToBase(user_nick, Administration);

                                            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                                    openFileOutput(file_nick, MODE_PRIVATE)));
                                            bnn.write(user_nick);
                                            bnn.close();
                                            BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                                    openFileOutput(file_name + user_nick, MODE_PRIVATE)));
                                            bn.write(user_name);
                                            bn.close();
                                            BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                                    openFileOutput(file_password + user_nick, MODE_PRIVATE)));
                                            bp.write(user_password);
                                            bp.close();
                                            BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                                    openFileOutput(file_email + user_nick, MODE_PRIVATE)));
                                            be.write(user_email);
                                            be.close();

                                            BufferedWriter brnn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_nick", MODE_PRIVATE)));
                                            BufferedWriter brn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                            BufferedWriter brp = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                            BufferedWriter bre = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                            brnn.flush();
                                            brnn.close();
                                            brn.flush();
                                            brn.close();
                                            brp.flush();
                                            brp.close();
                                            bre.flush();
                                            bre.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        Intent intent = new Intent(RegistrationActivity.this, WrapActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent, createAnimBundle(RegistrationActivity.this));
                                        finish();
                                    } else {

                                        MyToast(RegistrationActivity.this, "Почтовый адрес не подтвержден", Toast.LENGTH_LONG);

                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static String toTranslit(String src) {
        String f = " АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЧЦШЩЭЮЯЫЪЬабвгдеёжзийклмнопрстуфхчцшщэюяыъь";

        String[] t = {"_", "A", "B", "V", "G", "D", "E", "Jo", "Zh", "Z", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ch", "C", "Sh", "Csh", "E", "Ju", "Ja", "Y", "`", "'", "a", "b", "v", "g", "d", "e", "jo", "zh", "z", "i", "j", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ch", "c", "sh", "csh", "e", "ju", "ja", "y", "`", "'"};

        StringBuilder res = new StringBuilder(src.length());

        for (int i = 0; i < src.length(); ++i) res.append(t[f.indexOf(src.charAt(i))]);

        return res.toString();
    }

    private void checkDatabase() {
        CAPABLE.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NotNull DataSnapshot datasnapshot, String previousChildName) {
                if (datasnapshot.exists()) {
                    try {
                        String status = datasnapshot.getValue(String.class);
                        Log.wtf("Capable", status);

                        if (status.equals("NO")) {
                            MyToast(RegistrationActivity.this, "Ведутся работы, приложение временно недоступно", Toast.LENGTH_LONG);
                            /*Intent intent = new Intent(RegistrationActivity.this, WrapActivity.class);
                            startActivity(intent);
                            finish();*/
                        } else {
                            if (!status.equals("YES")) {
                                MyToast(RegistrationActivity.this, status, Toast.LENGTH_LONG);
                            }
                        }
                        if (status.equals("YES")) {

                            VERSION.addChildEventListener(new ChildEventListener() {
                                @SuppressLint("ResourceType")
                                @Override
                                public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {

                                    int last_version;
                                    try {
                                        last_version = Integer.parseInt(datasnapshot.getValue(String.class));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        last_version = datasnapshot.getValue(Integer.class);
                                    }
                                    Log.wtf("VersionDatabase", String.valueOf(last_version));
                                    Log.wtf("VersionApp", String.valueOf(BuildConfig.VERSION_CODE));

                                    if (last_version != BuildConfig.VERSION_CODE) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                        builder.setTitle("Требуется обновление")
                                                .setMessage("Неактуальная версия приложения. Требуется обновление на сайте")
                                                .setNegativeButton("Обновить", (dialog, which) -> {

                                                    Intent intent = new Intent(RegistrationActivity.this, FirstActivity.class);
                                                    startActivity(intent, createAnimBundle(RegistrationActivity.this));

                                                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://artem-romanovich.github.io/flychat_share/"));
                                                    //startActivity(browserIntent);
                                                });
                                        AlertDialog dialog = builder.create();

                                        dialog.show();
                                    } else {
                                        registrationBeforeVerification();
                                    }
                                }

                                @Override
                                public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                                }

                                @Override
                                public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                                }

                                @Override
                                public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
    }

    private void checkFieldsCorrect() {
        user_name = set_name.getText().toString();
        user_password = set_password.getText().toString();
        user_email = set_email.getText().toString();
        user_nick = set_nick.getText().toString();

        user_name = clearStringsStr(user_name);
        user_password = clearStringsStr(user_password);
        user_email = clearStringsStr(user_email);
        user_nick = clearStringsStr(user_nick);

        if (!hasConnection(RegistrationActivity.this)) {
            MyToast(RegistrationActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);

        } else {
            if (notBlankSpaceEnter(user_email) && notBlankSpaceEnter(user_password) &&
                    notBlankSpaceEnter(user_nick) && notBlankSpaceEnter(user_name)) {

                if (user_nick.length() <= max_nick_len) {
                    if (user_name.length() <= max_name_len) {
                        if (count(user_name, " ") == 1) {
                            if (user_password.length() >= min_password_len) {

                                thisUser(user_nick + "/nick").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {

                                            thisUser(user_nick + "/email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    if (!snapshot.exists()) {

                                                        checkDatabase();

                                                    } else {
                                                        MyToast(RegistrationActivity.this, "Пользователь с таким почтовым адресом уже существует", Toast.LENGTH_LONG);

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        } else {
                                            MyToast(RegistrationActivity.this, "Пользователь с таким никнеймом уже существует", Toast.LENGTH_SHORT);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                    }
                                });
                            } else {
                                MyToast(RegistrationActivity.this, "Введите пароль подлиннее (минимум 8 символов)", Toast.LENGTH_SHORT);

                            }
                        } else {
                            MyToast(RegistrationActivity.this, "Имя и Фамилия должны быть разделены одним пробелом", Toast.LENGTH_SHORT);

                        }
                    } else {
                        MyToast(RegistrationActivity.this, "Слишком длинное имя", Toast.LENGTH_SHORT);

                    }
                } else {
                    MyToast(RegistrationActivity.this, "Введите ник покороче", Toast.LENGTH_SHORT);

                }

            } else {
                MyToast(RegistrationActivity.this, "Пустое поле ввода", Toast.LENGTH_SHORT);

            }
        }
    }

    private void registrationBeforeVerification() {

        Auth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(RegistrationActivity.this, task -> {
                    if (task.isSuccessful()) {

                        takeTheRegStartedForm();

                        currentUser = Auth.getCurrentUser();
                        if (currentUser != null) {
                            currentUser.sendEmailVerification().addOnCompleteListener(RegistrationActivity.this, task1 -> {
                            });
                        }

                        try {
                            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput("tmp_file_nick", MODE_PRIVATE)));
                            bnn.write(user_nick);
                            bnn.close();

                            BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput("tmp_file_name", MODE_PRIVATE)));
                            bn.write(user_name);
                            bn.close();

                            BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput("tmp_file_password", MODE_PRIVATE)));
                            bp.write(user_password);
                            bp.close();

                            BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput("tmp_file_email", MODE_PRIVATE)));
                            be.write(user_email);
                            be.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        MyToast(RegistrationActivity.this,
                                "Регистрация оборвалась. Попробуйте выбрать другой адрес электроной почты", Toast.LENGTH_LONG);

                    }
                });
    }

    private void findViews() {
        set_password = findViewById(R.id.set_password);
        set_email = findViewById(R.id.set_email);
        set_name = findViewById(R.id.set_name);
        set_nick = findViewById(R.id.set_nick);

        replacement_register = findViewById(R.id.replacement_register);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_sign_in = findViewById(R.id.btn_sign_in);
        btn_register = findViewById(R.id.btn_register);
        btn_view_password = findViewById(R.id.btn_view_password);
        have_account = findViewById(R.id.have_account);
        alaalarm = findViewById(R.id.alaalarm);
    }

    private void prepareViews() {

        replacement_register.setVisibility(View.GONE);
        alaalarm.setVisibility(View.GONE);

        set_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        set_password.setTypeface(Typeface.createFromAsset(getAssets(), "font/montserrat_medium.ttf"));

        btn_sign_in.setEnabled(false);
        btn_sign_in.setBackgroundResource(R.drawable.btn_inactive);
    }
}