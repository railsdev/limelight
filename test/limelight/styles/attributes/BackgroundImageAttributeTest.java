//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.styles.attributes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BackgroundImageAttributeTest extends Assert
{
  private BackgroundImageAttribute attribute;

  @Before
  public void setUp() throws Exception
  {
    attribute = new BackgroundImageAttribute();
  }

  @Test
  public void shouldCreation() throws Exception
  {
    assertEquals("Background Image", attribute.getName());
    assertEquals("noneable string", attribute.getCompiler().type);
    assertEquals("none", attribute.getDefaultValue().toString());
  }
}
