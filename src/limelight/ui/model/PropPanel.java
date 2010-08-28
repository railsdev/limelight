//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.Context;
import limelight.LimelightError;
import limelight.styles.*;
import limelight.styles.abstrstyling.StyleValue;
import limelight.ui.EventAction;
import limelight.ui.PaintablePanel;
import limelight.ui.Painter;
import limelight.ui.Panel;
import limelight.ui.api.Prop;
import limelight.ui.events.*;
import limelight.ui.events.Event;
import limelight.ui.model.inputs.ScrollBarPanel;
import limelight.ui.painting.*;
import limelight.util.Box;
import limelight.util.Util;

import java.awt.*;
import java.util.Map;

public class PropPanel extends BasePanel implements PropablePanel, PaintablePanel, ChangeablePanel, StyleObserver
{
  private final Prop prop;
  private final ScreenableStyle style;
  private final RichStyle hoverStyle;
  private String styles;
  private Border borderShaper;
  private TextAccessor textAccessor;
  private Box boxInsideMargins;
  private Box boxInsideBorders;
  private Box boxInsidePadding;
  private Box childConsumableArea;
  private PaintAction afterPaintAction;
  private ScrollBarPanel verticalScrollbar;
  private ScrollBarPanel horizontalScrollbar;
  private boolean sizeChangePending = true;
  public boolean borderChanged = true;
  public Dimension greediness = new Dimension(0, 0);
  private Painter painter = DefaultPainter.instance;

  private Cursor preHoverCursor;

  public PropPanel(Prop prop)
  {
    this.prop = prop;
    textAccessor = TempTextAccessor.instance();
    style = new ScreenableStyle();
    hoverStyle = new RichStyle();
    style.addObserver(this);
    getEventHandler().add(MouseWheelEvent.class, MouseWheelAction.instance);
    getEventHandler().add(MouseEnteredEvent.class, HoverOnAction.instance);
    getEventHandler().add(MouseExitedEvent.class, HoverOffAction.instance);
  }

  public String getText()
  {
    return textAccessor.getText();
  }

  public void setText(String text) throws LimelightError
  {
    if(!Util.equal(text, getText()))
    {
      markAsNeedingLayout(); // TODO MDM - This is questionable...  The text panel would know if layout is needed.
    }
    textAccessor.setText(this, text);
  }

  public TextAccessor getTextAccessor()
  {
    return textAccessor;
  }

  public void setTextAccessor(TextAccessor textAccessor)
  {
    this.textAccessor = textAccessor;
  }

  public Prop getProp()
  {
    return prop;
  }

  @Override
  public Panel getOwnerOfPoint(Point point)
  {
    Point relativePoint = new Point(point.x - getX(), point.y - getY());
    if(verticalScrollbar != null && verticalScrollbar.containsRelativePoint(relativePoint))
      return verticalScrollbar;
    else if(horizontalScrollbar != null && horizontalScrollbar.containsRelativePoint(relativePoint))
      return horizontalScrollbar;

    return super.getOwnerOfPoint(point);
  }

  public synchronized Box getBoxInsideMargins()
  {
    if(boxInsideMargins == null)
    {
      Box bounds = getBoundingBox();
      boxInsideMargins = (Box) bounds.clone();
      Style style = getStyle();
      boxInsideMargins.shave(style.getCompiledTopMargin().pixelsFor(bounds.height),
          style.getCompiledRightMargin().pixelsFor(bounds.width),
          style.getCompiledBottomMargin().pixelsFor(bounds.height),
          style.getCompiledLeftMargin().pixelsFor(bounds.width));
    }
    return boxInsideMargins;
  }

  public synchronized Box getBoxInsideBorders()
  {
    if(boxInsideBorders == null)
    {
      Box bounds = getBoxInsideMargins();
      boxInsideBorders = (Box) bounds.clone();
      Style style = getStyle();
      boxInsideBorders.shave(style.getCompiledTopBorderWidth().pixelsFor(bounds.height),
          style.getCompiledRightBorderWidth().pixelsFor(bounds.width),
          style.getCompiledBottomBorderWidth().pixelsFor(bounds.height),
          style.getCompiledLeftBorderWidth().pixelsFor(bounds.width));
    }
    return boxInsideBorders;
  }

  public synchronized Box getBoxInsidePadding()
  {
    if(boxInsidePadding == null)
    {
      Box bounds = getBoxInsideBorders();
      boxInsidePadding = (Box) bounds.clone();
      Style style = getStyle();
      boxInsidePadding.shave(style.getCompiledTopPadding().pixelsFor(bounds.height),
          style.getCompiledRightPadding().pixelsFor(bounds.width),
          style.getCompiledBottomPadding().pixelsFor(bounds.height),
          style.getCompiledLeftPadding().pixelsFor(bounds.width));
    }
    return boxInsidePadding;
  }

  public Box getChildConsumableArea()
  {
    if(childConsumableArea == null)
    {
      getBoxInsidePadding();
      Box boxInsidePadding = getBoxInsidePadding();
      int width = verticalScrollbar == null ? boxInsidePadding.width : boxInsidePadding.width - verticalScrollbar.getWidth();
      int height = horizontalScrollbar == null ? boxInsidePadding.height : boxInsidePadding.height - horizontalScrollbar.getHeight();
      childConsumableArea = new Box(boxInsidePadding.x, boxInsidePadding.y, width, height);
    }
    return childConsumableArea;
  }

  @Override
  public Layout getDefaultLayout()
  {
    return PropPanelLayout.instance;
  }

  public void updateBorder()
  {
    if(borderShaper != null)
    {
      borderShaper.setBounds(getBoxInsideMargins());
      if(borderChanged)
      {
        borderShaper.updateDimentions();
        borderChanged = false;
      }
    }
  }

  public void paintOn(Graphics2D graphics)
  {
    if(!laidOut)
      return;

    painter.paint(graphics, this);

    if(afterPaintAction != null)
    {
      afterPaintAction.invoke(graphics);
    }
  }

  public ScreenableStyle getStyle()
  {
    return style;
  }

  public RichStyle getHoverStyle()
  {
    return hoverStyle;
  }

  public void setStyles(String styles)
  {
    this.styles = styles;
  }

  public String getStyles()
  {
    return styles;
  }

  public Border getBorderShaper()
  {
    if(borderShaper == null)
      borderShaper = new Border(getStyle(), getBoxInsideMargins());
    return borderShaper;
  }

  public void setCursor(Cursor cursor)
  {
    getRoot().setCursor(cursor);
  }

  //TODO I don't think this is needed any longer.
  @Override
  public void repaint()
  {
//System.err.println("repaint: " + this + ": " + (getParent() != null) + ", " + (getStyle().changed(Style.WIDTH) || getStyle().changed(Style.WIDTH)));
    //TODO Handle the case when the parent needs to repaint.
//    if(getParent() != null && (getStyle().changed(Style.WIDTH) || getStyle().changed(Style.WIDTH)))
//      getParent().repaint();
//    else
//    {
    doLayout();
    PaintJob job = new PaintJob(getAbsoluteBounds(), getRoot().getGraphics().getBackground());
    //TODO Why are we painting the root panel here?  So wastful! Maybe. Transparency?
    job.paint(getRoot().getPanel());
    job.applyTo(getRoot().getGraphics());
//    }
  }

  //TODO I don't think this is needed any longer.
  public void paintImmediately(int a, int b, int c, int d)
  {
    repaint();
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + " - " + getProp().getName();
  }

  public void setAfterPaintAction(PaintAction action)
  {
    afterPaintAction = action;
  }

  public PaintAction getAfterPaintAction()
  {
    return afterPaintAction;
  }

  @Override
  public boolean isFloater()
  {
    return getStyle().getCompiledFloat().isOn();
  }

  @Override
  public void doFloatLayout()
  {
    FloaterLayout.instance.doLayout(this);
  }

  //TODO super.clearCache() deals with absolute positioning.  Here the boxes are all relative.  They're uneccessarily being cleared.
  @Override
  public synchronized void clearCache()
  {
    super.clearCache();
    boxInsideMargins = null;
    boxInsideBorders = null;
    boxInsidePadding = null;
    childConsumableArea = null;
  }

  public void styleChanged(StyleAttribute attribute, StyleValue value)
  {
    if(isIlluminated() && getParent() != null && getRoot() != null)
      attribute.applyChange(this, value);
  }

  public ScrollBarPanel getVerticalScrollbar()
  {
    return verticalScrollbar;
  }

  public ScrollBarPanel getHorizontalScrollbar()
  {
    return horizontalScrollbar;
  }

  public void addVerticalScrollBar()
  {
    verticalScrollbar = new ScrollBarPanel(ScrollBarPanel.VERTICAL);
    add(verticalScrollbar);
    childConsumableArea = null;
  }

  public void addHorizontalScrollBar()
  {
    horizontalScrollbar = new ScrollBarPanel(ScrollBarPanel.HORIZONTAL);
    add(horizontalScrollbar);
    childConsumableArea = null;
  }

  public void removeVerticalScrollBar()
  {
    remove(verticalScrollbar);
    verticalScrollbar = null;
    childConsumableArea = null;
  }

  public void removeHorizontalScrollBar()
  {
    remove(horizontalScrollbar);
    horizontalScrollbar = null;
    childConsumableArea = null;
  }

  public void playSound(String filename)
  {
    Context.instance().audioPlayer.playAuFile(filename);
  }

  public boolean isSizeChangePending()
  {
    return sizeChangePending;
  }

  public void setSizeChangePending(boolean value)
  {
    sizeChangePending = value;
  }

  public void propagateSizeChangeUp()
  {
    doPropagateSizeChangeUp(getParent());
  }

  public void propagateSizeChangeDown()
  {
    doPropagateSizeChangeDown();
  }

  public void resetPendingSizeChange()
  {
    sizeChangePending = false;
  }

  public boolean isBorderChanged()
  {
    return borderChanged;
  }

  public void setBorderChanged(boolean value)
  {
    borderChanged = value;
  }

  @Override
  protected boolean canRemove(Panel child)
  {
    return child != verticalScrollbar && child != horizontalScrollbar;
  }

  @Override
  public void illuminate()
  {
    if(styles != null)
    {
      Map<String, RichStyle> store = getRoot().getStylesStore();
      String[] styleNames = styles.split("[ ,]+");
      for(String styleName : styleNames)
      {
        RichStyle style = store.get(styleName);
        if(style != null)
          getStyle().addExtension(style);

        RichStyle hoverStyle = store.get(styleName + ".hover");
        if(hoverStyle != null)
        {
          getHoverStyle().addExtension(hoverStyle);
          getStyle().setDefault(Style.CURSOR, "hand");
        }
      }
    }
    super.illuminate();
  }

  @Override
  public void delluminate()
  {
    style.tearDown();
    hoverStyle.tearDown();
    super.delluminate();
  }

  public void setPainter(Painter instance)
  {
    painter = instance;
  }

  public Painter getPainter()
  {
    return painter;
  }

  @Override
  public boolean hasFocus()
  {
    return textAccessor.hasFocus();
  }

  private static class MouseWheelAction implements EventAction
  {
    public static MouseWheelAction instance = new MouseWheelAction();

    public void invoke(Event event)
    {
      if(!(event.getPanel() instanceof PropPanel))
        return;
      
      MouseWheelEvent wheelEvent = (MouseWheelEvent) event;
      PropPanel panel = (PropPanel) event.getPanel();
      ScrollBarPanel scrollBar = wheelEvent.isVertical() ? panel.getVerticalScrollbar() : panel.getHorizontalScrollbar();
      if(scrollBar != null)
        scrollBar.setValue(scrollBar.getValue() + wheelEvent.getUnitsToScroll());
    }
  }

  private static class HoverOnAction implements EventAction
  {
    public static HoverOnAction instance = new HoverOnAction();

    public void invoke(Event event)
    {
      final PropPanel panel = (PropPanel) event.getPanel();
      //TODO MDM - If the prop has no suface area (perhasps it's a floater that floated out of bounds), does it still get the mouseExited event?
      if(!panel.getStyle().hasScreen())
        panel.getStyle().applyScreen(panel.getHoverStyle()); // TODO - MDM - This seems inefficient considering most of the time, there's no change in styles.

      Cursor currentCursor = panel.getRoot().getCursor();
      Cursor hoverCursor = panel.getStyle().getCompiledCursor().getCursor();
      if(hoverCursor != currentCursor)
      {
        panel.preHoverCursor = currentCursor;
        panel.getRoot().setCursor(hoverCursor);
      }
    }
  }

  private static class HoverOffAction implements EventAction
  {
    public static HoverOffAction instance = new HoverOffAction();

    public void invoke(Event event)
    {
      final PropPanel panel = (PropPanel) event.getPanel();
      if(panel.getStyle().hasScreen())
        panel.getStyle().removeScreen();

      if(panel.preHoverCursor != null)
      {
        final RootPanel root = panel.getRoot();
        // TODO MDM - If the panel is removed from the scene, the cursor is never changed.  Perhaps the right way to do this is in the MouseListener... when ever entering a panel, get the cursor and change it.... when exiting a panel, pop the cursor
        if(root != null)
          root.setCursor(panel.preHoverCursor);
        panel.preHoverCursor = null;
      }
    }
  }
}

