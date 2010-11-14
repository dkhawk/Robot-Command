package orbotix.sample.stickdrive;

import orbotix.sample.stickdrive.R;
import orbotix.robot.IGameControl;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainMenu extends Activity {
	private static final String LOG_TAG = "MainMenu";
	
	private static final int STOPSPLASH = 0;
	private static final int STOPLOADING = 1;
	
	private static final long SPLASHTIME = 3000;
	
	private ImageView splash;

	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				splash.setVisibility(View.GONE);
				break;
			case STOPLOADING:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private IGameControl gameControl;
	private ServiceConnection gameControlConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			gameControl = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			gameControl = IGameControl.Stub.asInterface(serviceBinder);
			updateButtons();
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
		splash = (ImageView) findViewById(R.id.splashscreen);

		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);

		Button drive_button = (Button)findViewById(R.id.DriveButton);
		drive_button.setEnabled(false);
		drive_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent drive_intent = new Intent(MainMenu.this, DriveActivity.class);
				startActivity(drive_intent);
			}
		});

		Button robot_button = (Button)findViewById(R.id.FindRobotButton);
		robot_button.setEnabled(false);
		robot_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Launch a RobotPickerActivity
				try {
					gameControl.showRobotPicker();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
				}
			}
		});
		
		if (!bindService(new Intent("orbotix.robot.IGameControl"),
				gameControlConnection, Context.BIND_AUTO_CREATE))
		{
			Log.e(LOG_TAG, "Bind to service failed.");
		}
    }
        
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unbindService(gameControlConnection);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	if (hasFocus) {
    		// update the game play button
    		updateButtons();
    	}
    }

    private void updateButtons() {
		if (gameControl != null) {
			Button button = (Button)findViewById(R.id.FindRobotButton);
			button.setEnabled(true);
			
			try {
				if(gameControl.hasRobotControl()) {
					button = (Button)findViewById(R.id.DriveButton);
					button.setEnabled(true);
					button.setBackgroundResource(R.drawable.playbuttonenabled);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
			}
			
		} else {
			Button button = (Button)findViewById(R.id.FindRobotButton);
			button.setEnabled(false);
			
    		button = (Button)findViewById(R.id.DriveButton);
    		button.setEnabled(false);
    		button.setBackgroundResource(R.drawable.playbuttondisabled);    			
		}
    }
    
 }