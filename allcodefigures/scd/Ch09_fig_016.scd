
figure 9.16

(
var createGUIFor, synthDefName, eventForSynthDef;
synthDefName = \default;
SynthDescLib.global.read;

//create a custom ControlSpec if necessary:
ControlSpec.specs.put(\out, ControlSpec(0, 128, \lin, 1, 0));

//model 

eventForSynthDef = {|synthDefName|
	var event, node;
	event = (
			instrument: synthDefName,
			play: {
				var args = event.select{|it| it.isKindOf(Number)}.asKeyValuePairs;
				node = Synth(~instrument, args);
					OSCpathResponder(Server.default.addr, ["/n_end", node.nodeID], 
						{|time, resp, msg| 
							node = nil;
							event.changed(\play, 0); 
							resp.remove;
						} 
					).add;		
				event.changed(\play, 1);
				
			},
			stopPlaying:{
				if( SynthDescLib.global[synthDefName].hasGate) {
					node.release;
				}{
					node.free;					
				};
				node = nil;
				event.changed(\play, 0);
				
			},
			setArg: {|inevent, argName, value|
				inevent.use{
					if(node.notNil){
						node.set(argName, value);
					};
					inevent.put(argName.asSymbol, value);
					inevent.changed(argName.asSymbol);					
				}
			}
	);
	SynthDescLib.global[synthDefName.asSymbol].controls.do{|it|
		event.put(it.name.asSymbol, it.defaultValue);
	};
	event
};

createGUIFor = {|event, window|
	var controlNames, playButton, height, updater, funcDict;

	//GUI
		
	controlNames = SynthDescLib.global[event[\instrument]].controls;
	
	height = controlNames.size * 24 +40;
	
	window = SCWindow("myWindow", Rect(100,100, 328, height));
	window.view.decorator_(FlowLayout(window.view.bounds));
	
	SCStaticText(window, 80@20).string_(event[\instrument].asString);
	
	playButton = SCButton(window, 120@20)
			.states_([["play"],["stop"]])
			.action_({|view| 
				if(view.value==1){
					event.play;	
				}{
					event.stopPlaying;
				}
			});
	
	funcDict = IdentityDictionary.new;
	
	//register button in funcDict
	funcDict.put(\play, {|value| {playButton.value_(value)}.defer});	
	
	window.view.decorator.nextLine;
	
	//create sliders
	
	controlNames.do{|control, i| 
		var spec, name, action, initValue, slider;
		name = control.name;
		spec = name.asSymbol.asSpec ? [0,1].asSpec;
		action = {|view| event.setArg(name, view.value)};
		initValue = control.defaultValue;
		slider = EZSlider(window, 300@20, name, spec, action, initValue);
		window.view.decorator.nextLine;
		
		//register slider at controller
		
		funcDict.put(control.asSymbol, {|value|
			{
				slider.value_(spec.unmap(value));
			}.defer
		})
	};
	
	// updater
	updater = {|theChanger, what, value|
		funcDict[what].value(value);
	};
	event.addDependant(updater);
	
	window.front;
	window.onClose_{event.stopPlaying; event.removeDependant(updater)};
	

};

//evaluate the GUI function 

createGUIFor.value(eventForSynthDef.(synthDefName));
)

