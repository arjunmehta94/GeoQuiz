package com.arjun.android.geoquiz;


 
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
  
import com.example.bluetooth1.R;
  
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
  
public class MainActivity extends Activity {
  private static final String TAG = "bluetooth1";
    
  Button btnOn, btnOff;
    
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
    
  // SPP UUID service 
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  
  // MAC-address of Bluetooth module (you must edit this line)
  private static String address = "00:15:FF:F2:19:5F";
    
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  
    setContentView(R.layout.activity_main);
  
    btnOn = (Button) findViewById(R.id.btnOn);
    btnOff = (Button) findViewById(R.id.btnOff);
      
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();
  
    btnOn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendData("1");
        Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
      }
    });
  
    btnOff.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sendData("0");
        Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
      }
    });
  }
   
  private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
      if(Build.VERSION.SDK_INT >= 10){
          try {
              final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
              return (BluetoothSocket) m.invoke(device, MY_UUID);
          } catch (Exception e) {
              Log.e(TAG, "Could not create Insecure RFComm Connection",e);
          }
      }
      return  device.createRfcommSocketToServiceRecord(MY_UUID);
  }
    
  @Override
  public void onResume() {
    super.onResume();
  
    Log.d(TAG, "...onResume - try connect...");
    
    // Set up a pointer to the remote node using it's address.
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
    
    // Two things are needed to make a connection:
    //   A MAC address, which we got above.
    //   A Service ID or UUID.  In this case we are using the
    //     UUID for SPP.
    
    try {
        btSocket = createBluetoothSocket(device);
    } catch (IOException e1) {
        errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
    }
        
    // Discovery is resource intensive.  Make sure it isn't going on
    // when you attempt to connect and pass your message.
    btAdapter.cancelDiscovery();
    
    // Establish the connection.  This will block until it connects.
    Log.d(TAG, "...Connecting...");
    try {
      btSocket.connect();
      Log.d(TAG, "...Connection ok...");
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
      
    // Create a data stream so we can talk to server.
    Log.d(TAG, "...Create Socket...");
  
    try {
      outStream = btSocket.getOutputStream();
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
    }
  }
  
  @Override
  public void onPause() {
    super.onPause();
  
    Log.d(TAG, "...In onPause()...");
  
    if (outStream != null) {
      try {
        outStream.flush();
      } catch (IOException e) {
        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
      }
    }
  
    try     {
      btSocket.close();
    } catch (IOException e2) {
      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
    }
  }
    
  private void checkBTState() {
    // Check for Bluetooth support and then check to make sure it is turned on
    // Emulator doesn't support Bluetooth and will return null
    if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth not support");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth ON...");
      } else {
        //Prompt user to turn on Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
  
  private void errorExit(String title, String message){
    Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    finish();
  }
  
  private void sendData(String message) {
    byte[] msgBuffer = message.getBytes();
  
    Log.d(TAG, "...Send data: " + message + "...");
  
    try {
      outStream.write(msgBuffer);
    } catch (IOException e) {
      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
      if (address.equals("00:00:00:00:00:00")) 
        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
        msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
        
        errorExit("Fatal Error", msg);       
    }
  }
}



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
public class MainActivity extends Activity implements OnItemClickListener, View.OnClickListener{
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	
	
	private static final String address = "00:13:12:12:64:62"; //////
	
	private static final String TAG = "bluetooh";
	private OutputStream outStream = null;
	
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	ArrayAdapter<String> listAdapter;
	//Button connectNew;
	ListView listView;
	BluetoothAdapter btAdapter;
	BluetoothSocket btSocket;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter; 
	BroadcastReceiver receiver;
	Button zero;
	Button one;
//	Handler mHandler = new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			super.handleMessage(msg);
//			switch(msg.what){
//			case SUCCESS_CONNECT:
//				ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
//				Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
//				String s = "succesfully connected";
//				connectedThread.write(s.getBytes());
//				break;
//				
//			case MESSAGE_READ:
//				byte[] readBuf = (byte[])msg.obj;
//				String string = new String(readBuf);
//				Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
//				break;
//			}
//		};
//	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    	
        init();
        
    
        zero = (Button) findViewById(R.id.zero);
        one = (Button) findViewById(R.id.one);
        
        zero.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              sendData("1");
              Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
          });
        
        one.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              sendData("1");
              Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
          });
        //no bluetooth detected in the device
       
        
        //mHandler = new Handler();
    }
    
    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) { 
          errorExit("Fatal Error", "Bluetooth not support");
        } else {
          if (btAdapter.isEnabled()) {
            Log.d(TAG, "...Bluetooth ON...");
          } else {
            //Prompt user to turn on Bluetooth
            turnOnBT();
            
          }
         
        }
      }
      
      private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
      }
    
    
    @SuppressLint("NewApi") private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
    	if(Build.VERSION.SDK_INT >= 10){
    		try{
    			final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] {UUID.class});
    			return (BluetoothSocket)m.invoke(device, MY_UUID);
    		} catch(Exception e){
    			Log.e("MainActivityBluetooh", "Could not create Insecure RFComm Connection", e);
    		}
    	}
    	return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    }


	private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
		
	}







	private void turnOnBT() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}







	private void getPairedDevices() {
		// TODO Auto-generated method stub
		devicesArray = btAdapter.getBondedDevices();
		if(devicesArray.size()>0){
			for(BluetoothDevice device:devicesArray){
				pairedDevices.add(device.getName());
			}
		}
	}







	private void init() {
		// TODO Auto-generated method stub
		//connectNew = (Button)findViewById(R.id.pair);
		listView = (ListView)findViewById(R.id.listView1);
		listView.setOnItemClickListener((OnItemClickListener) this);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		
		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();
    	
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();
		startDiscovery();
		getPairedDevices();
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent){
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action)){
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for(int a = 0; a < pairedDevices.size() ; a++){
						if(device.getName().equals(pairedDevices.get(a))){
							//append 
							s = "(Paired)";
							break;
						}
					}
					
					
					
					listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					//when the bluetooth has started discovery
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
					//when the bluetooth has finished discovery		
					
				}
				else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if(btAdapter.getState() == btAdapter.STATE_OFF){
						turnOnBT();
					}
				}
			}
		};
		
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);

	}
	  @Override
	  public void onResume() {
	    super.onResume();
	  
	    Log.d(TAG, "...onResume - try connect...");
	    
	    // Set up a pointer to the remote node using it's address.
	    BluetoothDevice device = btAdapter.getRemoteDevice(address);
	    
	    // Two things are needed to make a connection:
	    //   A MAC address, which we got above.
	    //   A Service ID or UUID.  In this case we are using the
	    //     UUID for SPP.
	    
	    try {
	        btSocket = createBluetoothSocket(device);
	    } catch (IOException e1) {
	        errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
	    }
	        
	    // Discovery is resource intensive.  Make sure it isn't going on
	    // when you attempt to connect and pass your message.
	    btAdapter.cancelDiscovery();
	    
	    // Establish the connection.  This will block until it connects.
	    Log.d(TAG, "...Connecting...");
	    try {
	      btSocket.connect();
	      Log.d(TAG, "...Connection ok...");
	    } catch (IOException e) {
	      try {
	        btSocket.close();
	      } catch (IOException e2) {
	        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
	      }
	    }
	      
	    // Create a data stream so we can talk to server.
	    Log.d(TAG, "...Create Socket...");
	  
	    try {
	      outStream = btSocket.getOutputStream();
	    } catch (IOException e) {
	      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
	    }
	  }
	  
	  @Override
	  public void onPause() {
	    super.onPause();
	  
	    Log.d(TAG, "...In onPause()...");
	  
	    if (outStream != null) {
	      try {
	        outStream.flush();
	      } catch (IOException e) {
	        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
	      }
	    }
	  
	    try     {
	      btSocket.close();
	    } catch (IOException e2) {
	      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
	    }
	  }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }







	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if(btAdapter.isDiscovering()){
			btAdapter.cancelDiscovery();
		}
		if(listAdapter.getItem(arg2).contains("Paired") && devices.get(arg2).getAddress() == address){
			
			onResume();
			
			
		}
		else{
			Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void sendData(String message) {
	    byte[] msgBuffer = message.getBytes();
	  
	    Log.d(TAG, "...Send data: " + message + "...");
	  
	    try {
	      outStream.write(msgBuffer);
	    } catch (IOException e) {
	      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
	      if (address.equals("00:00:00:00:00:00")) 
	        msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
	        msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
	        
	        errorExit("Fatal Error", msg);       
	    }
	  }
	
//	private class ConnectThread extends Thread {
//	    
//		private final BluetoothSocket mmSocket;
//	    private final BluetoothDevice mmDevice;
//	 
//	    public ConnectThread(BluetoothDevice device) {
//	        // Use a temporary object that is later assigned to mmSocket,
//	        // because mmSocket is final
//	        BluetoothSocket tmp = null;
//	        mmDevice = device;
//	 
//	        // Get a BluetoothSocket to connect with the given BluetoothDevice
//	        try {
//	            // MY_UUID is the app's UUID string, also used by the server code
//	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//	        } catch (IOException e) { }
//	        mmSocket = tmp;
//	    }
//	 
//	    public void run() {
//	        // Cancel discovery because it will slow down the connection
//	        btAdapter.cancelDiscovery();
//	 
//	        try {
//	            // Connect the device through the socket. This will block
//	            // until it succeeds or throws an exception
//	            mmSocket.connect();
//	        } catch (IOException connectException) {
//	            // Unable to connect; close the socket and get out
//	            try {
//	                mmSocket.close();
//	            } catch (IOException closeException) { }
//	            return;
//	        }
//	 
//	        // Do work to manage the connection (in a separate thread)
//	       // manageConnectedSocket(mmSocket);
//	        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
//	    }
//	 
////	    private void manageConnectedSocket(BluetoothSocket mmSocket2) {
////			// TODO Auto-generated method stub
////			
////		}
//
//		/** Will cancel an in-progress connection, and close the socket */
//	    public void cancel() {
//	        try {
//	            mmSocket.close();
//	        } catch (IOException e) { }
//	    }
//	}
//	
//	private class ConnectedThread extends Thread {
//	    
//		private final BluetoothSocket mmSocket;
//	    private final InputStream mmInStream;
//	    private final OutputStream mmOutStream;
//	 
//	    public ConnectedThread(BluetoothSocket socket) {
//	        mmSocket = socket;
//	        InputStream tmpIn = null;
//	        OutputStream tmpOut = null;
//	 
//	        // Get the input and output streams, using temp objects because
//	        // member streams are final
//	        try {
//	            tmpIn = socket.getInputStream();
//	            tmpOut = socket.getOutputStream();
//	        } catch (IOException e) { }
//	 
//	        mmInStream = tmpIn;
//	        mmOutStream = tmpOut;
//	    }
//	 
//	    public void run() {
//	        byte[] buffer;  // buffer store for the stream
//	        int bytes; // bytes returned from read()
//	 
//	        // Keep listening to the InputStream until an exception occurs
//	        while (true) {
//	            try {
//	                // Read from the InputStream
//	            	buffer = new byte[1024];
//	                bytes = mmInStream.read(buffer);
//	                // Send the obtained bytes to the UI activity
//	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//	                        .sendToTarget();
//	             
//	            } catch (IOException e) {
//	                break;
//	            }
//	        }
//	    }
//	 
//	    /* Call this from the main activity to send data to the remote device */
//	    public void write(byte[] bytes) {
//	        try {
//	            mmOutStream.write(bytes);
//	        } catch (IOException e) { }
//	    }
//	 
//	    /* Call this from the main activity to shutdown the connection */
//	    public void cancel() {
//	        try {
//	            mmSocket.close();
//	        } catch (IOException e) { }
//	    }
//	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
    
}
