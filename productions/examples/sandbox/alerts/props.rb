__install "header.rb"
arena do
  label :text => "Alert Message:"
  message_input :id => "message_input", :text => "Sample Message", :players => "text_area" 
  action :id => "alert_action", :text => "Open Alert", :on_mouse_clicked => "scene.open_alert"
  action :id => "incompatible_version_action", :text => "Open Incompatible Production", :on_mouse_clicked => "scene.open_incompatible_production"
end