//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model.inputs.keyProcessors;

import limelight.ui.model.inputs.KeyProcessor;
import limelight.ui.model.inputs.TextModel;

import java.awt.event.KeyEvent;

public class ExpandedSelectionOnKeyProcessor extends KeyProcessor
{
  public static KeyProcessor instance = new ExpandedSelectionOnKeyProcessor();

  @Override
  public void processKey(KeyEvent event, TextModel boxInfo)
  {
    boxInfo.setSelectionOn(false);
    int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_TAB){
      boxInfo.deleteSelection();
      boxInfo.insertChar(event.getKeyChar());
    }
    else if (boxInfo.isMoveUpEvent(keyCode))
      boxInfo.moveCaretUpALine();
    else if (boxInfo.isMoveDownEvent(keyCode))
      boxInfo.moveCaretDownALine();
    else
      SelectionOnKeyProcessor.instance.processKey(event, boxInfo);
  }
}
