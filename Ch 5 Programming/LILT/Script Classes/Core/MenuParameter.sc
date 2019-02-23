/* Parameter for handling menus in Script guis
IZ 051215
*/

MenuParameter : Parameter {
	var <>items;
	makeGui { | gui, adapterEnvir |
		var label, bufsink, numbox, menu, adapter;
		label = SCStaticText(gui, Rect(0,0,100,20)).string_(name);
		bufsink = SCDragSink(gui, Rect(0,0,140,20))
			.string_(" NO BUFFER ")
			.background_(Color(0.3, 0.6, 0.9))
			.font_(Font("Helvetica", 10))
			.canReceiveDragHandler_({ this.canReceiveDragHandler })
			.receiveDragHandler_({ this.receiveDragHandler });
		numbox = SCNumberBox(gui, Rect(0,0,50,20));
		numbox.value = script.envir[name];
		menu = SCPopUpMenu(gui, Rect(0, 0, 100, 20));
		menu.items = items;
		menu.font = Font("Helvetica", 10);
		menu.action = { | me | 
			this.action.(me.value, items[me.value]);
		};
		menu.keyDownAction =  { | me, char, mod, unicode |
			// do not react to space key bubbled from top view!
			//	if (char == $ , { me.valueAction = me.value + 1; ^me });
			switch (char,
				$\r, { me.valueAction = me.value + 1; },
				$\n, { me.valueAction = me.value + 1; },
				3.asAscii, { me.valueAction = me.value + 1; },
				{
					switch (unicode,
						16rF700, { me.valueAction = me.value + 1; },
						16rF703, { me.valueAction = me.value + 1; },
						16rF701, { me.valueAction = me.value - 1; },
						16rF702, { me.valueAction = me.value - 1; }
					)
				}
			)
		};
		adapterEnvir[name] = { | val |
			[\testing, \testing, val, this].postln;
		};
		// perform update of your gui after constructoin, thereby setting gui items
		script.changed(name, script.envir[name]);
	}
}
