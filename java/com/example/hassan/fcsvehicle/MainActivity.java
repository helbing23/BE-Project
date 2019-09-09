package com.example.hassan.fcsvehicle;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    String v_id, d_id;
    EditText vid, did;
    Button submit;
    BluetoothAdapter btAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submit = (Button) findViewById(R.id.submit);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        vid = (EditText) findViewById(R.id.v_id);
        did = (EditText) findViewById(R.id.d_id);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v_id=vid.getText().toString();
                d_id=did.getText().toString();
                if(v_id.matches("") || d_id.matches(""))
                    Toast.makeText(MainActivity.this,
                            "Fill in the details.",
                            Toast.LENGTH_LONG).show();
                else {
                    Intent intent = new Intent(MainActivity.this, Trips.class);
                    intent.putExtra("vid",v_id);
                    intent.putExtra("did",d_id);
                    startActivity(intent);
                }
            }
        });
    }
}