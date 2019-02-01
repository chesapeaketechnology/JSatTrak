/*
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package name.gano.worldwind.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitView;


import javax.media.opengl.GL;
import jsattrak.objects.AbstractSatellite;

/**
 * @author dcollins
 * @version $Id: BasicOrbitView.java 5276 2008-05-02 04:33:57Z dcollins $
 */
public class BasicModelView3 extends BasicOrbitView implements OrbitView
{
   
    // ofsets
    private double xOffset = 0;
    private double yOffset = 0;
    private double zOffset = 0;
    private double XYOffsetMultiplier = 0.05*Math.PI/180.0;//2500; // amount to increment each time
    private double ZOffsetMultiplier = 2500;
    // satellite object to follow
    private AbstractSatellite sat;

    OrbitView orbitViewModel;

    public BasicModelView3()
    {
        this(new BasicOrbitView());
    }
    
    // --- constructor
    public BasicModelView3(AbstractSatellite sat)
    {
        this(new BasicOrbitView());
        this.sat = sat;
    }

    public BasicModelView3(OrbitView orbitViewModel)
    {
        if (orbitViewModel == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewModelIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.orbitViewModel = orbitViewModel;
        this.collisionSupport.setCollisionThreshold(COLLISION_THRESHOLD);
        this.collisionSupport.setNumIterations(COLLISION_NUM_ITERATIONS);
        loadConfigurationValues();
    }
    
    // last constructor
    public BasicModelView3(OrbitView orbitViewModel, AbstractSatellite sat)
    {
        this.sat = sat;
        
        if (orbitViewModel == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewModelIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.orbitViewModel = orbitViewModel;
        this.collisionSupport.setCollisionThreshold(COLLISION_THRESHOLD);
        this.collisionSupport.setNumIterations(COLLISION_NUM_ITERATIONS);
        loadConfigurationValues();        
    }



   

    public OrbitView getOrbitViewModel()
    {
        return this.orbitViewModel;
    }

    private static Position normalizedPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new Position(
                Angle.normalizedLatitude(position.getLatitude()),
                Angle.normalizedLongitude(position.getLongitude()),
                position.getElevation());
    }

   


    public Position getEyePosition()
    {
        if (this.lastEyePosition == null)
            this.lastEyePosition = computeEyePositionFromModelview();
        return this.lastEyePosition;
    }

   




    public double getAutoNearClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeNearDistance(eyePos);
    }

    public double getAutoFarClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeFarDistance(eyePos);
    }

   


    protected void doApply(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Update DrawContext and Globe references.
        this.dc = dc;
        this.globe = this.dc.getGlobe();
        
        // ALWAYS SET CENTER -- SEG
        if(sat == null)
        {
            // if no sat set -- use default location
            setCenterPosition(Position.fromRadians(0+xOffset, 0+yOffset, 750000+zOffset));
            //Vec4 v = new Vec4(0+getXOffset(),0+getYOffset(),750000+getZOffset()+globe.getRadiusAt(Angle.ZERO,Angle.ZERO));
            //Position p = globe.computePositionFromPoint(v);
            //setCenterPosition(p);
        }
        else // sat exisits
        {   
            //Vec4 v = globe.computePointFromPosition(Angle.fromRadians(sat.getLatitude()), Angle.fromRadians(sat.getLongitude()), sat.getAltitude());
            ////Vec4 v = new Vec4(-sat.getPosMOD()[0]+getXOffset(),sat.getPosMOD()[2]+getZOffset(),sat.getPosMOD()[1]+getYOffset());
            //Vec4 v2 = v.add3(new Vec4(xOffset,yOffset,zOffset));
 
            Vec4 v = globe.computePointFromPosition(Angle.fromRadians(sat.getLatitude()+xOffset), Angle.fromRadians(sat.getLongitude()+yOffset), sat.getAltitude()+zOffset);
            
            Position p = globe.computePositionFromPoint(v); //v2
            //setCenterPosition(Position.fromDegrees(sat.getLatitude()*180.0/Math.PI, sat.getLongitude()*180.0/Math.PI, sat.getAltitude()));
            setCenterPosition(p);
        }
        
        //========== modelview matrix state ==========//
        // Compute the current modelview matrix.
        this.modelview = ViewUtil.computeTransformMatrix(this.globe, this.center,
                this.heading, this.pitch, this.roll);
        if (this.modelview == null)
            this.modelview = Matrix.IDENTITY;
        // Compute the current inverse-modelview matrix.
        this.modelviewInv = this.modelview.getInverse();
        if (this.modelviewInv == null)
            this.modelviewInv = Matrix.IDENTITY;

        //========== projection matrix state ==========//
        // Get the current OpenGL viewport state.
        int[] viewportArray = new int[4];
        this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
        this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
        // Compute the current clip plane distances.
        double nearDistance = this.nearClipDistance > 0 ? this.nearClipDistance : getAutoNearClipDistance();
        double farDistance = this.farClipDistance > 0 ? this.farClipDistance : getAutoFarClipDistance();
        // Compute the current projection matrix.
        this.projection = Matrix.fromPerspective(this.fieldOfView,
                this.viewport.getWidth(), this.viewport.getHeight(),
                nearDistance, farDistance);
        // Compute the current frustum.
        this.frustum = Frustum.fromPerspective(this.fieldOfView,
                (int) this.viewport.getWidth(), (int) this.viewport.getHeight(),
                nearDistance, farDistance);

        //========== load GL matrix state ==========//
        loadGLViewState(dc, this.modelview, this.projection);

        //========== after apply (GL matrix state) ==========//
        afterDoApply();
    }

    @Override
    protected void afterDoApply()
    {
        // Clear cached computations.
        this.lastEyePosition = null;
        this.lastEyePoint = null;
        this.lastUpVector = null;
        this.lastForwardVector = null;
        this.lastFrustumInModelCoords = null;
    }

   

    

   

    public double getXOffset()
    {
        return xOffset;
    }

    public void setXOffset(double xOffset)
    {
        this.xOffset = xOffset;
    }

    public double getYOffset()
    {
        return yOffset;
    }

    public void setYOffset(double yOffset)
    {
        this.yOffset = yOffset;
    }

    public double getZOffset()
    {
        return zOffset;
    }

    public void setZOffset(double zOffset)
    {
        this.zOffset = zOffset;
    }
    
    public void resetOffsets()
    {
        setZOffset(0);
        setYOffset(0);
        setXOffset(0);
    }
    
    public void incrementXOffset(double multiplierPosNeg)
    {
        xOffset += XYOffsetMultiplier*multiplierPosNeg;
    }
    
    public void incrementYOffset(double multiplierPosNeg)
    {
        yOffset += XYOffsetMultiplier*multiplierPosNeg;
    }
    
    public void incrementZOffset(double multiplierPosNeg)
    {
        zOffset += ZOffsetMultiplier*multiplierPosNeg;
    }

    public // satellite object to follow
    AbstractSatellite getSat()
    {
        return sat;
    }

    public void setSat(AbstractSatellite sat)
    {
        this.sat = sat;
    }


    
}