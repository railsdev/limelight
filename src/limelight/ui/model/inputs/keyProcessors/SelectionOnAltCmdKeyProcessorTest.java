package limelight.ui.model.inputs.keyProcessors;


import org.junit.Before;
import org.junit.Test;

import java.awt.event.KeyEvent;

public class SelectionOnAltCmdKeyProcessorTest extends AbstractKeyProcessorTest
{
  @Before
  public void setUp()
  {
    selectionBoxSetUp();
    processor = SelectionOnAltCmdKeyProcessor.instance;
    modifier = 12;
  }

  @Test
  public void canProcessCharacterAndDoNothing()
  {
    mockEvent = new MockKeyEvent(modifier, KeyEvent.VK_A);

    processor.processKey(mockEvent, model);

    assertSelection(1, SELECTION_START_INDEX, true);
  }

}