
OSCIISlider {

	var slider, valueText, nameText, spec, lastval, font;

	*new { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin; 
		^super.new.initOSCIISlider(w, bounds, name, min, max, start, step, warp);
		}
	
	initOSCIISlider { arg w, bounds, name, min=0.0, max=1.0, start=0.0, step=0.0, warp=\lin;
		var namerect, numberrect, slidrect;
		
		font =Font("Helvetica", 12);
		lastval= start;
		spec = ControlSpec(min,max,warp,step,start);
		
		namerect= Rect(bounds.left,(bounds.height)+bounds.top,(bounds.width)-30,20);
		numberrect= Rect(bounds.left+(bounds.width)-20,(bounds.height)+bounds.top,38,20);
		slidrect= Rect(bounds.left,bounds.top,bounds.width,bounds.height);
		
		nameText = SCStaticText(w, namerect)
			.font_(font)		
			.string_(name);
		valueText = SCStaticText(w, numberrect)
					.string_(lastval)
					.font_(font);
		slider = SCSlider.new( w, slidrect);
		
		slider.background_(Color.new255(160, 170, 255, 100));
		
		//set slider to default value, else will default to 0.0
		slider.value_(spec.unmap(lastval));
		
		//set associated variable to this value, client code will poll this rather than the slider directly
		//so safe for TempoClock use etc
		
		slider.action_({arg sl; var val; 
					val = spec.map(sl.value);  
					valueText.string_(val); 
					lastval=val;
					});
				
	} // end of main func
	
	// set value from outside
	value_ {arg val;
		slider.value_(spec.unmap(val));
		//slider.update;
		valueText.string_(val);
		lastval = val;
		}
		
	// get the value
	value{
		^lastval;
		}
		
	action_ { arg func;
		slider.action_({arg sl; var val; 
			val = spec.map(sl.value);  
			valueText.string_(val); 
			lastval=val;  
			//lastval = sl;
			func.value(lastval);
		});
	}
	
	valueAction_ { arg value; var val;
		slider.valueAction = spec.unmap(value);
	}
	
	setBgColor_ {arg color;
		slider.background_(color);
	}
	
	canFocus_ {arg bool;
		slider.canFocus_(bool);
	}
	
	keyDownAction_ { arg func;
		slider.keyDownAction_(func);
	}
	
	font_{arg argfont;
		font = argfont;
		valueText.font_(font); 
		nameText.font_(font);
	}
	
	remove {
		slider.remove; 
		nameText.remove;
		valueText.remove;
	}
} // end of class

