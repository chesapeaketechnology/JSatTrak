package jsattrak.utilities;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.worldwind.geom.SphereObject;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.Color;
import java.util.Hashtable;

/**
 * Abstract base class for common aspects of rederables for the various orbit models
 *
 * @author Proprietary information subject to the terms of a Non-Disclosure Agreement
 * @since CTI-1.0.0
 */
public abstract class AModelRenderable implements Renderable
{
    public enum RenderableIconType
    {
        THREE_DIMENSIONAL, SPHERE, CUSTOM, NONE
    }

    protected static final int DEFAULT_SPHERE_ICON_SIZE = 100000; // Underscore not supported in JDK 1.6
    protected static final int DEFAULT_FILL_TRANSPARENCY = 122;
    // Sphere Object
    protected SphereObject sphereIcon = new SphereObject(new Vec4(0, 0, 0, 0), DEFAULT_SPHERE_ICON_SIZE, true);

    private int fillTransparency = DEFAULT_FILL_TRANSPARENCY;

    // hashtable of satellites to render
    protected final Hashtable<String, AbstractSatellite> satelliteHashtable;

    // hastable of ground stations to render
    protected final Hashtable<String, GroundStation> groundStationHashtable;

    // WorldWind globe
    protected final Globe globe;

    protected final RenderableIconType renderableIconType;

    public AModelRenderable(Hashtable<String, AbstractSatellite> satelliteHashtable, Hashtable<String, GroundStation> groundStationHashtable, Globe globe, RenderableIconType renderableIconType)
    {
        this.satelliteHashtable = satelliteHashtable;
        this.groundStationHashtable = groundStationHashtable;
        this.globe = globe;
        this.renderableIconType = renderableIconType;
    }

    public AModelRenderable(Hashtable<String, AbstractSatellite> satelliteHashtable, Hashtable<String, GroundStation> groundStationHashtable, Globe globe)
    {
        this(satelliteHashtable, groundStationHashtable, globe, RenderableIconType.SPHERE);
    }

    protected GL2 initializeGL2ForDrawContext(DrawContext dc) throws IllegalArgumentException
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        javax.media.opengl.GL2 gl = dc.getGL().getGL2();

        //gl.glEnable(GL.GL_TEXTURE_2D); // removed so the sun shading wouldn't effect line colors
        gl.glPushAttrib(javax.media.opengl.GL2.GL_TEXTURE_BIT | javax.media.opengl.GL2.GL_ENABLE_BIT | javax.media.opengl.GL2.GL_CURRENT_BIT);
        gl.glMatrixMode(javax.media.opengl.GL2.GL_MODELVIEW);

        // Added so that the colors wouldn't depend on sun shading
        gl.glDisable(GL.GL_TEXTURE_2D);

        return gl;
    }

    public RenderableIconType getRenderableIconType()
    {
        return renderableIconType;
    }

    public int getFillTransparency()
    {
        return fillTransparency;
    }

    public void setFillTransparency(int fillTransparency)
    {
        if (fillTransparency < 0 || fillTransparency > 255)
        {
            throw new IllegalArgumentException("Invalid fillTransparency, must be in the range 0 to 255 but a value of " + fillTransparency + " was provided.");
        }
        this.fillTransparency = fillTransparency;
    }

    protected void setGlColor (GL2 gl, Color color)
    {
        gl.glColor3f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
    }
}
