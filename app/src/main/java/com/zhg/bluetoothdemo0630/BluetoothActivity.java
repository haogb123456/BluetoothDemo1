package com.zhg.bluetoothdemo0630;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhg.bluetooth.entity.ConnectModel;
import com.zhg.bluetooth.entity.MeasuringModel;
import com.zhg.bluetooth.ifc.ConnectListener;
import com.zhg.bluetooth.ifc.DeviceListener;
import com.zhg.bluetooth.ifc.MeasuringListener;
import com.zhg.bluetooth.service.BluetoothService;
import com.zhg.bluetooth.service.BluetoothService2;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity
        implements ConnectListener,MeasuringListener,DeviceListener{
    private String TAG = this.getClass().getSimpleName()+"---";
    public static final int PERMISSION_LOCATION = 100;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mBluetoothDevice;
    private BluetoothService2 mBluetoothService;
    private BluetoothAdapter mBluetoothAdapter;
    private String deviceName1 = "SE";//蓝牙名称以此开头
    private String deviceName2 = "NB";//蓝牙名称以此开头
    private String deviceAddress1 = "D8:B0:4C:E0:F2:5A";
    private String deviceAddress2 = "D8:B0:4C:C5:F7:DA";
    private int deviceCount = 2;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService2.LocalBinder) service).getService();
            initListener();
            mBluetoothAdapter = mBluetoothService.initBluetoothAdapter(BluetoothActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBluetoothService){
            unbindService(mServiceConnection);
            Intent intent = new Intent();
            intent.setAction("com.zhg.bluetooth.service");
            intent.setPackage(getPackageName());
            stopService(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initUI();
        initService();
        requestPermission();
    }

    private  void initUI(){
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= 23 && !isLocationOpen(BluetoothActivity.this)) {
                        Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(enableLocate);
                    }
                } else {
                    Toast.makeText(this,"读取位置权限被禁用", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initService() {
        Intent gattServiceIntent = new Intent(this, BluetoothService2.class);
        gattServiceIntent.setAction("com.zhg.bluetooth.service");
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
    }

    private void initListener(){
        if (null != mBluetoothService){
            mBluetoothService.setConnectListener(this);
            mBluetoothService.setDeviceListener(this);
            mBluetoothService.setMeasuringListener(this);
        }
    }

    public void onBluetoothClick(View view){
        switch (view.getId()){

            case R.id.btn_disCover:
                //扫描
                if (null != mBluetoothAdapter){
                    mBluetoothAdapter.startDiscovery();
                }else {
                    Log.e(TAG,"134 bluetoothAdapter is null");
                }
                break;

            case R.id.btn_connect:
                //链接
//                mBluetoothService.connect(device);//连接单个蓝牙
                mBluetoothService.connect(deviceList);//连接多个蓝牙
                break;

            case R.id.btn_disConnect:
                //断开链接
                mBluetoothService.disConnect();
                break;
            case R.id.btn_sendCMD:
                mBluetoothService.writeCharacteristic(deviceAddress2);
                break;
            case R.id.btn_sendCMD1:
                mBluetoothService.writeCharacteristic(deviceAddress1);
                break;
            default:
                break;
        }
    }

    private boolean isLocationOpen(final Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider|| isNetWorkProvider;
    }

    BluetoothDevice device;
    @Override
    public void onBluetoothDevice(BluetoothDevice device) {
        Log.i(TAG,"deviceName is "+device.getName()+"===="+device.getAddress());
        if (device.getName()!=null) {
            if (device.getName().startsWith(deviceName1) || device.getName().startsWith(deviceName2)){
                deviceList.add(device);
                if (deviceList.size()==deviceCount) {
                    mBluetoothAdapter.cancelDiscovery();
                }
//                this.device  =device;//连接一台蓝牙时
//                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }

    @Override
    public void onBluetoothConnect(final ConnectModel connectModel) {
    }

    @Override
    public void onCharacteristicChanged(MeasuringModel measuringModel) {
         //在这里处理返回的数据，如果是多个设备同时兼容，建议使用Adapter模式
    }
}
