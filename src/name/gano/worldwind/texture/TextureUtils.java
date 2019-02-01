/*
 * TextureUtils.java
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
 * Created on September 23, 2007, 1:13 PM
 *
 * Collection of static methods dealing with JOGL textures
 */

package name.gano.worldwind.texture;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.render.DrawContext;
import java.io.File;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 *
 * @author Shawn
 */
public class TextureUtils
{
    
    public static Texture loadTexture(DrawContext dc, String fnm)
    {
        String fileName = "images/" + fnm;
        Texture tex = null;
        try
        {
            tex = TextureIO.newTexture( new File(fileName), false);
            
            tex.setTexParameteri(dc.getGL().getGL2(),GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
            tex.setTexParameteri(dc.getGL().getGL2(),GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        }
        catch(Exception e)
        { 
            System.out.println("Error loading texture " + fileName);  
            e.printStackTrace();
        }
        
        return tex;
    }  // end of loadTexture()

    
}
