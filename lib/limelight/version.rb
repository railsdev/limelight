#- Copyright � 2008-2009 8th Light, Inc. All Rights Reserved.
#- Limelight and all included source files are distributed under terms of the GNU LGPL.

module Limelight
  module VERSION #:nodoc:
    unless defined? MAJOR
      MAJOR  = 0
      MINOR  = 3
      TINY   = 5

      STRING = [MAJOR, MINOR, TINY].join('.')
      TAG    = "REL_" + [MAJOR, MINOR, TINY].join('_')

      NAME   = "Limelight"
      URL    = "http://limelight.8thlight.com"  
    
      DESCRIPTION = "#{NAME}-#{STRING} - Limelight\n#{URL}"
    end
  end
end