package com.example.hassan.fcsvehicle;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.protocol.*;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Obd extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    int penalties=0;
    Context mContext;
    GPSTracker gps;
    BluetoothAdapter btAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket sock=null;
    Handler handler = new Handler();
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    List<String> s = new ArrayList<>();
    List<String> a = new ArrayList<>();
    String deviceAddress,duration="00:00:00",duration1,tripID,driverID,vehicleID,origin1,destination1,dist,drScore,avgF,penalty;
    Double time=0.0,timeC;
    String hh,mm,ss;
    Runnable runnableCode;
    RPMCommand engineRpmCommand = new RPMCommand();
    SpeedCommand speedCommand = new SpeedCommand();
    RuntimeCommand runtimeCommand = new RuntimeCommand();
    MassAirFlowCommand massAirFlowCommand = new MassAirFlowCommand();
    AirFuelRatioCommand airFuelRatioCommand = new AirFuelRatioCommand();
    double lp100=0,maf,mpg,gph,lph,n=1,prev=0,avg=0,afr,dscore=0,latitude=0.0,longitude=0.0,distance;
    float speedV;
    StringRequest request,distanceMatrix;
    int i=0,flag=0;
    private static final long INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 5000;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd);
        final TextView txt2 = (TextView) findViewById(R.id.speed);
        final TextView txt1 = (TextView) findViewById(R.id.rpm);
        final TextView txt3 = (TextView) findViewById(R.id.fuel);
        final TextView txt4 = (TextView) findViewById(R.id.runtime);
        final TextView status = (TextView) findViewById(R.id.obdStatus);
        final TextView destination = (TextView) findViewById(R.id.destination);
        final TextView kms = (TextView) findViewById(R.id.kms);
        final TextView eta = (TextView) findViewById(R.id.eta);
        mContext = this;
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Obd.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps = new GPSTracker(mContext, Obd.this);
            if (gps.canGetLocation()){
                if (!isGooglePlayServicesAvailable()) {
                    finish();
                }
                createLocationRequest();
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                flag=1;
            }else{
                gps.showSettingsAlert();
            }
        }
        Intent intent = getIntent();
        origin1 = intent.getExtras().getString("origin");
        destination1 = intent.getExtras().getString("destination");
        destination.setText(destination1);
        driverID = intent.getExtras().getString("driverID");
        vehicleID = intent.getExtras().getString("vehicleID");
        tripID = intent.getExtras().getString("tripID");
        final String url = "http://192.168.43.84:8090/LiveDataRestWS/webresources/livedata/" + vehicleID + "";
        final String distanceURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + origin1 + "&destinations=" + destination1 + "&key=AIzaSyBo6yuQkNH0TggdYt7JTXVEDZ_y8q1Pv68";
        final RequestQueue queue = Volley.newRequestQueue(Obd.this);
        distanceMatrix = new StringRequest(Request.Method.GET, distanceURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonRespRouteDistance = new JSONObject(response)
                                    .getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray ("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("distance");
                            dist = jsonRespRouteDistance.get("text").toString();
                            distance = (jsonRespRouteDistance.getDouble("value"))/1000;
                            jsonRespRouteDistance = new JSONObject(response)
                                    .getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray ("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("duration");
                            duration1 = jsonRespRouteDistance.get("text").toString();
                            time= jsonRespRouteDistance.getDouble("value");
                        }catch (JSONException e){}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Obd.this,
                        "Error "+error,
                        Toast.LENGTH_LONG).show();
            }
        });
        queue.add(distanceMatrix);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    a.add(bt.getAddress());
                    s.add(bt.getName());
                }
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(Obd.this, android.R.layout.simple_list_item_1, s);
        final ListView listView = (ListView) findViewById(R.id.listview1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    int i = listView.getSelectedItemPosition();
                    deviceAddress = a.get(i + 1);
                    bluetoothDevice = btAdapter.getRemoteDevice(deviceAddress);
                    btAdapter.cancelDiscovery();
                    Toast.makeText(Obd.this,
                            "Connecting",
                            Toast.LENGTH_SHORT).show();
                    try {
                        sock = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        sock.connect();
                        Toast.makeText(Obd.this,
                                "Successfull",
                                Toast.LENGTH_SHORT).show();
                        new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                        new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                        new TimeoutCommand(62).run(sock.getInputStream(), sock.getOutputStream());
                        new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());
                        Toast.makeText(Obd.this,
                                "OBD Connected", Toast.LENGTH_LONG).show();
                    } catch (Exception e1) {
                        Toast.makeText(Obd.this,
                                "Connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                    kms.setText(""+dist+"");
                    eta.setText(duration1);
                    if (sock.isConnected()) {
                        runnableCode = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    engineRpmCommand.run(sock.getInputStream(), sock.getOutputStream());
                                    txt1.setText("" + engineRpmCommand.getRPM() + "");
                                    speedCommand.run(sock.getInputStream(), sock.getOutputStream());
                                    speedV = speedCommand.getImperialSpeed();
                                    if (speedCommand.getMetricSpeed() > 70)
                                        penalties += 1;
                                    txt2.setText("" + speedCommand.getMetricSpeed() + "");
                                    try {
                                        massAirFlowCommand.run(sock.getInputStream(), sock.getOutputStream());
                                        maf = massAirFlowCommand.getMAF();
                                        airFuelRatioCommand.run(sock.getInputStream(), sock.getOutputStream());
                                        afr = airFuelRatioCommand.getAirFuelRatio();
                                        if (speedV > 0) {
                                            getFuelEff(speedV, maf, afr);
                                            txt3.setText("" + avg + "");
                                        } else {
                                            getFuelEff(speedV, maf, afr);
                                            txt3.setText("" + lph + "");
                                        }
                                    } catch (Exception e5) {
                                        txt3.setText("Data not available");
                                    }
                                    runtimeCommand.run(sock.getInputStream(), sock.getOutputStream());
                                    duration = runtimeCommand.getFormattedResult();
                                    txt4.setText("" + duration + "");
                                    status.setText("Connected");
                                } catch (Exception e) {
                                    try {
                                        sock.getInputStream().close();
                                        sock.getOutputStream().close();
                                        sock.close();
                                        handler.removeCallbacks(runnableCode);
                                        handler.postDelayed(runnableCode, 4000);
                                        hh = "0.0";
                                        mm = "0.0";
                                        ss = "0.0";
                                        n = 1;
                                    }catch (Exception e4){}
                                    status.setText("Disconnected");
                                }
                                latitude=mCurrentLocation.getLatitude();
                                longitude=mCurrentLocation.getLongitude();
                                request = new StringRequest(Request.Method.PUT, url,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(Obd.this,
                                                "Error " + error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                                    @Override
                                    public byte[] getBody() throws AuthFailureError {
                                        String postData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                                                "<liveData>\n" +
                                                "<DId>" + driverID + "</DId>\n" +
                                                "<destination>" + destination1 + "</destination>\n" +
                                                "<origin>" + origin1 + "</origin>\n" +
                                                "<speed>" + speedCommand.getMetricSpeed() + "</speed>\n" +
                                                "<VId>" + vehicleID + "</VId>\n" +
                                                "<XCoordi>" + latitude + "</XCoordi>\n" +
                                                "<YCoordi>" + longitude + "</YCoordi>\n" +
                                                "</liveData>"; // TODO get your final output
                                        try {
                                            return postData == null ? null :
                                                    postData.getBytes(getParamsEncoding());
                                        } catch (UnsupportedEncodingException uee) {
                                            // TODO consider if some other action should be taken
                                            return null;
                                        }
                                    }

                                    @Override
                                    public String getBodyContentType() {
                                        return "application/xml; charset=" +
                                                getParamsEncoding();
                                    }
                                };
                                queue.add(request);
                                handler.postDelayed(this, 5000);
                            }
                        };
                        handler.post(runnableCode);

                    } else
                        Toast.makeText(Obd.this,
                                "Failed to recieve data",
                                Toast.LENGTH_SHORT).show();
                }
            });
            Button b1 = (Button) findViewById(R.id.navigate);
            b1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:q=" + destination1 + ""));
                    startActivity(intent);

                }
            });
            Button b4 = (Button) findViewById(R.id.end);
            b4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dscore = (1 * distance) - (2 * penalties);
                    if (avg > 9.0)
                        dscore -= 5;
                    else if (9.0 > avg && avg < 8.5)
                        dscore += 2;
                    else if (8.5 > avg)
                        dscore += 5;
                    String[] tokens = duration.split(":");
                    for (String t : tokens) {
                        if(i==0){
                            hh=t;
                            i++;
                        }else if(i==1){
                            mm=t;
                            i++;
                        }else if(i==2){
                            ss=t;
                        }
                    }
                    timeC = (Double.parseDouble(hh) * 60 * 60) + (Double.parseDouble(mm) * 60) + Double.parseDouble(ss);
                    duration=""+hh+" hrs and "+mm+" mins";
                    if (timeC > (time + 900))
                        dscore -= 3;
                    else if((time+900) > timeC && timeC>=time)
                        dscore += 5;
                    else if(timeC<time)
                        dscore+=10;
                    drScore=Double.toString(dscore);
                    Intent intent = new Intent(Obd.this, TripSummary.class);
                    intent.putExtra("tripID", tripID);
                    intent.putExtra("vehicleID", vehicleID);
                    intent.putExtra("driverID", driverID);
                    intent.putExtra("origin", origin1);
                    intent.putExtra("destination", destination1);
                    intent.putExtra("dscore", drScore);
                    avgF=Double.toString((double)Math.round(avg));
                    intent.putExtra("lp100", avgF);
                    intent.putExtra("duration", duration);
                    penalty=Integer.toString(penalties);
                    intent.putExtra("penalties", penalty);
                    intent.putExtra("kmsDone", dist);
                    try {
                        sock.getInputStream().close();
                        sock.getOutputStream().close();
                        sock.close();
                        handler.removeCallbacks(runnableCode);
                        hh="0.0";mm="0.0";ss="0.0";
                        n = 1;
                    } catch (Exception e) {

                    }
                    startActivity(intent);
                }
            });
        }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        try {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){ Toast.makeText(Obd.this,"Cannot start location updates",Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
    public Double getFuelEff(Float speedV,Double maf, Double afr){
        double fps;
        if(speedV>0) {
            fps=maf/(afr*454);
            gph=(fps*3600)/6.701;
            mpg = speedV / gph;
            lp100 = 100/(mpg * 0.425144);
            avg=lp100+prev;
            prev=avg;
            avg=(avg)/n;
            n+=1;
            return (double)Math.round(avg);
        }else {
            fps=maf/(afr*454);
            gph=(fps*3600)/6.701;
            gph=maf*0.0805;
            lph=gph*3.78541;
            return (double)Math.round(lph);
        }
    }
}