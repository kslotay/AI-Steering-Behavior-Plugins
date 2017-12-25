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

import buckland.ch3.boid.Prey;
import buckland.ch3.common.D2.Vector2D;
import buckland.ch3.common.misc.Cgdi;
import static buckland.ch3.common.misc.Cgdi.gdi;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Kulvinder Lotay
 * Minnow class extends Prey, adds prey counter to foreground
 * and antenna to prey
 */
public class Minnow extends Prey {
    
    //Parameter to show the number of prey in bottom right-corner
    public final boolean SHOW_PREY_NUM = true;
    
    //Prey antenna size scale, multiplied with calulcated length
    public final double ANTENNA_SIZE_SCALE = 0.12;  
    
    //Offset for starting and ending points of antenna
    public final double ANTENNA_DRAW_OFFSET = 0.5;
    
    /**
     * Constructor
     * @param spawnPos Spawn position in the world.
     */
    public Minnow(Vector2D spawnPos){
        //Invoke superclass constructor
        super(spawnPos);
    }
    /**
     * Override render method of parent super class Prey
     * @param bTrueFalse Used by Vehicle super class
     */
    @Override
    public void Render(boolean bTrueFalse) {
        //Invoke superclass constructor
        super.Render(bTrueFalse);
        
        //Draw prey antenna in foreground
        drawAntenna();

        //Draw prey count in foreground
        drawPreyCount();
    }
    
    //Show number of remaining prey in game world
    protected void drawPreyCount() {
        //Check if parameter is set true
        if (SHOW_PREY_NUM) {
            // Get number of preys in game world
            int preyNum = this.m_pWorld.GetPrey().size() - 1;
            
            //Use grey text color
            gdi.TextColor(Cgdi.grey);
            
            //Draw text at position using graphics context
            gdi.TextAtPos(
                this.m_pWorld.cxClient() - 55, 
                this.m_pWorld.cyClient() - 20, 
                "Prey: " + preyNum
            );
        }
    }
    
    /**
     * Draw steering antenna at the head of the prey in direction of
     * velocity and proportional to speed
     */
    protected void drawAntenna() {
        //Prey heading
        Vector2D preyHeading = this.Heading();
        
        //Heading angle
        double headingAngle = Math.atan2(preyHeading.y, preyHeading.x);
        
        //Antenna length
        double aLength = ANTENNA_SIZE_SCALE * this.m_vVelocity.Length();
        
        //Antenna start x and y position
        int pAntennaStartY = (int) (this.Pos().y + ANTENNA_DRAW_OFFSET);
        int pAntennaStartX = (int) (this.Pos().x + ANTENNA_DRAW_OFFSET);
        
        //Calculate the end location of the antenna.
        //End location of for x and y can be determined
        //using trigonomety and algebra, taking the 
        //previously computed antenna length (antennaLength),
        //the angle of heading (headingAngle) and starting x and y:
        //pAntennaStartY + (aLength * sin(headingAngle)) for final y
        //pAntennaStartX + (aLength * cos(headingAngle)) for final x
        int pAntennaEndY = (int) (pAntennaStartY + (aLength * Math.sin(headingAngle)));
        int pAntennaEndX = (int) (pAntennaStartX + (aLength * Math.cos(headingAngle)));
        
        //Offset
        pAntennaEndY += ANTENNA_DRAW_OFFSET;
        pAntennaEndX += ANTENNA_DRAW_OFFSET;
        
        //Get graphics context for drawing Antenna
        Graphics2D antennaGraphics = gdi.GetHdc();
        
        //Set antenna color
        antennaGraphics.setColor(Color.RED);
        
        //Draw antenna in foreground
        antennaGraphics.drawLine(pAntennaStartX, pAntennaStartY, pAntennaEndX, pAntennaEndY);
    }
}
