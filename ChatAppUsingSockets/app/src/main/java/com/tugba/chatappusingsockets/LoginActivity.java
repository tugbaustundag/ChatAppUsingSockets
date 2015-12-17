package com.tugba.chatappusingsockets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class LoginActivity extends Activity {

    private EditText mUsernameView;

    private String mUsername;
    //Socket sınıfını kullanarak, sunucudaki chat yazılımı ile bağlantı kuruyoruz
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Kullanıcı chat sistemine giriş yapabilmesi için kullanıcı adını alıyoruz
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        //Socket sınıfına,kullanıcı login işlemini yapan metodu set ettim
        mSocket.on("login", onLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("login", onLogin);
    }

    /**
     *Kullanıcı chat sistemine giriş yapabilmesi için, edittextden alınan değer kontrol edilten sonra
     * kullanıcıyı socket sınıfına ekleyen metod
     */
    private void attemptLogin() {
        mUsernameView.setError(null);
        String username = mUsernameView.getText().toString().trim();

        // Kulanıcı adı kontrol ediliyor
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }
        mUsername = username;

        //Kullanıcı adı, socket'e ekleniyor
        mSocket.emit("add user", username);
    }

    /**
     * Socket sınıfını dinleyerek, login işlemni yapan metod
     */
    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            //numUsers; chat sisteminde kaç kişinin login olduğunu döndüren sayıdır.Yani chat sistemini kullanan
            //katılımcıları belirten sayıdır
            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}



