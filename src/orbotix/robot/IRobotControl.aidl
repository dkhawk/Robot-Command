/**
 * Copyright 2010 Orbotix Inc.
 * DO NOT MODIFY
 */
 
package orbotix.robot;

import orbotix.robot.Robot;
import orbotix.robot.IRobotControlCallback;


/**
* Interface to access individual robots and control them.
*/
interface IRobotControl {
    
    /**
    *   Registers a client for callbacks with the service. 
    *   @see IRobotControlCallback
    *   @param callback An implementation instance of IOrbotixServiceCallback
    */
    void registerCallback(IRobotControlCallback callback);
    
    /**
    *  Unregister a client for callbacks with the service.
    *  @see IRobotControlCallback
    *  @param callback The callback used originally to register the client.
    */
    void unregisterCallback(IRobotControlCallback callback);
    
    /**
    *  Method to get a list of the robots that have been set to be controlled.
    */
    List<Robot> getRobots();
    
    /**
     * Method for physical control of the robots' left motor.
     * <p><br><code>
     *<br> 
     *              // Left motor forward half speed<br>
     *<br>            
     *                      gameControl.setLeftMotorSpeed(robot_1, .5);<br>
     *<br> </code>
     * @param robot is a robot that you want to change the motor speed.
     * @param speed is an float between 1.0 and -1.0 (0 is stop)
     */
    void setLeftMotorSpeed(in Robot robot, float speed);
    
    /**
     * Method for physical control of the robots' right motor.
     * <p><br><code>
     *<br> 
     *              // Right motor forward half speed<br>
     *<br>            
     *                      gameControl.setRightMotorSpeed(robot_2, .5);<br>
     *<br> </code>
     * @param robot is a robot that you want to change the motor speed.
     * @param speed is an float between 1.0 and -1.0 (0 is stop)
     */
    void setRightMotorSpeed(in Robot robot, float speed);
    
    /**
    * Method for getting the input value from devices that support
    * prototyping. This is an asynchronous call, and the results are
    * returned by a callback. 
    * @see IRobotControlCallback
    * @param robot is the robot to get input values from.
    * @param port is the input port to get a value from.
    */
    void getInput(in Robot robot, byte port);
    
    /**
    * Method for setting an output on or off for devices that support
    * prototyping. Nothing is returned from this method nor a
    * callback at this time.
    * @param robot is the robot to set the output value to.
    * @param port is the port to set.
    * @param state is set to true for on(high) and false for off(low).
    */
    void setOutput(in Robot robot, byte port, boolean state);
}