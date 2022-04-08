package com.artrom.flychat.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.artrom.flychat.system.MyContacts;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import static com.artrom.flychat.MySystem.CONTACTS;
import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.contacts;
import static com.artrom.flychat.MySystem.createCloudChat;
import static com.artrom.flychat.MySystem.storageRef;
import static com.artrom.flychat.MySystem.thisUser;
import static com.artrom.flychat.MySystem.thisUserInfo;
import static com.artrom.flychat.internet.InternetFragment.addContactToBase;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private int layout;
    private final Context context;
    private final String my_nick;
    ArrayList<String> Requests_nick;
    ArrayList<String> Requests_name;
    ArrayList<String> Requests_status;

    ValueEventListener valueEventListener;
    boolean fuck_work_please;

    public RequestsAdapter(Context context, ArrayList<String> Requests_nick, ArrayList<String> Requests_name, ArrayList<String> Requests_status, String my_nick) {
        this.Requests_name = Requests_name;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.my_nick = my_nick;
        this.Requests_nick = Requests_nick;
        this.Requests_status = Requests_status;
    }

    @Override
    public RequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.request_item_new, parent, false);
        return new ViewHolder(view);
    }

    public void setIcon(String user, RequestsAdapter.ViewHolder viewHolder) {
        thisUser(user + "/nick").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String icon = snapshot.getValue(String.class);

                    StorageReference iconRef = storageRef.child("account_icon/" +user+"/"+ icon);
                    iconRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        Log.wtf("f", downloadUrl.toString());
                        Thread t = new Thread(() -> {
                            try {
                                HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl.toString()).openConnection();
                                connection.setRequestProperty("User-agent", "Mozilla/4.0");

                                connection.connect();
                                InputStream input = connection.getInputStream();

                                Bitmap bm = BitmapFactory.decodeStream(input);

                                ((WrapActivity) context).runOnUiThread(() -> {
                                    viewHolder.contact_icon.setBackground(new BitmapDrawable(context.getResources(), bm));
                                    /*viewHolder.photo_card.setStrokeColor(Color.parseColor("#808080"));
                                    viewHolder.photo_card.setStrokeWidth(3);*/
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        t.start();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBindViewHolder(RequestsAdapter.ViewHolder viewHolder, int position) {
        String user_name = Requests_name.get(position);
        String user_nick = Requests_nick.get(position);

        viewHolder.name_contact.setText(user_name + "\n" + user_nick);
        //viewHolder.name_contact.setText(user_name + "\nnickname: " + user_nick);

        viewHolder.switch_compat.setVisibility(View.GONE);

        fuck_work_please = true;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (fuck_work_please) {
                    if (dataSnapshot.exists()) {

                        String user_status = null;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            user_status = postSnapshot.getValue(String.class);
                        }

                        MyLog(String.valueOf(fuck_work_please));
                        //if (user_status.equals("request") || user_status.equals("blocked")) {
                        viewHolder.switch_compat.setChecked(
                                Objects.requireNonNull(user_status).equals("ok"));
                        viewHolder.switch_compat.setVisibility(View.VISIBLE);

                        viewHolder.switch_compat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (fuck_work_please) {

                                thisUserInfo("Request/" + my_nick).removeEventListener(valueEventListener);
                                fuck_work_please = false;

                                viewHolder.switch_compat.setClickable(false);
                                viewHolder.switch_compat.setChecked(!isChecked);

                                String action;
                                String capable;
                                String for_toast;
                                if (isChecked) {
                                    action = "разблокировать";
                                    capable = "";
                                    for_toast = "блокировки";
                                } else {
                                    action = "заблокировать";
                                    capable = "не ";
                                    for_toast = "разблокировки";
                                }

                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setTitle("Вы уверены?");
                                alert.setMessage("Вы собираетесь " + action + " пользователя. После этого он " + capable + "сможет отправлять вам сообщения. Продолжить?");
                                alert.setPositiveButton("Да", (dialog, which) -> {

                                    MyToast(context, "Для " + for_toast + " потребуется перезапуск", Toast.LENGTH_LONG);

                                    if (isChecked) {

                                        MyLog("yes");

                                        if (!CONTACTS.contains(user_nick)) {

                                            contacts.add(new MyContacts(user_name, ""));
                                            CONTACTS.add(user_nick);

                                            addContactToBase(user_nick, user_nick);
                                        }

                                        viewHolder.switch_compat.setChecked(true);
                                        thisUserInfo("Request/" + my_nick + "/" + user_nick).setValue("ok");
                                        thisUserInfo("Request/" + user_nick + "/" + my_nick).setValue("ok");//!!

                                        createCloudChat(my_nick, user_nick);

                                    } else {
                                        MyLog("no");

                                        viewHolder.switch_compat.setChecked(false);
                                        thisUserInfo("Request/" + my_nick + "/" + user_nick).setValue("blocked");
                                    }

                                });
                                alert.setNegativeButton("Отмена", (dialog, which) -> {
                                    dialog.dismiss();
                                });
                                alert.show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        thisUserInfo("Request/" + my_nick).addListenerForSingleValueEvent(valueEventListener);

        //viewHolder.name_contact.setText(user_name + " (" + user_nick + ")");

        //viewHolder.yes.setOnClickListener(view -> {
        //    if (!CONTACTS.contains(user_nick)) {
//
        //        contacts.add(new MyContacts(user_name, ""));
        //        CONTACTS.add(user_nick);
//
        //        addContactToBase(user_nick, user_nick);
//
        //    } else {
        //        MyToast(context, "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT);
        //    }
//
        //    thisUserInfo("Request/" + my_nick + "/" + user_nick).setValue("ok");
        //    viewHolder.main_lay.setVisibility(View.GONE);
        //
        //    createCloudChat(my_nick, user_nick);
        //});
        //viewHolder.no.setOnClickListener(view -> {
        //    thisUserInfo("Request/" + my_nick + "/" + user_nick).setValue("blocked");
        //    viewHolder.main_lay.setVisibility(View.GONE);
        //});

        setIcon(user_nick, viewHolder);
    }

    @Override
    public int getItemCount() {
        return Requests_nick.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name_contact;
        final ImageView contact_icon;
        final MaterialCardView photo_card;
        LinearLayout main_lay;
        SwitchCompat switch_compat;
        Button no, yes;

        ViewHolder(View view) {
            super(view);
            name_contact = view.findViewById(R.id.name_contact);
            contact_icon = view.findViewById(R.id.contact_icon);
            photo_card = view.findViewById(R.id.photo_card);
            main_lay = view.findViewById(R.id.main_lay);
            switch_compat = view.findViewById(R.id.switch_compat);
            //no = view.findViewById(R.id.no);
            //yes = view.findViewById(R.id.yes);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        thisUserInfo("Request/" + my_nick).removeEventListener(valueEventListener);
    }
}