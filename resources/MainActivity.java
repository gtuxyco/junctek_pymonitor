package com.juntek.vat;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.juntek.vat.fragment.AboutUsFragment;
import com.juntek.vat.fragment.CurveFragment;
import com.juntek.vat.fragment.MainFragment;
import com.juntek.vat.fragment.SettingFragment;
import com.juntek.vat.view.LineView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int DOWNLOAD_COMPLETE = 200;
    private static final int INSTALL_PERMISS_CODE = 500;
    private static final int PROGRESS = 100;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    public static double ahMax = 0.0d;
    public static double ahMaxTemp = 0.0d;
    public static int cMax = -1;
    public static boolean duankai = false;
    private static final int isProduct = 1;
    public static int model = -1;

    /* renamed from: no */
    public static int f50no = -1;
    public static int vMax = -1;
    public static volatile int versionFirm = -1;
    public volatile int IAP = -1;
    public boolean able;
    public volatile int bigPack = -1;
    private List<Byte> byteList = new ArrayList();
    /* access modifiers changed from: private */
    public List<Integer> byteListHex = new ArrayList();
    public boolean canFa = true;
    /* access modifiers changed from: private */
    public boolean connecting = false;
    public volatile int dataType;
    /* access modifiers changed from: private */
    public boolean destroyed = false;
    public volatile boolean duanlu = false;
    /* access modifiers changed from: private */
    public File file;
    public List<Fragment> fragmentList = new ArrayList();
    /* access modifiers changed from: private */
    public BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, final int i, int i2) {
            Log.d("dong", "status:" + i + " newState:" + i2);
            Log.d("dong", Thread.currentThread().getStackTrace()[2].getMethodName());
            if (i2 == 2) {
                Log.d("dong", "设备连接上 开始扫描服务");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        MainActivity.this.mBluetoothGatt.discoverServices();
                    }
                }, 500);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.tv_search.setText(MainActivity.this.getResources().getString(C0519R.string.disconnect));
                        ((MainFragment) MainActivity.this.fragmentList.get(0)).setConnect(true, MainActivity.this.mDevice.getName());
                        Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(C0519R.string.connected), 1).show();
                    }
                });
            }
            if (i2 == 0) {
                Log.d("dong", "设备连接断开");
                MainActivity.this.mBluetoothGatt.close();
                BluetoothGatt unused = MainActivity.this.mBluetoothGatt = null;
                MainActivity mainActivity = MainActivity.this;
                mainActivity.able = false;
                mainActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.tv_search.setText(MainActivity.this.getResources().getString(C0519R.string.search));
                        ((MainFragment) MainActivity.this.fragmentList.get(0)).setConnect(false, (String) null);
                        MainActivity.this.tv_search.setVisibility(0);
                        MainActivity mainActivity = MainActivity.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append(MainActivity.this.getResources().getString(C0519R.string.disconnect));
                        int i = i;
                        sb.append(i != 0 ? Integer.valueOf(i) : "");
                        Toast.makeText(mainActivity, sb.toString(), 1).show();
                        MainActivity.this.tv_cover.setVisibility(8);
                        MainActivity.this.pb_loading.setVisibility(8);
                    }
                });
                MainActivity.restartActivity(MainActivity.this);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.d("dong", Thread.currentThread().getStackTrace()[2].getMethodName());
            MainActivity.this.writed = true;
        }

        public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            Log.d("dong", Thread.currentThread().getStackTrace()[2].getMethodName());
            if (i == 0) {
                Log.d("dong", "开启监听成功");
                Log.d("dong", "******");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        MainActivity.this.able = true;
                        MainActivity.this.sendData(new byte[]{-69, -102, -87, 16, -18});
                        MainActivity.this.showProgress(false);
                        MainActivity.this.duanlu = true;
                    }
                }, 500);
                SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("Datadefault", 0);
                String string = sharedPreferences.getString(MainActivity.this.mDevice.getName(), "");
                if (string.length() > 0) {
                    try {
                        double parseDouble = Double.parseDouble(string);
                        ((MainFragment) MainActivity.this.fragmentList.get(0)).f80cv.setMaxShowCur(parseDouble);
                        ((CurveFragment) MainActivity.this.fragmentList.get(1)).setMaxShowC2(parseDouble);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int i2 = sharedPreferences.getInt(MainActivity.this.mDevice.getName() + "maxshowv", -1);
                if (i2 > -1) {
                    ((MainFragment) MainActivity.this.fragmentList.get(0)).f83vv.setMaxShowVol(i2);
                }
                int i3 = sharedPreferences.getInt(MainActivity.this.mDevice.getName() + "minshowv", -1);
                if (i3 > -1) {
                    ((MainFragment) MainActivity.this.fragmentList.get(0)).f83vv.setMinShowVol(i3);
                }
                if (i2 > -1 && i3 > -1) {
                    ((CurveFragment) MainActivity.this.fragmentList.get(1)).setMaxShowV((double) i3, (double) i2);
                }
                MainActivity mainActivity = MainActivity.this;
                mainActivity.lowc = sharedPreferences.getInt(MainActivity.this.mDevice.getName() + "lowc", -1);
                if (MainActivity.this.lowc > -1) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            ((SettingFragment) MainActivity.this.fragmentList.get(2)).setLowc(MainActivity.this.lowc);
                        }
                    });
                }
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("lastdevice", MainActivity.this.mDevice.getName());
                edit.commit();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        ((SettingFragment) MainActivity.this.fragmentList.get(2)).setFirm(MainActivity.versionFirm, MainActivity.this.localFirm);
                    }
                }, 2000);
            }
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.d("dong", Thread.currentThread().getStackTrace()[2].getMethodName());
            final BluetoothGattCharacteristic characteristic = MainActivity.this.mBluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                }
            }, 500);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    MainActivity.this.mBluetoothGatt.writeDescriptor(descriptor);
                }
            }, 1000);
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            MainActivity.this.duanlu = false;
            byte[] value = bluetoothGattCharacteristic.getValue();
            String str = new String(value);
            if (MainActivity.this.upgrade == 0) {
                if (str.equals("D")) {
                    MainActivity.this.online = true;
                }
            } else if (MainActivity.this.upgrade == 2) {
                MainActivity.this.type = str;
            } else if (MainActivity.this.upgrade == 1) {
                if (str.equals("C")) {
                    MainActivity.this.bigPack = 1;
                } else if (str.equals("E")) {
                    MainActivity.this.bigPack = 0;
                }
                Log.d("dongg", str);
            } else {
                for (byte b : value) {
                    MainActivity.this.byteListHex.add(Integer.valueOf(b & 255));
                }
                if (!MainActivity.this.destroyed) {
                    MainActivity.this.process();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Handler handler = new Handler() {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 100) {
                MainActivity.this.showProgressValue(true, ((Integer) message.obj).intValue());
            } else if (i == 200) {
                MainActivity.this.installApk();
                MainActivity.this.showProgressValue(false, 0);
            }
        }
    };
    /* access modifiers changed from: private */
    public String lastDeviceName = null;
    public volatile int localFirm = 100;
    private ImageButton lockButton;
    public int lowc = -1;
    /* access modifiers changed from: private */
    public BluetoothGatt mBluetoothGatt;
    /* access modifiers changed from: private */
    public BluetoothAdapter mBtAdapter = null;
    public BluetoothDevice mDevice = null;
    /* access modifiers changed from: private */
    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            if (bluetoothDevice.getName() != null && bluetoothDevice.getName().equals(MainActivity.this.lastDeviceName) && !MainActivity.this.connecting) {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.mDevice = bluetoothDevice;
                boolean unused = mainActivity.connecting = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        BluetoothGatt unused = MainActivity.this.mBluetoothGatt = MainActivity.this.mDevice.connectGatt(MainActivity.this, false, MainActivity.this.gattCallback);
                    }
                }, 500);
                MainActivity.this.mBtAdapter.stopLeScan(MainActivity.this.mLeScanCallback);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        MainActivity.this.showProgress(true);
                    }
                });
            }
        }
    };
    private Timer mainTimer;
    public volatile boolean online = false;
    /* access modifiers changed from: private */
    public ProgressBar pb_loading;
    private ProgressBar pb_progress;
    public Semaphore semaphore = new Semaphore(1);
    TabLayout tabLayout;
    private TextView tv_address;
    /* access modifiers changed from: private */
    public TextView tv_cover;
    /* access modifiers changed from: private */
    public TextView tv_model;
    /* access modifiers changed from: private */
    public TextView tv_search;
    /* access modifiers changed from: private */
    public TextView tv_temp;
    public volatile String type = "";
    public volatile int upgrade = -1;
    ViewPager viewPager;
    private int vol = 0;
    public volatile boolean writed = false;
    /* access modifiers changed from: private */
    public Thread zhuThread;

    public void sendData(String str) {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) C0519R.layout.activity_main);
        vMax = -1;
        cMax = -1;
        ahMax = 0.0d;
        ahMaxTemp = 0.0d;
        model = -1;
        f50no = -1;
        versionFirm = -1;
        initBluetooth();
        initView();
        getWindow().addFlags(128);
        if (!duankai) {
            check();
            checkFirm();
            String string = getSharedPreferences("Datadefault", 0).getString("lastdevice", "");
            if (string.length() > 0) {
                this.lastDeviceName = string;
                this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                if (this.mBtAdapter.isEnabled()) {
                    showProgress(true);
                    this.mBtAdapter.startLeScan(this.mLeScanCallback);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            if (!MainActivity.this.connecting) {
                                MainActivity.this.mBtAdapter.stopLeScan(MainActivity.this.mLeScanCallback);
                                MainActivity mainActivity = MainActivity.this;
                                Toast.makeText(mainActivity, mainActivity.getResources().getString(C0519R.string.f71mc), 0).show();
                                MainActivity.this.showProgress(false);
                            }
                        }
                    }, 5000);
                }
                Log.d("", "");
            }
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation == 2) {
            Log.d("dong", "ORIENTATION_LANDSCAPE");
            ((MainFragment) this.fragmentList.get(0)).initOrientation();
            ((CurveFragment) this.fragmentList.get(1)).initOrientation();
        } else if (configuration.orientation == 1) {
            Log.d("dong", "ORIENTATION_PORTRAIT");
            ((MainFragment) this.fragmentList.get(0)).initOrientation();
            ((CurveFragment) this.fragmentList.get(1)).initOrientation();
        }
    }

    private void checkFirm() {
        new OkHttpClient().newCall(new Request.Builder().url("http://68.168.132.244/app/vagg/kg.json").get().build()).enqueue(new Callback() {
            public void onFailure(Call call, IOException iOException) {
                Log.d("", "onFailure: ");
            }

            public void onResponse(Call call, Response response) throws IOException {
                try {
                    MainActivity.versionFirm = new JSONObject(response.body().string()).getInt("version");
                    if (MainActivity.this.isBlueToothAble()) {
                        ((SettingFragment) MainActivity.this.fragmentList.get(2)).setFirm(MainActivity.versionFirm, MainActivity.this.localFirm);
                    }
                    Log.d("", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void check() {
        new OkHttpClient().newCall(new Request.Builder().url("http://68.168.132.244/app/vag").get().build()).enqueue(new Callback() {
            public void onFailure(Call call, IOException iOException) {
                Log.d("", "onFailure: ");
            }

            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (MainActivity.this.getVersionCode(MainActivity.this) < new JSONObject(response.body().string()).getInt("versionCode")) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this).setPositiveButton(MainActivity.this.getResources().getString(C0519R.string.f72ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        File unused = MainActivity.this.downFile("http://68.168.132.244/app/KG.apk");
                                    }
                                }).setNegativeButton(MainActivity.this.getResources().getString(C0519R.string.cancel), (DialogInterface.OnClickListener) null).setTitle(MainActivity.this.getResources().getString(C0519R.string.havenew)).create().show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public File downFile(final String str) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    if (httpURLConnection.getResponseCode() == 200) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        if (inputStream != null) {
                            int contentLength = httpURLConnection.getContentLength();
                            File unused = MainActivity.this.file = MainActivity.this.getFile(str);
                            FileOutputStream fileOutputStream = new FileOutputStream(MainActivity.this.file);
                            byte[] bArr = new byte[1024];
                            int i = 0;
                            while (true) {
                                int read = inputStream.read(bArr);
                                if (read == -1) {
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, read);
                                i += read;
                                double d = (double) i;
                                Double.isNaN(d);
                                double d2 = (double) contentLength;
                                Double.isNaN(d2);
                                double d3 = ((d * 1.0d) / d2) * 100.0d;
                                Message obtainMessage = MainActivity.this.handler.obtainMessage();
                                obtainMessage.what = 100;
                                obtainMessage.obj = Integer.valueOf((int) d3);
                                MainActivity.this.handler.sendMessage(obtainMessage);
                            }
                            fileOutputStream.close();
                            fileOutputStream.flush();
                        }
                        inputStream.close();
                    }
                    Message obtainMessage2 = MainActivity.this.handler.obtainMessage();
                    obtainMessage2.what = 200;
                    MainActivity.this.handler.sendMessage(obtainMessage2);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
        return this.file;
    }

    /* access modifiers changed from: private */
    public void installApk() {
        Intent intent = new Intent("android.intent.action.VIEW");
        File file2 = this.file;
        if (file2 != null && file2.exists()) {
            if (Build.VERSION.SDK_INT >= 24) {
                intent.setFlags(1);
                intent.setDataAndType(FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", this.file), "application/vnd.android.package-archive");
                if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
                    startInstallPermissionSettingActivity();
                    return;
                }
            } else {
                intent.setDataAndType(Uri.fromFile(this.file), "application/vnd.android.package-archive");
                intent.setFlags(268435456);
            }
            if (getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                startActivity(intent);
            }
        }
    }

    private void startInstallPermissionSettingActivity() {
        startActivityForResult(new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", Uri.parse("package:" + getPackageName())), INSTALL_PERMISS_CODE);
    }

    /* access modifiers changed from: private */
    public File getFile(String str) {
        File file2 = new File(getExternalCacheDir(), "download");
        if (!file2.exists()) {
            file2.mkdir();
        }
        return new File(file2, getFilePath(str));
    }

    private String getFilePath(String str) {
        return str.substring(str.lastIndexOf("/"), str.length());
    }

    public int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void sendData(byte[] bArr) {
        BluetoothGatt bluetoothGatt;
        if (this.able && (bluetoothGatt = this.mBluetoothGatt) != null) {
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
            characteristic.setValue(bArr);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    private void initBluetooth() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }
    }

    private void initView() {
        this.tv_cover = (TextView) findViewById(C0519R.C0521id.tv_cover);
        this.tv_cover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            }
        });
        this.pb_loading = (ProgressBar) findViewById(C0519R.C0521id.pb_loading);
        this.pb_progress = (ProgressBar) findViewById(C0519R.C0521id.progress_bar_h);
        this.tabLayout = (TabLayout) findViewById(C0519R.C0521id.tab_layout);
        this.viewPager = (ViewPager) findViewById(C0519R.C0521id.view_pager);
        this.fragmentList.add(new MainFragment());
        this.fragmentList.add(new CurveFragment());
        this.fragmentList.add(new SettingFragment());
        this.fragmentList.add(new AboutUsFragment());
        this.viewPager.setOffscreenPageLimit(4);
        this.viewPager.setAdapter(new HomeFragmentAdapter(getSupportFragmentManager()));
        this.tabLayout.setupWithViewPager(this.viewPager);
        this.tv_address = (TextView) findViewById(C0519R.C0521id.tv_address);
        this.tv_temp = (TextView) findViewById(C0519R.C0521id.tv_temp);
        this.tv_model = (TextView) findViewById(C0519R.C0521id.tv_model);
        this.tv_search = (TextView) findViewById(C0519R.C0521id.tv_title);
        this.tv_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                BluetoothAdapter unused = MainActivity.this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!MainActivity.this.mBtAdapter.isEnabled()) {
                    System.out.println("蓝牙还没有打开");
                    MainActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
                } else if (MainActivity.this.tv_search.getText().equals(MainActivity.this.getResources().getString(C0519R.string.search))) {
                    MainActivity.this.startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class), 1);
                } else {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.able = false;
                    mainActivity.disconnect();
                    MainActivity.this.tv_search.setText(MainActivity.this.getResources().getString(C0519R.string.search));
                    ((MainFragment) MainActivity.this.fragmentList.get(0)).setConnect(false, (String) null);
                    MainActivity.restartActivity(MainActivity.this);
                }
            }
        });
        this.lockButton = (ImageButton) findViewById(C0519R.C0521id.bt_lock);
        this.lockButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.setSelected(!view.isSelected());
            }
        });
    }

    public void off() {
        ((MainFragment) this.fragmentList.get(0)).off();
    }

    /* renamed from: on */
    public void mo9244on() {
        ((MainFragment) this.fragmentList.get(0)).mo9351on();
    }

    private class HomeFragmentAdapter extends FragmentPagerAdapter {
        public HomeFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public Fragment getItem(int i) {
            return MainActivity.this.fragmentList.get(i);
        }

        public int getCount() {
            return MainActivity.this.fragmentList.size();
        }

        public CharSequence getPageTitle(int i) {
            return MainActivity.this.getResources().getStringArray(C0519R.array.tabs)[i];
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1 && i == INSTALL_PERMISS_CODE) {
            installApk();
        } else if (i != 1) {
            if (i != 2) {
                System.out.println("wrong request code");
            } else if (i2 == -1) {
                Toast.makeText(this, "蓝牙已经成功打开", 0).show();
            } else {
                System.out.println("蓝牙未打开");
                Toast.makeText(this, "打开蓝牙时发生错误", 0).show();
                finish();
            }
        } else if (i2 == -1 && intent != null) {
            this.mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Log.d("dong", this.mDevice.getName());
            this.tv_cover.setVisibility(0);
            this.pb_loading.setVisibility(0);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    MainActivity mainActivity = MainActivity.this;
                    BluetoothDevice bluetoothDevice = mainActivity.mDevice;
                    MainActivity mainActivity2 = MainActivity.this;
                    BluetoothGatt unused = mainActivity.mBluetoothGatt = bluetoothDevice.connectGatt(mainActivity2, false, mainActivity2.gattCallback);
                }
            }, 500);
        }
    }

    /* access modifiers changed from: private */
    public void process() {
        while (this.byteListHex.contains(238)) {
            ArrayList arrayList = new ArrayList();
            int size = this.byteListHex.size();
            int i = 0;
            while (true) {
                if (i >= size) {
                    break;
                } else if (this.byteListHex.get(0).intValue() == 238) {
                    arrayList.add(this.byteListHex.get(0));
                    this.byteListHex.remove(0);
                    break;
                } else {
                    arrayList.add(this.byteListHex.get(0));
                    this.byteListHex.remove(0);
                    i++;
                }
            }
            processCommand(arrayList);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processCommand(java.util.List<java.lang.Integer> r14) {
        /*
            r13 = this;
            r0 = 0
            r1 = 0
            r2 = 0
        L_0x0003:
            int r3 = r14.size()
            r4 = 2
            int r3 = r3 - r4
            if (r1 >= r3) goto L_0x0019
            java.lang.Object r3 = r14.get(r1)
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            int r2 = r2 + r3
            int r1 = r1 + 1
            goto L_0x0003
        L_0x0019:
            int r2 = r2 % 100
            int r1 = com.juntek.vat.util.BCDCode.normalDecToHex(r2)
            int r2 = r14.size()
            int r2 = r2 - r4
            java.lang.Object r2 = r14.get(r2)
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
            java.lang.Object r3 = r14.get(r0)
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            r5 = 185(0xb9, float:2.59E-43)
            r6 = 184(0xb8, float:2.58E-43)
            java.lang.String r7 = "history"
            r8 = 20
            r10 = 12
            r11 = 170(0xaa, float:2.38E-43)
            r12 = 1
            if (r3 != r11) goto L_0x0082
            if (r1 == r2) goto L_0x0082
            if (r2 == r10) goto L_0x0082
            int r1 = r14.size()
            int r1 = r1 - r12
            java.lang.Object r14 = r14.get(r1)
            java.lang.Integer r14 = (java.lang.Integer) r14
            r14.intValue()
            java.lang.Thread.sleep(r8)     // Catch:{ Exception -> 0x005d }
            goto L_0x0061
        L_0x005d:
            r14 = move-exception
            r14.printStackTrace()
        L_0x0061:
            com.juntek.vat.util.Command[] r14 = new com.juntek.vat.util.Command[r4]
            com.juntek.vat.util.Command r1 = new com.juntek.vat.util.Command
            int r2 = com.juntek.vat.fragment.CurveFragment.currentSec
            int r2 = r2 / 60
            r1.<init>(r2, r6)
            r14[r0] = r1
            com.juntek.vat.util.Command r0 = new com.juntek.vat.util.Command
            r0.<init>(r12, r5)
            r14[r12] = r0
            byte[] r14 = com.juntek.vat.util.BCDUtil.getCommandBytes(r14)
            r13.sendData((byte[]) r14)
            java.lang.String r14 = "1"
            android.util.Log.d(r7, r14)
            return
        L_0x0082:
            if (r1 == r2) goto L_0x0087
            if (r2 == r10) goto L_0x0087
            return
        L_0x0087:
            int r1 = r14.size()
            int r1 = r1 - r12
            r14.remove(r1)
            int r1 = r14.size()
            int r1 = r1 - r12
            r14.remove(r1)
            java.lang.Object r1 = r14.get(r0)
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r2 = 160(0xa0, float:2.24E-43)
            if (r1 != r11) goto L_0x0121
            r14.remove(r0)
            java.util.Iterator r14 = r14.iterator()
        L_0x00ac:
            r1 = 0
        L_0x00ad:
            boolean r3 = r14.hasNext()
            if (r3 == 0) goto L_0x00d9
            java.lang.Object r3 = r14.next()
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            if (r3 < r2) goto L_0x00d1
            r13.parseOffData(r1, r3)
            int r1 = com.juntek.vat.fragment.CurveFragment.currentSec
            int r3 = com.juntek.vat.fragment.CurveFragment.rightEdgeSec
            if (r1 > r3) goto L_0x00ce
            int r1 = com.juntek.vat.fragment.CurveFragment.currentSec
            int r3 = com.juntek.vat.fragment.CurveFragment.maxSec
            if (r1 <= r3) goto L_0x00ac
        L_0x00ce:
            com.juntek.vat.fragment.CurveFragment.readOk = r12
            return
        L_0x00d1:
            int r1 = r1 * 100
            int r3 = com.juntek.vat.util.BCDCode.hexToDec((int) r3)
            int r1 = r1 + r3
            goto L_0x00ad
        L_0x00d9:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r1 = ""
            r14.append(r1)
            int r1 = com.juntek.vat.fragment.CurveFragment.currentSec
            int r1 = r1 / 60
            r14.append(r1)
            java.lang.String r14 = r14.toString()
            android.util.Log.d(r7, r14)
            java.lang.Thread.sleep(r8)     // Catch:{ Exception -> 0x00f5 }
            goto L_0x00f9
        L_0x00f5:
            r14 = move-exception
            r14.printStackTrace()
        L_0x00f9:
            com.juntek.vat.util.Command[] r14 = new com.juntek.vat.util.Command[r4]
            com.juntek.vat.util.Command r1 = new com.juntek.vat.util.Command
            int r2 = com.juntek.vat.fragment.CurveFragment.currentSec
            int r2 = r2 / 60
            r1.<init>(r2, r6)
            r14[r0] = r1
            com.juntek.vat.util.Command r0 = new com.juntek.vat.util.Command
            r0.<init>(r12, r5)
            r14[r12] = r0
            byte[] r14 = com.juntek.vat.util.BCDUtil.getCommandBytes(r14)
            r13.sendData((byte[]) r14)
            int r14 = com.juntek.vat.fragment.CurveFragment.leftSec
            if (r14 < 0) goto L_0x0147
            com.juntek.vat.MainActivity$12 r14 = new com.juntek.vat.MainActivity$12
            r14.<init>()
            r13.runOnUiThread(r14)
            goto L_0x0147
        L_0x0121:
            r14.remove(r0)
            java.util.Iterator r14 = r14.iterator()
        L_0x0128:
            r1 = 0
        L_0x0129:
            boolean r3 = r14.hasNext()
            if (r3 == 0) goto L_0x0147
            java.lang.Object r3 = r14.next()
            java.lang.Integer r3 = (java.lang.Integer) r3
            int r3 = r3.intValue()
            if (r3 < r2) goto L_0x013f
            r13.parseCommand(r1, r3)
            goto L_0x0128
        L_0x013f:
            int r1 = r1 * 100
            int r3 = com.juntek.vat.util.BCDCode.hexToDec((int) r3)
            int r1 = r1 + r3
            goto L_0x0129
        L_0x0147:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.juntek.vat.MainActivity.processCommand(java.util.List):void");
    }

    private void parseCommand(final int i, final int i2) {
        runOnUiThread(new Runnable() {
            public void run() {
                int i = i2;
                if (i == 165) {
                    MainActivity mainActivity = MainActivity.this;
                    Toast.makeText(mainActivity, MainActivity.this.getResources().getString(C0519R.string.recover) + MainActivity.this.getResources().getString(C0519R.string.success), 1).show();
                } else if (i == 166) {
                    MainActivity mainActivity2 = MainActivity.this;
                    Toast.makeText(mainActivity2, MainActivity.this.getResources().getString(C0519R.string.restart) + MainActivity.this.getResources().getString(C0519R.string.success), 1).show();
                } else if (i == 182) {
                    SettingFragment settingFragment = (SettingFragment) MainActivity.this.fragmentList.get(2);
                    int i2 = i;
                    if (i2 == 0) {
                        i2 = 1;
                    }
                    settingFragment.setIX(i2);
                } else if (i == 183) {
                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setRelayMode(i);
                } else if (i != 240) {
                    switch (i) {
                        case 176:
                            double d = (double) i;
                            Double.isNaN(d);
                            MainActivity.ahMax = d / 10.0d;
                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setAhMax(i);
                            return;
                        case 177:
                            ((SettingFragment) MainActivity.this.fragmentList.get(2)).setOTP(i);
                            return;
                        case 178:
                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setUfine(i);
                            return;
                        case 179:
                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setIfine(i);
                            return;
                        case 180:
                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setTfine(i);
                            return;
                        default:
                            switch (i) {
                                case 192:
                                    ((MainFragment) MainActivity.this.fragmentList.get(0)).setSettingVol(i);
                                    return;
                                case 193:
                                    ((MainFragment) MainActivity.this.fragmentList.get(0)).setSettingCur(i);
                                    return;
                                case 194:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setDt(i);
                                    return;
                                case 195:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setPrt(i);
                                    return;
                                case 196:
                                    ((MainFragment) MainActivity.this.fragmentList.get(0)).setAddress(i);
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setAddress(i);
                                    return;
                                case 197:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setOVP(i);
                                    return;
                                case 198:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setLVP(i);
                                    return;
                                case 199:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setOCP(i);
                                    return;
                                case 200:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setNCP(i);
                                    return;
                                case 201:
                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setOPP(i);
                                    return;
                                default:
                                    switch (i) {
                                        case 208:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setRelay(i);
                                            return;
                                        case 209:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setDirecttion(i);
                                            return;
                                        case 210:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setAHRemain(i);
                                            return;
                                        case 211:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setLjCap(i);
                                            return;
                                        case 212:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setElecConsu(i);
                                            return;
                                        case 213:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setRunTime(i);
                                            return;
                                        case 214:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setLife(i);
                                            return;
                                        case 215:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setRmr(i);
                                            return;
                                        case 216:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setSettingPower(i);
                                            return;
                                        case 217:
                                            ((MainFragment) MainActivity.this.fragmentList.get(0)).setTemp(i);
                                            TextView access$1700 = MainActivity.this.tv_temp;
                                            StringBuilder sb = new StringBuilder();
                                            int i3 = i;
                                            sb.append(i3 + -100 < -20 ? "--" : Integer.valueOf(i3 - 100));
                                            sb.append("℃");
                                            access$1700.setText(sb.toString());
                                            return;
                                        default:
                                            switch (i) {
                                                case 224:
                                                    MainActivity.model = i;
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setSN(MainActivity.model, MainActivity.f50no);
                                                    int i4 = MainActivity.model / 1000;
                                                    MainActivity.vMax = ((MainActivity.model % 1000) / 100) * 100;
                                                    MainActivity.cMax = (MainActivity.model % 100) * 10;
                                                    if (MainActivity.vMax == 100) {
                                                        MainActivity.vMax = 120;
                                                    }
                                                    TextView access$1800 = MainActivity.this.tv_model;
                                                    StringBuilder sb2 = new StringBuilder();
                                                    sb2.append("KG");
                                                    sb2.append(MainActivity.model % 1000);
                                                    sb2.append(i4 == 1 ? "H" : "F");
                                                    access$1800.setText(sb2.toString());
                                                    if (MainActivity.this.tv_model.getText().toString().contains("F")) {
                                                        ((SettingFragment) MainActivity.this.fragmentList.get(2)).setIXShow(false);
                                                    } else {
                                                        ((SettingFragment) MainActivity.this.fragmentList.get(2)).setIXShow(true);
                                                    }
                                                    ((MainFragment) MainActivity.this.fragmentList.get(0)).refreshVol();
                                                    ((MainFragment) MainActivity.this.fragmentList.get(0)).refreshCur();
                                                    return;
                                                case 225:
                                                    int i5 = i;
                                                    if (i5 == 8629) {
                                                        MainActivity.this.IAP = 1;
                                                        return;
                                                    }
                                                    MainActivity.f50no = i5;
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setSN(MainActivity.model, MainActivity.f50no);
                                                    return;
                                                case 226:
                                                    MainActivity mainActivity3 = MainActivity.this;
                                                    mainActivity3.localFirm = i;
                                                    ((SettingFragment) mainActivity3.fragmentList.get(2)).setFirm(MainActivity.versionFirm, MainActivity.this.localFirm);
                                                    ((AboutUsFragment) MainActivity.this.fragmentList.get(3)).firminfo(MainActivity.this.localFirm);
                                                    return;
                                                case 227:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setLTP(i);
                                                    return;
                                                case 228:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setFDBS(i);
                                                    return;
                                                case 229:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setRTU(i);
                                                    return;
                                                case 230:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setFV(i);
                                                    return;
                                                case 231:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setLV(i);
                                                    return;
                                                case 232:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setFC(i);
                                                    return;
                                                case 233:
                                                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setDtime(i);
                                                    return;
                                                default:
                                                    return;
                                            }
                                    }
                            }
                    }
                } else {
                    ((SettingFragment) MainActivity.this.fragmentList.get(2)).setBaudRate(i);
                }
            }
        });
    }

    public void process(byte[] bArr) {
        Log.d("dong", "process");
        if (this.dataType == 1) {
            for (byte valueOf : bArr) {
                this.byteList.add(Byte.valueOf(valueOf));
            }
            byte[] bArr2 = new byte[this.byteList.size()];
            for (int i = 0; i < this.byteList.size(); i++) {
                bArr2[i] = this.byteList.get(i).byteValue();
            }
            try {
                String str = new String(bArr2, "UTF-8");
                if (str.contains(":r50=") && str.endsWith("\r\n")) {
                    try {
                        initMain(str);
                        ((MainFragment) this.fragmentList.get(0)).initMain(str);
                        ((CurveFragment) this.fragmentList.get(1)).initCurve(str);
                        Log.d("dong", "tiber");
                        this.byteList.clear();
                        this.dataType = 0;
                        if (this.semaphore.availablePermits() <= 0) {
                            this.semaphore.release();
                            Log.d("dongsuo", "50 release");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.byteList.clear();
                        this.dataType = 0;
                        if (this.semaphore.availablePermits() <= 0) {
                            this.semaphore.release();
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
                this.byteList.clear();
                this.dataType = 0;
                if (this.semaphore.availablePermits() <= 0) {
                    this.semaphore.release();
                    Log.d("dongsuo", "50 release");
                }
                throw th;
            }
        } else if (this.dataType == 2) {
            for (byte valueOf2 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf2));
            }
            byte[] bArr3 = new byte[this.byteList.size()];
            for (int i2 = 0; i2 < this.byteList.size(); i2++) {
                bArr3[i2] = this.byteList.get(i2).byteValue();
            }
            try {
                String str2 = new String(bArr3, "UTF-8");
                if (str2.contains(":r00=") && str2.endsWith("\r\n")) {
                    initModel(str2);
                    this.byteList.clear();
                    this.dataType = 0;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } else if (this.dataType == 3) {
            for (byte valueOf3 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf3));
            }
            byte[] bArr4 = new byte[this.byteList.size()];
            for (int i3 = 0; i3 < this.byteList.size(); i3++) {
                bArr4[i3] = this.byteList.get(i3).byteValue();
            }
            try {
                String str3 = new String(bArr4, "UTF-8");
                if (str3.contains(":r51=") && str3.endsWith("\r\n")) {
                    initSetting(str3);
                    this.byteList.clear();
                    this.dataType = 0;
                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        } else if (this.dataType == 4) {
            for (byte valueOf4 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf4));
            }
            byte[] bArr5 = new byte[this.byteList.size()];
            for (int i4 = 0; i4 < this.byteList.size(); i4++) {
                bArr5[i4] = this.byteList.get(i4).byteValue();
            }
            try {
                String str4 = new String(bArr5, "UTF-8");
                if (str4.contains(":w10=") && str4.endsWith("\r\n")) {
                    this.byteList.clear();
                    this.dataType = 0;
                    if (this.semaphore.availablePermits() <= 0) {
                        this.semaphore.release();
                        Log.d("dongsuo", "main fragment release");
                    }
                    showProgress(false);
                }
            } catch (Exception e5) {
                e5.printStackTrace();
            }
        } else if (this.dataType == 5) {
            for (byte valueOf5 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf5));
            }
            byte[] bArr6 = new byte[this.byteList.size()];
            for (int i5 = 0; i5 < this.byteList.size(); i5++) {
                bArr6[i5] = this.byteList.get(i5).byteValue();
            }
            try {
                String str5 = new String(bArr6, "UTF-8");
                if (str5.contains(":r53=") && str5.endsWith("\r\n")) {
                    initLineData(str5);
                    this.byteList.clear();
                    if (CurveFragment.offDatas[CurveFragment.rightEdgeSec - 1] == null || CurveFragment.offDatas[CurveFragment.rightEdgeSec] == null) {
                        sendData(":R53=1," + ((CurveFragment.currentSec % 255) + 1) + "," + CurveFragment.currentSec + ",\n");
                        return;
                    }
                    this.dataType = 0;
                    CurveFragment.readOk = true;
                }
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        } else if (this.dataType == 11) {
            for (byte valueOf6 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf6));
            }
            byte[] bArr7 = new byte[this.byteList.size()];
            for (int i6 = 0; i6 < this.byteList.size(); i6++) {
                bArr7[i6] = this.byteList.get(i6).byteValue();
            }
            try {
                String str6 = new String(bArr7, "UTF-8");
                if (str6.contains(":r53=") && str6.endsWith("\r\n")) {
                    initOffData(str6);
                    this.byteList.clear();
                    if (CurveFragment.currentSec > CurveFragment.rightEdgeSec) {
                        this.dataType = 0;
                        CurveFragment.readOk = true;
                        return;
                    }
                    sendData(":R53=1," + ((CurveFragment.currentSec % 255) + 1) + "," + CurveFragment.currentSec + ",\n");
                    double d = (double) (CurveFragment.currentSec - CurveFragment.leftSec);
                    Double.isNaN(d);
                    double d2 = d * 1.0d;
                    double d3 = (double) (CurveFragment.rightEdgeSec - CurveFragment.leftSec);
                    Double.isNaN(d3);
                    showProgressValue(true, (int) ((d2 / d3) * 100.0d));
                }
            } catch (Exception e7) {
                e7.printStackTrace();
            }
        } else if (this.dataType == 6) {
            for (byte valueOf7 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf7));
            }
            byte[] bArr8 = new byte[this.byteList.size()];
            for (int i7 = 0; i7 < this.byteList.size(); i7++) {
                bArr8[i7] = this.byteList.get(i7).byteValue();
            }
            try {
                String str7 = new String(bArr8, "UTF-8");
                if (str7.contains(":w61=") && str7.endsWith("\r\n")) {
                    this.byteList.clear();
                    this.dataType = 0;
                    if (this.semaphore.availablePermits() <= 0) {
                        this.semaphore.release();
                        Log.d("dongsuo", "main fragment release");
                    }
                }
            } catch (Exception e8) {
                e8.printStackTrace();
            }
        } else if (this.dataType == 7) {
            for (byte valueOf8 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf8));
            }
            byte[] bArr9 = new byte[this.byteList.size()];
            for (int i8 = 0; i8 < this.byteList.size(); i8++) {
                bArr9[i8] = this.byteList.get(i8).byteValue();
            }
            try {
                String str8 = new String(bArr9, "UTF-8");
                if (str8.contains(":w28=") && str8.endsWith("\r\n")) {
                    this.byteList.clear();
                    this.dataType = 0;
                    ahMax = ahMaxTemp;
                    ((MainFragment) this.fragmentList.get(0)).setYuShe();
                    if (this.semaphore.availablePermits() <= 0) {
                        this.semaphore.release();
                        Log.d("dongsuo", "main fragment release");
                    }
                }
            } catch (Exception e9) {
                e9.printStackTrace();
            }
        } else if (this.dataType == 8) {
            for (byte valueOf9 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf9));
            }
            byte[] bArr10 = new byte[this.byteList.size()];
            for (int i9 = 0; i9 < this.byteList.size(); i9++) {
                bArr10[i9] = this.byteList.get(i9).byteValue();
            }
            try {
                String str9 = new String(bArr10, "UTF-8");
                if (!str9.contains(":w34=")) {
                    if (!str9.contains(":w62=") && !str9.contains(":w60=") && !str9.contains(":w20=") && !str9.contains(":w21=") && !str9.contains(":w22=") && !str9.contains(":w23=") && !str9.contains(":w24=") && !str9.contains(":w25=") && !str9.contains(":w26=") && !str9.contains(":w29=") && !str9.contains(":w30=") && !str9.contains(":w31=") && !str9.contains(":w36=") && !str9.contains(":w27=") && !str9.contains(":w01=")) {
                        return;
                    }
                }
                if (str9.endsWith("\r\n")) {
                    if (str9.contains(":w62=")) {
                        ((CurveFragment) this.fragmentList.get(1)).clearData();
                    }
                    this.byteList.clear();
                    this.dataType = 0;
                    if (this.semaphore.availablePermits() <= 0) {
                        this.semaphore.release();
                        Log.d("dongsuo", "main fragment release");
                    }
                }
            } catch (Exception e10) {
                e10.printStackTrace();
            }
        } else if (this.dataType == 9) {
            for (byte valueOf10 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf10));
            }
            byte[] bArr11 = new byte[this.byteList.size()];
            for (int i10 = 0; i10 < this.byteList.size(); i10++) {
                bArr11[i10] = this.byteList.get(i10).byteValue();
            }
            try {
                String str10 = new String(bArr11, "UTF-8");
                if (str10.contains(":r51=") && str10.endsWith("\r\n")) {
                    initPreSetting(str10);
                    this.byteList.clear();
                    this.dataType = 0;
                }
            } catch (Exception e11) {
                e11.printStackTrace();
            }
        } else if (this.dataType == 10) {
            for (byte valueOf11 : bArr) {
                this.byteList.add(Byte.valueOf(valueOf11));
            }
            byte[] bArr12 = new byte[this.byteList.size()];
            for (int i11 = 0; i11 < this.byteList.size(); i11++) {
                bArr12[i11] = this.byteList.get(i11).byteValue();
            }
            try {
                String str11 = new String(bArr12, "UTF-8");
                if (str11.contains(":r01=") && str11.endsWith("\r\n")) {
                    initAddress(str11);
                    this.byteList.clear();
                    this.dataType = 0;
                }
            } catch (Exception e12) {
                e12.printStackTrace();
            }
        }
    }

    private void parseOffData(int i, int i2) {
        if (i2 == 160) {
            this.vol = i;
        } else if (i2 == 161) {
            double d = (double) (i / 10);
            double pow = Math.pow(10.0d, (double) (i % 10));
            Double.isNaN(d);
            int i3 = -((int) (d * pow));
            float f = ((float) i3) / 100.0f;
            LineView.Data data = new LineView.Data(CurveFragment.currentSec, (((float) this.vol) / 100.0f) / ((float) vMax), f / ((float) cMax), true);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            decimalFormat.setRoundingMode(RoundingMode.DOWN);
            data.setVolStr(decimalFormat.format(new BigDecimal(this.vol).divide(new BigDecimal("100"))) + "V");
            data.setCurStr(decimalFormat.format(new BigDecimal(i3).divide(new BigDecimal("100"))) + "A");
            data.setRealVol(((float) this.vol) / 100.0f);
            data.setRealCur(f);
            if (data.getTime() == 92574) {
                Log.d("", "");
            }
            if (CurveFragment.currentSec <= CurveFragment.maxSec && CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data.getTime()] == null) {
                CurveFragment.offDatas[data.getTime()] = data;
            }
            CurveFragment.currentSec++;
        } else if (i2 == 162) {
            double d2 = (double) (i / 10);
            double pow2 = Math.pow(10.0d, (double) (i % 10));
            Double.isNaN(d2);
            int i4 = (int) (d2 * pow2);
            float f2 = ((float) i4) / 100.0f;
            LineView.Data data2 = new LineView.Data(CurveFragment.currentSec, (((float) this.vol) / 100.0f) / ((float) vMax), f2 / ((float) cMax), true);
            DecimalFormat decimalFormat2 = new DecimalFormat("0.00");
            decimalFormat2.setRoundingMode(RoundingMode.DOWN);
            data2.setVolStr(decimalFormat2.format(new BigDecimal(this.vol).divide(new BigDecimal("100"))) + "V");
            data2.setCurStr(decimalFormat2.format(new BigDecimal(i4).divide(new BigDecimal("100"))) + "A");
            data2.setRealVol(((float) this.vol) / 100.0f);
            data2.setRealCur(f2);
            if (data2.getTime() == 92574) {
                Log.d("", "");
            }
            if (CurveFragment.currentSec <= CurveFragment.maxSec && CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data2.getTime()] == null) {
                CurveFragment.offDatas[data2.getTime()] = data2;
            }
            CurveFragment.currentSec++;
        }
    }

    private void initOffData(String str) {
        String[] split = str.split(",");
        int i = 0;
        for (int i2 = 2; i2 < split.length - 1; i2++) {
            i += Integer.parseInt(split[i2]);
        }
        if (Integer.parseInt(split[1]) != (i % 255) + 1) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.sendData(":R53=1," + ((CurveFragment.currentSec % 255) + 1) + "," + CurveFragment.currentSec + ",\n");
                }
            }, 100);
            return;
        }
        for (int i3 = 2; i3 < split.length - 2; i3 += 2) {
            int parseInt = Integer.parseInt(split[i3]);
            int parseInt2 = Integer.parseInt(split[i3 + 1]);
            if (parseInt > 42000) {
                LineView.Data data = new LineView.Data(CurveFragment.currentSec, 0.0f, 0.0f, false);
                if (CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data.getTime()] == null) {
                    CurveFragment.offDatas[data.getTime()] = data;
                }
                CurveFragment.currentSec++;
            } else {
                int i4 = parseInt2 - 50000;
                LineView.Data data2 = new LineView.Data(CurveFragment.currentSec, (((float) parseInt) / 100.0f) / ((float) vMax), (((float) i4) / 100.0f) / ((float) cMax), true);
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                data2.setVolStr(decimalFormat.format(new BigDecimal(parseInt).divide(new BigDecimal("100"))) + "V");
                data2.setCurStr(decimalFormat.format(new BigDecimal(i4).divide(new BigDecimal("100"))) + "A");
                if (CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data2.getTime()] == null) {
                    CurveFragment.offDatas[data2.getTime()] = data2;
                }
                CurveFragment.currentSec++;
            }
        }
    }

    private void initLineData(String str) {
        String[] split = str.split(",");
        int i = 0;
        for (int i2 = 2; i2 < split.length - 1; i2++) {
            i += Integer.parseInt(split[i2]);
        }
        if (Integer.parseInt(split[1]) != (i % 255) + 1) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.sendData(":R53=1," + ((CurveFragment.currentSec % 255) + 1) + "," + CurveFragment.currentSec + ",\n");
                }
            }, 100);
            return;
        }
        for (int i3 = 2; i3 < split.length - 2; i3 += 2) {
            int parseInt = Integer.parseInt(split[i3]);
            int parseInt2 = Integer.parseInt(split[i3 + 1]);
            if (parseInt > 42000) {
                LineView.Data data = new LineView.Data(CurveFragment.currentSec, 0.0f, 0.0f, false);
                if (CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data.getTime()] == null) {
                    CurveFragment.offDatas[data.getTime()] = data;
                }
                CurveFragment.currentSec++;
            } else {
                int i4 = parseInt2 - 50000;
                LineView.Data data2 = new LineView.Data(CurveFragment.currentSec, (((float) parseInt) / 100.0f) / ((float) vMax), (((float) i4) / 100.0f) / ((float) cMax), true);
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                decimalFormat.setRoundingMode(RoundingMode.DOWN);
                data2.setVolStr(decimalFormat.format(new BigDecimal(parseInt).divide(new BigDecimal("100"))) + "V");
                data2.setCurStr(decimalFormat.format(new BigDecimal(i4).divide(new BigDecimal("100"))) + "A");
                if (CurveFragment.currentSec <= CurveFragment.rightEdgeSec && CurveFragment.offDatas[data2.getTime()] == null) {
                    CurveFragment.offDatas[data2.getTime()] = data2;
                }
                CurveFragment.currentSec++;
            }
        }
    }

    private void initModel(String str) {
        String[] split = str.split(",");
        int parseInt = Integer.parseInt(split[2]) % 255;
        Integer.parseInt(split[1]);
        TextView textView = this.tv_model;
        textView.setText("KG-" + split[2]);
        vMax = Integer.parseInt(split[2].substring(1, 2)) * 100;
        cMax = Integer.parseInt(split[2].substring(2)) * 10;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                MainActivity mainActivity = MainActivity.this;
                mainActivity.dataType = 3;
                mainActivity.able = true;
                mainActivity.sendData(":R51=1,\n");
            }
        }, 200);
    }

    private void initSetting(String str) {
        String[] split = str.split(",");
        int i = 0;
        for (int i2 = 2; i2 < split.length - 1; i2++) {
            i += Integer.parseInt(split[i2]);
        }
        if (Integer.parseInt(split[1]) != (i % 255) + 1) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.dataType = 3;
                    mainActivity.sendData(":R51=1,\n");
                }
            }, 200);
            return;
        }
        double parseInt = (double) Integer.parseInt(split[10]);
        Double.isNaN(parseInt);
        ahMax = parseInt / 10.0d;
        ((MainFragment) this.fragmentList.get(0)).setYuShe();
        this.tv_cover.setVisibility(8);
        this.pb_loading.setVisibility(8);
        new Timer().schedule(new TimerTask() {
            public void run() {
                Thread unused = MainActivity.this.zhuThread = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                MainActivity.this.semaphore.acquire();
                                Log.d("dongsuo", "50 acquire");
                                MainActivity.this.dataType = 1;
                                MainActivity.this.sendData(":R50=1,\n");
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                return;
                            }
                        }
                    }
                });
                MainActivity.this.zhuThread.start();
            }
        }, 200);
    }

    private void initMain(String str) {
        String[] split = str.split(",");
        split[0] = split[0].substring(split[0].indexOf(61) + 1);
        int parseInt = Integer.parseInt(split[1]);
        int i = 0;
        for (int i2 = 2; i2 < 14; i2++) {
            i += Integer.parseInt(split[i2]);
        }
        if ((i % 255) + 1 == parseInt) {
            String format = new DecimalFormat("00").format((long) Integer.parseInt(split[0]));
            TextView textView = this.tv_address;
            textView.setText("P" + format);
            int parseInt2 = Integer.parseInt(split[8]) + -100;
            Integer.parseInt(split[9]);
            TextView textView2 = this.tv_temp;
            StringBuilder sb = new StringBuilder();
            sb.append(parseInt2 < -20 ? "--" : Integer.valueOf(parseInt2));
            sb.append("℃");
            textView2.setText(sb.toString());
        }
    }

    private void initPreSetting(String str) {
        String[] split = str.split(",");
        int i = 0;
        for (int i2 = 2; i2 < split.length - 1; i2++) {
            i += Integer.parseInt(split[i2]);
        }
        if (Integer.parseInt(split[1]) != (i % 255) + 1) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.dataType = 9;
                    mainActivity.sendData(":R51=1,\n");
                }
            }, 200);
            return;
        }
        try {
            ((SettingFragment) this.fragmentList.get(2)).initData(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.semaphore.availablePermits() <= 0) {
            this.semaphore.release();
            Log.d("dongsuo", "main fragment release");
        }
    }

    private void initAddress(String str) {
        try {
            ((SettingFragment) this.fragmentList.get(2)).initAddress(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.semaphore.availablePermits() <= 0) {
            this.semaphore.release();
            Log.d("dongsuo", "main fragment release");
        }
    }

    public void showProgress(boolean z) {
        if (z) {
            this.tv_cover.setVisibility(0);
            this.pb_loading.setVisibility(0);
            return;
        }
        this.tv_cover.setVisibility(8);
        this.pb_loading.setVisibility(8);
    }

    public void showProgressValue(boolean z, int i) {
        if (z) {
            this.tv_cover.setVisibility(0);
            findViewById(C0519R.C0521id.ll_progress).setVisibility(0);
            this.pb_progress.setProgress(i);
            return;
        }
        this.tv_cover.setVisibility(8);
        findViewById(C0519R.C0521id.ll_progress).setVisibility(8);
    }

    public boolean isBlueToothAble() {
        return this.able && this.mBluetoothGatt != null;
    }

    public void onDestroy() {
        Log.d("dong", "onDestroy");
        duankai = false;
        this.destroyed = true;
        disconnect();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void disconnect() {
        Thread thread = this.zhuThread;
        if (thread != null) {
            thread.interrupt();
        }
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
            Log.d("dong", "手动断开");
        }
    }

    public static void restartActivity(Activity activity) {
        duankai = true;
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }
}
