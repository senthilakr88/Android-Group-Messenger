package edu.buffalo.cse.cse486586.groupmessenger;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
	
	
	 static final String TAG = GroupMessengerActivity.class.getSimpleName();
	 static final String[] remoteport={"11108","11112","11116","11120","11124"};
	 static final String REMOTE_PORT0 = "11108";
	 static final String REMOTE_PORT1 = "11112";
	 static final String REMOTE_PORT2 = "11116";
	 static final String REMOTE_PORT3 = "11120";
	 static final String REMOTE_PORT4 = "11124";	 
	 static final int SERVER_PORT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        
        
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             * 
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
        
        

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        findViewById(R.id.button4).setOnClickListener(new OnClickListener(){
                	
        	        @Override
                	public void onClick(View v)
                	{
                	
                		
               		final EditText editText = (EditText) findViewById(R.id.editText1);
                		String msg = editText.getText().toString() + "\n";
                        editText.setText(""); // This is one way to reset the input box.
                        
                   //     TextView localTextView = (TextView) findViewById(R.id.textView1);
                   //     localTextView.append("\t"+msg); // This is one way to display a string.
                        
                        //TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                        //remoteTextView.append("\n");
                        
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                		
                		
                	}
                	
                	
                });
           	
     
        
        
        
        
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void>{
    	
    	
    	private final Uri mUri=buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
    	
    	
    	private Uri buildUri(String scheme, String authority) {
    	        Uri.Builder uriBuilder = new Uri.Builder();
    	        uriBuilder.authority(authority);
    	        uriBuilder.scheme(scheme);
    	        return uriBuilder.build();
    	    }
    	

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            
           while(true)
           {
            	try {
					Socket sock=serverSocket.accept();
					InputStream ism=sock.getInputStream();
					/* Create a Input Stream Reader and wrap it with Buffered Reader for Efficient Reading*/
					BufferedReader br= new BufferedReader(new InputStreamReader(ism));
					String ss=null;
					while((ss=br.readLine())!=null)
					{
						System.out.println(ss);
						/* Pass the data to OnProgress Update*/
						Log.v("log",ss);
						publishProgress(ss);
			            		
					}
					 br.close();
			           
				     sock.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
            
				
            	
           }

        }
    
        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strings[0] + "\t\n");
        //    TextView localTextView = (TextView) findViewById(R.id.local_text_display);
        //    localTextView.append("\n");
            
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             * 
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            
          //  OnPTestClickListener testob=new OnPTestClickListener();
            GroupMessengerProvider gmp=new GroupMessengerProvider();
            String string = strings[0];
            String[] str1=string.split(".");
            ContentValues cv=new ContentValues();
            String valfield=string.substring(str1[0].length()+1);
            cv.put("key",str1[0]);
            cv.put("value",valfield);
            gmp.insert(mUri,cv);
            
            
         

            return;
        }
    }
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
            
            	String remotePort1 = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort1 = REMOTE_PORT1;
 
                /* create a data output stream and send the data using writeBytes method */
             //   for (int i=0;i<2;i++)
              //  {
                
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort1));
                
                DataOutputStream dos=null;
            
                String msgToSend = msgs[0];
                Log.v("log",msgToSend);
                dos=new DataOutputStream(socket.getOutputStream());
             
                dos.writeBytes(msgToSend);
                dos.close();
             
                socket.close();
             //   }
                
               
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
