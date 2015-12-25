package lec.chessproto;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceChooser extends AppCompatActivity {

    private BluetoothService btService;

    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    private ArrayAdapter<String> arrayAdapter;

    private MenuItem progressBar;
    private int devicesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        Button enableDiscoverable = (Button) findViewById(R.id.enable_discoverable);
        enableDiscoverable.setOnClickListener(onEnableDiscoverable);

        Button scanForDevices = (Button) findViewById(R.id.scan_for_devices);
        scanForDevices.setOnClickListener(onScanForDevices);

        ListView deviceList = (ListView) findViewById(R.id.device_list);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.device);
        deviceList.setAdapter(arrayAdapter);
        deviceList.setOnItemClickListener(onItemClickListener);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(DeviceChooser.class, getString(R.string.chat_name));
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            btService.registerActivity(DeviceChooser.class, getString(R.string.chat_name));

            btService.setOnConnected(new BluetoothService.OnConnected() {
                @Override
                public void success() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DeviceChooser.this.setResult(RESULT_OK);
                            DeviceChooser.this.finish();
                        }
                    });
                }
            });
            initBt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        progressBar = menu.findItem(R.id.menu_item_progress_bar);
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_chooser, menu);
        progressBar = menu.findItem(R.id.menu_item_progress_bar);
        return true;
    }

    private void initBt() {
        Set<BluetoothDevice> paired = btService.getBluetoothAdapter().getBondedDevices();
        for (BluetoothDevice device : paired) {
            arrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        btService.startAcceptThread();
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener onEnableDiscoverable = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
                    REQUEST_ENABLE_DISCOVERABLE);
        }
    };

    private View.OnClickListener onScanForDevices = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            arrayAdapter.clear();
            btService.getBluetoothAdapter().startDiscovery();

            progressBar.setVisible(true);
            devicesFound = 0;
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            btService.getBluetoothAdapter().cancelDiscovery();

            String str = ((TextView) view).getText().toString();
            String address = str.substring(str.length() - 17); // Length of address

            makeToast(address);

            btService.startConnectThread(address);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                ++devicesFound;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                progressBar.setVisible(false);
                makeToast(Integer.toString(devicesFound) +
                        " " + getString(R.string.n_devices_found_suffix));
            }
        }
    };

    @Override
    protected void onStop() {
        if (btService != null) {
            btService.unregisterActivity();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (btService.getBluetoothAdapter() != null) {
            btService.getBluetoothAdapter().cancelDiscovery();
        }

        unregisterReceiver(broadcastReceiver);

        unbindService(connection);

        super.onDestroy();
    }
}
