//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.styles.attributes;

import limelight.styles.compiling.DimensionAttributeCompiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BackgroundImageYAttributeTest extends Assert
{
  private BackgroundImageYAttribute attribute;

  @Before
  public void setUp() throws Exception
  {
    attribute = new BackgroundImageYAttribute();
  }

  @Test
  public void shouldCreation() throws Exception
  {
    assertEquals("Background Image Y", attribute.getName());
    assertEquals("y-coordinate", attribute.getCompiler().type);
    assertEquals("0", attribute.getDefaultValue().toString());
  }
}
