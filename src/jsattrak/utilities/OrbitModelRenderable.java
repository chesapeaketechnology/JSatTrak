/*
 * OrbitModel.java
 *
 * aka ECIModelRenderable - draws models  in ECI coordinates using ECIRenderableLayer
 *=====================================================================
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
 *
 */

package jsattrak.utilities;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.astro.MathUtils;
import name.gano.worldwind.geom.Cone;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Shawn E. Gano
 */
public class OrbitModelRenderable extends AModelRenderable
{

    protected static final double CONE_HEIGHT_SCALING_FACTOR = 0.990;
    protected Cone coverage3dCone;

    public OrbitModelRenderable(Hashtable<String, AbstractSatellite> satHash, Globe globe, RenderableIconType renderableIconType)
    {
        // <> not supported in JDK 1.6
        super(satHash, new Hashtable<String, GroundStation>(), globe, renderableIconType);
    }

    public OrbitModelRenderable(Hashtable<String, AbstractSatellite> satHash, Globe globe)
    {
        // <> not supported in JDK 1.6
        super(satHash, new Hashtable<String, GroundStation>(), globe);

        Color coneColor = Color.GREEN;
        coverage3dCone = new Cone(globe, 45, -90, 1000000, 2000000, Angle.fromDegrees(0), Angle.fromDegrees(-90), coneColor);
    }

    @Override
    public void render(DrawContext dc)
    {
        GL2 gl = initializeGL2ForDrawContext(dc);

        // for each satellite

        Set<AbstractSatellite> satellitesToRender = new CopyOnWriteArraySet<AbstractSatellite>(satelliteHashtable.values());

        for (AbstractSatellite satellite : satellitesToRender) // search through all sat nodes
        {
            // set color
            Color satelliteColor = satellite.getSatColor();
            gl.glColor3f(satelliteColor.getRed() / 255.0f, satelliteColor.getGreen() / 255.0f, satelliteColor.getBlue() / 255.0f);

            if (shouldRenderLaggingOrbit(satellite))
            {
                renderLaggingOrbitPath(gl, satellite);
            }
            if (shouldRenderLeadingOrbit(satellite))
            {
                renderLeadingOrbitPath(gl, satellite);
            } // show orbit trace

            // 3D satellite icon model is rendered Here
            if (shouldRender3dSatelliteModel(satellite))
            {
                satellite.getThreeDModel().render(dc); // render satellite model
            } else if (shouldRenderSphericalIcon(satellite))
            {
                renderSphericalIcon(satellite, dc);
            }

            // draw name
            if (shouldRender3dName(satellite))
            {
                render3dSatelliteName(dc, satellite);
            }

            // draw earth footprint
            if (shouldRender3dFootprint(satellite))
            {
                render3dFootprint(dc, satellite);
            }
        } // for each sat

        gl.glPopAttrib();
    } // render

    protected void render3dFootprint(DrawContext dc, AbstractSatellite satellite)
    {
        Color satelliteColor = satellite.getSatColor();
        double[] xyz = satellite.getTEMEPos();
        double[] lla = satellite.getLLA();
        if (lla != null && xyz != null && xyz.length >= 3)
        {
//                    surfCirc.setCenter(LatLon.fromRadians(lla[0], lla[1]));
//                    surfCirc.setRadius(calcFootPrintRadiusFromAlt(lla[2]));
//                    surfCirc.setBorderColor(satColor);
//                    surfCirc.setPaint(new Color(satColor.getRed(), satColor.getGreen(), satColor.getBlue(), circleViewTransparency)); // sets interior color
//                    surfCirc.render(dc);

            // test cone
            //cone.setLatLonRadians( lla[0] , lla[1], lla[2] );
            if (coverage3dCone == null)
            {
                coverage3dCone = new Cone(globe, 45, -90, 1000000, 2000000, Angle.fromDegrees(0), Angle.fromDegrees(-90), satelliteColor);
            }
            coverage3dCone.setVertexPosition(-xyz[0], xyz[2], xyz[1]);
            double[] rh = calcConeRadiusHeightFromAlt(lla[2]);
            coverage3dCone.setGroundRange(rh[0]);
            coverage3dCone.setColor(satelliteColor);
            coverage3dCone.setHeight(rh[1] * CONE_HEIGHT_SCALING_FACTOR); // minus a little because of rendering artifacts
            coverage3dCone.render(dc);
        }
    }

    protected void render3dSatelliteName(DrawContext dc, AbstractSatellite satellite)
    {
        // this may be REALLY slow creating this every repaint! - maybe store in sat object and update its position?
        AnnotationAttributes geoAttr = createFontAttribs(satellite.getSatColor());
        GlobeAnnotation an = new GlobeAnnotation(satellite.getName(), Position.fromRadians(satellite.getLatitude(), satellite.getLongitude(), satellite.getAltitude()), geoAttr);

        // annotation - without any attribs, gives a bubble box
        // annotation doesn't strech well in GLCanvas
        //GlobeAnnotation an = new GlobeAnnotation(gs.getStationName(), Position.fromDegrees(gs.getLatitude(), gs.getLongitude(), gs.getAltitude()));
        an.render(dc);
    }

    protected void renderLeadingOrbitPath(GL2 gl, AbstractSatellite satellite)
    {
        // plot lead orbit
        gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
        for (int i = 0; i < satellite.getNumGroundTrackLeadPts(); i++)
        {
            // add next Mean of Date vertex
            double[] xyz = satellite.getGroundTrackXyzLeadPt(i);
            if (!Double.isNaN(xyz[0]))
            {
                gl.glVertex3f((float) -xyz[0], (float) xyz[2], (float) xyz[1]);
            }
        }
        gl.glEnd();
    }

    protected void renderLaggingOrbitPath(GL2 gl, AbstractSatellite satellite)
    {
        // plot lag orbit
        gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
        for (int i = 0; i < satellite.getNumGroundTrackLagPts(); i++)
        {
            // add next Mean of Date vertex
            double[] xyz = satellite.getGroundTrackXyzLagPt(i);
            if (!Double.isNaN(xyz[0]))
            {
                gl.glVertex3f((float) -xyz[0], (float) xyz[2], (float) xyz[1]);
            }
        }
        gl.glEnd();
    }

    protected boolean shouldRenderLeadingOrbit(AbstractSatellite satellite)
    {
        return satellite.isShow3DOrbitTrace() && satellite.isShow3DOrbitTraceECI();
    }

    protected boolean shouldRenderLaggingOrbit(AbstractSatellite satellite)
    {
        return satellite.isShow3DOrbitTrace() && satellite.isShow3DOrbitTraceECI();
    }

    protected boolean shouldRender3dSatelliteModel(AbstractSatellite satellite)
    {
        return satellite.isUse3dModel() &&
                satellite.getTEMEPos() != null &&
                !RenderableIconType.NONE.equals(renderableIconType) &&
                satellite.getThreeDModel() != null;
    }

    protected boolean shouldRenderSphericalIcon(AbstractSatellite satellite)
    {
        return RenderableIconType.SPHERE.equals(renderableIconType) && satellite.getTEMEPos() != null;
    }

    protected void renderSphericalIcon(AbstractSatellite satellite, DrawContext dc)
    {
        // default "sphere" for model
        double[] xyz = satellite.getTEMEPos();
        sphereIcon.setCenter(-xyz[0], xyz[2], xyz[1]);
        sphereIcon.render(dc);
    }

    protected boolean shouldRender3dName(AbstractSatellite satellite)
    {
        return satellite.isShow3DName();
    }

    protected boolean shouldRender3dFootprint(AbstractSatellite satellite)
    {
        return satellite.isShow3DFootprint();
    }

    private AnnotationAttributes createFontAttribs(Color textColor)
    {
        AnnotationAttributes geoAttr = new AnnotationAttributes();
        geoAttr.setFrameShape(FrameFactory.SHAPE_NONE);  // No frame
        geoAttr.setFont(Font.decode("Arial-ITALIC-12"));
        geoAttr.setTextColor(textColor);
//TODO            
        geoAttr.setTextAlign(AVKey.CENTER);
        geoAttr.setDrawOffset(new Point(0, 5)); // centered just above
//TODO
        geoAttr.setEffect(AVKey.TEXT_EFFECT_OUTLINE);  // Black outline
        geoAttr.setBackgroundColor(Color.BLACK);

        return geoAttr;
    } //createFontAttribs

    public double calcFootPrintRadiusFromAlt(double alt) // double lat, double lon, 
    {
        double earthRad = globe.getEquatorialRadius();
        double lambda0 = Math.acos(earthRad / (earthRad + alt));

        // length through earth from nadir point to horizon
        //double radius = earthRad*Math.sin(lambda0); 

        // arc length frp, nadir point to horizon edge
        //double arcLenght = lambda0/(2.0*Math.PI) * 2.0 * Math.PI * earthRad;
        double arcLenght = lambda0 * earthRad;

//        // projection length radius)
//        double projLenth = earthRad*Math.sin(lambda0);

        return arcLenght;
    }

    public double[] calcConeRadiusHeightFromAlt(double alt)
    {
        double[] rh = new double[2];

        double earthRad = globe.getEquatorialRadius();
        double lambda0 = Math.acos(earthRad / (earthRad + alt));

        // projection length radius)
        rh[0] = earthRad * Math.sin(lambda0);

        // height
        rh[1] = earthRad + alt - earthRad * Math.cos(lambda0);

        return rh;
    }

    public void updateMJD(double eciRotDeg)
    {
        for (AbstractSatellite sat : satelliteHashtable.values()) // search through all sat nodes
        {
            // set position 
            // DIES HERE IF NO 3D MODEL  - I.E. 3D model not selected either!
            if (sat.isUse3dModel())
            {
                if (sat.getThreeDModel() != null)
                {
                    sat.getThreeDModel().setPosition(new Position(Angle.fromRadians(sat.getLatitude()),
                            Angle.fromRadians(sat.getLongitude()),
                            sat.getAltitude()));
                    // set roll pitch yaw (assume user wants LVLH, velcorty aligned)

                    // calculate TEME velocity and set rotation angles and axis
                    if (sat.getTEMEPos() != null)
                    {
                        sat.getThreeDModel().setMainRotationAngleAxis(sat.getTEMEVelocity(), sat.getTEMEPos());

                        // set velcoity for test plotting
                        sat.getThreeDModel().velUnitVec = MathUtils.UnitVector(sat.getTEMEVelocity());
                    }

                    // Set ECI angle
                    sat.getThreeDModel().setEciRotAngleDeg(eciRotDeg);
                }
            } // 3D model
        } // for each sat
    } // update MJD
}
