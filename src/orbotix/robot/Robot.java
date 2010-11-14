/*
 * Copyright 2010 Orbotix Inc.
 * 
 * DO NOT MODIFY IN CLIENT CODE.
 */
package orbotix.robot;

import java.util.Observable;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Class to represent an available robotic device. 
 * @author Brian Smith
 *
 */
public class Robot extends Observable implements Parcelable {
	private static final String LOG_TAG = "Robot";
	private static final boolean DEBUG = false;
	
	/** Action used in broadcast intent when a robot is found */
	protected static final String ACTION_FOUND = "orbotix.robot.initent.action.FOUND";
	/** Extra data used in broadcast intent*/
	protected static final String EXTRA_ROBOT_ID = "orbotix.robot.RobotId";
	
	private final BluetoothDevice bluetoothDevice;
	
	/** State member to keep track if the robot is under control */
	private Boolean underControl = false;
	/** State member to keep track of the robot's connection state. */
	private Boolean connected = false;
	/** State member to keep track of the robot discovery state */
	private Boolean discovered = false;
	
	protected static final Parcelable.Creator<Robot> CREATOR
		= new Parcelable.Creator<Robot>() {

			@Override
			public Robot createFromParcel(Parcel source) {
				return new Robot(source);
			}

			@Override
			public Robot[] newArray(int size) {
				return new Robot[size];
			}
	};
	
	/**
	 * Constructor with no arguments used for IPC
	 * calls.
	 */
	protected Robot() {
		bluetoothDevice = null;
	}
	
	/**
	 * Constructor from a parcelable source.
	 * @param source The Parcel source.
	 */
	private Robot(Parcel source) {
		bluetoothDevice = BluetoothDevice.CREATOR.createFromParcel(source);
		underControl = (Boolean) source.readValue(null);
		connected = (Boolean) source.readValue(null);
		discovered = (Boolean) source.readValue(null);
	}
	
	/**
	 * Constructor for creating a Robot object.
	 * @param device An Bluetooth device object.
	 */
	protected Robot(BluetoothDevice device) {
		bluetoothDevice = device;
	}
	
	/**
	 * Accessor to the underlaying BluetoothDevice
	 * @return A BluetoothDevice object.
	 */
	public BluetoothDevice getDevice() {
		return bluetoothDevice;
	}
	
	/**
	 * Accessor to the robot's name.
	 * @return String with the name of the robot.
	 */
	public String getName() {
		if (bluetoothDevice == null) return "Orbotix Robot";
		return bluetoothDevice.getName();
	}

	/**
	 * Provides a unique identifier for the robot.
	 * @return A unique identifier.
	 */
	public String getUniqueId() {
		if (bluetoothDevice == null) return null;
		return bluetoothDevice.getAddress();
	}
	
	/**
	 * Method to check if the robot is known.
	 * @return true if the robot has been used before(paired), otherwise false.
	 */
	public boolean isKnown() {
		if (bluetoothDevice == null) return false;
		return bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED;
	}
	
	/**
	 * Method to check the connection status.
	 * @return true if connected to the robot, otherwise false.
	 */
	public boolean isUnderControl() {
		if (DEBUG) Log.d(LOG_TAG, this + " under control:" + underControl);
		return underControl;
	}
	
	/**
	 * Accessor to set the connected state of the robot. Notifies 
	 * Observers of the change.
	 * @param state The current connection state of the robot.
	 */
	protected void setUnderControl(Boolean state) {
		if (DEBUG) Log.d(LOG_TAG, this + "set under control:" + state);
		underControl = state;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Accessor to check the connection state.
	 * @return The connection state.
	 */
	public Boolean isConnected() {
		return connected;
	}
	
	/**
	 * Accessor to set if the robot is connected
	 * @param newState true if connected, and false if not connected.
	 */
	protected void setConnected(boolean newState) {
		connected = newState;
	}
	
	/**
	 * Accessor to check if robot was discovered during bluetooth discovery.
	 * @return true if the robot was discovered, otherwise false.
	 */
	public boolean wasDiscovered() {
		return discovered;
	}
	
	/**
	 * Accessor to set if the robot was discovered during bluetooth discovery.
	 * @param newState The new state of discovery.
	 */
	protected void setDiscovered(boolean newState) {
		discovered = newState;
	}
	
	/**
	 * Provides a formated string of the robot name and status.
	 * @return A representative string for the robot.
	 */
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		bluetoothDevice.writeToParcel(dest, flags);
		dest.writeValue(underControl);
		dest.writeValue(connected);
		dest.writeValue(discovered);
	}
}
