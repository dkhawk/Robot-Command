/*
 * Copyright 2010 Orbotix Inc.
 * DO NOT MODIFY
 */
 
package orbotix.robot;

/**
 *  Callback interface for OrbotixService. Used to pass messages and 
 *  data back to client.
 *  @see IGameControl
 */
oneway interface IOrbotixServiceCallback {
    /**
     * Called after the service has finished preparing a robot for use.
     * @param failed Number of robots that failed to setup. 
     */
    void prepareRobotFinished(int failed);
    
    /**
     * Called when the robot is out of control of the service.
     */
    void robotLostControl();
}