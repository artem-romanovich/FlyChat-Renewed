package com.artrom.flychat.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.artrom.flychat.internet.InternetActivity;
import com.artrom.flychat.system.MyContacts;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.artrom.flychat.MySystem.ANIM_MSEC;
import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.READ_EXTERNAL_permission;
import static com.artrom.flychat.MySystem.WRITE_EXTERNAL_permission;
import static com.artrom.flychat.MySystem.adapter_messages;
import static com.artrom.flychat.MySystem.checkStoragePermissions;
import static com.artrom.flychat.MySystem.createAnimBundle;
import static com.artrom.flychat.MySystem.getChatId;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.storageRef;
import static com.artrom.flychat.MySystem.thisUser;
import static com.artrom.flychat.MySystem.thisUserInfo;
import static com.artrom.flychat.internet.InternetFragment.linlay_bar;
import static com.artrom.flychat.internet.InternetFragment.linlay_list;
import static com.artrom.flychat.internet.InternetFragment.no_users;

//import static com.artrom.flychat.MySystem.Administration;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private final String my_nick;
    private final ArrayList<MyContacts> contacts;
    private final ArrayList<String> CONTACTS;

    public ContactsAdapter(
            Context context, ArrayList<MyContacts> contacts, ArrayList<String> CONTACTS, String my_nick) {
        this.contacts = contacts;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.my_nick = my_nick;
        this.CONTACTS = CONTACTS;
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contact_item, parent, false);

        linlay_bar.setVisibility(View.VISIBLE);
        linlay_list.setVisibility(View.GONE);
        no_users.setVisibility(View.GONE);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        Handler handler1 = new Handler();
        handler1.postDelayed(() -> {
            if (position == CONTACTS.size() - 1) {
                linlay_bar.setVisibility(View.GONE);
                no_users.setVisibility(View.GONE);
                linlay_list.setVisibility(View.VISIBLE);
            }
        }, 100);

        MyContacts myContact = contacts.get(position);
        String user = CONTACTS.get(position);

        viewHolder.name_contact.setText(myContact.getName());
        //viewHolder.status_contact.setBackgroundResource(R.drawable.premium);
        viewHolder.last_msg.setText("Напишите первым!");

        viewHolder.itemView.setOnClickListener(view -> {

            if (!hasConnection(context)) {
                MyToast(context, "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);
            } else {

                thisUserInfo("Request/" + user + "/" + my_nick).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String request_status = dataSnapshot.getValue(String.class);

                            if (request_status.equals("request")) {
                                MyToast(context, "Запрос отправлен, дождитесь его принятия", Toast.LENGTH_SHORT);
                            }
                            if (request_status.equals("blocked")) {
                                MyToast(context, "Вы были заблокированы пользователем", Toast.LENGTH_SHORT);
                            }
                            if (request_status.equals("ok")) {

                                thisUser(user + "/nick").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        thisUser(user + "/icon").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                String icon = null;
                                                if (snapshot.exists()) {
                                                    icon = snapshot.getValue(String.class);
                                                }
                                                MyLog("iconiconicon", icon);

                                                Intent intent = new Intent(context, InternetActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                intent.putExtra("current_interlocutor", user);
                                                intent.putExtra("name_interlocutor", myContact.getName());
                                                intent.putExtra("current_chat", getChatId(user, my_nick));
                                                intent.putExtra("icon", icon);

                                                context.startActivity(intent, createAnimBundle(context));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        } else {
                            if (user.equals(my_nick)
                                //|| user.equals(Administration)
                            ) {

                                thisUser(user + "/nick").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                        thisUser(user + "/icon").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                String icon = null;
                                                if (snapshot.exists()) {
                                                    icon = snapshot.getValue(String.class);
                                                }
                                                MyLog("iconiconicon", icon);

                                                Intent intent = new Intent(context, InternetActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                intent.putExtra("current_interlocutor", user);
                                                intent.putExtra("name_interlocutor", myContact.getName());
                                                intent.putExtra("current_chat", getChatId(user, my_nick));
                                                intent.putExtra("icon", icon);

                                                context.startActivity(intent, createAnimBundle(context));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Отправить запрос?")
                                        .setMessage("Если пользователь примет запрос, вы сможете отправлять ему сообщения.")
                                        .setPositiveButton("Отправить", (dialog, which) -> {
                                            thisUserInfo("Request/" + user + "/" + my_nick).setValue("request");
                                        })
                                        .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    }
                });
            }
        });

        viewHolder.itemView.setOnLongClickListener(view -> {

            //if (!user.equals(Administration)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Удаление");
            alert.setMessage("Вы точно хотите удалить данного пользователя из списка своих контактов?");
            alert.setPositiveButton("Отмена", (dialogInterface, i) -> {
                removeContactFromBase(my_nick, user);
                adapter_messages.notifyDataSetChanged();
            });
            alert.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
            alert.show();
            //}

            return false;
        });

        AlphaAnimation alphaAnimation_up = new AlphaAnimation(0.05f, 1f);
        AlphaAnimation alphaAnimation_down = new AlphaAnimation(1f, 0.05f);
        alphaAnimation_up.setDuration(ANIM_MSEC * 2);
        alphaAnimation_down.setDuration(ANIM_MSEC * 2);

        if (!READ_EXTERNAL_permission(context) || !WRITE_EXTERNAL_permission(context)) {

            checkStoragePermissions(context);

        } else {

            thisUser(user + "/icon").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        String icon = snapshot.getValue(String.class);
                        MyLog("user", String.valueOf(user));
                        MyLog("icon", String.valueOf(icon));

                        try {
                            setIcon(user, icon, viewHolder, alphaAnimation_up, alphaAnimation_down);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void setIcon(String user, String icon, ViewHolder viewHolder, AlphaAnimation alphaAnimation_up, AlphaAnimation alphaAnimation_down) {

        String previousIcon = whatPreviousIcon(user);

        MyLog("previousIcon", previousIcon);
        MyLog("icon", icon);

        if (previousIcon == null)
            load_set_saveIconBase(user, icon, viewHolder, alphaAnimation_up, alphaAnimation_down);
        else if (previousIcon.equals(icon)) {
            setPreviousIcon(user, viewHolder);
        } else {
            load_set_saveIconBase(user, icon, viewHolder, alphaAnimation_up, alphaAnimation_down);
        }
    }

    private void load_set_saveIconBase(
            String user, String icon, ViewHolder viewHolder, AlphaAnimation alphaAnimation_up, AlphaAnimation alphaAnimation_down) {

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

                    ((WrapActivity) context).runOnUiThread(() -> {

                        //------------------------------------------------------------------------------------------LOAD AND SET

                        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bm);

                        viewHolder.contact_icon.startAnimation(alphaAnimation_down);

                        Handler handler1 = new Handler();
                        handler1.postDelayed(() -> {
                            viewHolder.contact_icon.setBackground(bitmapDrawable);
                            viewHolder.contact_icon.startAnimation(alphaAnimation_up);
                        }, ANIM_MSEC);

                        //------------------------------------------------------------------------------------------SAVE

                        ByteArrayOutputStream blob = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 0, blob);
                        byte[] bitmapdata = blob.toByteArray();

                        try {
                            FileOutputStream fos = context.openFileOutput(user + "PREVIOUS_ICON_ICON", MODE_PRIVATE);
                            fos.flush();
                            fos.write(bitmapdata);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        MyLog("PREVIOUS_ICON", icon);
                        try {
                            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                    context.openFileOutput(user + "PREVIOUS_ICON", MODE_PRIVATE)));
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
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(user + "PREVIOUS_ICON")));
            icon = br.readLine();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return icon;
    }

    private void setPreviousIcon(String user, ViewHolder viewHolder) {
        try {
            FileInputStream fin = context.openFileInput(user + "PREVIOUS_ICON_ICON");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);

            ((WrapActivity) context).runOnUiThread(() -> {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Drawable d = new BitmapDrawable(context.getResources(), bitmap);
                viewHolder.contact_icon.setBackground(d);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void deleteOldIcons(String fileContactIconDir, File[] previousIcons, String icon) {
        for (File file : previousIcons) {
            if (file.exists() && !fileContactIconDir.equals(String.valueOf(file))) {
                file.delete();

                MyLog("testicon", fileContactIconDir);
                MyLog("testicon", String.valueOf(file));
            }
        }

        StorageReference deleteRef = storageRef.child("account_icon/" + user);
        deleteRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                MyLog("icon_to_delete", String.valueOf(item));

                String icon_to_delete = String.valueOf(item);

                if (!icon_to_delete.equals("gs://flychat-renewed.appspot.com/account_icon/" + user + "/" + icon)) {

                    storageRef.child("account_icon/" + user + "/" +
                            item.toString().substring(item.toString().lastIndexOf("/") + 1)).delete().addOnSuccessListener(aVoid -> {
                        Log.d("TAG", "onSuccess: deleted file" + icon);
                    });
                }
            }
        });
    }*/

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name_contact, last_msg;
        final ImageView contact_icon, status_contact;
        final MaterialCardView photo_card;
        final LinearLayout main_lay;

        ViewHolder(View view) {
            super(view);
            name_contact = view.findViewById(R.id.name_contact);
            status_contact = view.findViewById(R.id.status_contact);
            last_msg = view.findViewById(R.id.last_msg);
            contact_icon = view.findViewById(R.id.contact_icon);
            photo_card = view.findViewById(R.id.photo_card);
            main_lay = view.findViewById(R.id.main_lay);
        }
    }

    public static void removeContactFromBase(String remove_for_him, String remove_him) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG", "Number of children: " + dataSnapshot.getChildrenCount());
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String value = (String) ds.getValue();
                    String key = ds.getKey();
                    Log.d(key, value);

                    if (value != null) {
                        if (value.equals(remove_him)) {
                            thisUser(remove_for_him + "/Contacts/" + key).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        thisUser(remove_for_him + "/Contacts/").addListenerForSingleValueEvent(valueEventListener);
    }
}