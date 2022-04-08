package com.artrom.flychat.notification;

/*public class MyFirebaseIdService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        Token token1 = new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(readFile(MyFirebaseIdService.this, file_nick)).setValue(token1);
    }
}*/
/*
public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        updateToken(refreshToken);
    }

    private void updateToken(String refreshToken) {
        database.getReference("Tokens").child(readFile(MyFirebaseIdService.this, file_nick)).setValue(refreshToken);
    }
}*/
