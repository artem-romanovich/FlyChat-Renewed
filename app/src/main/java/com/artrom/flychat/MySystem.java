package com.artrom.flychat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.adapters.ContactsAdapter;
import com.artrom.flychat.adapters.MessageAdapter;
import com.artrom.flychat.system.MyContacts;
import com.artrom.flychat.system.MyMessage;
import com.artrom.flychat.system.SmoothScroller;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;
import static com.artrom.flychat.internet.InternetActivity.recyclerView;

public class MySystem {

    //Firebase
    public final static FirebaseAuth Auth = FirebaseAuth.getInstance();
    public final static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public final static FirebaseStorage storage = FirebaseStorage.getInstance();
    public final static StorageReference storageRef = storage.getReference();

    public final static DatabaseReference CAPABLE = database.getReference("Status/Capable");
    public final static DatabaseReference VERSION = database.getReference("Status/Version");
    public final static DatabaseReference LINK = database.getReference("Status/share_link");
    public final static DatabaseReference INTERNET_MESSAGES = database.getReference("Internet_Messages/");

    public final static DatabaseReference info_connected = database.getReference(".info/connected");

    DatabaseReference USER;
    DatabaseReference FILES;
    DatabaseReference INTERLOCUTOR;
    DatabaseReference REQUEST;
    DatabaseReference NOTIFICATION;

    //Strings
    public final static String file_name = "file_username";
    public final static String file_nick = "file_nick";
    public final static String file_password = "file_password";
    public final static String file_email = "file_email";

    //public final static String Administration = "Administration";

    //Contacts
    public static ContactsAdapter contactsAdapter;
    public static ArrayList<String> CONTACTS;
    public static ArrayList<String> CHAT_NUMBER;
    public static ArrayList<MyContacts> contacts;

    //Messages
    public static ArrayList<MyMessage> messages;
    public static MessageAdapter adapter_messages;

    //Internet
    ///public static int max_message_length = 2500;

    //Account
    public static int photo_quality = 50;
    public static int max_nick_len = 20;
    public static int max_name_len = 20;
    public static int min_password_len = 8;

    //Int
    public final static int splash_screen_delay = 4;

    //Others
    public static Toast toast;
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ACCESS_BACKGROUND_LOCATION = 2;
    public static final int ANIM_MSEC = 250;

    public static Toolbar toolbar;

    public static long s;

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        if (dir != null) {
            return dir.delete();
        } else return false;
    }

    public static Bundle createAnimBundle(Context context) {
        return ActivityOptionsCompat.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
    }

    public static boolean READ_EXTERNAL_permission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean WRITE_EXTERNAL_permission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void checkStoragePermissions(Context context) {
        if (!READ_EXTERNAL_permission(context)) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (!WRITE_EXTERNAL_permission(context)) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public static int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }

    public static void createCloudChat(String my_nick, String user_nick) {
        String[] tmp = new String[]{my_nick, user_nick};
        Arrays.sort(tmp);
        database.getReference("Messages").child(tmp[0] + "_" + tmp[1]).setValue(1);
    }

    public static String getChatId(String my_nick, String user_nick) {
        String[] tmp = new String[]{my_nick, user_nick};
        Arrays.sort(tmp);
        return tmp[0] + "_" + tmp[1];
    }

    public static DatabaseReference CHAT(String my_nick, String user_nick) {
        return database.getReference("Messages").child(getChatId(my_nick, user_nick));
    }

    public static void MyToast(Context context, String string, int length) {

        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, string, length);
        toast.show();
    }

    public static boolean checkForRegStarted(Context context) {
        String tmp = null;
        try {
            BufferedReader br_nn = new BufferedReader(
                    new InputStreamReader(context.openFileInput("tmp_file_nick")));
            tmp = br_nn.readLine();
            br_nn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp != null && !tmp.equals("");
    }

    public static void MyLog(String string) {
        Log.wtf("my_log", string);
    }

    public static void MyLog(String tag, String string) {
        Log.wtf(tag, string);
    }

    public static DatabaseReference thisUser(String string) {
        return database.getReference("Users/UsersList/" + string);
    }

    public static DatabaseReference onlineUsers(String string) {
        return database.getReference("Users/UsersInfo/Online_Users/" + string);
    }

    public static DatabaseReference thisUserInfo(String string) {
        return database.getReference("Users/UsersInfo/" + string);
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String clearStringsStr(String string) {

        string = string.trim();

        for (int i = 0; i < string.length(); i++) {
            string = string.replaceAll(" {2}", " ");
            string = string.replaceAll("\n\n", "\n");
        }

        return string;
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    public static boolean notBlankSpaceEnter(String string) {
        return !string.equals("") && !string.equals(" ") && !string.equals("/n");
    }

    public static String readFile(Context context, String file_name) {
        String string = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(file_name)));
            string = br.readLine();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String[] getCurrentTimeFromBase(long delay_time) {

        long estimatedServerTimeMs = delay_time + System.currentTimeMillis();

        Date myDate = new Date(estimatedServerTimeMs);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentTime = dateFormat.format(myDate);

        String get_time_To_look = customizeDateView(currentTime);
        String get_time_To_base = customizeDateBase(currentTime);

        return new String[]{currentTime, get_time_To_look, get_time_To_base};
    }

    public static String customizeDateView(String date) {
        return String.format("%s.%s\n%s.%s.%s", date.substring(8, 10), date.substring(10, 12), date.substring(6, 8), date.substring(4, 6), date.substring(0, 4));
    }

    public static String customizeDateBase(String date) {
        return String.format("%s-%s-%s_%s:%s:%s", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8), date.substring(8, 10), date.substring(10, 12), date.substring(12));
    }

    public static String customizeDateOnlyTime(String date) {
        return date.substring(8, 10) + "." + date.substring(10, 12);
    }

    public static void loadDataContacts(Context context, String user_nick) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences" + user_nick, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task" + user_nick, null);
        if (json != null) {
            json = "[" + json + "]";
        }
        Type type = new TypeToken<ArrayList<MyContacts>>() {
        }.getType();
        contacts = gson.fromJson(json, type);
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesstr = context.getSharedPreferences("sharedPreferencesstr" + user_nick, Context.MODE_PRIVATE);
        Gson gsonstr = new Gson();
        String jsonstr = sharedPreferencesstr.getString("taskstr" + user_nick, null);
        Type typestr = new TypeToken<ArrayList<String>>() {
        }.getType();
        CONTACTS = gsonstr.fromJson(jsonstr, typestr);
        if (CONTACTS == null) {
            CONTACTS = new ArrayList<>();
        }

        SharedPreferences sharedPreferenceschatid = context.getSharedPreferences("sharedPreferenceschatid" + user_nick, Context.MODE_PRIVATE);
        Gson gsonchatid = new Gson();
        String jsonchatid = sharedPreferenceschatid.getString("taskchatid" + user_nick, null);
        Type typechatid = new TypeToken<ArrayList<String>>() {
        }.getType();
        CHAT_NUMBER = gsonchatid.fromJson(jsonchatid, typechatid);
        if (CHAT_NUMBER == null) {
            CHAT_NUMBER = new ArrayList<>();
        }

        //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
        //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();

        //Log.wtf(TAG, json);
        //Log.wtf(TAG, jsonstr);
        //Log.wtf(TAG, jsonchatid);
    }

    public static void saveDataContacts(Context context, String my_nick) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences" + my_nick, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        if (json != null) {
            json = json.substring(1, json.length() - 1);
        }
        editor.putString("task" + my_nick, json);
        editor.apply();

        SharedPreferences sharedPreferencesstr = context.getSharedPreferences("sharedPreferencesstr" + my_nick, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
        SharedPreferences.Editor editorstr = sharedPreferencesstr.edit();
        Gson gsonstr = new Gson();
        String jsonstr = gsonstr.toJson(CONTACTS);
        editorstr.putString("taskstr" + my_nick, jsonstr);
        editorstr.apply();

        SharedPreferences sharedPreferenceschatid = context.getSharedPreferences("sharedPreferenceschatid" + my_nick, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
        SharedPreferences.Editor editorchatid = sharedPreferenceschatid.edit();
        Gson gsonchatid = new Gson();
        String jsonchatid = gsonchatid.toJson(CHAT_NUMBER);
        editorchatid.putString("taskchatid" + my_nick, jsonchatid);
        editorchatid.apply();

        //Log.wtf(TAG, json);
        //Log.wtf(TAG, jsonstr);
        //Log.wtf(TAG, jsonchatid);
    }

    public static void loadBaseContacts(Context context, String user_nick, RecyclerView recycle_view,
                                        LinearLayout linlay_bar, LinearLayout linlay_list, LinearLayout no_users) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Number of children: " + dataSnapshot.getChildrenCount());

                if (dataSnapshot.getChildrenCount() == 0) {

                    if (contacts == null) {
                        contacts = new ArrayList<>();
                    }
                    if (CONTACTS == null) {
                        CONTACTS = new ArrayList<>();
                    }
                    if (CHAT_NUMBER == null) {
                        CHAT_NUMBER = new ArrayList<>();
                    }

                    contactsAdapter = new ContactsAdapter(context, contacts, CONTACTS, user_nick);

                    linlay_bar.setVisibility(View.GONE);
                    linlay_list.setVisibility(View.GONE);
                    no_users.setVisibility(View.VISIBLE);

                } else {
                    int count = 0;
                    boolean isAdmin = false;
                    boolean isMe = false;

                    linlay_bar.setVisibility(View.VISIBLE);
                    linlay_list.setVisibility(View.GONE);
                    no_users.setVisibility(View.GONE);

                    if (contacts == null) {
                        contacts = new ArrayList<>();
                    }
                    if (CONTACTS == null) {
                        CONTACTS = new ArrayList<>();
                    }
                    if (CHAT_NUMBER == null) {
                        CHAT_NUMBER = new ArrayList<>();
                    }

                    contacts.clear();
                    CONTACTS.clear();
                    CHAT_NUMBER.clear();

                    TreeMap map = new TreeMap<String, MyContacts>();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String value = (String) ds.getValue();

                        count += 1;
                        int finalCount = count;

                        if (value != null
                            //&& value.equals(Administration)
                        ) {
                            isAdmin = true;
                        }
                        if (value != null && value.equals(user_nick)) {
                            isMe = true;
                        }

                        Log.d(String.valueOf(count), value);

                        boolean finalIsAdmin = isAdmin;
                        boolean finalIsMe = isMe;
                        thisUser(value + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    String new_user_name = snapshot.getValue(String.class);

                                    CONTACTS.add(value);
                                    contacts.add(new MyContacts(new_user_name, ""));
                                    map.put(value, new MyContacts(new_user_name, ""));

                                    if (String.valueOf(finalCount).equals(String.valueOf(dataSnapshot.getChildrenCount()))) {

                                        Log.wtf("CONTACTS", String.valueOf(CONTACTS));

                                        Collections.sort(CONTACTS);

                                        Log.wtf("CONTACTS", String.valueOf(CONTACTS));

                                        //if (finalIsAdmin) {
                                        //    CONTACTS.remove(Administration);
                                        //    CONTACTS.add(0, Administration);
                                        //}
                                        if (finalIsMe) {
                                            CONTACTS.remove(user_nick);
                                            CONTACTS.add(0, user_nick);
                                        }
                                        Log.wtf("CONTACTS", String.valueOf(CONTACTS));

                                        for (int i = 0; i < map.size(); i++) {
                                            contacts.set(i, (MyContacts) map.get(CONTACTS.get(i)));
                                        }

                                        contactsAdapter = new ContactsAdapter(context, contacts, CONTACTS, user_nick);
                                        contactsAdapter.notifyDataSetChanged();
                                        recycle_view.setAdapter(contactsAdapter);

                                        linlay_bar.setVisibility(View.GONE);
                                        no_users.setVisibility(View.GONE);
                                        linlay_list.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        thisUser(user_nick + "/Contacts/").addListenerForSingleValueEvent(valueEventListener);
    }

    public static int SPtoPixels(Context context, double sp) {
        return (int) (sp * context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static void loadBaseMessages(Context context, String interlocutor_nick, String my_nick) {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long numberOfChildren = dataSnapshot.getChildrenCount();

                int maxMess = 100;
                ProgressDialog progressDialog = null;
                if (numberOfChildren > maxMess) {
                    progressDialog = new ProgressDialog(context, R.style.AppCompatProgressDialogStyle);
                    progressDialog.setTitle("Обновление...");
                    progressDialog.show();
                }

                Log.d(TAG, "Number of Messages: " + numberOfChildren);
                int count = 0;

                Map<String, MyMessage> map = new TreeMap<>();
                ArrayList<String> Times = new ArrayList<>();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String value = (String) ds.getValue();
                    count += 1;
                    Log.d(String.valueOf(count), value);

                    try {
                        assert value != null;
                        String time = value.split("\\|")[0];
                        String sender = value.split("\\|")[1];
                        String msg = value.split("\\|")[2];

                        String customTime = customizeDateOnlyTime(time);

                        MyMessage new_message = new MyMessage(sender, msg, customTime);

                        Times.add(time);
                        map.put(ds.getKey(), new_message);
                        //map.put(Long.parseLong(time), new_message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (count == numberOfChildren) {

                        messages = loadDataMessages(my_nick, context);

                        MyLog("messages11", String.valueOf(messages));
                        MyLog("map11", String.valueOf(map));

                        if (messages.isEmpty()) {
                            for (Map.Entry<String, MyMessage> entry : map.entrySet()) {
                                messages.add(entry.getValue());
                            }
                        } else {

                            MyLog("messages.size()", String.valueOf(messages.size()));

                            ArrayList<MyMessage> map_messages = new ArrayList<>();
                            for (Map.Entry<String, MyMessage> entry : map.entrySet()) {
                                map_messages.add(entry.getValue());
                            }

                            MyLog("map_messages.size()", String.valueOf(map_messages.size()));

                            int wtf_hold = map_messages.size();
                            for (int ii = 0; ii < wtf_hold; ii++) {

                                MyMessage msg = map_messages.get(ii);

                                boolean contains_in_messages = false;
                                for (int jj = 0; jj < messages.size(); jj++) {

                                    if (MyMessage.equalMessages(msg, messages.get(jj))) {
                                        contains_in_messages = true;
                                        break;
                                    }
                                }
                                if (!contains_in_messages) messages.add(msg);
                            }
                        }
                        Collections.sort(Times);

                        MyLog("messages", String.valueOf(messages));

                        saveDataMessages(my_nick, context);
                        //addDataSeparators(Times, messages);

                        MyLog("messages", String.valueOf(messages));

                        adapter_messages = new MessageAdapter(context, messages, my_nick);
                        recyclerView.setAdapter(adapter_messages);

                        new ItemTouchHelper.SimpleCallback(
                                0,
                                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

                            @Override
                            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                Toast.makeText(context, "on Swiped ", Toast.LENGTH_SHORT).show();
                                adapter_messages.notifyDataSetChanged();
                            }
                        };

                        if (numberOfChildren > maxMess) {
                            Handler handler = new Handler();
                            handler.postDelayed(progressDialog::dismiss, 1000);
                        }

                        //ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToActionCallback(adapter_messages));
                        //itemTouchHelper.attachToRecyclerView(recyclerView);

                        //ItemMoveCallBackImpl mMoveCallBack = new ItemMoveCallBackImpl(null);
                        //ItemTouchHelper touchHelper = new ItemTouchHelper(mMoveCallBack);
                        //touchHelper.attachToRecyclerView(recyclerView);
                    }
                }
            }

            private void addDataSeparators(ArrayList<String> Times, ArrayList<MyMessage> messages) {

                String time0 = Times.get(0);
                Locale loc = Locale.forLanguageTag("ru");

                int day0 = Integer.parseInt(MessageFormat.format("{0}{1}",
                        String.valueOf(time0.charAt(6)), String.valueOf(time0.charAt(7))));

                String mounth0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    Month month = Month.of(
                            Integer.parseInt(MessageFormat.format("{0}{1}",
                                    String.valueOf(time0.charAt(4)), String.valueOf(time0.charAt(5)))));

                    mounth0 = firstUpperCase(month.getDisplayName(TextStyle.FULL_STANDALONE, loc));

                } else {

                    mounth0 = new DateFormatSymbols().getMonths()[
                            Integer.parseInt(MessageFormat.format("{0}{1}",
                                    String.valueOf(time0.charAt(4)), String.valueOf(time0.charAt(5)))) - 1];
                }

                String year0 = MessageFormat.format("{0}{1}{2}{3}",
                        String.valueOf(time0.charAt(0)), String.valueOf(time0.charAt(1)),
                        String.valueOf(time0.charAt(2)), String.valueOf(time0.charAt(3)));

                messages.add(0, new MyMessage("system", year0 + " " + mounth0 + " " + day0, null));

                int considerMessagesSizeChange = 2;

                for (int i = 0; i < Times.size() - 1; i++) {
                    String time1 = Times.get(i);
                    String time2 = Times.get(i + 1);

                    int day1 = Integer.parseInt(MessageFormat.format("{0}{1}",
                            String.valueOf(time1.charAt(6)), String.valueOf(time1.charAt(7))));
                    int day2 = Integer.parseInt(MessageFormat.format("{0}{1}",
                            String.valueOf(time2.charAt(6)), String.valueOf(time2.charAt(7))));

                    int mounth1 = Integer.parseInt(MessageFormat.format("{0}{1}",
                            String.valueOf(time1.charAt(4)), String.valueOf(time1.charAt(5))));
                    int mounth2 = Integer.parseInt(MessageFormat.format("{0}{1}",
                            String.valueOf(time2.charAt(4)), String.valueOf(time2.charAt(5))));

                    int year1 = Integer.parseInt(MessageFormat.format("{0}{1}{2}{3}",
                            String.valueOf(time1.charAt(0)), String.valueOf(time1.charAt(1)),
                            String.valueOf(time1.charAt(2)), String.valueOf(time1.charAt(3))));
                    int year2 = Integer.parseInt(MessageFormat.format("{0}{1}{2}{3}",
                            String.valueOf(time2.charAt(0)), String.valueOf(time2.charAt(1)),
                            String.valueOf(time2.charAt(2)), String.valueOf(time2.charAt(3))));

                    if (year1 < year2) {
                        //MyLog("dataSep", time1 + time2);

                        String mounth_2;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                            Month month2 = Month.of(mounth2);
                            mounth_2 = firstUpperCase(month2.getDisplayName(TextStyle.FULL_STANDALONE, loc));

                        } else {
                            mounth_2 = new DateFormatSymbols().getMonths()[mounth2 - 1];
                        }

                        messages.add(i + considerMessagesSizeChange, new MyMessage("system", year2 + " " + mounth_2 + " " + day2, null));
                        considerMessagesSizeChange += 1;
                    } else {

                        if (mounth1 < mounth2) {

                            String mounth_2;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                                Month month2 = Month.of(mounth2);
                                mounth_2 = firstUpperCase(month2.getDisplayName(TextStyle.FULL_STANDALONE, loc));

                            } else {
                                mounth_2 = new DateFormatSymbols().getMonths()[mounth2 - 1];
                            }

                            //MyLog("dataSep", time1 + time2);
                            messages.add(i + considerMessagesSizeChange, new MyMessage("system", mounth_2 + " " + day2, null));
                            considerMessagesSizeChange += 1;
                        } else {

                            if (day1 < day2) {

                                String mounth_2;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                                    Month month2 = Month.of(mounth2);
                                    mounth_2 = firstUpperCase(month2.getDisplayName(TextStyle.FULL_STANDALONE, loc));

                                } else {
                                    mounth_2 = new DateFormatSymbols().getMonths()[mounth2 - 1];
                                }

                                //MyLog("dataSep", time1 + time2);
                                messages.add(i + considerMessagesSizeChange, new MyMessage("system", mounth_2 + " " + day2, null));
                                considerMessagesSizeChange += 1;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        CHAT(my_nick, interlocutor_nick).child("messages").addListenerForSingleValueEvent(valueEventListener);
        //MyLog(CHAT(my_nick, interlocutor_nick).child("messages").toString());
    }

    public static ArrayList<MyMessage> loadDataMessages(String my_nick, Context context) {

        SharedPreferences sharedPreferencesArrayAll = context.getSharedPreferences(my_nick + "sharedPreferencesArrayAll", Context.MODE_PRIVATE);
        Gson gsonArrayAll = new Gson();
        String jsonArrayAll = sharedPreferencesArrayAll.getString(my_nick + "taskArrayAll", null);
        Type typeArrayAll = new com.google.gson.reflect.TypeToken<ArrayList<MyMessage>>() {
        }.getType();
        messages = gsonArrayAll.fromJson(jsonArrayAll, typeArrayAll);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public static void saveDataMessages(String my_nick, Context context) {

        SharedPreferences sharedPreferencesArrayAll = context.getSharedPreferences(my_nick + "sharedPreferencesArrayAll", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorArrayAll = sharedPreferencesArrayAll.edit();
        Gson gsonArrayAll = new Gson();
        String jsonArrayAll = gsonArrayAll.toJson(messages);
        editorArrayAll.putString(my_nick + "taskArrayAll", jsonArrayAll);
        editorArrayAll.apply();
    }

    static class MapKeyComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

    public static void MySmoothScroll(Context context, RecyclerView recyclerView) {

        recyclerView.setLayoutManager(new SmoothScroller(context));
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    public static String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
