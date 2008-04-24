require File.expand_path(File.dirname(__FILE__) + "/cell2")

module Table2
  
  def self.extended(prop)
    prop.cell_index = {}
    10.times do |y|
      row = Limelight::Prop.new(:name => "row2", :id => y.to_s)
      prop.add(row)
      10.times do |x|
        id = "#{x},#{y}"
        bg_color = ( (x + y) % 2 == 0 ) ? "blue" : "#DDDDDD"
        cell = Limelight::Prop.new(:name => "cell2", :id => id, :text => id)
        cell.extend(Cell2)
        cell.style.background_color = bg_color
        row.add(cell)     
      end
    end
  end
  
  attr_accessor :cell_index
  
end