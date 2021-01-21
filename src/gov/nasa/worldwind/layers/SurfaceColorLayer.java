/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.SurfaceImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * @author Wiehann Matthysen
 */
public final class SurfaceColorLayer extends SurfaceImageLayer
{
    private SurfaceImage image;
    
    public SurfaceColorLayer(Color color)
    {
        image = new SurfaceImage(createColorImage(color), Sector.FULL_SPHERE);
        addRenderable(image);
    }
    
    public SurfaceColorLayer()
    {
        this(Color.BLACK);
    }
    
    public void setColor(Color color)
    {
        image.setImageSource(createColorImage(color), Sector.FULL_SPHERE);
    }
    
    private static BufferedImage createColorImage(Color color)
    {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); 
        Graphics2D graphics = (Graphics2D) image.getGraphics(); 
        graphics.setColor(color);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight()); 
        graphics.dispose();
        return image;
    }
}
