package com.artrom.flychat.internet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.artrom.flychat.MySystem.ANIM_MSEC;
import static com.artrom.flychat.MySystem.CHAT;
import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.clearStringsStr;
import static com.artrom.flychat.MySystem.database;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.getCurrentTimeFromBase;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.info_connected;
import static com.artrom.flychat.MySystem.loadBaseMessages;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.onlineUsers;
import static com.artrom.flychat.MySystem.readFile;
import static com.artrom.flychat.MySystem.s;
import static com.artrom.flychat.MySystem.storageRef;
import static com.artrom.flychat.MySystem.thisUserInfo;

public class InternetActivity extends AppCompatActivity {

    EditText get_message;
    TextView current_interlocutor;
    ImageButton btn_edit_message, btn_attach, btn_back;
    ImageView image;
    Button top_delete, top_copy, top_cross;
    LinearLayout linlay_extra;

    public String my_name;
    public String my_nick;
    public String interlocutor_name;
    public String interlocutor_nick;
    public String chatId;
    String mkey;

    public static RecyclerView recyclerView;

    DatabaseReference chatRef;
    ChildEventListener childEventListener;
    long delay_time_long;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_internet);

        interlocutor_nick = getInterlocutorInfo()[0];
        chatId = getInterlocutorInfo()[1];
        interlocutor_name = getInterlocutorInfo()[2];

        findViewsId();

        prepareViews();

        setInternetAbort();

        my_nick = readFile(InternetActivity.this, file_nick);
        my_name = readFile(InternetActivity.this, "file_username" + my_nick);

        setInterlocutorTitle(interlocutor_name, interlocutor_nick, my_nick);

        TMP_ONLINE();

        //adapter_messages = new MessageAdapter(InternetActivity.this, messages, MESSAGES, my_nick);
        //recyclerView.setAdapter(adapter_messages);

        setListeners1(interlocutor_nick, my_nick);

        //SEND_TEST_NUMBER_MESSAGES(10);
    }

    private void SEND_TEST_NUMBER_MESSAGES(long repeat) {
        if (!hasConnection(InternetActivity.this)) {
            MyToast(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG);
        } else {

            DatabaseReference offsetRef = database.getReference(".info/serverTimeOffset");
            offsetRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    delay_time_long = snapshot.getValue(Long.class);
                    for (int i = 0; i < repeat; i++) {
                        String msg = String.valueOf(i);

                        if (notBlankSpaceEnter(msg)) {

                            String currentTime = getCurrentTimeFromBase(delay_time_long)[0];
                            String currentTimeToBase = getCurrentTimeFromBase(delay_time_long)[2];

                            msg = currentTime + "|" + my_nick + "|" + msg;

                            try {
                                mkey = CHAT(interlocutor_nick, my_nick).push().getKey();
                                CHAT(interlocutor_nick, my_nick)
                                        .child("messages")
                                        .child(currentTimeToBase + mkey)
                                        .setValue(msg);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            get_message.setText("");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void setListeners1(String interlocutor_nick, String my_nick) {

        btn_edit_message.setOnClickListener(v -> {

            if (!hasConnection(InternetActivity.this)) {
                MyToast(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG);
            } else {

                DatabaseReference offsetRef = database.getReference(".info/serverTimeOffset");
                offsetRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        delay_time_long = snapshot.getValue(Long.class);
                        sendMessage(interlocutor_nick, my_nick, delay_time_long);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        btn_back.setOnClickListener(view -> {
            finish();
        });

        chatRef = CHAT(my_nick, interlocutor_nick);//.child("messages");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MyLog("onChildAdded");
                loadBaseMessages(InternetActivity.this, interlocutor_nick, my_nick);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MyLog("onChildChanged");
                loadBaseMessages(InternetActivity.this, interlocutor_nick, my_nick);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                MyLog("onChildRemoved");
                loadBaseMessages(InternetActivity.this, interlocutor_nick, my_nick);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MyLog("onChildMoved");
                loadBaseMessages(InternetActivity.this, interlocutor_nick, my_nick);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                MyLog("onCancelled");
                loadBaseMessages(InternetActivity.this, interlocutor_nick, my_nick);
            }
        };
        chatRef.addChildEventListener(childEventListener);
    }

    private void sendMessage(String interlocutor_nick, String my_nick, long delay_time_long) {
        String msg = get_message.getText().toString();

        msg = clearStringsStr(msg);

        if (notBlankSpaceEnter(msg)) {

            String currentTime = getCurrentTimeFromBase(delay_time_long)[0];
            String currentTimeToBase = getCurrentTimeFromBase(delay_time_long)[2];

            msg = currentTime + "|" + my_nick + "|" + msg;

            try {
                mkey = CHAT(interlocutor_nick, my_nick).push().getKey();
                CHAT(interlocutor_nick, my_nick)
                        .child("messages")
                        .child(currentTimeToBase + mkey)
                        .setValue(msg);

                //MyToast(getApplicationContext(), String.valueOf(s), Toast.LENGTH_LONG);

            } catch (Exception e) {
                e.printStackTrace();
            }

            get_message.setText("");
        }
    }

    private void TMP_ONLINE() {
        ValueEventListener connectedEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    boolean connected = snapshot.getValue(Boolean.class);
                    DatabaseReference OU = onlineUsers(my_nick);

                    Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        try {
                            if (connected) {
                                OU.onDisconnect().setValue(ServerValue.TIMESTAMP);
                                OU.setValue("online");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }, 1500);
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        info_connected.addValueEventListener(connectedEventListener);
    }

    private void setInterlocutorTitle(String interlocutor_name, String interlocutor_nick, String my_nick) {
        btn_edit_message.setVisibility(View.VISIBLE);
        get_message.setVisibility(View.VISIBLE);

        MyLog(interlocutor_name);

        String s = "<b>" + interlocutor_name + "</b>";
        current_interlocutor.setText(Html.fromHtml(s));

        if (my_nick.equals(interlocutor_nick)) {

            String string = "<b>Переписка с самим собой</b>";
            current_interlocutor.setText(Html.fromHtml(string));

        } else {

            thisUserInfo("/Online_Users/" + interlocutor_nick).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        try {
                            DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                            offsetRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy в HH:mm", Locale.getDefault());
                                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                                    SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                    try {
                                        String last_time = dataSnapshot.getValue(String.class);

                                        if (last_time.equals("online")) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "online";
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        long date = dataSnapshot.getValue(Long.class);
                                        Date myDate = new Date(date);
                                        String last_time = dateFormat2.format(myDate);

                                        long delay_time_long = snapshot.getValue(Long.class) + System.currentTimeMillis();

                                        Date todayDate = new Date(delay_time_long);
                                        Date yesterdayDate = new Date(delay_time_long - Long.parseLong("86400000"));
                                        Date dbyDate = new Date(delay_time_long - 2 * Long.parseLong("86400000"));
                                        String todaystr = dateFormat2.format(todayDate);
                                        todaystr = todaystr.substring(0, 8) + "000000";
                                        String yesterdaystr = dateFormat2.format(yesterdayDate);
                                        yesterdaystr = yesterdaystr.substring(0, 8) + "000000";
                                        String dbystr = dateFormat2.format(dbyDate);
                                        dbystr = dbystr.substring(0, 8) + "000000";

                                        String add = "Был(а) в сети";

                                        if (Long.parseLong(last_time) >= Long.parseLong(todaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Сегодня в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(yesterdaystr) <= Long.parseLong(last_time) &&
                                                Long.parseLong(last_time) <= Long.parseLong(todaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Вчера в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(dbystr) <= Long.parseLong(last_time) &&
                                                Long.parseLong(last_time) <= Long.parseLong(yesterdaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Позавчера в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(last_time) <= Long.parseLong(dbystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + dateFormat1.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        String s = "<b>" + interlocutor_name + "</b>";
                        current_interlocutor.setText(Html.fromHtml(s));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }
    }

    private String[] getInterlocutorInfo() {
        String interlocutor_nick = getIntent().getStringExtra("current_interlocutor");
        String chatId = getIntent().getStringExtra("current_chat");
        String interlocutor_name = getIntent().getStringExtra("name_interlocutor");
        String icon = getIntent().getStringExtra("icon");

        return new String[]{interlocutor_nick, chatId, interlocutor_name, icon};
    }

    private void setInternetAbort() {
        Thread t = new Thread(() -> {
            while (true) {
                if (!hasConnection(InternetActivity.this)) {

                    Intent intent = new Intent(InternetActivity.this, WrapActivity.class);
                    intent.putExtra("toast", "1");
                    startActivity(intent);
                    break;
                }
            }
            finish();
        });
        t.start();
    }

    private void prepareViews() {
        current_interlocutor.setVisibility(View.VISIBLE);
        linlay_extra.setVisibility(View.GONE);

        AlphaAnimation alphaAnimation_up = new AlphaAnimation(0f, 1f);
        AlphaAnimation alphaAnimation_down = new AlphaAnimation(1f, 0.05f);
        alphaAnimation_up.setDuration(ANIM_MSEC / 2);
        //image.setVisibility(View.INVISIBLE);
        //recyclerView.setVisibility(View.GONE);

        String icon = getInterlocutorInfo()[3];
        String interlocutor_nick = getInterlocutorInfo()[0];
        MyLog("icon", icon);
        MyLog("interlocutor_nick", interlocutor_nick);

        String previousIcon = whatPreviousIcon(interlocutor_nick);

        MyLog("previousIcon", previousIcon);
        MyLog("icon", icon);

        if (previousIcon == null)
            load_set_saveIconBase(interlocutor_nick, icon, alphaAnimation_up, alphaAnimation_down);
        else if (previousIcon.equals(icon)) {
            setPreviousIcon(interlocutor_nick);
        } else {
            load_set_saveIconBase(interlocutor_nick, icon, alphaAnimation_up, alphaAnimation_down);
        }
    }

    private void load_set_saveIconBase(
            String user, String icon, AlphaAnimation alphaAnimation_up, AlphaAnimation alphaAnimation_down) {

        StorageReference iconRef = storageRef.child("account_icon/" + user + "/" + icon);

        iconRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
            Log.wtf("f", downloadUrl.toString());
            Thread t = new Thread(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl.toString()).openConnection();
                    connection.setRequestProperty("User-agent", "Mozilla/4.0");

                    connection.connect();
                    InputStream input = connection.getInputStream();

                    Bitmap bm = BitmapFactory.decodeStream(input);

                    InternetActivity.this.runOnUiThread(() -> {

                        //------------------------------------------------------------------------------------------LOAD AND SET

                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bm);

                        image.startAnimation(alphaAnimation_down);

                        Handler handler1 = new Handler();
                        handler1.postDelayed(() -> {
                            image.setBackground(bitmapDrawable);
                            image.startAnimation(alphaAnimation_up);
                        }, ANIM_MSEC);

                        //------------------------------------------------------------------------------------------SAVE

                        ByteArrayOutputStream blob = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 0, blob);
                        byte[] bitmapdata = blob.toByteArray();

                        try {
                            FileOutputStream fos = openFileOutput(user + "PREVIOUS_ICON_ICON", MODE_PRIVATE);
                            fos.flush();
                            fos.write(bitmapdata);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        MyLog("PREVIOUS_ICON", icon);
                        try {
                            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput(user + "PREVIOUS_ICON", MODE_PRIVATE)));
                            bnn.write(icon);
                            bnn.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
        });
    }

    private String whatPreviousIcon(String user) {

        String icon = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(user + "PREVIOUS_ICON")));
            icon = br.readLine();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return icon;
    }

    private void setPreviousIcon(String user) {
        try {
            FileInputStream fin = openFileInput(user + "PREVIOUS_ICON_ICON");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);

            InternetActivity.this.runOnUiThread(() -> {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                image.setBackground(d);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findViewsId() {
        btn_edit_message = findViewById(R.id.btn_edit_message);
        btn_attach = findViewById(R.id.btn_attach);
        get_message = findViewById(R.id.get_message);
        current_interlocutor = findViewById(R.id.current_interlocutor);
        linlay_extra = findViewById(R.id.linlay_extra);
        top_copy = findViewById(R.id.top_copy);
        top_cross = findViewById(R.id.top_cross);
        top_delete = findViewById(R.id.btn_all_users);
        image = findViewById(R.id.image);
        recyclerView = findViewById(R.id.messages_recycle);
        btn_back = findViewById(R.id.btn_back);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatRef.removeEventListener(childEventListener);
    }
}