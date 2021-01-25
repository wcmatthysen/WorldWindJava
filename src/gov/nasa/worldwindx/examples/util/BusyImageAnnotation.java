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
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.GL2;

public class BusyImageAnnotation extends ImageAnnotation
{
    protected Angle angle;
    protected Angle increment;
    protected long lastFrameTime;

    public BusyImageAnnotation(Object imageSource)
    {
        super(imageSource);
        this.setUseMipmaps(false);

        this.angle = Angle.ZERO;
        this.increment = Angle.fromDegrees(300);
    }

    public Angle getAngle()
    {
        return this.angle;
    }

    public void setAngle(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double a = angle.degrees % 360;
        a = (a > 180) ? (a - 360) : (a < -180 ? 360 + a : a);
        this.angle = Angle.fromDegrees(a);
    }

    public Angle getIncrement()
    {
        return this.increment;
    }

    public void setIncrement(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.increment = angle;
    }

    public void drawContent(DrawContext dc, int width, int height, double opacity, Position pickPosition)
    {
        super.drawContent(dc, width, height, opacity, pickPosition);
        this.updateState(dc);
    }

    protected void transformBackgroundImageCoordsToAnnotationCoords(DrawContext dc, int width, int height,
        WWTexture texture)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Rotate around an axis originating from the center of the image and coming out of the screen.
        double hw = (double) texture.getWidth(dc) / 2d;
        double hh = (double) texture.getHeight(dc) / 2d;
        gl.glTranslated(hw, hh, 0);
        gl.glRotated(-this.getAngle().degrees, 0, 0, 1);
        gl.glTranslated(-hw, -hh, 0);

        super.transformBackgroundImageCoordsToAnnotationCoords(dc, width, height, texture);
    }

    protected void updateState(DrawContext dc)
    {
        // Increment the angle by a fixed increment each frame.
        Angle increment = this.getIncrement();
        increment = this.adjustAngleIncrement(dc, increment);
        this.setAngle(this.getAngle().add(increment));

        // Fire a property change to force a repaint.
        dc.getView().firePropertyChange(AVKey.VIEW, null, dc.getView());

        // Update the frame time stamp.
        this.lastFrameTime = dc.getFrameTimeStamp();
    }

    protected Angle adjustAngleIncrement(DrawContext dc, Angle unitsPerSecond)
    {
        long millis = dc.getFrameTimeStamp() - this.lastFrameTime;
        double seconds = millis / 1000.0;
        double degrees = seconds * unitsPerSecond.degrees;

        return Angle.fromDegrees(degrees);
    }
}