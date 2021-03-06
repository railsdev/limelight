//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.model;

import limelight.About;
import limelight.Context;
import limelight.LimelightException;
import limelight.model.api.*;
import limelight.model.events.*;
import limelight.styles.RichStyle;
import limelight.ui.events.stage.StageActivatedEvent;
import limelight.ui.model.FakeScene;
import limelight.ui.model.MockStage;
import limelight.ui.model.Scene;
import limelight.ui.model.inputs.MockEventAction;
import limelight.util.Opts;
import limelight.util.Util;
import limelight.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

public class ProductionTest
{
  private FakeProduction production;
  private MockStudio studio;
  private MockEventAction action;

  @Before
  public void setUp() throws Exception
  {
    production = new FakeProduction("/foo/bar");
    studio = new MockStudio();
    Context.instance().studio = studio;
    action = new MockEventAction();
  }

  @Test
  public void nameIsSetBasedOnPath() throws Exception
  {
    assertEquals("bar", production.getName());
  }

  @Test
  public void settingTheName() throws Exception
  {
    production.setName("foo");

    assertEquals("foo", production.getName());
  }

  @Test
  public void allowClose() throws Exception
  {
    assertEquals(true, production.allowClose());

    production.setAllowClose(false);

    assertEquals(false, production.allowClose());
  }

  @Test
  public void minimumLimelightVersion() throws Exception
  {
    assertEquals("0.0.0", production.getMinimumLimelightVersion());

    production.setMinimumLimelightVersion("9.9.9");

    assertEquals("9.9.9", production.getMinimumLimelightVersion());
  }

  @Test
  public void closing() throws Exception
  {
    production.open();
    assertNull(production.getClosingThread());

    production.close();

    assertNotNull(production.getClosingThread());
    production.getClosingThread().join();
    assertEquals(true, production.closeFinalized);
    assertEquals(false, production.isOpen());
  }

    @Test
  public void isLimelightVersionCompatible() throws Exception
  {
    Version version = About.version;
    assertEquals(true, production.isLimelightVersionCompatible());

    production.setMinimumLimelightVersion(version.toString());
    assertEquals(true, production.isLimelightVersionCompatible());

    final String minusOne = new Version(version.getMajor(), version.getMinor(), version.getPatch() - 1).toString();
    production.setMinimumLimelightVersion(minusOne);
    assertEquals(true, production.isLimelightVersionCompatible());

    final String plusOne = new Version(version.getMajor(), version.getMinor(), version.getPatch() + 1).toString();
    production.setMinimumLimelightVersion(plusOne);
    assertEquals(false, production.isLimelightVersionCompatible());
  }

  @Test
  public void canProceedWithCompatibility() throws Exception
  {
    assertEquals(true, production.canProceedWithCompatibility());

    production.setMinimumLimelightVersion("99.99.99");
    studio.shouldProceedWithIncompatibleVersion = false;
    assertEquals(false, production.canProceedWithCompatibility());

    studio.shouldProceedWithIncompatibleVersion = true;
    assertEquals(true, production.canProceedWithCompatibility());
  }

  @Test
  public void illuminateProduction() throws Exception
  {
    production.getEventHandler().add(ProductionCreatedEvent.class, action);

    production.illuminateProduction();

    assertEquals(true, production.illuminated);
    assertEquals(true, action.invoked);
  }

  @Test
  public void loadProduction() throws Exception
  {
    production.getEventHandler().add(ProductionLoadedEvent.class, action);

    production.loadProduction();

    assertEquals(true, production.librariesLoaded);
    assertEquals(true, production.stagesLoaded);
    assertEquals("/foo/bar", production.loadStylesPath);
    assertEquals(true, action.invoked);
  }

  @Test
  public void stylesLoadedForProductionContainBuiltinStyles() throws Exception
  {
    assertEquals(null, production.getStyles());

    production.loadProduction();

    assertEquals(HashMap.class, production.getStyles().getClass());
    assertEquals(true, production.getStyles().containsKey("limelight_builtin_curtains"));
  }

  @Test
  public void openSceneWithNoActiveStage() throws Exception
  {
    production.loadProduction();
    final MockStage stage = (MockStage)production.getTheater().getDefaultStage();
    Scene scene = new FakeScene();
    production.stubbedScene = scene;

    production.openScene("scenePath", Util.toMap());

    assertEquals("scenePath", production.loadedScenePath);
    assertEquals(scene, stage.getScene());
    assertEquals(true, stage.opened);
  }

  @Test
  public void openSceneWithActiveStage() throws Exception
  {
    production.loadProduction();
    MockStage stage = new MockStage("active");
    production.getTheater().add(stage);
    new StageActivatedEvent().dispatch(stage);

    Scene scene = new FakeScene();
    production.stubbedScene = scene;

    production.openScene("scenePath", Util.toMap());

    assertEquals("scenePath", production.loadedScenePath);
    assertEquals(scene, stage.getScene());
    assertEquals(true, stage.opened);
  }

  @Test
  public void openSceneWithStage() throws Exception
  {
    production.loadProduction();
    MockStage stage = new MockStage("mock");
    production.getTheater().add(stage);
    Scene scene = new FakeScene();
    production.stubbedScene = scene;

    production.openScene("scenePath", "mock", Util.toMap());

    assertEquals("scenePath", production.loadedScenePath);
    assertEquals(scene, stage.getScene());
    assertEquals(true, stage.opened);
  }

  @Test
  public void openingSceneWithMissingStage() throws Exception
  {
    production.loadProduction();

    try
    {
      production.openScene("scenePath", "blah", Util.toMap());
      fail("should throw");
    }
    catch(LimelightException e)
    {
      assertEquals("No such stage: blah", e.getMessage());
    }

  }

  @Test
  public void openSceneUpdatesOptionsWithNameAndPath() throws Exception
  {
    production.loadProduction();
    production.getTheater().add(new MockStage("mock"));
    production.stubbedScene = new FakeScene();
    final Map<String,Object> options = Util.toMap();

    production.openScene("scenePath/sceneName", "mock", options);

    assertNotSame(options, production.loadedSceneOptions);
    assertEquals("sceneName", production.loadedSceneOptions.get("name"));
    assertEquals("scenePath/sceneName", production.loadedSceneOptions.get("path"));
  }

  @Test
  public void openSceneLoadsStylesExtendingProductionStyles() throws Exception
  {
    production.loadProduction();
    production.getTheater().add(new MockStage("mock"));
    production.getStyles().put("newStyle", new RichStyle());
    Scene scene = new FakeScene();
    production.stubbedScene = scene;

    production.openScene("scenePath", "mock", Util.toMap());

    assertEquals(HashMap.class, scene.getStyles().getClass());
    assertEquals(true, scene.getStyles().containsKey("limelight_builtin_curtains"));
    assertEquals(true, scene.getStyles().containsKey("newStyle"));
  }

  @Test
  public void closeProduction() throws Exception
  {
    production.open();
    MockEventAction closingAction = new MockEventAction();
    MockEventAction closedAction = new MockEventAction();
    production.getEventHandler().add(ProductionClosingEvent.class, closingAction);
    production.getEventHandler().add(ProductionClosedEvent.class, closedAction);

    production.close();
    production.getClosingThread().join();

    assertEquals(true, closingAction.invoked);
    assertEquals(false, production.getTheater().isOpen());
    assertEquals(true, closedAction.invoked);
  }

  @Test
  public void openDefaultScenes() throws Exception
  {
    production.getEventHandler().add(ProductionOpenedEvent.class, action);
    MockStage stage = new MockStage();
    production.getTheater().add(stage);
    stage.setDefaultSceneName("defaultScene");
    Scene scene = new FakeScene();
    production.stubbedScene = scene;

    production.open(new Opts());

    assertEquals(true, action.invoked);
    assertEquals(true, stage.isOpen());
    assertEquals(scene, stage.getScene());
  }

  @Test
  public void defaultScenesCanBeDisabled() throws Exception
  {
    MockStage stage = new MockStage();
    production.getTheater().add(stage);
    stage.setDefaultSceneName("defaultScene");
    production.stubbedScene = new FakeScene();

    production.open(Util.toMap("open-default-scenes", false));

    assertEquals(false, stage.isOpen());
    assertEquals(null, stage.getScene());
  }

  @Test
  public void backstage() throws Exception
  {
    assertEquals(0, production.getBackstage().size());

    production.getBackstage().put("foo", "BAR");
    assertEquals("BAR", production.getBackstage().get("foo"));
  }
}
