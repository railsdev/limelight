//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.styles.attributes;

import limelight.styles.compiling.DimensionAttributeCompiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SecondaryBackgroundColorAttributeTest extends Assert
{
  private SecondaryBackgroundColorAttribute attribute;

  @Before
  public void setUp() throws Exception
  {
    attribute = new SecondaryBackgroundColorAttribute();
  }

  @Test
  public void shouldCreation() throws Exception
  {
    assertEquals("Secondary Background Color", attribute.getName());
    assertEquals("color", attribute.getCompiler().type);
    assertEquals("#00000000", attribute.getDefaultValue().toString());
  }
}
