package com.javadsh.final_project_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity
{
    private Handler mHandler;
    DataBaseHelper db ;
    private SimpleCursorAdapter dataAdapter;
    ListView listView;
    private static final int REQUEST_BLUETOOTH = 1;
    ArrayList<String> DEVICE_ADDRESS = new ArrayList<String>();
    ArrayList<View> OutView = new ArrayList<View>();
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static boolean updating = true;
    boolean wait=true;
    int status;
    byte buffer[];
    boolean stopThread;
    ProgressBar progressBar;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent Add_device = new Intent(getApplicationContext(),Add_Device.class);
                updating=false;
                startActivity(Add_device);
            }
        });
        mHandler = new Handler();
        listView = (ListView) findViewById(R.id.DeviceList);
        fillListView();

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RelativeLayout rv = (RelativeLayout)view;
                ToggleButton toggleButton = (ToggleButton)rv.getChildAt(1);

                TextView textView = (TextView)rv.getChildAt(0);
                String id = textView.getText().toString();
                int state = db.fetchStatusByMacId(id);
                if(state==1)
                {
                    off(id);
                    toggleButton.setChecked(false);
                }
                else if(state==0)
                {
                    on(id);
                    toggleButton.setChecked(true);
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                RelativeLayout rv = (RelativeLayout)view;
                TextView textView = (TextView)rv.getChildAt(0);
                final String mac_id = textView.getText().toString();

                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View promptView = layoutInflater.inflate(R.layout.popup3, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(promptView);

                // setup a dialog window
                alertDialogBuilder.setCancelable(true)
                        .setPositiveButton("تعویض نام", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                changename(mac_id);
                            }
                        })
                        .setNegativeButton("تعویض رمز",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        changepass(mac_id);
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
                return true;
            }
        });


        Cursor cursor = db.fetchBtDevices();
        if(cursor.moveToFirst())
        {
            DEVICE_ADDRESS.clear();
            for(int l = 0 ; l < cursor.getCount();l++)
            {
                DEVICE_ADDRESS.add(cursor.getString(cursor.getColumnIndex("device_mac_id")));
                cursor.moveToNext();
            }
        }
        BTinit();

        periodicUpdate.run();
        try {
            refresh();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private Runnable periodicUpdate = new Runnable () {
        public void run() {
            // scheduled another events to be in 10 seconds later
            mHandler.postDelayed(periodicUpdate,1000);
            // below is whatever you want to do
            progressBar.setProgress(progress++);
            if(progress==15)
            {
                try {
                if(updating)
                {
                    refresh();
                }
                }
                catch (Exception e)
                {

                }
            }
            else if(progress==30)
            {
                progress=0;
                Toast.makeText(getApplicationContext(),"به روزرسانی شروع شد",Toast.LENGTH_SHORT).show();
                try {
                    if(updating)
                    {
                        refresh();
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"آپدیت نشد",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public void AllOn()
    {
        for(int i=0; i<listView.getCount();i++)
        {
            View view = listView.getChildAt(i);
            RelativeLayout rv = (RelativeLayout)view;
            ToggleButton toggleButton = (ToggleButton)rv.getChildAt(1);
            TextView textView = (TextView)rv.getChildAt(0);
            String id = textView.getText().toString();
            on(id);
            toggleButton.setChecked(true);
        }
    }

    public void on(String mac_id)
    {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice iterator : bondedDevices) {
            boolean found = false;
            if (iterator.getAddress().equals(mac_id)) {
                device = iterator;
                found = true;
            }
            if (found == true) {
                BTconnect();
                String string = "1";
                string.concat("\n");
                try {
                    outputStream.write(string.getBytes());
                    BtDevices btDevices = new BtDevices(device.getAddress(), device.getName(),1);
                    db.updateDevice(btDevices);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Toast.makeText(this,"روشن شد",Toast.LENGTH_SHORT).show();
        }
    }
    public void AllOff()
    {
        for(int i=0; i<listView.getCount();i++)
        {
            View view = listView.getChildAt(i);
            RelativeLayout rv = (RelativeLayout)view;
            ToggleButton toggleButton = (ToggleButton)rv.getChildAt(1);
            TextView textView = (TextView)rv.getChildAt(0);
            String id = textView.getText().toString();
            off(id);
            toggleButton.setChecked(false);
        }
    }
    public void off(String mac_id)
    {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice iterator : bondedDevices) {
            boolean found = false;
            if (iterator.getAddress().equals(mac_id)) {
                device = iterator;
                found = true;
            }
            if (found == true) {
                BTconnect();
                String string = "2";
                string.concat("\n");
                try {
                    outputStream.write(string.getBytes());
                    BtDevices btDevices = new BtDevices(device.getAddress(), device.getName(),0);
                    db.updateDevice(btDevices);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(this, "خاموش شد", Toast.LENGTH_SHORT).show();
        }
    }

    public void changename(final String mac_id)
    {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogUserInput1);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {

                        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                        View promptView = layoutInflater.inflate(R.layout.custom_popup, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(promptView);

                        // setup a dialog window
                        alertDialogBuilder.setCancelable(true)
                                .setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        String str="4AT+NAME"+editText.getText().toString()+'9';
                                        send(str,mac_id);
                                        Add_Device.Delete(mac_id);
                                        db.deleteFromDevices(null,mac_id);
                                        fillListView();
                                    }
                                })
                                .setNegativeButton("لغو",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                        // create an alert dialog
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();

                    }
                })
                .setNegativeButton("لغو",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void send(String str,String mac_id)
    {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice iterator : bondedDevices) {
            boolean found = false;
            if (iterator.getAddress().equals(mac_id)) {
                device = iterator;
                found = true;
            }
            if (found == true) {
                wait = true;
                BTconnect();
                String string = str;
                string.concat("\n");
                try {
                    outputStream.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException
    {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    public void changepass(final String mac_id)
    {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.popup2, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogUserInput1);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {

                        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                        View promptView = layoutInflater.inflate(R.layout.custom_popup, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(promptView);

                        // setup a dialog window
                        alertDialogBuilder.setCancelable(true)
                                .setPositiveButton("تایید", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        String str="4AT+PIN"+editText.getText().toString()+'9';
                                        send(str,mac_id);
                                        Add_Device.Delete(mac_id);
                                        db.deleteFromDevices(null,mac_id);
                                        fillListView();
                                    }
                                })
                                .setNegativeButton("لغو",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                        // create an alert dialog
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();

                    }
                })
                .setNegativeButton("لغو",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void refresh() throws InterruptedException {
        //////////////////////////////////////////////////////////////new refresh

        for (int pos = listView.getFirstVisiblePosition(); pos < listView.getFirstVisiblePosition() + listView.getChildCount(); pos++) {
            //View rlv = listView.getAdapter().getView(pos, null, listView);

            View rlv = listView.getChildAt(pos);
            RelativeLayout rv = (RelativeLayout) rlv;
            TextView textView = (TextView) rv.getChildAt(0);
            String id = textView.getText().toString();
            ToggleButton toggleButton = (ToggleButton) rv.getChildAt(1);


            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice iterator : bondedDevices) {
                boolean found = false;
                if (iterator.getAddress().equals(id)) {
                    device = iterator;
                    found = true;
                }
                if (found == true) {
                    wait = true;
                    BTconnect();
                    String string = "3";
                    string.concat("\n");
                    try {
                        outputStream.write(string.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    beginListenForData();
                    BtDevices btDevices = new BtDevices(device.getAddress(), device.getName(), status);
                    db.updateDevice(btDevices);
                }

                if (status == 0) {
                    toggleButton.setChecked(false);
                } else if (status == 1) {
                    toggleButton.setChecked(true);
                }
            }
        }
    }


    public void BTinit()
    {
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "دستگاه از بلوتوث پشتیبانی نمیکند", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            while (!bluetoothAdapter.isEnabled()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}
    }



    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            Toast.makeText(getApplicationContext(),"وصل شد",Toast.LENGTH_SHORT).show();
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return connected;
    }


    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[8];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            handler.post(new Runnable() {
                                public void run()
                                {
                                    if (string.equals("0"))
                                    {
                                        status = 0 ;
                                    }
                                    else
                                    {
                                        status= 1 ;
                                    }
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });
        thread.start();

    }



    public void fillListView()
    {
        db = new DataBaseHelper(getApplicationContext());


        Cursor cursor = db.fetchBtDevices();

        String[] columns = new String[]{
                "device_name",
                "device_mac_id",
        };

        int[] to = new int[] {
                R.id.device_name,
                R.id.device_macid,
        };

        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.device_info,
                cursor,
                columns,
                to,
                0);


        listView.invalidateViews();
        // Assign adapter to ListView
        try{
            listView.setAdapter(dataAdapter);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "هنوز دستگاهی متصل نشده است", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.on) {
            AllOn();
            return true;
        }
        else if (id == R.id.off) {
            AllOff();
            return true;
        }
        else{
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return true;
        }
    }
    protected void onResume()
    {
        super.onResume();
        updating=true;
        fillListView();
    }
}
