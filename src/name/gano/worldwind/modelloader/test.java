/*
 * ModelLayerTest.java
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
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;


import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;
import java.awt.Color;
import java.util.Hashtable;

import java.util.Random;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.CustomSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.ECEFModelRenderable;
import jsattrak.utilities.OrbitModelRenderable;
import jsattrak.utilities.TLE;
import name.gano.astro.time.Time;
import name.gano.worldwind.layers.Earth.CoverageRenderableLayer;
import name.gano.worldwind.layers.Earth.ECEFRenderableLayer;
import name.gano.worldwind.layers.Earth.ECIRenderableLayer;
import name.gano.worldwind.layers.Earth.EcefTimeDepRenderableLayer;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public class test extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                Model3DLayer_new layer = new Model3DLayer_new();
//                layer.setMaitainConstantSize(true);
//                layer.setSize(300000);
                
                Random generator = new Random();
//                Model modelOBJ = ModelFactory.createModel("test/data/penguin.obj");
//                for (int i=0; i<100; i++) {
//                    layer.addModel(new WWModel3D(modelOBJ,
//                            new Position(Angle.fromDegrees(generator.nextInt()%80),
//                                         Angle.fromDegrees(generator.nextInt()%180),
//                                         500000)));
//                }
                
//                Model model3DS = ModelFactory.createModel("test/data/globalstar/Globalstar.3ds");
//                model3DS.setUseLighting(false); // turn off lighting!
////                for (int i=0; i<100; i++) {
////                    layer.addModel(new WWModel3D(model3DS,
////                            new Position(Angle.fromDegrees(generator.nextInt()%80),
////                                         Angle.fromDegrees(generator.nextInt()%180),
////                                         750000)));
////                }
//                
//                WWModel3D_new model3D = new WWModel3D_new(model3DS,
//                            new Position(Angle.fromDegrees(0),
//                                         Angle.fromDegrees(0),
//                                         750000));
//                model3D.setMaitainConstantSize(true);
//                model3D.setSize(300000);
//                
//                layer.addModel(model3D);
                // hastable to store all the Ground Stations
     Hashtable<String,GroundStation> gsHash = new Hashtable<String,GroundStation>();
                
                // Coverage Data Layer
                //CoverageRenderableLayer cel = new CoverageRenderableLayer(app.getCoverageAnalyzer());
        //cel.setEnabled(false); // off by default
                //insertBeforeCompass(this.getWwd(),cel);
     
  




     
     
                Hashtable<String, AbstractSatellite> satHash = new Hashtable<String, AbstractSatellite>();
                 //CustomSatellite prop = new CustomSatellite("poot",new Time());
                 
                 
                 
                 TLE newTLE = new TLE("ISS","1 25544U 98067A   09160.12255947  .00017740  00000-0  12823-3 0    24","2 25544  51.6405 348.2892 0009223  92.2562   9.3141 15.73542580604683");

        // Julian Date we are interested in
        double julianDate = 2454992.0; // 09 Jun 2009 12:00:00.000 UTC

        // Create SGP4 satelite propogator
        AbstractSatellite prop = null;
        try
        {
            prop = new CustomSatellite(newTLE.getSatName(), new Time());
            prop.propogate2JulDate(julianDate);
            prop.setShowGroundTrack(true); // if we arn't using the JSatTrak plots midas well turn this off to save CPU time
        }
        catch(Exception e)
        {
            System.out.println("Error Creating SGP4 Satellite");
            System.exit(1);
        }

        // prop to the desired time
        prop.propogate2JulDate(julianDate);

                 
                 
                 
                 prop.setShow3DOrbitTrace(true);
                 // display settings for the ISS
                 prop.setShow3D(true);
prop.setShow3DFootprint(false);
prop.setShow3DName(false);
prop.setSatColor(Color.GREEN);
prop.setGroundTrackLagPeriodMultiplier(1);
prop.setGroundTrackLeadPeriodMultiplier(2);
prop.setGrnTrkPointsPerPeriod(131); //  smoother line than the 81 default
prop.setGroundTrackIni2False(); // flag so satellite recalculates ground track now (not later)

// set default 3d model and turn on the use of 3d models
prop.setThreeDModelPath("/isscomplete/iss_complete.3ds");
prop.setUse3dModel(true);
                 satHash.put("poot", prop);
                double currentMJD;

                // add EcefTimeDepRenderableLayer layer
//                EcefTimeDepRenderableLayer timeDepLayer = new EcefTimeDepRenderableLayer(currentMJD,app);
//        m.getLayers().add(timeDepLayer);
                currentMJD = new Time().getMJD();
                // add ECI Layer -- FOR SOME REASON IF BEFORE EFEF and turned off ECEF Orbits don't show up!! Coverage effecting this too, strange
                ECIRenderableLayer eciLayer = new ECIRenderableLayer(currentMJD); // create ECI layer
                OrbitModelRenderable orbitModel = new OrbitModelRenderable(satHash, this.getWwd().getModel().getGlobe());
        eciLayer.addRenderable(orbitModel); // add renderable object
        eciLayer.setCurrentMJD(currentMJD); // update time again after adding renderable
        insertBeforeCompass(this.getWwd(),eciLayer); // add ECI Layer
                Renderable eciRadialGrid;
        //eciLayer.addRenderable(eciRadialGrid); // add grid (optional if it is on or not)
                // add ECEF Layer
                ECEFRenderableLayer ecefLayer = new ECEFRenderableLayer(); // create ECEF layer
                ECEFModelRenderable ecefModel = new ECEFModelRenderable(satHash, gsHash, this.getWwd().getModel().getGlobe());
        ecefLayer.addRenderable(ecefModel); // add renderable object
        insertBeforeCompass(this.getWwd(),ecefLayer); // add ECI Layer
                
                insertBeforeCompass(this.getWwd(), layer);
                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Model Layer Test", AppFrame.class);
    }
}
