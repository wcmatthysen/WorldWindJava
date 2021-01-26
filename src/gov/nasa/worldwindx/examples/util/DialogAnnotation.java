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

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: DialogAnnotation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DialogAnnotation extends GlobeAnnotation implements java.awt.event.ActionListener
{
    protected static final String CLOSE_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-cancel.png";
    protected static final String BUSY_IMAGE_PATH = "images/indicator-16.gif";
    protected static final String DEPRESSED_MASK_PATH
        = "gov/nasa/worldwindx/examples/images/16x16-button-depressed-mask.png";

    protected static final String CLOSE_TOOLTIP_TEXT = "Close window";

    protected boolean busy;
    protected ButtonAnnotation closeButton;
    protected ImageAnnotation busyImage;
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public DialogAnnotation(String text, Position position, Font font, Color textColor)
    {
        super(text, position, font, textColor);
        this.initComponents();
        this.layoutComponents();
        this.setBusy(false);
    }

    public DialogAnnotation(String text, Position position, Font font)
    {
        this(text, position, font, null);
    }

    public DialogAnnotation(String text, Position position)
    {
        this(text, position, null);
    }

    public DialogAnnotation(Position position)
    {
        this("", position);
    }

    public boolean isBusy()
    {
        return this.busy;
    }

    public void setBusy(boolean busy)
    {
        this.busy = busy;
        this.getBusyImage().getAttributes().setVisible(busy);
    }

    public ButtonAnnotation getCloseButton()
    {
        return this.closeButton;
    }

    public ImageAnnotation getBusyImage()
    {
        return this.busyImage;
    }

    public java.awt.event.ActionListener[] getActionListeners()
    {
        return this.listenerList.getListeners(java.awt.event.ActionListener.class);
    }

    public void addActionListener(java.awt.event.ActionListener listener)
    {
        this.listenerList.add(java.awt.event.ActionListener.class, listener);
    }

    public void removeActionListener(java.awt.event.ActionListener listener)
    {
        this.listenerList.remove(java.awt.event.ActionListener.class, listener);
    }

    //**************************************************************//
    //********************  Action Listener  ***********************//
    //**************************************************************//

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        // Notify my listeners of the event.
        this.fireActionPerformed(e);
    }

    protected void fireActionPerformed(java.awt.event.ActionEvent e)
    {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == java.awt.event.ActionListener.class)
            {
                ((java.awt.event.ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    //**************************************************************//
    //********************  Annotation Components  *****************//
    //**************************************************************//

    protected void initComponents()
    {
        this.closeButton = new ButtonAnnotation(CLOSE_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.closeButton.setActionCommand(AVKey.CLOSE);
        this.closeButton.addActionListener(this);
        this.closeButton.setToolTipText(CLOSE_TOOLTIP_TEXT);

        this.busyImage = new BusyImageAnnotation(BUSY_IMAGE_PATH);
    }

    protected void layoutComponents()
    {
        AnnotationNullLayout layout = new AnnotationNullLayout();
        this.setLayout(layout);
        this.addChild(this.busyImage);
        this.addChild(this.closeButton);
        layout.setConstraint(this.busyImage, AVKey.NORTHWEST);
        layout.setConstraint(this.closeButton, AVKey.NORTHEAST);
    }

    protected void setupContainer(Annotation annotation)
    {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setAdjustWidthToText(AVKey.SIZE_FIXED);
        defaultAttribs.setSize(new java.awt.Dimension(0, 0));

        annotation.setPickEnabled(false);
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    protected void setupLabel(Annotation annotation)
    {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        annotation.setPickEnabled(false);
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    protected void setupDefaultAttributes(AnnotationAttributes attributes)
    {
        java.awt.Color transparentBlack = new java.awt.Color(0, 0, 0, 0);

        attributes.setBackgroundColor(transparentBlack);
        attributes.setBorderColor(transparentBlack);
        attributes.setBorderWidth(0);
        attributes.setCornerRadius(0);
        attributes.setDrawOffset(new java.awt.Point(0, 0));
        attributes.setHighlightScale(1);
        attributes.setInsets(new java.awt.Insets(0, 0, 0, 0));
        attributes.setLeader(AVKey.SHAPE_NONE);
    }
}
