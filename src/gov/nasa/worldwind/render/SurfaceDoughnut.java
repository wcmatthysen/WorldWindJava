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
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wiehann Matthysen
 */
public class SurfaceDoughnut extends SurfaceCircle
{
    protected double innerRadius;

    public void setInnerRadius(double to)
    {
        innerRadius = to;
    }    

    @Override
    protected List<LatLon> computeLocations(Globe globe, int intervals)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.majorRadius == 0 && this.minorRadius == 0) {
            return null;
        }

        boolean closed = this.theta.equals(Angle.POS360);

        int numIntervals = Math.max(MIN_NUM_INTERVALS, intervals);
        int numLocations = 1 + numIntervals;
        double da = (this.theta.radians) / (numLocations - 1);
        double globeRadius = globe.getRadiusAt(this.center.getLatitude(), this.center.getLongitude());

        List<LatLon> locations = new ArrayList<LatLon>(numLocations);

        // If the ellipse is not closed, start drawing from the center-position.
        if (!closed) {
            locations.add(this.center);
        }

        for (int i = 0; i < numLocations; i++)
        {
            double angle = 0.0;
            // If the ellipse is closed, snap angle to 0-degrees on final location.
            if (closed) {
                angle = (i != numIntervals) ? i * da : 0;
            } else {
                angle = i * da;
            }

            double xLength = this.majorRadius * Math.cos(angle);
            double yLength = this.minorRadius * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);

            // azimuth runs positive clockwise from north and through theta degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength)
                - this.heading.radians);

            locations.add(LatLon.greatCircleEndPosition(this.center, azimuth, distance / globeRadius));
        }

        // go in reverse
        for (int i = numLocations - 1; i >= 0; i--)
        {
            double angle = 0.0;
            // If the ellipse is closed, snap angle to 0-degrees on final location.
            if (closed) {
                angle = (i != numIntervals) ? i * da : 0;
            } else {
                angle = i * da;
            }

            double xLength = innerRadius * Math.cos(angle);
            double yLength = innerRadius * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);

            // azimuth runs positive clockwise from north and through theta degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength)
                - this.heading.radians);

            locations.add(LatLon.greatCircleEndPosition(this.center, azimuth, distance / globeRadius));
        }

        // If the ellipse is not closed, end at the center-position.
        if (!closed) {
            locations.add(this.center);
        }

        return locations;
    }
}
