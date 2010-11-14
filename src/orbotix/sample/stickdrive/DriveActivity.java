package orbotix.sample.stickdrive;

import orbotix.sample.stickdrive.R;
import orbotix.robot.IGameControl;
import orbotix.robot.IOrbotixServiceCallback;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class DriveActivity extends Activity {
	private static final String LOG_TAG = "DriveActivity";
	private final Boolean DEBUG = true;

	private static final int PREPARE_ROBOT_FINISHED = 1;
	private static final int ROBOT_LOST_CONTROL = 2;
	private static final float SPEED_THRESHOLD = (float) 0.05;
	private static final float DEAD_ZONE = (float) 0.1;
	private static final long COMMAND_DELAY = 250; // delay between sending
													// commands to the robot

	private float leftSpeed = (float) 0.0;
	private float rightSpeed = (float) 0.0;

	private static final int DIALOG_SETUP_ID = 1;

	private Button fullStopButton;

	private static final String DEBUG_TAG = "PlayArea";
	private PowerManager.WakeLock screenWakeLock;
	private String speedText;

	private class PlayAreaView extends View {
		private GestureDetector gestures;
		private Matrix translate;
		private Bitmap droid;
		private int center_y;
		private int center_x;
		private int icon_offset_y;
		private int icon_offset_x;
		private float new_leftSpeed;
		private float new_rightSpeed;
		private int max_dim;

		public PlayAreaView(Context context) {
			super(context);
			translate = new Matrix();
			gestures = new GestureDetector(DriveActivity.this,
					new GestureListener(this));
			droid = BitmapFactory.decodeResource(getResources(),
					R.drawable.droid_g);
			center_y = this.getHeight() / 2;
			center_x = this.getWidth() / 2;
			icon_offset_y = droid.getHeight() / 2;
			icon_offset_x = droid.getWidth() / 2;
			speedText = "0, 0";
		}

		protected void onDraw(Canvas canvas) {
			if (center_y == 0 || center_x == 0) {
				center_y = canvas.getHeight() / 2;
				center_x = canvas.getWidth() / 2;
				icon_offset_y = droid.getHeight() / 2;
				icon_offset_x = droid.getWidth() / 2;
				translate.setTranslate(center_x - icon_offset_x, center_y
						- icon_offset_y);
				max_dim = (canvas.getHeight() > canvas.getWidth()) ? canvas.getHeight() : canvas.getWidth();
			}
			
			Matrix m = canvas.getMatrix();
			Paint paint = new Paint();
			paint.setColor(Color.BLUE);
			canvas.drawCircle(center_x, center_y, 10, paint);
			canvas.drawBitmap(droid, translate, null);
			canvas.drawText(speedText, 0, 20, paint);

			// Draw a circle around the center.
			Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
			Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());

			float[] mtx = new float[9];
			translate.getValues(mtx);

			float x_offset = mtx[Matrix.MTRANS_X];
			float y_offset = mtx[Matrix.MTRANS_Y];

			if (Math.abs(x_offset) > max_dim) {
				if (x_offset < -max_dim) {
					x_offset = -max_dim;
				} else {
					x_offset = max_dim;
				}
			}

			if (Math.abs(y_offset) > max_dim) {
				if (y_offset < -max_dim) {
					y_offset = -max_dim;
				} else {
					y_offset = max_dim;
				}
			}

			
			float scaled_dx = (x_offset - (center_x - icon_offset_x))
					/ (center_x - icon_offset_x);
			float scaled_dy = (y_offset - (center_y - icon_offset_y))
					/ (center_y - icon_offset_y);

			if (scaled_dy < -1.0) {
				scaled_dy = (float) -1.0;
			}

			if (scaled_dy > 1.0) {
				scaled_dy = (float) 1.0;
			}

			if (scaled_dx < -1.0) {
				scaled_dx = (float) -1.0;
			}

			if (scaled_dx > 1.0) {
				scaled_dx = (float) 1.0;
			}

			Log.d(DEBUG_TAG, "Scale: " + scaled_dx + ", " + scaled_dy);
			double angle = Math.atan2(scaled_dy, scaled_dx);
			angle = angle + (Math.PI / 4.0) + (Math.PI / 2.0);
			Log.d(DEBUG_TAG, "Angle: " + angle);

			double angle_degrees = angle / Math.PI * 180;
			Log.d(DEBUG_TAG, "Angle degrees: " + angle_degrees);

			double velocity = Math.sqrt(scaled_dx * scaled_dx + scaled_dy
					* scaled_dy) * 1.25;
			Log.d(DEBUG_TAG, "Velocity: " + velocity);

			new_leftSpeed = (float) (velocity * Math.sin(angle));
			new_rightSpeed = (float) (velocity * Math.cos(angle));

			double scale = 1.0;
			
			if ((new_leftSpeed > new_rightSpeed) && Math.abs(new_leftSpeed) > 1.0) {
				scale = 1.0 / Math.abs(new_leftSpeed);
			}

			if ((new_rightSpeed >= new_leftSpeed) && Math.abs(new_rightSpeed) > 1.0) {
				scale = 1.0 / Math.abs(new_rightSpeed);
			}
			
			new_leftSpeed *= scale;
			new_rightSpeed *= scale;
			
			Log.d(DEBUG_TAG, "Speed: " + new_leftSpeed + ", " + new_rightSpeed);
			// Toast.makeText(DragController.this, "" + speed_x + ", " +
			// speed_y, Toast.LENGTH_SHORT).show();
			setSpeed(new_leftSpeed, new_rightSpeed);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return gestures.onTouchEvent(event);
		}

		public void onMove(float dx, float dy) {
			translate.postTranslate(dx, dy);
			invalidate();
		}

		private Matrix animateStart;
		private Interpolator animateInterpolator;
		private long startTime;
		private long endTime;
		private float totalAnimDx;
		private float totalAnimDy;

		public void onAnimateMove(float dx, float dy, long duration) {
			animateStart = new Matrix(translate);
			animateInterpolator = new OvershootInterpolator();
			startTime = System.currentTimeMillis();
			endTime = startTime + duration;
			totalAnimDx = dx;
			totalAnimDy = dy;
			post(new Runnable() {
				@Override
				public void run() {
					onAnimateStep();
				}
			});
		}

		private void onAnimateStep() {
			long curTime = System.currentTimeMillis();
			float percentTime = (float) (curTime - startTime)
					/ (float) (endTime - startTime);
			float percentDistance = animateInterpolator
					.getInterpolation(percentTime);
			float curDx = percentDistance * totalAnimDx;
			float curDy = percentDistance * totalAnimDy;
			translate.set(animateStart);
			onMove(curDx, curDy);

			Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
			if (percentTime < 1.0f) {
				post(new Runnable() {
					@Override
					public void run() {
						onAnimateStep();
					}
				});
			}
		}

		public void onResetLocation() {
			translate.reset();
			translate.setTranslate(center_x - icon_offset_x, center_y
					- icon_offset_y);
			invalidate();
		}
	}

	private class GestureListener implements GestureDetector.OnGestureListener,
			GestureDetector.OnDoubleTapListener {
		PlayAreaView view;

		public GestureListener(PlayAreaView view) {
			this.view = view;
		}

		public boolean onDown(MotionEvent e) {
			Log.v(DEBUG_TAG, "onDown");
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.v(DEBUG_TAG, "onDoubleTap");
			speedText = "0, 0";
			view.onResetLocation();
			return true;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2,
				final float velocityX, final float velocityY) {
			Log.v(DEBUG_TAG, "onFling");
			final float distanceTimeFactor = 0.4f;
			final float totalDx = (distanceTimeFactor * velocityX / 2);
			final float totalDy = (distanceTimeFactor * velocityY / 2);

			view.onAnimateMove(totalDx, totalDy,
					(long) (1000 * distanceTimeFactor));
			return true;
		}

		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.v(DEBUG_TAG, "onScroll");

			view.onMove(-distanceX, -distanceY);
			return true;
		}

		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	private IGameControl gameControl;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				gameControl.unregisterCallback(serviceCallback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gameControl = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			gameControl = IGameControl.Stub.asInterface(serviceBinder);
			try {
				gameControl.registerCallback(serviceCallback);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Setup driving
			try {
				gameControl.prepareRobots();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// setContentView(R.layout.drive);
		setContentView(R.layout.dragcontroller);

		FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
		PlayAreaView image = new PlayAreaView(this);
		frame.addView(image);

		// Button quit_button = (Button) findViewById(R.id.QuitButton);
		// quit_button.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// finish();
		// }
		// });

		// TODO(dkhawk) Need to get full stop to work.
		// fullStopButton = (Button) findViewById(R.id.StopButton);
		// fullStopButton.setOnClickListener(new OnClickListener() {

		// @Override
		// public void onClick(View v) {
		// try {
		// if (gameControl.isAtFullStop()) {
		// gameControl.setFullStop(false);
		// fullStopButton
		// .setBackgroundResource(R.drawable.full_stop);
		// } else {
		// gameControl.setFullStop(true);
		// fullStopButton
		// .setBackgroundResource(R.drawable.run_motor);
		// resetSeekBars();
		// }
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// });

		// Add this to the SeekBars' change listener.
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) {
			Log.d(LOG_TAG, "onStart()");
		}

		// Keep screen on.
		PowerManager power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		screenWakeLock = power_manager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "DriveActivity");
		screenWakeLock.acquire();

		bindService(new Intent("orbotix.robot.IGameControl"),
				serviceConnection, BIND_AUTO_CREATE);

		showDialog(DIALOG_SETUP_ID);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) {
			Log.d(LOG_TAG, "onStop()");
		}

		if (gameControl != null) {
			try {
				gameControl.shutdownRobots();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			unbindService(serviceConnection);
		}

		// Stop preventing the screen from sleeping
		screenWakeLock.release();
		screenWakeLock = null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_SETUP_ID:
			ProgressDialog progress_dialog = new ProgressDialog(this);
			progress_dialog
					.setTitle(getString(R.string.drive_setup_progress_title));
			progress_dialog
					.setMessage(getString(R.string.drive_setup_progress_message));
			dialog = progress_dialog;
			break;

		default:
			break;
		}
		return dialog;
	}

	private void showRobotLostControlAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(this.getString(R.string.lost_control));
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showFailedToSetupAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(this.getString(R.string.drive_failed_to_setup));
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private IOrbotixServiceCallback serviceCallback = new IOrbotixServiceCallback.Stub() {

		@Override
		public void robotLostControl() throws RemoteException {
			Message message = serviceHandler.obtainMessage(ROBOT_LOST_CONTROL);
			message.sendToTarget();
		}

		@Override
		public void prepareRobotFinished(int failed) throws RemoteException {
			Message message = serviceHandler.obtainMessage(
					PREPARE_ROBOT_FINISHED, failed, 0);
			message.sendToTarget();
		}
	};

	private Handler serviceHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PREPARE_ROBOT_FINISHED:
				dismissDialog(DIALOG_SETUP_ID);
				removeDialog(DIALOG_SETUP_ID); // needed only once

				if (msg.arg1 > 0) {
					showFailedToSetupAlert();
				}
				break;
			case ROBOT_LOST_CONTROL:
				showRobotLostControlAlert();
				break;
			default:
				break;
			}
		};
	};

	private long lastCommandTime;

	public void setSpeed(float speedLeft, float speedRight) {
		long newTime = System.currentTimeMillis();
		// Prevent the send queue from becoming flooded.
		if ((newTime - lastCommandTime) < COMMAND_DELAY) {
			return;
		}
		// TODO(dkhawk) if both speeds are 0, do a full stop.

		Log.i(LOG_TAG, "Setting speeds to " + speedLeft + ", " + speedRight);
		try {
			gameControl.setLeftMotorSpeed(speedLeft);
			gameControl.setRightMotorSpeed(speedRight);
			speedText = "" + speedLeft + ", " + speedRight;
			lastCommandTime = System.currentTimeMillis();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

// if (seekBar.getId() == R.id.LeftMotorSeekBar) {
// if ((new_speed == 0 && leftSpeed != 0) || Math.abs(leftSpeed - new_speed) >
// SPEED_THRESHOLD) {
// Log.i(LOG_TAG, "Setting left motor to " + new_speed);
// try {
// gameControl.setLeftMotorSpeed(new_speed);
// lastCommandTime = System.currentTimeMillis();
// leftSpeed = new_speed;
// Toast.makeText(this, "Left to " + leftSpeed, Toast.LENGTH_SHORT).show();
// } catch (RemoteException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// }
// } else {
// if ((new_speed == 0 && rightSpeed != 0) || Math.abs(rightSpeed - new_speed) >
// SPEED_THRESHOLD) {
// Log.i(LOG_TAG, "Setting right motor to " + new_speed);
// try {
// gameControl.setRightMotorSpeed(new_speed);
// lastCommandTime = System.currentTimeMillis();
// rightSpeed = new_speed;
// Toast.makeText(this, "Right to " + rightSpeed, Toast.LENGTH_SHORT).show();
// } catch (RemoteException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// }
