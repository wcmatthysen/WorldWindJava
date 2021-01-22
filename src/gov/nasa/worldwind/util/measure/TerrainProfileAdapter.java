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
package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.Logging;

import java.beans.*;
import java.util.ArrayList;

/**
 * Adapter that forwards control-point position changes from a {@link MeasureTool}
 * to a {@link TerrainProfileLayer} so that the height-data along the measured
 * path can be visualized.
 * 
 * @author Wiehann Matthysen
 */
public class TerrainProfileAdapter implements PropertyChangeListener
{
    protected WorldWindow wwd;
    protected TerrainProfileLayer profileLayer;
    
    /**
     * Construct an adapter for the specified <code>WorldWindow</code> and <code>TerrainProfileLayer</code>.
     *
     * @param wwd the <code>WorldWindow</code> the specified layer is associated with.
     * @param layer <code>TerrainProfileLayer</code> to forward control-point events to.
     */
    public TerrainProfileAdapter(WorldWindow wwd, TerrainProfileLayer layer)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.wwd = wwd;
        this.profileLayer = layer;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        MeasureTool measureTool = (MeasureTool)event.getSource();
        // Measure shape position list changed - update terrain profile
        if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
        {
            ArrayList<? extends LatLon> positions = measureTool.getPositions();
            if (positions != null && positions.size() > 1)
            {
                this.profileLayer.setPathPositions(positions);
                this.profileLayer.setEnabled(true);
            } else
            {
                this.profileLayer.setEnabled(false);
            }
            this.wwd.redraw();
        }
    }
}
