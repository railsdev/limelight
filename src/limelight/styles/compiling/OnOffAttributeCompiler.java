//- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the MIT License.

package limelight.styles.compiling;

import limelight.styles.abstrstyling.StyleCompiler;
import limelight.styles.abstrstyling.StyleValue;
import limelight.styles.values.SimpleOnOffValue;

public class OnOffAttributeCompiler extends StyleCompiler
{
  public StyleValue compile(Object value)
  {
    String lowerCaseValue = stringify(value).toLowerCase();
    if("on".equals(lowerCaseValue) || "true".equals(lowerCaseValue))
      return new SimpleOnOffValue(true);
    else if("off".equals(lowerCaseValue) || "false".equals(lowerCaseValue))
      return new SimpleOnOffValue(false);
    else
      throw makeError(value);
  }
}
