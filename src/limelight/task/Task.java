//- Copyright 2008 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package limelight.task;

public abstract class Task
{
  private TaskEngine engine;
  private String name;

  public Task(String name)
  {
    this.name = name;
  }

  public Task()
  {
  }

  public abstract boolean isReady();
  public abstract void perform();

  public TaskEngine getEngine()
  {
    return engine;
  }

  public void setEngine(TaskEngine engine)
  {
    this.engine = engine;
  }

  public String getName()
  {
    return name;
  }
}
