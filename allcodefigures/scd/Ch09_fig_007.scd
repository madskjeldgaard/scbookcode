figure 9.7

(
var window, slider, updater, model, level, numberbox, setValueFunction, oscresponder;

//model
model = (myValue: 1);
setValueFunction = {|value| 
			model [\myValue] = value; 
			model.changed(\value, value);
};

//view
window = SCWindow("myWindow", Rect(100,100, 288, 80));
numberbox = SCNumberBox(window, Rect(20, 20, 44,20))
			.value_(model[\myValue])
			.action_({|view| setValueFunction.value(view.value)}); 
slider = SCSlider(window, Rect(68,20, 200, 20))
			.value_(model[\myValue])
			.action_({|view| setValueFunction.value(view.value)}); 
level = SCLevelIndicator(window, Rect(272, 20, 5, 20))
			.warning_(0.8)
			.critical_(1.0);
	
window.front;
window.onClose_({model.removeDependant(updater); oscresponder.remove;});

//updater
updater = {|theChanger, what, val| 
					{
					if(what == \value, {
						numberbox.value_(val);
						slider.value_(val);
						level.value_(val);
					});
					}.defer;
				};
model.addDependant(updater);

// play a Synth on the server
SynthDef("send_trig",{
	SendTrig.kr(Dust.kr(2.0), 0, LFNoise1.kr(1, 0.5, 0.5));
}).play(s);

// register to receive the message from the Synth above and let it set the model
oscresponder = OSCresponderNode(s.addr, '/tr', { arg time, responder, msg;
	setValueFunction.value(msg[3]);
}).add;

)

