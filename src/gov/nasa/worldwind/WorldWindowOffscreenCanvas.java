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
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.*;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.beans.*;
import java.util.*;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.opengl.*;

/**
 * <code>WorldWindowOffscreenCanvas</code> provides an offscreen, platform-independent canvas for displaying WorldWind
 * {@link Model}s (globe and layers). Construction options exist to specify a specific graphics device and to share
 * graphics resources with another graphics device.
 * <p>
 * Note: {@code WorldWindowOffscreenCanvas} is independent of any UI-toolkit library such as Swing or AWT. The actual
 * rendering is performed to an offscreen frame-buffer by making use of a {@link com.jogamp.opengl.GLOffscreenAutoDrawable}.
 * To retrieve an image of the offscreen WorldWindow, a call must to be made to {@link #redrawNow()} followed by
 * {@link com.jogamp.opengl.util.awt.AWTGLReadBufferUtil#readPixelsToBufferedImage(com.jogamp.opengl.GL, boolean)}.
 * The size of the offscreen WorldWindow can be set via the {@link #setSize(int, int)} method.
 * <p>
 * This class is capable of supporting stereo devices. To cause a stereo device to be selected and used, specify the
 * Java VM property "gov.nasa.worldwind.stereo.mode=device" prior to creating an instance of this class. A stereo
 * capable {@link SceneController} such as {@link gov.nasa.worldwind.StereoSceneController} must also be specified in
 * the WorldWind {@link Configuration}. The default configuration specifies a stereo-capable controller. To prevent
 * stereo from being used by subsequently opened {@code WorldWindowOffscreenCanvas}es, set the property to a an empty string,
 * "". If a stereo device cannot be selected and used, this falls back to a non-stereo device that supports WorldWind's
 * minimum requirements.
 * <p>
 * Under certain conditions, JOGL replaces the <code>GLContext</code> associated with instances of this class. This then
 * necessitates that all resources such as textures that have been stored on the graphic devices must be regenerated for
 * the new context. WorldWind does this automatically by clearing the associated {@link GpuResourceCache}. Objects
 * subsequently rendered automatically re-create those resources. If an application creates its own graphics resources,
 * including textures, vertex buffer objects and display lists, it must store them in the <code>GpuResourceCache</code>
 * associated with the current {@link gov.nasa.worldwind.render.DrawContext} so that they are automatically cleared, and
 * be prepared to re-create them if they do not exist in the <code>DrawContext</code>'s current
 * <code>GpuResourceCache</code> when needed. Examples of doing this can be found by searching for usages of the method
 * {@link GpuResourceCache#get(Object)} and {@link GpuResourceCache#getTexture(Object)}.
 *
 * @author Wiehann Matthysen
 */
public class WorldWindowOffscreenCanvas implements WorldWindow, PropertyChangeListener
{
    // The offscreen canvas.
    protected final GLOffscreenAutoDrawable canvas;
    
    /** The drawable to which {@link WorldWindow} methods are delegated. */
    protected final WorldWindowGLDrawable wwd; // WorldWindow interface delegates to wwd

    /** 
     * Constructs a new <code>WorldWindowOffscreenCanvas</code> on the default graphics device.
     * 
     * @param dimension the size, specified as a <code>Dimension</code>, of the <code>WorldWindow</code>.
     */
    public WorldWindowOffscreenCanvas(Dimension dimension)
    {
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            GLDrawableFactory factory = GLDrawableFactory.getFactory(GLProfile.getMaxFixedFunc(true));
            GLCapabilities caps = Configuration.getRequiredGLCapabilities();
            GLCapabilitiesChooser chooser = new BasicGLCapabilitiesChooser();
            this.canvas = factory.createOffscreenAutoDrawable(null, caps, chooser, dimension.width, dimension.height);
            this.wwd.initDrawable(this.canvas);
            this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowOffscreenCanvas</code> on the default graphics device and shares graphics resources
     * with another <code>WorldWindow</code>.
     *
     * @param dimension the size, specified as a <code>Dimension</code>, of the <code>WorldWindow</code>.
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     */
    public WorldWindowOffscreenCanvas(Dimension dimension, WorldWindow shareWith)
    {
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            GLDrawableFactory factory = GLDrawableFactory.getFactory(GLProfile.getMaxFixedFunc(true));
            GLCapabilities caps = Configuration.getRequiredGLCapabilities();
            GLCapabilitiesChooser chooser = new BasicGLCapabilitiesChooser();
            GLContext context = shareWith != null ? shareWith.getContext() : null;
            this.canvas = factory.createOffscreenAutoDrawable(null, caps, chooser, dimension.width, dimension.height);
            if (context != null)
                this.canvas.setSharedContext(context);
            this.wwd.initDrawable(this.canvas);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowOffscreenCanvas</code> on a specified graphics device and shares graphics resources
     * with another <code>WorldWindow</code>.
     *
     * @param dimension the size, specified as a <code>Dimension</code>, of the <code>WorldWindow</code>.
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     * @param device    the <code>AbstractGraphicsDevice</code> on which to create the window. May be null, in which case the
     *                  default screen device of the local {@link GraphicsEnvironment} is used.
     */
    public WorldWindowOffscreenCanvas(Dimension dimension, WorldWindow shareWith, AbstractGraphicsDevice device)
    {
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            GLDrawableFactory factory = GLDrawableFactory.getFactory(GLProfile.getMaxFixedFunc(true));
            GLCapabilities caps = Configuration.getRequiredGLCapabilities();
            GLCapabilitiesChooser chooser = new BasicGLCapabilitiesChooser();
            GLContext context = shareWith != null ? shareWith.getContext() : null;
            this.canvas = factory.createOffscreenAutoDrawable(device, caps, chooser, dimension.width, dimension.height);
            if (context != null)
                this.canvas.setSharedContext(context);
            this.wwd.initDrawable(this.canvas);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowOffscreenCanvas</code> on a specified device with the specified capabilities and
     * shares graphics resources with another <code>WorldWindow</code>.
     *
     * @param dimension the size, specified as a <code>Dimension</code>, of the <code>WorldWindow</code>.
     * @param shareWith a <code>WorldWindow</code> with which to share graphics resources.
     * @param device       the <code>GraphicsDevice</code> on which to create the window. May be null, in which case the
     *                     default screen device of the local {@link GraphicsEnvironment} is used.
     * @param capabilities a capabilities object indicating the OpenGL rendering context's capabilities. May be null, in
     *                     which case a default set of capabilities is used.
     * @param chooser      a chooser object that customizes the specified capabilities. May be null, in which case a
     *                     default chooser is used.
     */
    public WorldWindowOffscreenCanvas(Dimension dimension, WorldWindow shareWith, AbstractGraphicsDevice device,
        GLCapabilities capabilities, GLCapabilitiesChooser chooser)
    {
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            GLDrawableFactory factory = GLDrawableFactory.getFactory(GLProfile.getMaxFixedFunc(true));
            GLCapabilities caps = Configuration.getRequiredGLCapabilities();
            GLContext context = shareWith != null ? shareWith.getContext() : null;
            this.canvas = factory.createOffscreenAutoDrawable(device, caps, chooser, dimension.width, dimension.height);
            if (context != null)
                this.canvas.setSharedContext(context);
            this.wwd.initDrawable(this.canvas);
            if (shareWith != null)
                this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            else
                this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    @Override
    public GLContext getContext()
    {
        return this.wwd.getContext();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        //noinspection StringEquality
        if (evt.getPropertyName() == WorldWind.SHUTDOWN_EVENT)
            this.shutdown();
    }

    public void shutdown()
    {
        WorldWind.removePropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
        this.wwd.shutdown();
        this.canvas.destroy();
    }
    
    @Override
    public boolean isEnableGpuCacheReinitialization()
    {
        return this.wwd.isEnableGpuCacheReinitialization();
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization)
    {
        this.wwd.setEnableGpuCacheReinitialization(enableGpuCacheReinitialization);
    }

    /** Constructs and attaches the {@link View} for this <code>WorldWindow</code>. */
    protected void createView()
    {
        this.setView((View) WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME));
    }

    /** Constructs and attaches the {@link InputHandler} for this <code>WorldWindow</code>. */
    protected void createDefaultInputHandler()
    {
        // Offscreen window doesn't need an InputHandler.
        // We fall back to the NoOpInputHandler.
        this.setInputHandler(null);
    }

    public InputHandler getInputHandler()
    {
        return this.wwd.getInputHandler();
    }

    public void setInputHandler(InputHandler inputHandler)
    {
        if (this.wwd.getInputHandler() != null)
            this.wwd.getInputHandler().setEventSource(null); // remove this window as a source of events

        this.wwd.setInputHandler(inputHandler != null ? inputHandler : new NoOpInputHandler());
        if (inputHandler != null)
            inputHandler.setEventSource(this);
    }

    public SceneController getSceneController()
    {
        return this.wwd.getSceneController();
    }

    public void setSceneController(SceneController sceneController)
    {
        this.wwd.setSceneController(sceneController);
    }

    public GpuResourceCache getGpuResourceCache()
    {
        return this.wwd.getGpuResourceCache();
    }

    public void redraw()
    {
        //this.wwd.redraw();
    }

    public void redrawNow()
    {
        this.wwd.redrawNow();
    }

    public void setModel(Model model)
    {
        // null models are permissible
        this.wwd.setModel(model);
    }

    public Model getModel()
    {
        return this.wwd.getModel();
    }

    public void setView(View view)
    {
        // null views are permissible
        if (view != null)
        {
            this.wwd.setView(view);
            // We need to manually set this as the NoOpInputHandler won't.
            view.getViewInputHandler().setWorldWindow(this);
        }
    }

    public View getView()
    {
        return this.wwd.getView();
    }

    public void setModelAndView(Model model, View view)
    {   // null models/views are permissible
        this.setModel(model);
        this.setView(view);
    }

    public void addRenderingListener(RenderingListener listener)
    {
        this.wwd.addRenderingListener(listener);
    }

    public void removeRenderingListener(RenderingListener listener)
    {
        this.wwd.removeRenderingListener(listener);
    }

    public void addSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().addSelectListener(listener);
        this.wwd.addSelectListener(listener);
    }

    public void removeSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().removeSelectListener(listener);
        this.wwd.removeSelectListener(listener);
    }

    public void addPositionListener(PositionListener listener)
    {
        this.wwd.addPositionListener(listener);
    }

    public void removePositionListener(PositionListener listener)
    {
        this.wwd.removePositionListener(listener);
    }

    public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.addRenderingExceptionListener(listener);
    }

    public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.removeRenderingExceptionListener(listener);
    }

    public Position getCurrentPosition()
    {
        return this.wwd.getCurrentPosition();
    }

    public PickedObjectList getObjectsAtCurrentPosition()
    {
        return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getPickedObjectList() : null;
    }

    public PickedObjectList getObjectsInSelectionBox()
    {
        return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getObjectsInPickRectangle() : null;
    }

    public Object setValue(String key, Object value)
    {
        return this.wwd.setValue(key, value);
    }

    public AVList setValues(AVList avList)
    {
        return this.wwd.setValues(avList);
    }

    public Object getValue(String key)
    {
        return this.wwd.getValue(key);
    }

    public Collection<Object> getValues()
    {
        return this.wwd.getValues();
    }

    public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.wwd.getEntries();
    }

    public String getStringValue(String key)
    {
        return this.wwd.getStringValue(key);
    }

    public boolean hasKey(String key)
    {
        return this.wwd.hasKey(key);
    }

    public Object removeKey(String key)
    {
        return this.wwd.removeKey(key);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.wwd.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.wwd.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.wwd.removePropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.wwd.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.wwd.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.wwd.firePropertyChange(propertyChangeEvent);
    }

    public AVList copy()
    {
        return this.wwd.copy();
    }

    public AVList clearList()
    {
        return this.wwd.clearList();
    }

    public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        this.wwd.setPerFrameStatisticsKeys(keys);
    }

    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.wwd.getPerFrameStatistics();
    }

    @Override
    public void setSize(int width, int height)
    {
        this.wwd.setSize(width, height);
    }

    @Override
    public Dimension getSize()
    {
        return this.wwd.getSize();
    }
}
