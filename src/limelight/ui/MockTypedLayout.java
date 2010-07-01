package limelight.ui;

import limelight.ui.text.TypedLayout;
import limelight.util.Box;
import limelight.util.StringUtil;

import java.awt.*;
import java.awt.font.TextHitInfo;

public class MockTypedLayout implements TypedLayout
{
  public static int ASCENT = 1;
  public static int DESCENT = 2;
  public static int LEADING = 3;
  public static int CHAR_WIDTH = 10;
  public static int CHAR_HEIGHT = 10;

  public String text;
  public boolean hasDrawn;
  public int drawnX;
  public int drawnY;

  public MockTypedLayout(String value)
  {
    text = value;
  }

  public void draw(Graphics2D graphics, float x, float y)
  {
    hasDrawn = true;
    this.drawnX = (int)x;
    this.drawnY = (int)y;
  }

  public String getText()
  {
    return text;
  }

  public float getAscent()
  {
    return ASCENT;
  }

  public float getDescent()
  {
    return DESCENT;
  }

  public float getLeading()
  {
    return LEADING;
  }

  public TextHitInfo hitTestChar(float x, float y)
  {
    int index = (int) (x / 10);
    index = Math.min(index, text.length());
    return TextHitInfo.leading(index);
  }

  public int getIndexAt(int x)
  {
    int index = Math.min(x / 10, text.length());

    while(index > 0 && StringUtil.isNewlineChar(text.charAt(index - 1)))
      index--;

    return index;
  }

  public Box getCaretShape(int caretIndex)
  {
    return new Box(caretIndex * CHAR_WIDTH, 0, 1, CHAR_HEIGHT);
  }

  public int getWidth()
  {
    return getWidthOf(text);
  }

  public int getHeight()
  {
    return CHAR_HEIGHT;
  }

  public int getX(int index)
  {
    return index * CHAR_WIDTH;
  }

  public int getWidthOf(String text)
  {
    return CHAR_WIDTH * text.length();
  }
}
