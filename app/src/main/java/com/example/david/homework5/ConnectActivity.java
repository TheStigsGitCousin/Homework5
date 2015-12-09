package com.example.david.homework5;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ConnectActivity extends AppCompatActivity {

    EditText hostEditText;
    EditText portEditText;
    TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        hostEditText=(EditText)findViewById(R.id.hostEditText);
        portEditText=(EditText)findViewById(R.id.portEditText);
        statusTextView=(TextView)findViewById(R.id.connectStatusText);
    }

    public void connectGame(View view){
        String host=hostEditText.getText().toString();
        String portString=portEditText.getText().toString();
        if(host.equals("")||portString.equals("")) {
            statusTextView.setText("Port or host are not set.");
            return;
        }
        int port=8080;
        try{
            port = Integer.parseInt(portString);
        }catch(Exception e){
            statusTextView.setText("Port must be an integer.");
            return; }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("host", host);
        intent.putExtra("port", port);
        startActivity(intent);
    }
}
