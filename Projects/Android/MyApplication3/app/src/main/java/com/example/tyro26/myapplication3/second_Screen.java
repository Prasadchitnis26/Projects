package com.example.tyro26.myapplication3;

import android.provider.Settings;
import android.os.*;
import org.json.*;
import com.google.gson.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.*;
import java.io.*;
import java.util.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.*;
import android.location.*;

public class second_Screen extends AppCompatActivity {
    static String recom = "";
    public TextView  txt8;

    public void p() {
        int rating = 0;
        boolean b;
        txt8 = (TextView) findViewById(R.id.textView10);
        DevicePolicyManager dm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        KeyguardManager km = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        int stat = dm.getStorageEncryptionStatus();//this checks whether the device has Storage Encryption
        char k3[];
        if (stat == 0) {
            k3 = String.valueOf(stat).toCharArray();
        } else if (stat == 2 || stat == 3) {// best case
            k3 = String.valueOf(stat).toCharArray();
            rating = rating + 2;
        } else {// second best
            k3 = String.valueOf(stat).toCharArray();
            rating = rating + 1;
            recom = recom + "Activate Storage Encryption \n ";
        }
        if (km.isDeviceSecure())//this is used to check whether the device uses password protection
            b = true;
        else
            b = false;

        String tem = "";
        if (b) {// lock
            tem = "secure";
            rating = rating + 2;
            char[] k2 = tem.toCharArray();
            // txt2.setText(k2, 0, k2.length);
        } else {
            tem = "insecure";
            char[] k2 = tem.toCharArray();
            // txt2.setText(k2, 0, k2.length);
            recom = recom + "Add Screen Lock \n ";
        }

        String value = "";
        String val = android.os.Build.VERSION.RELEASE;//this is used to check the OS version of the device
        if (val.charAt(0) == '6' || val.charAt(0) == '5') {
            value = "high";// osversion
            rating = rating + 2;
        }
        if (val.charAt(0) == '4' || val.charAt(0) == '3') {
            value = "medium";
            recom = recom + "Update OS Version \n ";
            rating = rating + 1;
        }
        if (val.charAt(0) == '2' || val.charAt(0) == '1') {
            value = "low";
            recom = recom + "Update OS Version \n ";
        }
        int apiLevel = Build.VERSION.SDK_INT;//this is used to check the API level of the device
        String ttt = "";// apilevel
        if (apiLevel < 10) {
            ttt = "low";

        }
        if (apiLevel < 18 && apiLevel >= 10) {
            ttt = "medium";
            rating = rating + 1;
        }
        if (apiLevel >= 18) {
            ttt = "high";
            rating = rating + 2;
        }

        Settings.Secure ss = new Settings.Secure();
        int var = ss.getInt(getApplicationContext().getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        String usb = "";//this is used to check whether USB Debugging is enabled or not
        char kus[];
        if (var == 0) {
            usb = "secure";
            rating = rating + 2;
        }
        if (var == 1) {
            usb = "insecure";
            recom = recom + "Disable USB Debugging \n ";
        }

        String sk = "";
        char[] l;
        PackageManager pm = getApplicationContext().getPackageManager();
        boolean gps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        boolean b3 = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gps) {//this is used to check whether the device has GPS enabled

            if (b3) {
                sk = "high";
                rating = rating + 2;
            } else {
                sk = "medium";
                rating = rating + 1;
            }
        } else {
            sk = "low";
        }

        String v = "";
        char kv[];//this checks whether the application is theft proof
        if ((stat == 2 || stat == 3) && b && b3) {
            v = "high";
            rating = rating + 2;
        } else if (((stat == 2 || stat == 3) && b) || ((stat == 2 || stat == 3) && b3) || (b && b3)
                || ((stat == 1 || stat == 4) && gps)) {
            v = "medium";
            rating = rating + 1;
        } else {
            v = "low";
        }

        int total = 14;//this is used to calculate the software level security of the device
        int totalSecurityRating = (rating * 100) / 14;
        txt8.setText(String.valueOf(totalSecurityRating));
        String skk = "";
        TextView t2 = (TextView) findViewById(R.id.textView5);
        char ks2[] = (recom).toCharArray();
        t2.setText(ks2, 0, ks2.length);
    }
    /*
    This method is used to get package name of all the applications
     */
    public void p2() throws JSONException {
        String v = "";
        PackageManager pmk = getPackageManager();
        List<ApplicationInfo> apps = pmk.getInstalledApplications(0);
        System.out.println(apps.size());
        String arr2[] = new String[apps.size()];
        int c = 0;
        for (ApplicationInfo app : apps) {
            if (!(ApplicationInfo.FLAG_SYSTEM == 0)) {
                arr2[c] = app.packageName;
                c++;
                v = "";
            }
        }
        String arr[] = new String[apps.size()];
        for (int i = 0; i < arr.length; i++) {
            try {
                arr[i]="https://42matters.com/api/1/apps/lookup.json?p=" + arr2[i] + "&access_token=def9ddbfe40fbf8b755cbeb9ccc0649101d14adf";
            } catch (Exception e) {
            }
            System.out.println(arr2[i]);
        }
        String jsonData[] = new String[arr.length];
        for (int i = 0; i < jsonData.length; i++) {
            try {
                jsonData[i] = "";
                URL u = new URL(arr[i]);
                HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String s = br.readLine();
                while (s != null) {
                    jsonData[i] = jsonData[i] + s;
                    s = br.readLine();
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {
            }
        }
        p3(jsonData);

    }
    /*
    This method is used to get the rating of every application and then calculate the Application level Security
     */
    public void p3(String json[]) throws JSONException {
        int c = 0;
        int c2 = json.length;
        for (int i = 0; i < json.length; i++) {
            Gson gson = new Gson();
            Andr a = gson.fromJson(json[i], Andr.class);

             if (a != null && a.getRating() >=3.0) {
                c++;
            }
        }
        String ss = String.valueOf((c * 100) / c2);
        TextView t = (TextView) findViewById(R.id.textView11);
        char ks[] = (ss).toCharArray();
        t.setText(ks, 0, ks.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second__screen);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        p();
        try {
            p2();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /*
    This class is used by the GSON library
     */
    public class Andr {
        private float rating;
        private String[] badges;

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public String[] getBadges() {
            return badges;
        }

        public void setBadges(String[] badges) {
            this.badges = badges;
        }

    }
}
