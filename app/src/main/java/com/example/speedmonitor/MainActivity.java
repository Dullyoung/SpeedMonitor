package com.example.speedmonitor;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "aaaaa";
    LocationManager mLocationManager;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv_msg);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        init();
    }

    private void init() {
        PermissionHelper permissionHelper = new PermissionHelper();
        permissionHelper.setMustPermissions2(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionHelper.checkAndRequestPermission(this, new PermissionHelper.OnRequestPermissionsCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onRequestPermissionSuccess() {
                List<String> providers = mLocationManager.getProviders(true);
                Log.i(TAG, "initViews: " + providers.toString());
                String locationProvider;
                if (providers.contains(LocationManager.GPS_PROVIDER)) {
                    //如果是GPS定位
                    Log.d(TAG, "如果是GPS定位");
                    locationProvider = LocationManager.GPS_PROVIDER;
                } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                    //如果是网络定位
                    Log.d(TAG, "如果是网络定位");
                    locationProvider = LocationManager.NETWORK_PROVIDER;
                } else {
                    Log.d(TAG, "没有可用的位置提供器");

                    textView.setText("没有可用的位置提供器");
                    Toast.makeText(MainActivity.this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 777);
                    return;
                }
                textView.setText("正在使用" + locationProvider + "定位");
                mLocationManager.requestLocationUpdates(locationProvider, 0, 0, new LocationListener() {

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
                        }
                        double distance = getDistance(lastLongitude, lastLatitude,
                                location.getLongitude(), location.getLatitude());
                        long time = System.currentTimeMillis() - lastTime;
                        String speed;
                        if (time == 0) {
                            speed = "0 mm/s";
                        } else {
                            speed = distance / (time * 1000) + " mm/s";
                        }
                        String text = "上次时间:" + lastTime +
                                "两次时间间隔" + time +
                                "\n本次纬度:" + location.getLatitude() +
                                "\n上次纬度:" + lastLatitude +
                                "\n本次经度:" + location.getLongitude() +
                                "\n上次经度:" + lastLongitude +
                                "\n速度：" + speed +
                                "\n定位方式：" + locationProvider +
                                "\n精度" + location.getAccuracy();
                        textView.setText(text);
                        setUI(speed);
                    }
                });

            }

            @Override
            public void onRequestPermissionError() {
                Log.i(TAG, "onRequestPermissionError: ");
                Toast.makeText(MainActivity.this, "请打开相关权限", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 888);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 || requestCode == 888) {
            Log.i(TAG, "onActivityResult: " + requestCode + "---" + resultCode);
            init();
        }
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