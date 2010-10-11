package limelight.commands;

import limelight.Context;
import limelight.io.FileUtil;

import java.util.Map;

public class OpenCommand extends Command
{
  private static Arguments arguments;
  public static Arguments arguments()
  {
    if(arguments == null)
    {
      arguments = new Arguments();
      arguments.addOptionalParameter("production", "Path to production directory, .llp file, or .lll file.  If none is provided, the Playbills production is opened.");
    }
    return arguments;
  }

  @Override
  public void doExecute(Map<String, String> args)
  {
    String production = args.get("production");
    if(production == null)
      production = defaultProduction();

    Context.instance().studio.open(production);
  }

  @Override
  public String description()
  {
    return "Opens a Limelight Production";
  }

  @Override
  public String name()
  {
    return "open";
  }

  @Override
  public Arguments getArguments()
  {
    return arguments();
  }

  public String defaultProduction()
  {
    return FileUtil.pathTo(Context.instance().limelightHome, "productions", "playbills.lll");
  }
}
