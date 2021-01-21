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
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Demonstrates how to use the {@link gov.nasa.worldwindx.examples.util.ScreenSelector} utility to perform
 * multiple-object selection in screen space.
 *
 * @author dcollins
 * @version $Id: ScreenSelection.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ScreenSelection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ScreenSelector screenSelector;
        protected SelectionHighlightController selectionHighlightController;

        public AppFrame()
        {
            // Create a screen selector to display a screen selection rectangle and track the objects intersecting
            // that rectangle.
            this.screenSelector = new ScreenSelector(this.getWwd());

            // Set up a custom highlight controller that highlights objects both under the cursor and inside the
            // selection rectangle. Disable the superclass' default highlight controller to prevent it from interfering
            // with our highlight controller.
            this.selectionHighlightController = new SelectionHighlightController(this.getWwd(), this.screenSelector);
            this.getWwjPanel().highlightController.dispose();

            // Create a button to enable and disable screen selection.
            JButton btn = new JButton(new EnableSelectorAction());
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.add(btn, BorderLayout.CENTER);
            this.getControlPanel().add(panel, BorderLayout.SOUTH);

            // Create layer of highlightable shapes to select.
            this.addShapes();
        }

        protected void addShapes()
        {
            RenderableLayer layer = new RenderableLayer();

            ShapeAttributes highlightAttrs = new BasicShapeAttributes();
            highlightAttrs.setInteriorMaterial(Material.RED);
            highlightAttrs.setOutlineMaterial(Material.WHITE);

            for (int lon = -180; lon <= 170; lon += 10)
            {
                for (int lat = -60; lat <= 60; lat += 10)
                {
                    ExtrudedPolygon poly = new ExtrudedPolygon(Arrays.asList(
                        LatLon.fromDegrees(lat - 1, Angle.normalizedDegreesLongitude(lon - 1)),
                        LatLon.fromDegrees(lat - 1, Angle.normalizedDegreesLongitude(lon + 1)),
                        LatLon.fromDegrees(lat + 1, Angle.normalizedDegreesLongitude(lon + 1)),
                        LatLon.fromDegrees(lat + 1, Angle.normalizedDegreesLongitude(lon - 1))),
                        100000d);
                    poly.setHighlightAttributes(highlightAttrs);
                    poly.setSideHighlightAttributes(highlightAttrs);
                    layer.addRenderable(poly);
                }
            }

            this.getWwd().getModel().getLayers().add(layer);
        }

        protected class EnableSelectorAction extends AbstractAction
        {
            public EnableSelectorAction()
            {
                super("Start");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new DisableSelectorAction());
                screenSelector.enable();
            }
        }

        protected class DisableSelectorAction extends AbstractAction
        {
            public DisableSelectorAction()
            {
                super("Stop");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new EnableSelectorAction());
                screenSelector.disable();
            }
        }
    }

    public static void main(String[] args)
    {
        start("WorldWind Screen Selection", AppFrame.class);
    }
}
