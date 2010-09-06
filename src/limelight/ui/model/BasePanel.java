//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model;

import limelight.styles.Style;
import limelight.ui.Panel;
import limelight.ui.model.inputs.ScrollBarPanel;
import limelight.util.Box;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public abstract class BasePanel implements Panel
{
  protected int height;
  protected int width;
  private int x;
  private int y;
  private Panel parent;
  protected Point absoluteLocation;
  private Box absoluteBounds;
  protected final LinkedList<Panel> children;
  private List<Panel> readonlyChildren;
  private boolean sterilized;
  private Box boundingBox;
  protected Layout neededLayout = getDefaultLayout();
  protected boolean laidOut;
  private boolean illuminated;
  protected EventHandler eventHandler;

  protected BasePanel()
  {
    width = 50;
    height = 50;
    children = new LinkedList<Panel>();
    eventHandler = new EventHandler(this);
  }

  public int getHeight()
  {
    return height;
  }

  public int getWidth()
  {
    return width;
  }

  public int getX()
  {
    return x;
  }

  public int getY()
  {
    return y;
  }

  public void setSize(int w, int h)
  {
    if(w != width || h != height)
    {
//      //TODO Test Me!
//      markAsDirty();

      clearCache();
      width = w;
      height = h;
    }
  }

  public void clearCache()
  {
    absoluteLocation = null;
    boundingBox = null;
    absoluteBounds = null;

    for(Panel child : getChildren())
      child.clearCache();
  }

  public void setLocation(int x, int y)
  {
    if(x != this.x || y != this.y)
      clearCache();
    this.x = x;
    this.y = y;
  }

  public Point getLocation()
  {
    return new Point(x, y);
  }

  public Point getAbsoluteLocation()
  {
    if(absoluteLocation == null)
    {
      int x = this.x;
      int y = this.y;

      if(parent != null)
      {
        Point absoluteParentLocation = parent.getAbsoluteLocation();
        x += absoluteParentLocation.x;
        y += absoluteParentLocation.y;
      }

      absoluteLocation = new Point(x, y);
    }
    return absoluteLocation;
  }

  public Box getAbsoluteBounds()
  {
    if(absoluteBounds == null)
    {
      Point absoluteLocation = getAbsoluteLocation();
      absoluteBounds = new Box(absoluteLocation.x, absoluteLocation.y, getWidth(), getHeight());
    }
    return absoluteBounds;
  }

  public Panel getParent()
  {
    return parent;
  }

  public void setParent(Panel newParent)
  {
    if(newParent != parent && isIlluminated())
      delluminate();
    
    parent = newParent;

    if(parent != null && parent.isIlluminated())
      illuminate();
  }

  //TODO  MDM Change my return type to RootPanel
  public RootPanel getRoot()
  {
    if(parent == null)
      return null;
    return parent.getRoot();
  }

  public boolean isDescendantOf(Panel panel)
  {
    if(parent == null)
      return false;
    else if(parent == panel)
      return true;
    else
      return parent.isDescendantOf(panel);
  }

  public Panel getClosestCommonAncestor(Panel panel)
  {
    Panel ancestor = getParent();
    while(ancestor != null && !panel.isDescendantOf(ancestor))
      ancestor = ancestor.getParent();

    return ancestor;
  }

  public Graphics2D getGraphics()
  {
    Box bounds = getAbsoluteBounds();
    return (Graphics2D) getRoot().getGraphics().create(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  public void doLayout()
  {
    Layout layout = neededLayout;
    if(layout != null)
      layout.doLayout(this);
    else
      getDefaultLayout().doLayout(this);
  }

  // TODO MDM Calling this from the layout is error prone.  A Layout might forget to call.  Can easily solve by resetting when ever the panel is laid out (prior to layout)
  public synchronized void resetLayout()
  {
    neededLayout = null;
  }

  public Layout getDefaultLayout()
  {
    return BasePanelLayout.instance;
  }

  public EventHandler getEventHandler()
  {
    return eventHandler;
  }

  public void add(Panel panel)
  {
    add(-1, panel);
  }

  public void add(int index, Panel child)
  {
    if(sterilized && !(child instanceof ScrollBarPanel))
      throw new SterilePanelException("Unknown name");

    synchronized(children)
    {
      if(index == -1)
        children.add(child);
      else
        children.add(index, child);
      readonlyChildren = null;
    }

    child.setParent(this);
    doPropagateSizeChangeUp(this);
    markAsNeedingLayout();
  }

  public boolean hasChildren()
  {
    return children.size() > 0;
  }

  public List<Panel> getChildren()
  {
    if(readonlyChildren == null)
    {
      synchronized(children)
      {
        readonlyChildren = Collections.unmodifiableList(new ArrayList<Panel>(children));
        return readonlyChildren;
      }
    }
    else
      return readonlyChildren;
  }

  public boolean isChild(Panel child)
  {
    return children.contains(child);
  }

  public boolean remove(Panel child)
  {
    boolean removed = false;
    synchronized(children)
    {
      removed = children.remove(child);
      readonlyChildren = null;
    }
    if(removed)
    {
      child.setParent(null);
      doPropagateSizeChangeUp(this);
      markAsNeedingLayout();
      return true;
    }
    return false;
  }

  public void removeAll()
  {
    if(children.size() > 0)
    {
      synchronized(children)
      {
        for(Iterator<Panel> iterator = children.iterator(); iterator.hasNext();)
        {
          Panel child = iterator.next();
          if(canRemove(child))
          {
            child.setParent(null);
            iterator.remove();
          }
        }
        readonlyChildren = null;
      }
      sterilized = false;
      doPropagateSizeChangeUp(this);
      markAsNeedingLayout();
    }
  }

  protected boolean canRemove(Panel child)
  {
    return true;
  }

  public void sterilize()
  {
    sterilized = true;
  }

  public boolean isSterilized()
  {
    return sterilized;
  }

  public void repaint()
  {
    getParent().repaint();
  }

  public Panel getOwnerOfPoint(Point point)
  {
    if(children.size() > 0)
    {
      synchronized(children)
      {
        for(ListIterator<Panel> iterator = children.listIterator(children.size()); iterator.hasPrevious();)
        {
          Panel panel = iterator.previous();
          if(panel.isFloater() && panel.getAbsoluteBounds().contains(point))
            return panel.getOwnerOfPoint(point);
        }
        for(Panel panel : children)
        {
          if(!panel.isFloater() && panel.getAbsoluteBounds().contains(point))
            return panel.getOwnerOfPoint(point);
        }
      }
    }
    return this;
  }

  public Box getBoundingBox()
  {
    if(boundingBox == null)
      boundingBox = new Box(0, 0, getWidth(), getHeight());
    return boundingBox;
  }

  public boolean isFloater()
  {
    return false;
  }

  public void doFloatLayout()
  {
    // Panels are not floaters by default.
  }

  public void consumableAreaChanged()
  {
    Style style = getStyle();    
    if(!needsLayout() && style != null && style.hasDynamicDimension())
    {
      markAsNeedingLayout();
      synchronized(children)
      {
        for(Panel child : children)
          child.consumableAreaChanged();
      }
    }
  }

  public boolean canBeBuffered()
  {
//    return true;
    return false;  // Seems to be twice as fast without buffering.
  }

  public synchronized void markAsNeedingLayout(Layout layout)
  {
    if(getRoot() != null)
    {
      if(neededLayout == null)
      {
        neededLayout = layout; // Set first... race conditions otherwise.
        getRoot().addPanelNeedingLayout(this);
      }
      else if(layout.overides(neededLayout))
        neededLayout = layout;
    }
  }

  public void markAsNeedingLayout()
  {
    markAsNeedingLayout(getDefaultLayout());
  }

  public boolean needsLayout()
  {
    return neededLayout != null;
  }

  //TODO This is a little inefficient.  Reconsider what get's passed to props.
  protected MouseEvent translatedEvent(MouseEvent e)
  {
    e = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false);
    Point absoluteLocation = getAbsoluteLocation();
    e.translatePoint(absoluteLocation.x * -1, absoluteLocation.y * -1);
    return e;
  }

  public Iterator<Panel> iterator()
  {
    return new PanelIterator(this);
  }

  protected void doPropagateSizeChangeUp(Panel panel)
  {
    if(panel != null && !panel.needsLayout() && panel instanceof BasePanel)
    {
      panel.markAsNeedingLayout();
      doPropagateSizeChangeUp(panel.getParent());
    }
  }

  public void markAsDirty()
  {
    RootPanel rootPanel = getRoot();
    if(rootPanel != null)
    {
      rootPanel.addDirtyRegion(getAbsoluteBounds());
    }
  }

  public boolean isLaidOut()
  {
    return laidOut;
  }

  public void wasLaidOut()
  {
    laidOut = true;
  }

  protected void doPropagateSizeChangeDown()
  {
    synchronized(children)
    {
      for(Panel child : children)
        child.consumableAreaChanged();
    }
  }

  public boolean isIlluminated()
  {
    return illuminated;
  }

  public void illuminate()
  {
    illuminated = true;
    for(Panel child : children)
      child.illuminate();
  }

  public void delluminate()
  {
    illuminated = false;
    for(Panel child : children)
      child.delluminate();
  }

  public boolean hasFocus()
  {
    return false;
  }
}
