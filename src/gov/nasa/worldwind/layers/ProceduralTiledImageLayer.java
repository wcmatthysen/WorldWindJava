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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Patrick Murris
 */
public abstract class ProceduralTiledImageLayer extends BasicTiledImageLayer
{
    public ProceduralTiledImageLayer(LevelSet levelSet)
    {
        super(levelSet);
    }

    public ProceduralTiledImageLayer(AVList params)
    {
        super(params);
    }

    abstract BufferedImage createTileImage(TextureTile tile, BufferedImage image);

    @Override
    protected void retrieveTexture(final TextureTile tile, DownloadPostProcessor postProcessor)
    {
        final File outFile = WorldWind.getDataFileStore().newFile(tile.getPath());
        if (outFile == null || outFile.exists())
            return;

        // Create and save tile texture image.
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        image = createTileImage(tile, image);
        try
        {
            ImageIO.write(image, "png", outFile);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionSavingRetrievedTextureFile", outFile.getPath());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
        }
    }
}
