#- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

require 'limelight/java_util'
require 'limelight/pen'
require 'limelight/paint_action'
require 'limelight/animation'
require 'limelight/util/hashes'

module Limelight

  # Prop is the fundamental building block of a scene.  A prop represents a rectangular area in the scene, of almost any dimension.
  # It may have borders, backgrounds, margin, padding, and it may contain other props or text.  However it is the props'
  # Styles that determine their size and appearance.
  #
  # A Prop may have one parent and many children.  Hense, when put together, they form a tree structure.  The Scene is
  # the root Prop of a tree.
  #
  class Prop

    class << self
      def panel_class #:nodoc:
        return Java::limelight.ui.model.Prop
      end
    end

    include Java::limelight.ui.api.PropProxy

#    attr_accessor :style, :hover_style
    attr_reader :panel, :players #:nodoc:
#    attr_reader :children, :parent, :name, :id, :players
    getters :loader #:nodoc:


    # When creating a Prop, an optional Hash is accepted. These are called initialization options.
    # The key/value pairs in the initialiaztion options will be used to
    # set properties on the Prop, it Style, or included Player properties. These properties are not set
    # until the prop is added to a Prop tree with a Scene.
    #
    def initialize(hash = Util::StringHash.new)
      @panel = self.class.panel_class.new(self)
#      @style = @panel.style
#      @children = []
#      @options = {}
      @panel.add_options(Util::Hashes.select(hash))
    end

    def id
      return @panel.getId
    end

    def name
      return @panel.name
    end

    def style
      return @panel.style
    end

    def hover_style
      return @panel.hover_style
    end

    # Add a Prop as a child of this Prop.
    #
    def add(child)
#      child.set_parent(self)
#      @children << child
      @panel.add(child.panel)
    end

    # Same as add.  Returns self so adding may be chained.
    #
    #   prop << child1 << child2 << child3
    #
    def <<(child)
      add(child)
      return self
    end

    # Allows the adding of child Props using the PropBuilder DSL.
    #
    #    prop.build do
    #      child1 do
    #        grand_child
    #      end
    #      child2
    #    end
    #
    def build(options = {}, & block)
      require 'limelight/dsl/prop_builder'
      builder = Limelight::DSL::PropBuilder.new(self)
      builder.__install_instance_variables(options)
      builder.__loader__ = scene.loader
      builder.instance_eval(& block)
    end

    # Removes a child Prop.  The child Prop will be parentless after removal.
    #
    def remove(child)
#      if children.delete(child)
#        scene.unindex_prop(child) if scene
        @panel.remove(child.panel)
#      end
    end

    # Removes all child Props.
    #
    def remove_all
      @panel.remove_all
#      @children.each { |child| scene.unindex_prop(child) } if scene
#      @children = []
    end

    # Injects the behavior of the specified Player into the Prop.  The Player must be a Module.
    #
    def include_player(player_module)
      unless self.is_a?(player_module)
        extend player_module
        self.casted if player_module.instance_methods.include?("casted")
      end
    end

    # A hook to invoke behavior after a Prop is painted.
    #
    def after_painting(flag = true, & block)
      if flag
        @panel.after_paint_action = PaintAction.new(& block)
      else
        @panel.after_paint_action = nil
      end
    end

    # Searches all descendant of the Prop (including itself) for Props with the specified name.
    # Returns an Array of matching Props. Returns an empty Array if none are found.
    #
    def find_by_name(name)
      return @panel.find_by_name(name).map { |descendant| descendant.prop } 
    end

    # Sets the text of this Prop.  If a prop is given text, it will become sterilized (it may not have any more children).
    # Some Players such as text_box, will cause the text to appear in the text_box.
    #
    def text=(value)
      @panel.text = value.to_s
    end

    # Returns the text of the Prop.
    #
    def text
      return @panel.text
    end

    def parent
      return @panel.parent.prop if @panel.parent
    end

    def children
      @panel.child_prop_panels.map { |child| child.prop } 
    end

    # Returns the scene to which this prop belongs to.
    #
    def scene
#      return nil if @parent.nil?
#      @scene = @parent.scene if @scene.nil?
      return @panel.root.prop
    end

    # TODO get rid of me.... The Java Prop interface declares this method.
    def loader
      return scene.production.root;
    end

    # Returns the current Production this Prop lives in.
    #
    def production
      return scene.production
    end

    def to_s #:nodoc:
      return "#{self.class.name}[id: #{@id}, name: #{@name}]"
    end

    def inspect #:nodoc:
      return self.to_s
    end

#    # unusual name because it's not part of public api
#    def set_parent(parent) #:nodoc:
#      @parent = parent
#      if @parent.illuminated?
#        illuminate
#      end
#    end

#    # Allows the addition of extra initialization options.  Will raise an exception if the Prop has already been
#    # illuminated (added to a scene).
#    #
#    def add_options(more_options)
#      return unless more_options
#      raise "Too late to add options" if illuminated?
#
#      @name = more_options.delete(:name) if more_options.has_key?(:name)
#      @additional_styles = more_options.delete(:styles) if more_options.has_key?(:styles)
#      panel.setStyles("#{@name}, #{@additional_styles}")
#
#      @options.merge!(more_options)
#    end

    # Returns a Point representing the location of the Prop's top-left corner within its parent.
    #
    #   location = prop.location
    #   location.x # the Prop's distance from the left of its parent's bounds
    #   location.y # the Prop's distance from the top of its parent's bounds
    #
    def location
      return panel.get_location
    end

    # Returns a Point representing the location of the Prop's top-left corner within its Stage (Window).
    #
    #   location = prop.absolute_location
    #   location.x # the Prop's distance from the left edge of the stage
    #   location.y # the Prop's distance from the top edge of the stage
    #
    def absolute_location
      return panel.get_absolute_location.clone
    end

    # Returns a Box representing the relative bounds of the Prop. Is useful with using the Pen.
    #
    #   bounds = prop.bounds
    #   bounds.x, box.y # represents the Prop's location within its parent Prop
    #   bounds.width, box.height # represents the Prop's dimensions
    #
    def bounds
      return panel.get_bounds.clone
    end

    # Returns a Box representing the bounds inside the borders of the prop.
    #
    #   bounds = prop.bordered_bounds
    #   bounds.x # the distance between the left edge of the Prop and the inside edge of its left border
    #   bounds.y # the distance between the top edge of the Prop and the inside edge of its top border
    #   bounds.width # the distance between the inside edges of the left and right border
    #   bounds.height # the distance between the inside edges of the top and bottom borders
    #
    def bordered_bounds
      return panel.get_bordered_bounds.clone
    end

    # Returns a Box representing the bounds inside the padding of the prop.
    #
    #   bounds = prop.padded_bounds
    #   bounds.x # the distance between the left edge of the Prop and the inside edge of its left padding
    #   bounds.y # the distance between the top edge of the Prop and the inside edge of its top padding
    #   bounds.width # the distance between the inside edges of the left and right padding
    #   bounds.height # the distance between the inside edges of the top and bottom padding
    #
    def padded_bounds
      return panel.get_padded_bounds.clone
    end

    # Returns a Pen object. Pen objects allow to you to draw directly on the screen, withing to bounds of this Prop.
    #
    def pen
      return Pen.new(panel.getGraphics)
    end

    # Initiate an animation loop.  Options may include :name (string), :updates_per_second (int: defaults to 60)
    # An Animation object is returned.
    # The provided block will be invoked :updates_per_second times per second until the Animation is stopped.
    #
    #    @animation = prop.animate(:updates_per_second => 20) do
    #      prop.style.border_width = (prop.style.top_border_width.to_i + 1).to_s
    #      @animation.stop if prop.style.top_border_width.to_i > 60
    #    end
    #
    # This above example will cause the Prop's border to grow until it is 60 pixels wide.
    #
    def animate(options={}, & block)
      animation = Animation.new(self, block, options)
      animation.start
      return animation
    end

    # Plays a sound on the computers audio output.  The parameter is the filename of a .au sound file.
    # This filename should relative to the root directory of the current Production, or an absolute path.
    #
    def play_sound(filename)
      @panel.play_sound(scene.loader.path_to(filename))
    end

    # Luanches the spcified URL using the OS's default handlers. For example, opening a URL in a browser:
    #
    #   launch('http://www.google.com')
    #
    # To create a link prop add an accessor on the player (say url) and use that in the prop definition
    # Ex:
    #
    #   link :text => "I am a link", :url => "http://www.8thlight.com"
    def launch(url)
      Context.instance.os.launch(url)
    end

    # GUI Events ##########################################

    def on_mouse_pressed(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MousePressedEvent, action)
    end

    def on_mouse_released(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseReleasedEvent, action)
    end

    def on_mouse_clicked(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseClickedEvent, action)
    end

    def on_mouse_moved(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseMovedEvent, action)
    end

    def on_mouse_dragged(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseDraggedEvent, action)
    end

    def on_mouse_entered(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseEnteredEvent, action)
    end

    def on_mouse_exited(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseExitedEvent, action)
    end

    def on_mouse_wheel(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::MouseWheelEvent, action)
    end

    def on_key_pressed(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::KeyPressedEvent, action)
    end

    def on_key_released(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::KeyReleasedEvent, action)
    end

    def on_char_typed(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::CharTypedEvent, action)
    end

    def on_focus_gained(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::FocusGainedEvent, action)
    end

    def on_focus_lost(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::FocusLostEvent, action)
    end

    def on_button_pushed(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::ButtonPushedEvent, action)
    end

    def on_value_changed(& action)
      @panel.event_handler.add(Limelight::UI::PanelEvents::ValueChangedEvent, action)
    end

    def apply_options(options)
      options = Util::Hashes.select(options)
      options.keys.each do |key|
        setter_sym = "#{key.to_s}=".to_sym
        if self.respond_to?(setter_sym)
          self.send(setter_sym, options.delete(key))
        elsif self.style.respond_to?(setter_sym)
          self.style.send(setter_sym, options.delete(key).to_s)
        elsif is_event_setter(key)
          define_event(key, options.delete(key))
        end
      end
    end
    alias :applyOptions :apply_options

    private ###############################################

    def is_event_setter(symbol)
      string_value = symbol.to_s
      return string_value[0..2] == "on_" && self.respond_to?(symbol)
    end

    def define_event(symbol, value)
      self.send(symbol) { eval(value) };
    end

  end
end