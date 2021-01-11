package com.javadsh.final_project_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class Add_Device extends AppCompatActivity
{
    ListView listViewPaired;
    ListView listViewDetected;
    static BluetoothDevice bdDevice;
    public static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    TextView textviewtmp;
    static ArrayList<String> arrayListpaired;
    ArrayAdapter<String> detectedAdapter;
    static CustomAdapter adapter;
    static ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    static ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    ListItemClicked listItemClicked;
    static DataBaseHelper dataBaseHelper;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__device);
        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        listViewDetected = (ListView) findViewById(R.id.listViewDetected);
        listViewPaired = (ListView) findViewById(R.id.listViewPaired);

        arrayListpaired = new ArrayList<String>();


        detectedAdapter = new ArrayAdapter<String>(Add_Device.this, android.R.layout.simple_selectable_list_item);
        listViewDetected.setAdapter(detectedAdapter);

        adapter= new CustomAdapter(arrayListpaired,this);
        listViewPaired.setAdapter(adapter);
        detectedAdapter.notifyDataSetChanged();


        listItemClicked = new ListItemClicked();
        listViewDetected.setOnItemClickListener(listItemClicked);



        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);
        dataBaseHelper = new DataBaseHelper(getApplicationContext());
        getPairedDevices();
        mHandler = new Handler();


    }

    public void scan(View view)
    {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "دستگاه از بلوتوث پشتیبانی نمیکند", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            while (!bluetoothAdapter.isEnabled())
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        arrayListpaired.clear();
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        Log.i("Log", "in the start searching method");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(myReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();
        getPairedDevices();
    }

    private void getPairedDevices() {
        arrayListpaired.clear();
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                arrayListpaired.add(device.getName()+"\n"+device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public static void Delete(int position) {
        bdDevice = arrayListPairedBluetoothDevices.get(position);
        try {
            Boolean removeBonding = removeBond(bdDevice);
            if(removeBonding)
            {
                arrayListpaired.remove(position);
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataBaseHelper.deleteFromDevices(bdDevice.getName());
    }

    public static void Delete(String mac_id) {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice iterator : bondedDevices) {
            if (iterator.getAddress().equals(mac_id)) {
                bdDevice = iterator;
            }
        }
        try {
            Boolean removeBonding = removeBond(bdDevice);
            if(removeBonding)
            {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void add(int position)
    {
        bdDevice = arrayListPairedBluetoothDevices.get(position);
        Cursor tmp  = dataBaseHelper.fetchByNameDevice(bdDevice.getName());
        if(tmp.getCount()==0)
        {
            String deviceName = bdDevice.getName();
            String deviceHardwareAddress = bdDevice.getAddress();
            BtDevices btDevices = new BtDevices(deviceHardwareAddress,deviceName,0);
            dataBaseHelper.createDevices(btDevices);
        }

    }


    class ListItemClicked implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            bdDevice = arrayListBluetoothDevices.get(position);
            try {
                createBond(bdDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public boolean createBond(BluetoothDevice btDevice) throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public static boolean removeBond(BluetoothDevice btDevice) throws Exception
    {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }



    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Toast.makeText(context, "دستگاه جدید یافت شد", Toast.LENGTH_SHORT).show();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try
                {
                    device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if(arrayListBluetoothDevices.size()<1) // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the array list.
                    detectedAdapter.add(device.getName()+"\n"+device.getAddress());
                    arrayListBluetoothDevices.add(device);
                    detectedAdapter.notifyDataSetChanged();
                }
                else
                {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for(int i = 0; i<arrayListBluetoothDevices.size();i++)
                    {
                        if(device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress()))
                        {
                            flag = false;
                        }
                    }
                    if(flag == true)
                    {
                        detectedAdapter.add(device.getName()+"\n"+device.getAddress());
                        arrayListBluetoothDevices.add(device);
                        detectedAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    protected void onStop() {
        super.onStop();
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
