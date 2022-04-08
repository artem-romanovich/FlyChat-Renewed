package com.artrom.flychat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.artrom.flychat.internet.CreateChatActivity;
import com.artrom.flychat.internet.RequestsActivity;
import com.artrom.flychat.pre.FirstActivity;
import com.artrom.flychat.system.MyContacts;
import com.artrom.flychat.system.TextActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.artrom.flychat.MySystem.CHAT_NUMBER;
import static com.artrom.flychat.MySystem.CONTACTS;
import static com.artrom.flychat.MySystem.INTERNET_MESSAGES;
import static com.artrom.flychat.MySystem.LINK;
import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.REQUEST_ACCESS_BACKGROUND_LOCATION;
import static com.artrom.flychat.MySystem.clearStringsStr;
import static com.artrom.flychat.MySystem.contacts;
import static com.artrom.flychat.MySystem.contactsAdapter;
import static com.artrom.flychat.MySystem.createAnimBundle;
import static com.artrom.flychat.MySystem.deleteDir;
import static com.artrom.flychat.MySystem.file_name;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.loadBaseContacts;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.readFile;
import static com.artrom.flychat.MySystem.thisUser;
import static com.artrom.flychat.MySystem.toolbar;
import static com.artrom.flychat.internet.InternetFragment.addContactToBase;

public class WrapActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    NavigationView mNavigationView;
    DrawerLayout drawer_layout;

    String[] AllUsersList;
    public int flag;
    public String chatId;

    ImageButton btn_find_user;
    EditText find_by_nick;
    RelativeLayout main_layout_int_fragment;
    RecyclerView recycle_view;
    ListView listView;
    LinearLayout linlay_list, linlay_bar, linlay_btn;

    SwipeRefreshLayout swipeRefreshLayout;

    Button buttonPositiveInvolvement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrap);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView toolbarText = findViewById(R.id.toolbarText);
        toolbarText.setText("Обновление...");
        setSupportActionBar(toolbar);

        findViews();

        setInternetChecker();

        prepareLeftMenu();

        prepareBottomNavigation();

        setListeners();

        createAllUsersList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_ACCESS_BACKGROUND_LOCATION);

            switch (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:

                    if (ContextCompat.checkSelfPermission(this.getBaseContext(),
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_ACCESS_BACKGROUND_LOCATION);
                    }
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }

            boolean hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasBackgroundLocationPermission) {
                ActivityCompat.requestPermissions(this, new String[]{
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_ACCESS_BACKGROUND_LOCATION);

                //Toast.makeText(getApplicationContext(), "Необходим доступ к местоположению в любом режиме. Разрешите доступ вручную в настройках", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(WrapActivity.this, FirstActivity.class);
                intent.putExtra("BACKGROUND_LOCATION", "show");
                startActivity(intent);
                finish();
            }
        }
    }

    private void findViews() {
        mNavigationView = findViewById(R.id.nav_view_wrap);
        drawer_layout = findViewById(R.id.drawer_layout);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        main_layout_int_fragment = findViewById(R.id.main_layout_int_fragment);
        linlay_bar = findViewById(R.id.linlay_bar);
        linlay_btn = findViewById(R.id.linlay_btn);
        linlay_list = findViewById(R.id.linlay_list);
        //btn_add_user = findViewById(R.id.btn_add_user);
        //btn_requests = findViewById(R.id.btn_requests);
    }

    @SuppressLint({"NonConstantResourceId", "RtlHardcoded"})
    private void setListeners() {
        mNavigationView.setNavigationItemSelectedListener(item -> {

            mNavigationView.setItemTextAppearance(R.style.TextItemsStyle);
            drawer_layout.closeDrawer(Gravity.LEFT);

            switch (item.getItemId()) {

                case R.id.nav_quit:
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        try {
                            File dir = getApplicationContext().getCacheDir();
                            if (dir != null && dir.isDirectory()) {

                                int dirSize = Integer.parseInt(String.valueOf(dir.length() / 1024));
                                if (dirSize >= 100) {
                                    MyToast(getApplicationContext(),
                                            "Очищено " + dirSize + " Кбайт",
                                            Toast.LENGTH_SHORT);
                                    deleteDir(dir);
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_HOME);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }, 500);
                    //handler.postDelayed(this::finish, 500);
                    break;

                case R.id.nav_share:
                    ApplicationInfo app = getApplicationInfo();
                    String filePath = app.sourceDir;

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.setPackage("com.android.bluetooth");

                    Uri appUri = FileProvider.getUriForFile(
                            getApplicationContext(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            new File(filePath));

                    //Uri appUri = FileProvider.getUriForFile(
                    //        getApplicationContext(),
                    //        "com.example.flychat.provider",
                    //        new File(filePath));

                    intent.putExtra(Intent.EXTRA_STREAM, appUri);
                    startActivity(Intent.createChooser(intent, "Share app"));

                    break;

                case R.id.nav_about:
                    //MyToast(WrapActivity.this, "Скоро", Toast.LENGTH_SHORT);

                    Intent intent1 = new Intent(WrapActivity.this, TextActivity.class);
                    Handler handler1 = new Handler();
                    handler1.postDelayed(() -> {
                        startActivity(intent1, createAnimBundle(WrapActivity.this));
                    }, 250);
                    break;

                case R.id.nav_site:
                    goToSite();
                    break;
            }
            return true;
        });
    }

    private void goToSite() {
        if (hasConnection(getApplicationContext())) {
            MyToast(WrapActivity.this, "Подождите...", Toast.LENGTH_SHORT);

            LINK.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataSnapshot.getValue(String.class)));
                        startActivity(browserIntent);
                    } else {
                        MyToast(WrapActivity.this, "Сайт изменил свой адрес", Toast.LENGTH_SHORT);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        } else {
            MyToast(WrapActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG);
        }
    }

    private void prepareBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_internet,
                R.id.navigation_messages,
                R.id.navigation_bluetooth,
                R.id.navigation_account)
                .build();

        NavController navController_bottom_nav = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController_bottom_nav, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController_bottom_nav);
    }

    private void prepareLeftMenu() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view_wrap);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_settings,
                R.id.nav_site,
                R.id.nav_share,
                R.id.nav_gratitude,
                R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wrap_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_icon) {
            if (!drawer_layout.isDrawerOpen(Gravity.LEFT)) {
                drawer_layout.openDrawer(Gravity.LEFT);
            }
            return true;
        }

        if (id == R.id.block_user) {
            if (hasConnection(getApplicationContext())) {
                try {
                    Intent intent = new Intent(getApplicationContext(), RequestsActivity.class);
                    startActivity(intent, createAnimBundle(getApplicationContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                MyToast(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG);
            return true;
        }

        if (id == R.id.add_user) {
            if (hasConnection(getApplicationContext())) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WrapActivity.this, R.style.RadioDialogTheme);

                builder.setTitle("Выберите действие");
                final String[] listItems = new String[]{"Добавить контакт", "Создать беседу"};

                final int[] action = {-1};

                builder.setSingleChoiceItems(listItems, -1, (dialog, which) -> {

                    action[0] = which;
                    buttonPositiveInvolvement.setTextColor(Color.parseColor("#FF6547FF"));
                    buttonPositiveInvolvement.setEnabled(true);
                });
                builder.setPositiveButton("Подтвердить", (dialog1, which1) -> {
                    if (action[0] == 0) {
                        findContact();
                    }
                    if (action[0] == 1) {
                        Intent intent = new Intent(WrapActivity.this, CreateChatActivity.class);
                        startActivity(intent, createAnimBundle(WrapActivity.this));
                    }
                });
                builder.setNegativeButton("Отмена", (dialog, which) -> {
                });

                AlertDialog customAlertDialog = builder.create();
                customAlertDialog.show();

                buttonPositiveInvolvement = customAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositiveInvolvement.setTextColor(Color.parseColor("#808080"));
                buttonPositiveInvolvement.setEnabled(false);

                Button buttonNegativeInvolvement = customAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegativeInvolvement.setTextColor(Color.parseColor("#FF6547FF"));
            } else
                MyToast(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void findContact() {
        try {
            if (!hasConnection(getApplicationContext())) {
                MyToast(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG);
            } else {

                String user_nick = readFile(WrapActivity.this, file_nick);
                String user_name = readFile(WrapActivity.this, file_name + user_nick);

                View rowList = getLayoutInflater().inflate(R.layout.users_row, null);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(WrapActivity.this, R.style.MyDialogTheme);

                listView = rowList.findViewById(R.id.listView);
                btn_find_user = rowList.findViewById(R.id.new_btn_find_user);
                find_by_nick = rowList.findViewById(R.id.new_find_by_nick);

                ArrayAdapter adapter = new ArrayAdapter<>(WrapActivity.this, android.R.layout.simple_list_item_1, AllUsersList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if (rowList.getParent() != null)
                    ((ViewGroup) rowList.getParent()).removeView(rowList);
                alertDialog.setView(rowList);

                AlertDialog dialog = alertDialog.create();

                Display display = WrapActivity.this.getWindowManager().getDefaultDisplay();
                int width = display.getWidth();

                dialog.show();
                dialog.getWindow().setLayout(width, width + 50);
                //dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                btn_find_user.setClickable(true);

                AdapterView.OnItemClickListener onItemClickListener = (parent, view, position, id) -> {
                    String user = AllUsersList[position];
                    find_by_nick.setText(user.substring(user.indexOf("(") + 1, user.length() - 1));
                };
                listView.setOnItemClickListener(onItemClickListener);

                btn_find_user.setOnClickListener(view -> {

                    String find_nick = find_by_nick.getText().toString();
                    find_nick = clearStringsStr(find_nick);

                    MyLog(find_nick);

                    if (notBlankSpaceEnter(find_nick)) {

                        if (user_nick.equals(find_nick)) {
                            if (!CONTACTS.contains(find_nick)) {

                                String finalFind_nick = find_nick;

                                AlertDialog.Builder alert = new AlertDialog.Builder(WrapActivity.this);
                                alert.setTitle("Здесь можно хранить приватную информацию");
                                alert.setMessage("Все сообщения, отправленные самому себе, хранятся только у вас на телефоне.");
                                alert.setPositiveButton("Понятно", (dialog1, which) -> {

                                    contacts.add(new MyContacts(user_name, ""));
                                    CONTACTS.add(finalFind_nick);

                                    find_by_nick.setText("");
                                    flag = 1;

                                    addContactToBase(user_nick, finalFind_nick);
                                    loadBaseContacts(WrapActivity.this, finalFind_nick, recycle_view, linlay_bar, linlay_list, null);

                                    CHAT_NUMBER.add("Chat_" + finalFind_nick + "_" + finalFind_nick);

                                    contactsAdapter.notifyDataSetChanged();
                                    recycle_view.setAdapter(contactsAdapter);

                                });
                                alert.show();

                            } else {
                                MyToast(WrapActivity.this, "Вами уже была создана переписка с самим собой", Toast.LENGTH_SHORT);
                            }
                            btn_find_user.setClickable(true);

                        } else {

                            if (!CONTACTS.contains(find_nick)) {

                                String finalFind_nick1 = find_nick;
                                thisUser(find_nick + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot data_Snapshot) {

                                        String get_name_from_base = data_Snapshot.getValue(String.class);

                                        if (data_Snapshot.exists() && get_name_from_base != null) {

                                            contacts.add(new MyContacts(get_name_from_base, ""));
                                            CONTACTS.add(finalFind_nick1);
                                            contactsAdapter.notifyDataSetChanged();
                                            flag = 1;
                                            find_by_nick.setText("");

                                            chatId = "Chat_" + user_nick + "_" + finalFind_nick1;
                                            DatabaseReference ChatSearchRef = INTERNET_MESSAGES.child(chatId);
                                            ValueEventListener eventListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (!dataSnapshot.exists()) {
                                                        chatId = "Chat_" + user_nick + "_" + finalFind_nick1;
                                                    } else {
                                                        chatId = "Chat_" + finalFind_nick1 + "_" + user_nick;
                                                    }
                                                    CHAT_NUMBER.add(chatId);
                                                    //saveDataContacts(requireContext(), user_nick);

                                                }


                                                @Override
                                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                                }
                                            };
                                            ChatSearchRef.addListenerForSingleValueEvent(eventListener);
                                        }

                                        dialog.hide();
                                        contactsAdapter.notifyDataSetChanged();

                                        if (flag == 0) {
                                            MyToast(WrapActivity.this, "Пользователь с ником \"" + finalFind_nick1 + "\" не найден", Toast.LENGTH_SHORT);
                                        } else {
                                            MyToast(WrapActivity.this, "Пользователь " + get_name_from_base + " успешно найден!", Toast.LENGTH_SHORT);

                                            addContactToBase(user_nick, finalFind_nick1);
                                        }
                                        flag = 0;
                                        btn_find_user.setClickable(true);
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                        btn_find_user.setClickable(true);
                                    }
                                });
                            } else {
                                MyToast(WrapActivity.this, "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT);
                                btn_find_user.setClickable(true);
                            }
                        }
                    } else {
                    }
                });

                find_by_nick.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // текст только что изменили
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // текст будет изменен
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        String written = find_by_nick.getText().toString();
                        ArrayList<String> Fittable = new ArrayList<>();

                        for (String value : AllUsersList) {
                            if (value.contains(written)) {
                                Fittable.add(value);
                            }
                        }

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            String user = Fittable.get(position);
                            find_by_nick.setText(user.substring(user.indexOf("(") + 1, user.length() - 1));
                        });

                        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, Fittable.toArray(new String[0]));
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAllUsersList() {
        DatabaseReference AllUsers = FirebaseDatabase.getInstance().getReference("Users/UsersList");
        AllUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
                if (users != null) {

                    ArrayList<String> Users = new ArrayList<>();

                    try {
                        for (Map.Entry<String, Object> entry : users.entrySet()) {

                            Map singleUser = (Map) entry.getValue();

                            String user;
                            if (singleUser.get("nick") != null) {
                                user = singleUser.get("name") + " (" + singleUser.get("nick") + ")";
                            } else {
                                user = (String) singleUser.get("name");
                            }

                            Users.add(user);
                        }
                        Collections.sort(Users);

                        AllUsersList = Users.toArray(new String[0]);

                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setInternetChecker() {

        final TextView[] toolbarText = {findViewById(R.id.toolbarText)};

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (!hasConnection(WrapActivity.this)) {
                        WrapActivity.this.runOnUiThread(() -> {
                            //toolbar.setTitle("Нет сети");
                            toolbarText[0].setText("Нет сети");
                            main_layout_int_fragment.setBackgroundResource(R.color.red);
                        });
                    } else {
                        showOnlineUsers(toolbarText);
                        main_layout_int_fragment.setBackgroundResource(R.color.main_dark_purple);
                    }
                } catch (Exception ignored) {
                }
            }
        });
        thread.start();
    }

    private void showOnlineUsers(TextView[] toolbarText) {
        DatabaseReference AllUsers = FirebaseDatabase.getInstance().getReference("Users/UsersList");
        AllUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
                if (users != null) {
                    //toolbar.setTitle(users.size() + " online");

                    //requireActivity().runOnUiThread(() -> toolbar.setTitle(users.size() + " online"));

                    toolbarText[0].setText(users.size() + " online");

                    toolbarText[0].setOnClickListener(view -> {

                        ArrayList<String> Users = new ArrayList<>();

                        try {

                            for (Map.Entry<String, Object> entry : users.entrySet()) {

                                Map singleUser = (Map) entry.getValue();

                                String user;
                                if (singleUser.get("nick") != null) {
                                    user = singleUser.get("name") + " (" + singleUser.get("nick") + ")";
                                } else {
                                    user = (String) singleUser.get("name");
                                }

                                Users.add(user);
                            }
                            Collections.sort(Users);

                            StringBuilder userstr = new StringBuilder();
                            for (int i = 0; i < Users.size(); i++) {
                                userstr.append(Users.get(i)).append('\n');
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(WrapActivity.this);
                            builder.setTitle("Список всех пользователей")
                                    .setMessage(userstr.toString())
                                    .setNegativeButton("Пригласить", (dialog, which) -> {

                                        LINK.addValueEventListener(new ValueEventListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onDataChange(@NotNull DataSnapshot dataSnapshot1) {
                                                if (dataSnapshot1.exists()) {
                                                    ClipboardManager clipboard = (ClipboardManager) WrapActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("", dataSnapshot1.getValue(String.class));
                                                    clipboard.setPrimaryClip(clip);
                                                    MyToast(WrapActivity.this, "Ссылка скопирована", Toast.LENGTH_SHORT);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                            }
                                        });
                                    })
                                    .setPositiveButton("ОК", (dialog, id) -> dialog.cancel());

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } catch (Exception ignored) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }
}