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
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import java.util.Iterator;
import java.util.Vector;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import net.java.joglutils.model.DisplayListRenderer;
import net.java.joglutils.model.iModel3DRenderer;

/**
 *
 * @author RodgersGB
 */
public class Model3DLayer extends AbstractLayer {
    private Vector list;
    private boolean maitainConstantSize = false;
    private double size = 1;
    
    /** Creates a new instance of Model3DLayer */
    public Model3DLayer() {
        list = new Vector();
    }

    public void addModel(WWModel3D model) {
        list.add(model);
    }
    
    public void removeModel(WWModel3D model) {
        list.remove(model);
    }
    
    @Override
    protected void doRender(DrawContext dc) {
        try {
            beginDraw(dc);
            Iterator<WWModel3D> it = list.iterator();
            int x = 1;
            while (it.hasNext())
            {
            	//System.out.println(x + " rendering model " + it.next().getModel());
                x++;
                draw(dc, it.next());
            }
        }
        // handle any exceptions
        catch (Exception e) {
            // handle
            e.printStackTrace();
        }
        // we must end drawing so that opengl
        // states do not leak through.
        finally {
            endDraw(dc);
        }
    }
    
    // draw this layer
    protected void draw(DrawContext dc, WWModel3D model) {
        GL2 gl = dc.getGL().getGL2();
        
        Position pos = model.getPosition();
        if (model.getFollowTerrain())
        {
        	double elevation = dc.getGlobe().getElevation(pos.getLatitude(), pos.getLongitude());
        	if (model.isRealSize())
        	{
        		// Adjust position for 1/2 model height
        		elevation += model.getHeight() / 2.0;
        	}
                
        	pos = new Position(new LatLon(pos.getLatitude(),pos.getLongitude()), elevation);
        }
        	
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
        double localSize = this.computeSize(dc, loc);
        localSize *= model.computeSizeScale(dc, loc);
        
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            dc.getView().pushReferenceCenter(dc, loc);
            
            gl.glRotated(pos.getLongitude().degrees, 0,1,0);
            gl.glRotated(-pos.getLatitude().degrees, 1,0,0);
            gl.glScaled(localSize, localSize, localSize);
            
            //System.out.println("lsize = " + localSize + ", radius = " + model.getModel().getBounds().getRadius());
            
            gl.glRotated(model.getYaw(), 0,0,1);
            gl.glRotated(model.getPitch(), 1,0,0);
            gl.glRotated(model.getRoll(), 0,1,0);
            
            // Get an instance of the display list renderer
            iModel3DRenderer renderer = DisplayListRenderer.getInstance();
            renderer.render(gl, model.getModel());
            dc.getView().popReferenceCenter(dc);
        }
    }
    
    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        
        Vec4 cameraPosition = dc.getView().getEyePoint();
        
        gl.glPushAttrib(
            GL2.GL_TEXTURE_BIT |
            GL2.GL_COLOR_BUFFER_BIT |
            GL2.GL_DEPTH_BUFFER_BIT |
            GL2.GL_HINT_BIT |
            GL2.GL_POLYGON_BIT |
            GL2.GL_ENABLE_BIT | 
            GL2.GL_CURRENT_BIT | 
            GL2.GL_LIGHTING_BIT | 
            GL2.GL_TRANSFORM_BIT);

        //float[] lightPosition = {0F, 100000000f, 0f, 0f};
        float[] lightPosition =
            {(float) (cameraPosition.x + 1000), (float) (cameraPosition.y + 1000), (float) (cameraPosition.z + 1000), 1.0f};
        
        /** Ambient light array */
        float[] lightAmbient = {0.4f, 0.4f, 0.4f, 0.4f};
        /** Diffuse light array */
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        /** Specular light array */
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        
        float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f};
        
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);

        gl.glDisable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
    }
    
    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        
        gl.glMatrixMode(javax.media.opengl.GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        
        gl.glPopAttrib();
    }
    
    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.maitainConstantSize)
            return size;
        
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double currentSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (currentSize < 2)
            currentSize = 2;
        
        return currentSize;
    }

    public boolean isConstantSize() {
        return maitainConstantSize;
    }

    public void setMaitainConstantSize(boolean maitainConstantSize) {
        this.maitainConstantSize = maitainConstantSize;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
