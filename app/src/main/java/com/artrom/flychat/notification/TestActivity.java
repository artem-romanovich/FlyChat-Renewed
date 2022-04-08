package com.artrom.flychat.notification;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.artrom.flychat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.readFile;

public class TestActivity extends AppCompatActivity {
    EditText UserTB, Title, Message;
    Button send;
    private APIService apiService;

    //public final static String BASE_URL = "https://fcm.googleapis.com";
    //public final static String CONTENT_TYPE = "application/json";
    //public final static String SERVER_KEY = "AAAACO5Bfsw:APA91bGMWDhgllAhGfhDSyXXzjRZGTtHHcJuNkCnNn5Jk9Qb8BWBe3Xx2eMHr3OJ19nVfRmneHemG1PWZ_BfL_P3tyibKspqn_zkMWOe_GLZFvUi0wPdKo9YqI8xV3tTmCsfNyK5_0Ep";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        UserTB = findViewById(R.id.UserID);
        Title = findViewById(R.id.edtTitle);
        Message = findViewById(R.id.edtMessage);
        send = findViewById(R.id.btnSend);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        FirebaseMessaging.getInstance().subscribeToTopic(readFile(TestActivity.this, file_nick))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "subscribeToTopic");
                    }
                });

        send.setOnClickListener(view -> {

            String title = null;
            String message = null;

            if (getIntent().getExtras() != null) {
                for (String key : getIntent().getExtras().keySet()) {
                    if (key.equals("title")) {
                        title = getIntent().getExtras().getString(key);
                    } else if (key.equals("message")) {
                        message = getIntent().getExtras().getString(key);
                    }
                    MyLog(title + " " + message);
                }
            }

            FirebaseDatabase.getInstance().getReference().child("Tokens").child(UserTB.getText().toString().trim()).child("token")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            //sendNotifications(
                            //        usertoken,
                            //        Title.getText().toString().trim(),
                            //        Message.getText().toString().trim());
                            //MyLog(usertoken + " " + Title.getText().toString().trim() + " " + Message.getText().toString().trim());
                        }

                        @Override

                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            UpdateToken();
        });
    }

    private void UpdateToken() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Token token = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(readFile(TestActivity.this, file_nick)).setValue(token);
    }

    public void sendNotifications(String usertoken, String title, String message) {

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder("https://fcm.googleapis.com")
                .setMessageId(Integer.toString(555445))
                .addData("my_message", "Hello World")
                .addData("my_action", "SAY_HELLO")
                .build());

        //Data data = new Data(title, message);
        //NotificationSender sender = new NotificationSender(data, usertoken);
        /*apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(TestActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
            }
        });*/
    }
}