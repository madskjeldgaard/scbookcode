figure 9.5

// simple MVC example
(
var window, slider, level, updater, model, numberbox, setValueFunction;

//model
model = (myValue: 1);
setValueFunction = {|value| 
			model [\myValue] = value; 
			model.changed(\value, value);
};

//view
window = SCWindow("myWindow", Rect(100, 100, 288, 80));
numberbox = SCNumberBox(window, Rect(20, 20, 44, 20))
			.value_(model[\myValue])
			.action_({|view| setValueFunction.value(view.value)}); 
slider = SCSlider(window, Rect(68, 20, 200, 20))
			.value_(model[\myValue])
			.action_({|view| setValueFunction.value(view.value)}); 

level = SCLevelIndicator(window, Rect(272, 20, 5, 20))
			.warning_(0.8)
			.critical_(1.0);

window.front;
window.onClose_({model.removeDependant(updater);});

//updater
updater = {|theChanger, what, val| 
					if(what == \value, {
						numberbox.value_(val);
						slider.value_(val);
						level.value_(val);
					});
				};
model.addDependant(updater);

setValueFunction.value(0.4);
)


