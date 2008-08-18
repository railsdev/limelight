#- Copyright 2008 8th Light, Inc.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require File.expand_path(File.dirname(__FILE__) + "/../spec_helper")
require 'limelight/prop'
require 'limelight/players/text_box'

describe Limelight::Players::TextBox do

  before(:each) do
    @prop = Limelight::Prop.new
    @prop.add_controller(Limelight::Players::TextBox)
  end

  it "should have a TextField" do
    @prop.panel.children[0].class.should == Limelight::UI::Model::Inputs::TextBoxPanel
  end
  
  it "should use the TextField for the text accessor" do
    @prop.text = "blah"
    @prop.panel.children[0].text.should == "blah"
    
    @prop.panel.children[0].text = "harumph"
    @prop.text.should == "harumph"
  end

end