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

import gov.nasa.worldwind.render.DeclutteringTextRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.util.Logging;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Layer to support objects of type {@link GeographicText}
 * 
 * @author Bruno Spyckerelle
 */
public class TextLayer extends AbstractLayer
{
    private final DeclutteringTextRenderer textRenderer;
    private final Collection<GeographicText> geographicTexts;

    public TextLayer()
    {
        this.textRenderer = new DeclutteringTextRenderer();
        this.geographicTexts = new ConcurrentLinkedQueue<GeographicText>();
    }

    /**
     * Adds the specified <code>text</code> to this layer's internal collection.
     * @param text {@link GeographicText} to add.
     * @throws IllegalArgumentException If <code>text</code> is null.
     */
    public void addGeographicText(GeographicText text)
    {
        if (text == null)
        {
            String msg = "nullValue.GeographicTextIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.geographicTexts.add(text);
    }

    public void addGeographicTexts(Iterable<? extends GeographicText> texts) {
        if (texts == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        for (GeographicText text : texts)
        {
            if (text != null)
            {
                this.geographicTexts.add(text);
            }
        }
    }

    public void removeGeographicText(GeographicText text)
    {
        if (text == null)
        {
            String msg = "nullValue.GeographicTextIsNull";
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.geographicTexts.remove(text);
    }

    public void removeAllGeographicTexts()
    {
        this.geographicTexts.clear();
    }

    public Iterable<GeographicText> getActiveGeographicTexts()
    {
        return this.geographicTexts;
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        this.textRenderer.render(dc, getActiveGeographicTexts());
    }
}
