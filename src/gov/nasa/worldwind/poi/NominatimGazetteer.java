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
package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Wiehann Matthysen
 */
public class NominatimGazetteer implements Gazetteer, ReverseLookupGazetteer
{
    private static final String DEF_NOMINATIM_GEOCODE_SERVICE = "https://nominatim.openstreetmap.org";

    private String geocodeService;

    public NominatimGazetteer()
    {
        this.geocodeService = Configuration.getStringValue(AVKey.NOMINATIM_GEOCODE_SERVICE, DEF_NOMINATIM_GEOCODE_SERVICE);
    }

    public void setGeocodeService(String geocodeService)
    {
        this.geocodeService = geocodeService;
    }

    public String getGeocodeService()
    {
        return this.geocodeService;
    }

    @Override
    public List<PointOfInterest> findPlaces(String lookupString) throws NoItemException, ServiceException
    {
        if (lookupString == null || lookupString.length() < 1)
        {
            return null;
        }

        String urlString;
        try
        {
            urlString = this.geocodeService + "/search?q=" + URLEncoder.encode(lookupString, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            urlString = this.geocodeService + "/search?q=" + lookupString.replaceAll(" ", "+");
        }

        urlString += "&format=xml&addressdetails=1";

        String locationString = POIUtils.callService(urlString);

        if (locationString == null || locationString.length() < 1)
        {
            return null;
        }

        return this.parseSearchString(locationString);
    }

    @Override
    public List<PointOfInterest> findPlaces(LatLon latlon) throws NoItemException, ServiceException
    {
        if (latlon == null)
        {
            return null;
        }

        String latStr = String.format("%.6f", latlon.getLatitude().getDegrees());
        String lonStr = String.format("%.6f", latlon.getLongitude().getDegrees());
        String urlString = this.geocodeService + "/reverse?lat=" + latStr + "&lon=" + lonStr;
        urlString += "&format=xml&addressdetails=1";

        String locationString = POIUtils.callService(urlString);

        if (locationString == null || locationString.length() < 1)
        {
            return null;
        }

        return this.parseReverseGeocodeString(locationString);
    }

    private void buildDisplayName(XPath xpath, org.w3c.dom.Node place, StringBuilder displayName) throws XPathExpressionException
    {
        String house = xpath.evaluate("house_number", place);
        if (house != null && !house.isEmpty())
        {
            displayName.append(house);
            displayName.append(" ");
        }

        String road = xpath.evaluate("road", place);
        if (road != null && !road.isEmpty())
        {
            displayName.append(road);
            displayName.append(", ");
        }

        String suburb = xpath.evaluate("suburb", place);
        if (suburb != null && !suburb.isEmpty())
        {
            displayName.append(suburb);
            displayName.append(", ");
        }

        String village = xpath.evaluate("village", place);
        if (village != null && !village.isEmpty())
        {
            displayName.append(village);
            displayName.append(", ");
        }

        String town = xpath.evaluate("town", place);
        if (town != null && !town.isEmpty())
        {
            displayName.append(town);
            displayName.append(", ");
        }

        String city = xpath.evaluate("city", place);
        if (city != null && !city.isEmpty())
        {
            displayName.append(city);
            displayName.append(", ");
        }

        String county = xpath.evaluate("county", place);
        if (county != null && !county.isEmpty())
        {
            displayName.append(county);
            displayName.append(", ");
        }

        String state = xpath.evaluate("state", place);
        if (state != null && !state.isEmpty())
        {
            displayName.append(state);
            displayName.append(", ");
        }

        String stateDistrict = xpath.evaluate("state_district", place);
        if (stateDistrict != null && !stateDistrict.isEmpty())
        {
            displayName.append(stateDistrict);
            displayName.append(", ");
        }

        String country = xpath.evaluate("country", place);
        displayName.append(country);
    }
    
    protected List<PointOfInterest> parseSearchString(String locationString) throws WWRuntimeException
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(locationString.getBytes("UTF-8")));

            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            org.w3c.dom.NodeList resultNodes =
                (org.w3c.dom.NodeList) xpath.evaluate("/searchresults/place", doc, XPathConstants.NODESET);

            List<PointOfInterest> positions = new ArrayList<PointOfInterest>(resultNodes.getLength());

            for (int i = 0; i < resultNodes.getLength(); i++)
            {
                org.w3c.dom.Node place = resultNodes.item(i);
                String lat = xpath.evaluate("@lat", place);
                String lon = xpath.evaluate("@lon", place);

                StringBuilder displayName = new StringBuilder();
                buildDisplayName(xpath, place, displayName);

                if (lat != null && lon != null)
                {
                    LatLon latlon = LatLon.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));
                    PointOfInterest loc = new BasicPointOfInterest(latlon);
                    loc.setValue(AVKey.DISPLAY_NAME, displayName.toString());
                    positions.add(loc);
                }
            }

            return positions;
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("Gazetteer.URLException", locationString);
            Logging.logger().log(Level.SEVERE, msg);
            throw new WWRuntimeException(msg);
        }
    }

    protected List<PointOfInterest> parseReverseGeocodeString(String reverseGeocodeString)
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(reverseGeocodeString.getBytes("UTF-8")));

            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            org.w3c.dom.NodeList resultNodes =
                (org.w3c.dom.NodeList) xpath.evaluate("/reversegeocode/result", doc, XPathConstants.NODESET);

            org.w3c.dom.NodeList addressNodes =
                (org.w3c.dom.NodeList) xpath.evaluate("/reversegeocode/addressparts", doc, XPathConstants.NODESET);

            List<PointOfInterest> positions = new ArrayList<PointOfInterest>(resultNodes.getLength());

            for (int i = 0; i < addressNodes.getLength(); i++)
            {
                org.w3c.dom.Node result = resultNodes.item(i);
                String lat = xpath.evaluate("@lat", result);
                String lon = xpath.evaluate("@lon", result);

                org.w3c.dom.Node address = addressNodes.item(i);

                StringBuilder displayName = new StringBuilder();
                buildDisplayName(xpath, address, displayName);

                if (lat != null && lon != null)
                {
                    LatLon latlon = LatLon.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));
                    PointOfInterest loc = new BasicPointOfInterest(latlon);
                    loc.setValue(AVKey.DISPLAY_NAME, displayName.toString());
                    positions.add(loc);
                }
            }

            return positions;
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("Gazetteer.URLException", reverseGeocodeString);
            Logging.logger().log(Level.SEVERE, msg);
            throw new WWRuntimeException(msg);
        }
    }
}
