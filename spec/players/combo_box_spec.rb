#- Copyright 2008 8th Light, Inc.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require File.expand_path(File.dirname(__FILE__) + "/../spec_helper")
require 'limelight/scene'
require 'limelight/prop'
require 'limelight/players/combo_box'

describe Limelight::Players::ComboBox do

  before(:each) do
    @scene = Limelight::Scene.new(:casting_director => make_mock("caster", :fill_cast => nil))
    @prop = Limelight::Prop.new(:scene => @scene)
    @prop.include_player(Limelight::Players::ComboBox)
  end
  
  it "should have a ComboBox" do
    @prop.panel.children[0].class.should == Limelight::UI::Model::Inputs::ComboBoxPanel
  end
  
  it "should have settable value" do
    @prop.choices = ["1", "2", "3"]
    @prop.value.should == "1"
    
    @prop.value = "3"
    @prop.value.should == "3"
  end

end