/*
 Copyright (c) Kulvinder Lotay

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package boid.savanna;

import static buckland.ch3.ParamLoader.Prm;
import buckland.ch3.boid.Predator;
import buckland.ch3.boid.Prey;
import buckland.ch3.boid.Vehicle;
import buckland.ch3.common.D2.Vector2D;
import static buckland.ch3.common.misc.Cgdi.gdi;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kulvinder Lotay
 * Shark class extends Predator, adds hunting behavior,
 * draws a killzone, and chases/kills prey in killzone
 * 
 */
public class Shark extends Predator {
    //Peripheral vision for shark killzone drawing
    public static final double PERIPHERAL_VISION = 36;
    
    //Parameter for velocity multiplier when chasing prey
    public static final double VELOCITY_MULTIPLIER = 4;
    
    //Variable to hold starting velocity
    protected Vector2D initVelocity = new Vector2D(0, 0);
    
    //Last calulated heading angle in degrees
    protected double dThetaDegrees;
    
    //Shark kill zone radius
    protected double killZoneRadius = -1;
    
    //Caught prey
    protected int captureCount = 0;
   
    /**
     * Constructor
     * @param spawnPos Spawn position in the world.
     */
    public Shark(Vector2D spawnPos) {
        super(spawnPos);
        super.SetMaxSpeed(120);
        
        this.killZoneRadius = Prm.ViewDistance;
    }
    
    /**
     * Override update method of parent super class Predator
     * This method gets invoked every cycle during the update phase by game world.
     * @param dTimeElapsed Elapsed time in seconds since last update
     */
    @Override
    public void Update(double dTimeElapsed) {
        //Run superclass operations, passing time elapsed
        super.Update(dTimeElapsed);
        
        //Calculate heading in degrees
        this.dThetaDegrees = Math.atan2(this.Heading().y, this.Heading().x) * 180 / Math.PI;
        
        if(initVelocity == (new Vector2D(0, 0))) {
            this.initVelocity = this.Velocity();
        }
        
        //All captured prey
        List<Vehicle> sharkBoxOfPrey = new ArrayList<>();
        
        //List of prey from gameworld
        List<Vehicle> sharkPreyList = this.m_pWorld.GetPrey();
        
        //Prey that move out of range
        List<Vehicle> sharkCannotEatList = new ArrayList<>();
        
        //Check for prey in the kill zone
        for (Vehicle preyVehicle : sharkPreyList) {
            Prey vector2d = (Prey) preyVehicle;
            
            //Check prey distance from shark
            double preyDistance = this.Pos().Distance(vector2d.Pos());
            
            //Check angle between
            double dTheta = AngleBetweenHeadingAndPreyVector(vector2d);
            
            //Check if in kill zone, if yes then add to lunchbox
            if (preyDistance <= killZoneRadius && (dTheta < PERIPHERAL_VISION / 2)) {
                sharkBoxOfPrey.add(vector2d);
            }
        }
        
        //Chase and kill, and get list of retired prey
        List<Vehicle> retiredPreyList = ChaseAndKill(sharkBoxOfPrey, sharkCannotEatList);
        
        //Update prey lists and shark box of prey
        sharkBoxOfPrey.removeAll(retiredPreyList);
        sharkBoxOfPrey.removeAll(sharkCannotEatList);
        sharkPreyList.removeAll(retiredPreyList);
    }
    
    /**
     * Check the angle between the predator heading and prey location vector
     * @param vector2d the prey
     * @return angle in degrees
     */
    protected double AngleBetweenHeadingAndPreyVector(Prey vector2d) {
        //Get vector of shark to prey location
        //Shark heading vector
        Vector2D preyVector = vector2d.Pos().sub(this.Pos());
        Vector2D heading = this.Heading();
        
        //Normalize
        heading.Normalize();
        preyVector.Normalize();

        //Check if prey is in the direction killzone angle
        //Calculate dot product to get angle between them
        double sharkFacing = heading.Dot(preyVector);

        //Calculate heading theta
        double headingTheta = Math.atan2(heading.y, heading.x) * 180 / Math.PI;

        //Calculate angle between heading and prey pos vector, absolute value
        double preyThetaDegrees = Math.acos(sharkFacing) * 180 / Math.PI;
        double dTheta = Math.abs(preyThetaDegrees - headingTheta);
        
        return dTheta;
    }
    
    /**
     * Chase and try to kill the prey in the hitList
     * @param hitList list of prey in killzone range
     * @param sharkCannotEatList list of prey that are no longer in range
     * @return list of prey that were killed/retired
     */
    protected List<Vehicle> ChaseAndKill(List<Vehicle> hitList, List<Vehicle> sharkCannotEatList) {
        //List of prey eaten
        List<Vehicle> sharkEatList = new ArrayList<>();
        
        //Chase prey in killzone
        for (Vehicle preyVehicle : hitList) {
            Prey vector2d = (Prey) preyVehicle;
            
            //Check prey distance from shark
            double preyDistance = this.Pos().Distance(vector2d.Pos());
            
            //Check absolute value of angle between
            double dTheta = AngleBetweenHeadingAndPreyVector(vector2d);
            
            //Check if still in kill zone, if yes then check for collision and retire
            if (preyDistance <= killZoneRadius && (dTheta < PERIPHERAL_VISION / 2)) {
                double currentTimeElapsed = this.m_dTimeElapsed;
                
                //Speed up if prey in area and time elapsed less than 250 since last 'sprint'
                if((currentTimeElapsed + 250) > this.m_dTimeElapsed) {
                    this.SetVelocity(new Vector2D(this.initVelocity.y * VELOCITY_MULTIPLIER, 
                                                  this.initVelocity.x * VELOCITY_MULTIPLIER));
                }
                else {
                    this.SetVelocity(initVelocity);
                }
                
                //Pursue prey if in killzone
                this.m_pSteering.PursuitOn(preyVehicle);
                
                //If prey collides, retire the prey and increment catch counter
                if (preyDistance - preyVehicle.BRadius() <= this.BRadius()){
                    sharkEatList.add(preyVehicle);
                    this.captureCount++;
                }
            }
            //Else remove from lunchbox since its no longer in range, and stop pursue
            else {
                sharkCannotEatList.add(preyVehicle);
                this.m_pSteering.PursuitOff();
            }
        }
        return sharkEatList;
    }
    
    /**
     * Override render method of parent super class Predator
     * @param bTrueFalse Used by Vehicle super class
     */
    @Override
    public void Render(boolean bTrueFalse) {
        //Call superclass method
        super.Render(bTrueFalse);
        
        //Get graphics context
        Graphics2D g = gdi.GetHdc();
        
        //Draw killzone arms
        DrawKillzone(g);
        
        //Draw killscore
        DrawKillscore(g);
    }
    
    /**
     * Draw the killzone
     * @param g Graphics context
     */
    protected void DrawKillzone(Graphics2D g) {
        //Calculate the heading angle
        double headingAngle = Math.atan2(Heading().y, Heading().x);
        
        //Draw both arms, using heading angle and half peripheral vision
        DrawArm(g, headingAngle + PERIPHERAL_VISION / 2 * Math.PI / 180);
        DrawArm(g, headingAngle - PERIPHERAL_VISION / 2 * Math.PI / 180);
    }
    
    /**
     * Draw killzone arm
     * @param g Graphics context
     * @param angle angle to draw arm
     * Uses the same method of calculation as in minnow
     */
    protected void DrawArm(Graphics2D g, double angle) {
        //Calculate starting points of arm
        int armStartX = (int) (this.Pos().x);
        int armStartY = (int) (this.Pos().y);
        
        //Calculate ending points of arm - same calculation as Minnow
        int armEndX = (int) (killZoneRadius * Math.cos(angle) + armStartX + 0.5);
        int armEndY = (int) (killZoneRadius * Math.sin(angle) + armStartY + 0.5);
        
        //Set color
        g.setColor(Color.GREEN);
        
        //Draw
        g.drawLine(armStartX, armStartY, armEndX, armEndY);
    }
    
    /**
     * 
     * @param g 
     */
    protected void DrawKillscore(Graphics2D g) {
        //Convert capture count to string
        String capturedPreyStr = String.format("%3d", captureCount);
        
        //Set graphics context color
        g.setColor(Color.BLACK);
        
        //Draw the capture number using default font
        int dPosX = (int) Pos().x;
        int dPosY = (int) Pos().y;
        
        //Draw
        g.drawString(capturedPreyStr + " ", dPosX + 10, dPosY + 10);
    }
    
}
