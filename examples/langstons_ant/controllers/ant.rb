class Ant  
  
  NORTH = 1
  SOUTH = 2
  EAST = 3
  WEST = 4
  
  BLACK = "black"
  WHITE = "white"
  
  def initialize(x, y, world, world_size)
    @world = world
    @x = x
    @y = y
    @world_size = world_size
    @direction = NORTH
    @log = @world.page.find("log")
    @steps = 0
  end
  
  def walk    
    loop do   
      make_step
      @steps += 1
      @log.update_counter(@steps)
      @log.update_location("#{@x}, #{@y}")
      # sleep(.25)
    end
  end
  
  def make_step
    cell = @world.cell_index["#{@x},#{@y}"]
    if cell.style.background_color == BLACK
      cell.style.background_color = WHITE
      go_right
    else
      cell.style.background_color = BLACK
      go_left
    end 
    cell.update_now
    wrap_if_needed
  end
  
  def go_right
    case @direction
    when NORTH 
      @direction = EAST
      @x += 1
    when EAST
      @direction = SOUTH
      @y += 1
    when SOUTH
      @direction = WEST
      @x -= 1
    when WEST
      @direction = NORTH
      @y -= 1
    end
  end
  
  def go_left
    case @direction
    when NORTH 
      @direction = WEST
      @x -= 1
    when EAST
      @direction = NORTH
      @y -= 1
    when SOUTH
      @direction = EAST
      @x += 1
    when WEST
      @direction = SOUTH
      @y += 1
    end
  end
  
  def wrap_if_needed
    @y = 0 if @y >= @world_size
    @y = @world_size - 1 if @y < 0
    @x = 0 if @x >= @world_size 
    @x = @world_size - 1 if @x < 0
  end
  
end