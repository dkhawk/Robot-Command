/**
 * Copyright 2010 Orbotix Inc.
 * DO NOT MODIFY
 */
package orbotix.robot;

import orbotix.robot.IOrbotixServiceCallback;

/**
* Provides a simple interface for discovering and control multiple robotic 
* devices.
*/
interface IGameControl {

    /**
    *   Registers a client for callbacks with the service. 
    *   @see IOrbotixServiceCallback
    *   @param callback An implementation instance of IOrbotixServiceCallback
    */
    void registerCallback(IOrbotixServiceCallback callback);
    
    /**
    *  Unregister a client for callbacks with the service.
    *  @see IOrbotixServiceCallback
    *  @param callback The callback used originally to register the client.
    */
    void unregisterCallback(IOrbotixServiceCallback callback);
    
    /**
    *  Prepares to control all robots, which includes establishing a bluetooth 
    *  connection to them.
    */
    void prepareRobots();
    
    /**
    *  Stops the motors on all robots and releases control of them.
    */
    void shutdownRobots();

    /**
    *  Starts driving the robots with the accelerometer.
    */
    void startDriving();
    
    /**
     * Method for physical control of the robots' left motor.
     * <p><br><code>
     *<br> 
     *              // Left motor forward half speed<br>
     *<br>            
     *                      gameControl.setLeftMotorSpeed(.5);<br>
     *<br> </code>
     * @param speed     A float between 1.0 and -1.0 (0 is stop)
     */
    void setLeftMotorSpeed(float speed);
    
    /**
     * Method for physical control of the robots' right motor.
     * <p><br><code>
     *<br> 
     *              // Right motor backward half speed<br>
     *<br>            
     *                      gameControl.setRightMotorSpeed(-.5);<br>
     *<br> </code>
     * @param speed     A float between 1.0 and -1.0 (0 is stop)
     */
    void setRightMotorSpeed(float speed);
    
    /**
    *  Stops driving the robots with the accelerometer.
    */
    void stopDriving();
    
    /**
    *  Accessor to check if robots are prevented from moving.
    *  @return true if the robots are at a full stop.
    */
    boolean isAtFullStop();
    
    /**
    *  Setter to prevent the sending of motor commands to the robot. 
    *  @param newState Set to true to prevent the robots from moving, and false
    *  to allow them to start moving again. 
    */
    void setFullStop(boolean newState);
    
    /**
    *  Accessor to check that there are robots ready to control.
    *  @return true if robots are available for your control, otherwise false.
    */
    boolean hasRobotControl();

    /**
    *  Shows a picker activity, for the use to pick Orbotix robotix devices
    *  to control.
    */
    void showRobotPicker();
}
