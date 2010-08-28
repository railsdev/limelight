//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model.inputs.keyProcessors;

import limelight.ui.events.KeyEvent;
import limelight.ui.model.inputs.KeyProcessor;
import limelight.ui.model.inputs.TextModel;

public class SelectionOnCmdKeyProcessor extends KeyProcessor
{
  public static KeyProcessor instance = new SelectionOnCmdKeyProcessor();

  public void processKey(KeyEvent event, TextModel model)
  {
    switch (event.getKeyCode())
    {
      case KeyEvent.KEY_A:
        model.selectAll();
        break;
      case KeyEvent.KEY_V:
        model.deleteSelection();
        model.pasteClipboard();
        model.setSelectionOn(false);
        break;
      case KeyEvent.KEY_C:
        model.copySelection();
        break;
      case KeyEvent.KEY_X:
        model.copySelection();
        model.deleteSelection();
        model.setSelectionOn(false);
        break;
      case KeyEvent.KEY_RIGHT:
        model.sendCaretToEndOfLine();
        model.setSelectionOn(false);
        break;
      case KeyEvent.KEY_LEFT:
        model.sendCursorToStartOfLine();
        model.setSelectionOn(false);
        break;
      case KeyEvent.KEY_UP:
        model.setCaretIndex(0);
        model.setSelectionOn(false);
        break;
      case KeyEvent.KEY_DOWN:
        model.setCaretIndex(model.getText().length());
        model.setSelectionOn(false);
        break;
    }
  }

}
