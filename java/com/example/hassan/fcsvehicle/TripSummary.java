package com.example.hassan.fcsvehicle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.io.UnsupportedEncodingException;

public class TripSummary extends AppCompatActivity {
    StringRequest submit,delete,update,get;
    Double getDscore,dscore;
    int eventType,score1,score2;
    String tripID,vehicleID,driverID,duration,origin1,destination1,drScore,getDriverScore,penalty,fuelcon,det,dname,daddress,lic_no,phone,distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);
        final TextView tid= (TextView) findViewById(R.id.t_idS);
        final TextView vid = (TextView) findViewById(R.id.v_idS);
        final TextView did = (TextView) findViewById(R.id.d_idS);
        final TextView distanceC = (TextView) findViewById(R.id.dist_cov);
        final TextView timeC = (TextView) findViewById(R.id.time_con);
        final TextView fuelC = (TextView) findViewById(R.id.fuel_con);
        final TextView penalties = (TextView) findViewById(R.id.penalties);
        final TextView score = (TextView) findViewById(R.id.score);
        final TextView origin = (TextView) findViewById(R.id.originS);
        final TextView destination = (TextView) findViewById(R.id.destinationS);
        final RequestQueue queue = Volley.newRequestQueue(TripSummary.this);
        Intent intent = getIntent();
        tripID = intent.getExtras().getString("tripID");
        vehicleID = intent.getExtras().getString("vehicleID");
        driverID = intent.getExtras().getString("driverID");
        origin1 = intent.getExtras().getString("origin");
        destination1 = intent.getExtras().getString("destination");
        drScore=intent.getExtras().getString("dscore");
        dscore=Double.parseDouble(drScore);
        score2=dscore.intValue();
        fuelcon=""+intent.getExtras().getString("lp100")+" lp100 kms";
        duration = intent.getExtras().getString("duration");
        penalty = intent.getExtras().getString("penalties");
        distance = intent.getExtras().getString("kmsDone");
        final String url ="http://192.168.43.84:8090/TripsSummaryWS/webresources/tripsummary";
        final String url1 = "http://192.168.43.84:8090/TripsRestWS/webresources/trips/"+tripID+"";
        final String url2 = "http://192.168.43.84:8090/DriversWS/webresources/drivers/"+driverID+"";
        tid.setText(""+tripID+"");
        vid.setText(""+vehicleID+"");
        did.setText(""+driverID+"");
        score.setText(""+drScore+"");
        penalties.setText(""+penalty+"");
        origin.setText(""+origin1+"");
        destination.setText(""+destination1+"");
        timeC.setText(""+duration+"");
        distanceC.setText(""+distance+"");
        fuelC.setText(""+fuelcon+"");
        get = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            InputStream stream = new ByteArrayInputStream(response.getBytes());
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
                                        det=parser.getText();
                                        break;
                                    case XmlPullParser.END_TAG:
                                        if(tag_name.equalsIgnoreCase("drivers")){

                                        }
                                        else if(tag_name.equalsIgnoreCase("address")){
                                            daddress=det;}
                                        else if(tag_name.equalsIgnoreCase("dname")){
                                            dname=det;}
                                        else if(tag_name.equalsIgnoreCase("dscore"))
                                            getDriverScore=det;
                                        else if (tag_name.equalsIgnoreCase("licenseNo"))
                                            lic_no=det;
                                        else if(tag_name.equalsIgnoreCase("phoneNo"))
                                            phone=det;
                                        break;
                                    default:
                                        break;
                                }
                                eventType = parser.next();
                            }
                        } catch (Exception e){}
                        Toast.makeText(TripSummary.this,"address: "+daddress+" name: "+dname+" dscore: "+getDriverScore+" lic: "+lic_no+" phone: "+phone,Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TripSummary.this,
                        "Error "+error,
                        Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(get);
        Button goMain = (Button) findViewById(R.id.submitTrip);
        goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 submit = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(TripSummary.this,
                                        "Trip summary submitted",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TripSummary.this,
                                "Error "+error,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String postData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                                "<tripSummary>\n" +
                                "<avgFuelCons>"+fuelcon+"</avgFuelCons>\n"+
                                "<DId>"+driverID+"</DId>\n"+
                                "<destination>"+destination1+"</destination>\n" +
                                "<dscore>"+score2+"</dscore>\n"+
                                "<kmsdone>"+distance+"</kmsdone>\n"+
                                "<origin>"+origin1+"</origin>\n"+
                                "<penalties>"+penalty+"</penalties>\n" +
                                "<TId>"+tripID+"</TId>\n"+
                                "<tdate>27-05-18</tdate>"+
                                "<timeCons>"+duration+"</timeCons>\n"+
                                "<VId>"+vehicleID+"</VId>\n"+
                                "</tripSummary>"; // TODO get your final output
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
                queue.add(submit);
                delete = new StringRequest(Request.Method.DELETE, url1,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(TripSummary.this,
                                        "Pending trip deleted",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TripSummary.this,
                                "Error "+error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(delete);
                getDscore=Double.parseDouble(getDriverScore);
                score1=(int) (getDscore+dscore);
                update = new StringRequest(Request.Method.PUT, url2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(TripSummary.this,
                                        "Driver score updated.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TripSummary.this,
                                "Error "+error,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String postData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
                                "<drivers>\n" +
                                "<address>"+daddress+"</address>\n"+
                                "<DId>"+driverID+"</DId>\n"+
                                "<dname>"+dname+"</dname>\n" +
                                "<dscore>"+score1+"</dscore>\n"+
                                "<licenseNo>"+lic_no+"</licenseNo>\n"+
                                "<phone>"+phone+"</phone>\n"+
                                "</drivers>"; // TODO get your final output
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
                queue.add(update);
                Intent intent = new Intent(TripSummary.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
