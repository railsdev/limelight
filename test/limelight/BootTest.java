//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight;

import limelight.audio.RealAudioPlayer;
import limelight.caching.Cache;
import limelight.caching.TimedCache;
import limelight.model.Studio;
import limelight.os.MockOS;
import limelight.os.OS;
import limelight.os.UnsupportedOS;
import limelight.ui.Panel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BootTest
{
  @Before
  public void setUp() throws Exception
  {
    Boot.reset();
    Boot.startBackgroundThreads = false;
    Context.removeInstance();
  }

  @After
  public void tearDown() throws Exception
  {
    Context.instance().killThreads();
    System.setProperty("os.name", "blah");
  }
  
  @Test
  public void tempFileIsAddedToContext() throws Exception
  {
    Boot.configureContext();

    assertNotNull(Context.instance().tempDirectory);
  }

  @Test
  public void bufferedImageCacheIsAddedToContext() throws Exception
  {
    Boot.configureContext();

    Cache<Panel, BufferedImage> cache = Context.instance().bufferedImageCache;
    assertEquals(TimedCache.class, cache.getClass());
    assertEquals(1, ((TimedCache)cache).getTimeoutSeconds(), 0.01);
  }

  @Test
  public void frameManagerIsAddedToContext() throws Exception
  {
    Boot.configureContext();

    assertNotNull(Context.instance().frameManager);
  }

  @Test
  public void audioPlayerIsAdded() throws Exception
  {
    Boot.configureContext();

    assertEquals(RealAudioPlayer.class, Context.instance().audioPlayer.getClass());
  }

//  public void testKeyboardFocusListenerIsInstalled() throws Exception
//  {
//    Boot.configureContext();
//
//    assertSame(Context.instance().keyboardFocusManager, java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager());
//  }

  @Test
  public void bufferedImagePoolIsInstalled() throws Exception
  {
    Boot.configureContext();

    assertNotNull(Context.instance().bufferedImagePool);
  }

  @Test
  public void studioIsInstalled() throws Exception
  {
    Boot.configureContext();

    assertEquals(Studio.class, Context.instance().studio.getClass());
  }

  @Test
  public void threadsDontStartInDevelopment() throws Exception
  {
    Boot.configureContext();

    final Context context = Context.instance();
    assertEquals(false, context.panelPanter.isRunning());
    assertEquals(false, context.animationLoop.isRunning());
    assertEquals(false, context.cacheCleaner.isRunning());
  }

  @Test
  public void threadsDOStartNormally() throws Exception
  {
    Boot.startBackgroundThreads = true;
    Boot.configureContext();

    final Context context = Context.instance();
    assertEquals(true, context.panelPanter.isRunning());
    assertEquals(true, context.animationLoop.isRunning());
    assertEquals(true, context.cacheCleaner.isRunning());
  }

  @Test
  public void settingSystemCofiguration() throws Exception
  {
    System.setProperty("limelight.home", "/limelighthome");
    MockOS os = new MockOS();
    Context.instance().os = os;

    Boot.configureSystemProperties();

    assertEquals(true, os.systemPropertiesConfigured);
    assertEquals("true", System.getProperty("jruby.interfaces.useProxy"));
  }

  @Test
  public void darwinOS() throws Exception
  {
    try
    {
      Thread.currentThread().getContextClassLoader().loadClass("limelight.os.darwin.DarwinOS");
    }
    catch(ClassNotFoundException e)
    {
      return;
    }
    System.setProperty("os.name", "Mac OS X");
    Boot.configureOS();

    OS os = Context.instance().os;
    assertEquals("limelight.os.darwin.DarwinOS", os.getClass().getName());
  }

  @Test
  public void windowsXPOS() throws Exception
  {
    System.setProperty("os.name", "Windows XP");
    Boot.configureOS();
    OS os = Context.instance().os;
    assertEquals("limelight.os.win32.Win32OS", os.getClass().getName());
  }

  @Test
  public void windowsVistaOS() throws Exception
  {
    System.setProperty("os.name", "Windows Vista");
    Boot.configureOS();
    OS os = Context.instance().os;
    assertEquals("limelight.os.win32.Win32OS", os.getClass().getName());
  }

  @Test
  public void unsupportedOS() throws Exception
  {
    System.setProperty("os.name", "Something Unsupported");
    Boot.configureOS();

    OS os = Context.instance().os;
    assertEquals(UnsupportedOS.class, os.getClass());
  }
}
