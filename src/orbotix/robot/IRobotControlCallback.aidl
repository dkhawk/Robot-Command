/**
 * Copyright 2010 Orbotix Inc.
 * DO NOT MODIFY
 */
 
package orbotix.robot;

import orbotix.robot.Robot;

/**
* Callback interface for clients to receive data back when
* using the IRobotControl interface.
*/
oneway interface IRobotControlCallback {

    /**
    * Callback method for receiving development board input values.
    * Called after invoking IRobotControl.getInput().
    * @param robot is the robot that was used in IRobotControl.getInput().
    * @param port is the input port.
    * @param value is the ADC value.
    */
    void receivedInput(out Robot robot, byte port, int value);
     
}