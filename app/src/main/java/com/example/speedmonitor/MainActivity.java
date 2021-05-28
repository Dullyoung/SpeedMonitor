package com.example.speedmonitor;


import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "aaaaa";
    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        PermissionHelper permissionHelper = new PermissionHelper();
        permissionHelper.setMustPermissions2(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionHelper.checkAndRequestPermission(this, new PermissionHelper.OnRequestPermissionsCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onRequestPermissionSuccess() {
                List<String> providers = mLocationManager.getProviders(true);
                Log.i(TAG, "initViews: " + providers.toString());
                String locationProvider;
                if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                    //如果是网络定位
                    Log.d(TAG, "如果是网络定位");
                    locationProvider = LocationManager.NETWORK_PROVIDER;
                } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                    //如果是GPS定位
                    Log.d(TAG, "如果是GPS定位");
                    locationProvider = LocationManager.GPS_PROVIDER;
                } else {
                    Log.d(TAG, "没有可用的位置提供器");

                    return;
                }
                mLocationManager.requestLocationUpdates(locationProvider, 100, 0, new LocationListener() {

                    /**
                     * 当某个位置提供者的状态发生改变时
                     */
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle arg2) {

                    }

                    /**
                     * 某个设备打开时
                     */
                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    /**
                     * 某个设备关闭时
                     */
                    @Override
                    public void onProviderDisabled(String provider) {

                    }

                    /**
                     * 手机位置发生变动
                     */
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "onLocationChanged: " + location);
                        Log.i(TAG, "onLocationChanged: " + lastTime +
                                "--" + lastLatitude + "---" + lastLongitude);
                        if (lastTime == 0) {
                            lastTime = System.currentTimeMillis();
                            lastLatitude = location.getLatitude();
                            lastLongitude = location.getLongitude();
                            return;
                        }
                        double distance = getDistance(lastLongitude, lastLatitude,
                                location.getLongitude(), location.getLatitude());
                        long time = System.currentTimeMillis() - lastTime;
                        String speed = distance / (time * 1000) + "mm/s";
                        TextView textView = findViewById(R.id.tv_msg);
                        String text = "lastTime:" + lastTime + "   lastLatitude:" + lastLatitude +
                                "   lastLongitude:" + lastLongitude + "   speed" + speed;
                        textView.setText(text);
                        setUI(speed);
                    }
                });

            }

            @Override
            public void onRequestPermissionError() {
                Log.i(TAG, "onRequestPermissionError: ");
            }
        });
    }

    private void setUI(String speed) {
        Log.i(TAG, "setUI: " + speed);
    }

    private long lastTime;
    private double lastLatitude;
    private double lastLongitude;

    private static final double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    // 返回单位是:毫米
    public static int getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        //有小数的情况;注意这里的10000d中的“d”
        s = Math.round(s * 10000d) / 10000d;
        return (int) s;
    }

}