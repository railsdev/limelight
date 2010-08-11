//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.ui.model.inputs;

import limelight.ui.model.Layout;
import limelight.ui.model.PropPanel;
import limelight.ui.model.PropPanelLayout;
import limelight.ui.Panel;
import limelight.styles.Style;

import java.awt.*;
import java.util.LinkedList;

public class ScrollLayout extends PropPanelLayout
{
  private int orientation;
  private ScrollBar2Panel scrollBar;

  public ScrollLayout(int orientation, ScrollBar2Panel scrollBar)
  {
    this.orientation = orientation;
    this.scrollBar = scrollBar;
  }

  public void doLayout(Panel aPanel)
  {
    PropPanel panel = (PropPanel) aPanel;
    panel.resetLayout();
    int dx = orientation == ScrollBar2Panel.HORIZONTAL ? scrollBar.getValue() : 0;
    int dy = orientation == ScrollBar2Panel.VERTICAL ? scrollBar.getValue() : 0;

    LinkedList<PropPanelLayout.Row> rows = buildRows(panel);
    Dimension consumedDimensions = new Dimension();
    calculateConsumedDimentions(rows, consumedDimensions);
    layoutRows(panel, consumedDimensions, rows, dx, dy);

    panel.markAsDirty();
    panel.wasLaidOut();
  }

  public void layoutRows(PropPanel panel, Dimension consumeDimension, LinkedList<Row> rows, int dx, int dy)
  {
    Style style = panel.getStyle();
    int y = style.getCompiledVerticalAlignment().getY(consumeDimension.height, panel.getChildConsumableArea());
    y = Math.max(0, y);
    y -= dy;
    for(Row row : rows)
    {
      int x = style.getCompiledHorizontalAlignment().getX(row.width, panel.getChildConsumableArea());
      x = Math.max(0, x);
      x -= dx;
      row.layoutComponents(x, y, style.getCompiledVerticalAlignment());
      y += row.height;
    }
  }

  public boolean overides(Layout other)
  {
    return false;
  }

  public void doLayout(Panel panel, boolean topLevel)
  {
    doLayout(panel);
  }
}
