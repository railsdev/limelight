$: << File.expand_path(File.dirname(__FILE__) + "/../../../ruby_src/lib")
require 'init'
require 'limelight/book'

Limelight::Book.new().load("langstons_ant/ant.llm")