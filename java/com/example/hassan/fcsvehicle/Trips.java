package com.example.hassan.fcsvehicle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Trips extends AppCompatActivity{
    int eventType;
    String trips,tidS,originS,destinationS,vehicleID,driverID,responseXML;
    String url = "http://192.168.43.84:8090/TripsRestWS/webresources/trips";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        final ListView listView= (ListView) findViewById(R.id.listview2);
        final List<String> vid = new ArrayList<>();
        final List<String> tid = new ArrayList<>();
        final List<String> tidV = new ArrayList<>();
        final List<String> origin = new ArrayList<>();
        final List<String> originV = new ArrayList<>();
        final List<String> destination = new ArrayList<>();
        final List<String> destinationV = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(Trips.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseXML=response;
                        try {
                            InputStream stream = new ByteArrayInputStream(responseXML.getBytes());
                            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                            XmlPullParser parser = xmlPullParserFactory.newPullParser();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                            parser.setInput(reader);
                            eventType = parser.getEventType();
                            while(eventType!=XmlPullParser.END_DOCUMENT){
                                String tag_name = parser.getName();
                                switch (eventType) {
                                    case XmlPullParser.START_DOCUMENT:
                                        break;
                                    case XmlPullParser.START_TAG:
                                        break;
                                    case XmlPullParser.TEXT:
                                        trips=parser.getText();
                                        break;
                                    case XmlPullParser.END_TAG:
                                        if(tag_name.equalsIgnoreCase("trips")){

                                        }
                                        else if(tag_name.equalsIgnoreCase("DId")){

                                        }
                                        else if(tag_name.equalsIgnoreCase("destination")){
                                            destination.add(trips);}
                                        else if(tag_name.equalsIgnoreCase("origin"))
                                            origin.add(trips);
                                        else if (tag_name.equalsIgnoreCase("TId"))
                                            tid.add(trips);
                                        else if(tag_name.equalsIgnoreCase("VId"))
                                            vid.add(trips);
                                        break;
                                    default:
                                        break;
                                }
                                eventType = parser.next();
                            }
                        } catch (Exception e){}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Trips.this,
                        "Error "+error,
                        Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
        Button startTrip = (Button) findViewById(R.id.startTrip);
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                vehicleID=intent.getExtras().getString("vid");
                driverID=intent.getExtras().getString("did");
                for(int i=0;i<vid.size();i++)
                {
                    if(vehicleID.equals(""+vid.get(i)+"")) {
                        tidV.add(tid.get(i));
                        originV.add(origin.get(i));
                        destinationV.add(destination.get(i));
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Trips.this, android.R.layout.simple_list_item_1, destinationV);
                listView.setAdapter(adapter);
        }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                int i = listView.getSelectedItemPosition();
                tidS=tidV.get(i+1);
                originS=originV.get(i+1);
                destinationS=destinationV.get(i+1);
                Intent intent = new Intent(Trips.this, Obd.class);
                intent.putExtra("origin",originS);
                intent.putExtra("destination",destinationS);
                intent.putExtra("tripID",tidS);
                intent.putExtra("driverID",driverID);
                intent.putExtra("vehicleID",vehicleID);
                startActivity(intent);
            }
        });

    }

}

