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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.LevelSet;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Procedural height-map layer.
 * 
 * @author Patrick Murris
 */
public class HeightmapLayer extends ProceduralTiledImageLayer
{
    
    private final Globe globe;
    
    public HeightmapLayer(Globe globe)
    {
        super(makeLevels());
        this.globe = globe;
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();
        params.setValue(AVKey.TILE_WIDTH, 128);
        params.setValue(AVKey.TILE_HEIGHT, 128);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Heightmap");
        params.setValue(AVKey.DATASET_NAME, "Heightmap");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 10);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36.0), Angle.fromDegrees(36.0)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        return new LevelSet(params);
    }

    @Override
    protected BufferedImage createTileImage(TextureTile tile, BufferedImage image)
    {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        double latStep = tile.getSector().getDeltaLatDegrees() / height;
        double lonStep = tile.getSector().getDeltaLonDegrees() / width;

        for (int x = 0; x < width; x++)
        {
            double lon = tile.getSector().getMinLongitude().degrees + lonStep * x + (lonStep / 2.0);
            for (int y = 0; y < height; y++)
            {
                double lat = tile.getSector().getMaxLatitude().degrees - latStep * y - (latStep / 2.0);
                double elevation = this.globe.getElevation(Angle.fromDegrees(lat), Angle.fromDegrees(lon));
                double ratio = (elevation  - this.globe.getMinElevation()) / (this.globe.getMaxElevation() - this.globe.getMinElevation());
                float hue = (float)Math.atan(Math.pow(1.0 - ratio, 2.0));
                image.setRGB(x, y, Color.HSBtoRGB(hue, 1f, 1f));
            }
        }
        return image;
    }

    @Override
    public String toString()
    {
        return "Heightmap Layer";
    }
}
