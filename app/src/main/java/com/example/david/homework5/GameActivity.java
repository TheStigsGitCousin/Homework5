package com.example.david.homework5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity implements IActivity {

    private TextView statusTextView;
    private EditText inputEditText;
    private TextView gameTextView;

    private boolean isConnected = false;

    private static SocketService handler;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        statusTextView = (TextView) findViewById(R.id.statusTextView);
        gameTextView = (TextView) findViewById(R.id.gameTextView);
        inputEditText = (EditText) findViewById(R.id.letterEditText);
        Intent intent = getIntent();
        String host = intent.getStringExtra("host");
        int port = intent.getIntExtra("port", 8080);
        handler = new SocketService(port, host);
        serviceIntent = new Intent(this, SocketService.class);

        BroadcastReceiver rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra(SocketService.EXTENDED_DATA_TYPE);
                String status = intent.getStringExtra(SocketService.EXTENDED_DATA_STATUS);
                if (type.equals("message")) {
                    messageReceived(status);
                } else if (type.equals("status")) {
                    statusChanged(status);
                } else if (type.equals("connected")) {
                    connected();
                } else if (type.equals("error")) {
                    error(status);
                }
            }
        };

        IntentFilter mStatusIntentFilter = new IntentFilter(
                SocketService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, mStatusIntentFilter);

        newGame(null);
        startServerHandler();
    }

    public void sendLetter(View view) {
        if (!isConnected)
            return;

        handler.addCommand("guess|" + inputEditText.getText());
        inputEditText.setText("");
    }

    public void newGame(View view) {
        handler.addCommand("startgame| ");
    }

    private void startServerHandler() {
        if (serviceIntent != null)
            startService(serviceIntent);
    }

    @Override
    public void connected() {
        isConnected = true;
        inputEditText.setEnabled(true);
    }

    @Override
    public void error(String errorMessage) {
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra("message", errorMessage);
        startActivity(intent);
    }

    @Override
    public void messageReceived(String message) {
        gameTextView.setText(message);
    }

    @Override
    public void statusChanged(String status) {
        statusTextView.setText(status);
    }
}
