package com.artrom.flychat.system;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.artrom.flychat.R;

public class TextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle((Html.fromHtml("<font color=\"#ffffff\">О приложении</font>")));

        String s =
                "<i>Основная информация:</i><br><br>" +

                "Создатель и владелец проекта: <br><b>Artem Romanovich</b><br><br>" +
                "Почта создателя: <br><b>artrom170@gmail.com</b><br><br>" +
                "Лицензия: <br><b>Apache License, Version 2.0</b><br><br>" +
                "Версия: <br><b>1.7.3</b><br><br>" +
                "Сайт: <br><b>artem-romanovich.github.io/flychat_share/</b>" +

                "<br><br><br><i>Здесь приведены ответы на самые распространенные вопросы:</i><br><br>" +

                "<b>В: Как корректно пройти регистрацию?<br></b>" +
                "О: Необходимо нажать на красную кнопку \"создать аккаунт\" в разделе \"аккаунт\". Заполните все поля и нажмите \"получить ссылку\". На указанный почтовый адрес придет ссылка для авторизации. После нужно вернуться обратно в приложение и \"подтвердить ссылку\". Так аккаунт будет создан и подтвержден.<br><br>" +

                "<b>В: Не находятся Bluetooth-устройства поблизости.<br></b>" +
                "О: Перед тем, как первый раз начинать искать искать устройства, обязательно вручную зайдите в настройки приложения и обновите тип доступа, как это показано в разделе \"Видео\" на сайте (Bluetooth общение).<br>" +
                "К сожалению, это глобальная проблема версий Android >10. Также, устройства могут находяться слишком далеко друг от друга. Радиус действия зависит от версии Bluetooth (уточните для своего устройства).<br>" +
                "Пропускная способность значительно снижается в помещении.<br><br>" +

                "<b>В: Как скрыть службу из панели уведомлений?<br></b>" +
                "О: Данное уведомление является обязательным информированием пользователя о службе поиска новых сообщений. Его можно отключить в настройках приложения (Уведомления/FlyChat goto/отключить).<br><br><br><br>";

        TextView textView = findViewById(R.id.textView);
        textView.setTextSize(17);
        textView.setText(Html.fromHtml(s));
    }
}