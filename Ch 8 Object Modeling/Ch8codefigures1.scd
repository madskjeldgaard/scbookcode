
/////////////////////// Object Modeling code figures /////////////////// 



	//	figure 8.1 - a Puppet class, and tests for it.
	
Puppet { 
	var <>myfreq; // an instance variable with a getter and a setter method
	
		// a method for creating a new object of this kind
	*new { |myfreq=50| ^super.new.myfreq_(myfreq) }
	
		// a simple method that uses 'myfreq' for something audible.
	blip { { Blip.ar(myfreq, 11) * XLine.kr(1, 0.01, 0.6, doneAction: 2) }.play; }
}

	// tests for the behavior implemented so far:
m = Puppet.new(50);// make an instance of Puppet, pass in myfreq

m.dump;			// test that myfreq is set correctly
m.myfreq;			// test accessing myfreq
m.blip;			// should sound
m.myfreq_(100);	// test setting myfreq
m.blip;		 // should sound differently







	// figure  8.2 - a puppet modeled as an event.

m = (); 			// make an empty event
m.myfreq_(50);	// put something in it with a setter method: a pseudo-instance variable
m.myfreq;			// look it up with a getter method
				// put a function into it with a setter: 
				// this becomes a pseudo-method
m.blip_({ |ev| { Blip.ar(ev.myfreq, 11) * XLine.kr(1, 0.01, 0.6, doneAction: 2) }.play; });
m.blip;		// execute the function with a pseudo-method call (same name)





	// figure  8.3 - add more instance variables, change the blip method.
(
m.numHarms_(20); 	// a new instvar
m.decay_(0.3); 	// and another
				// update the blip method to use them:
m.blip_({ |ev| 
	{ Blip.ar(ev.myfreq, ev.numHarms) 
	* XLine.kr(1, 0.01, ev.decay, doneAction: 2) }.play; 
});
)
m.blip;	// test






	//	figure 8.4 - A minimal shout window sketch.

z = z ? (); 	// make an empty event as a pseudo-object
z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
z.txtView.string_("Shout this!");
z.txtView.font_(Font("Monaco", 32));

	// tune appearances
z.win.alpha_(0.7);		// make the window slightly transparent
z.win.view.background_(Color.clear);	// make the window's top view,
z.txtView.background_(Color.clear);	// and textview fully transparent
z.win.alwaysOnTop_(true);	// make sure it is always on top .

z.win.close;	// close when done



	// figure 8.5 - add a pseudo-method.
(
z.makeWin = { |z, message="Shout this!"|
	z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
	z.txtView.string_(message);
	z.txtView.font_(GUI.font.new("Monaco", 32));
	z.txtView.background_(Color.clear);
};
)
z.makeWin; 
z.makeWin("Try showing that."); 





	// figure 8.6 - more pseudo-methods.

z.setMessage = { |z, str| z.txtView.string_(str) };

z.setMessage("Does this update?");	// test
(
z.shout = { |z, str|
	if (z.win.isNil or: { z.win.isClosed }) { z.makeWin };
	z.setMessage(str);
};
)
z.shout("Do we get this?"); // test

z.win.close;
z.shout("Do we get this too?"); // also when window has closed?





	// figure 8.7 - text color animation

z.txtView.stringColor_(Color.red);	// try a single color
(
z.animate = { |z, dt=0.2, n = 6| 
	var colors = [Color.red, Color.green, Color.black];
	Task { 
		n.do { |i|Ê
			dt.wait; 
			z.txtView.stringColor_(colors.wrapAt(i))
		}
	}.play(AppClock)
};
)
z.animate;			// test with default values 
z.animate(0.1, 24); 	// and test with arguments given





	// figure 8.8 is an image - "c6_ObjMod_fig8.8_shoutWin.png"





	// figure 8.9 - using codeDump to shout 

this.codeDump = { |str, result, func| [str, result, func].printAll };

a = 1 + 2;	// code appears in post window

z.shoutTag = "//!!";
this.codeDump = { |str| if (str.beginsWith(z.shoutTag)) { z.shout(str.drop(z.shoutTag.size)) } };

//!! a comment with a 'shout tag' now gets shouted!





	//	figure 8.10 - updated setMessage flashes text.
(
z.setMessage = { |z, str| 
	var messSize = str.size; 
	var fontsize = (1.64 * z.txtView.bounds.width) / max(messSize, 32);
	z.txtView.font_(GUI.font.new("Monaco", fontsize));
	z.txtView.string_(str);
	z.animate;
};
)
//!! a long comment gets scaled down to a rather smaller font size, minimally fontsize 32! 

//!! short is big!
(
z.makeWin = { |q, message="Shout this!"|
	z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
	z.txtView.background_(Color.clear);
	z.setMessage(message);
};
)
z.makeWin("shout.");






	//	figure 8.11 - a Shout class

// begin of file - Shout.sc
Shout {
	classvar <>tag="//!!";
	var <win, <txtView;
	
	*new { ^super.new }
} 
// end of file - Shout.sc

// tests: 
Shout.tag;
Shout.tag_("//SHOUT");

a = Shout.new; 
a.win;
a.txtView;




	// figure 8.12 - more class variables and initClass method.

// begin of file - Shout.sc
Shout {
	classvar <>tag="//!!", <>width=1250, <>defaultCodeDumpFunc; 
	var <win, <txtView;
	
	*initClass { 
		defaultCodeDumpFunc = { |str| if (str.beginsWith(tag)) { Shout(str.drop(tag.size)) } };
	}
	*new { ^super.new }
} 
// end of Shout.sc

Shout.width;
Shout.defaultCodeDumpFunc;




	// figure 8.13 - converting makeWin.

z.makeWin = { |message="Shout this!"|
	z.win = GUI.window.new("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = GUI.textView.new(z.win, Rect(0, 0,1200, 100));
	z.txtView.background_(Color.clear);
	z.setMessage(message);
};


Shout {	
	...
	*new { |message| ^super.new.makeWin(message); }
	
	makeWin { |message="Shout this!"| 
	
		win = Window("Shout'er", Rect(20, 800, width, 80)).front;
		win.alpha_(0.7);
		win.view.background_(Color.clear);
		win.alwaysOnTop_(true);
		
		txtView = TextView(win, win.bounds.moveTo(0,0));
		txtView.background_(Color.clear);
		txtView.font_(Font.new("Monaco", 32));
		this.setMessage(message);
	}

	setMessage { |message| 
		txtView.string_(message.asString)
	}
}

// tests:
Shout.new;
a = Shout.new("Blabla");
a.setMessage("Otto");






	//	figure 8.14 - converting z.shout to Shout.new.

z.shout = { |z, str|
	if (z.win.isNil or: { z.win.isClosed }) { z.makeWin };
	z.setMessage(str);
};

Shout { 
	classvar <top;
	...
	*new { |message="ÁShout'er!"| 

		if (top.isNil or: { top.win.isClosed }) { 
			top = this.basicNew(message); 
		} {
			top.setMessage(message);
		};
	}
		// the method formerly known as *new
	*basicNew { |message="Shout this!"| ^super.new.makeWin(message) } 

	*close { try { top.win.close } }
	
	...
}

// tests: 
Shout("Test 1, 2");
Shout("Test 1, 2, 3, 4");	// same window
Shout.close;

Shout("Test 1, 2");		// new window






	//	figure 8.15 - converting animate to a class method.

z.animate = { |z, dt=0.2, n = 6| 
	var colors = [Color.red, Color.green, Color.black];
	Task { 
		n.do { |i|Ê
			dt.wait; 
			z.txtView.stringColor_(colors.wrapAt(i))
		}
	}.play(AppClock)
};

Shout { 
	...
	animate { |dt=0.2, n=6|
		var colors = [Color.red, Color.green, Color.black]; 
		Task { 
			n.do { |i| 
				txtView.stringColor_(colors.wrapAt(i)); 
				dt.wait 
			};
			txtView.stringColor_(Color.black); // make sure we end black
		}.play(AppClock);
	}
	...	
}

// tests: 
a = Shout("Test 1, 2");
Shout.top.animate;





 
	// figure 8.16 - converting setMessage

z.setMessage = { |z, str| 
	var messSize = str.size; 
	var fontsize = (1.64 * z.txtView.bounds.width) / max(messSize, 32);
	z.txtView.font_(GUI.font.new("Monaco", fontsize));
	z.txtView.string_(str);
	z.animate;
};

Shout { 
	...
	setMessage { |message| 
		var messSize, fontSize;
		messSize = message.size;
		fontSize = (1.64 * width) / max(messSize, 32);
		
		defer { 
			txtView.font_(Font("Monaco", fontSize))
				.string_(message.asString);
		};
		this.animate;
	}
	...	
}

Shout("Test 1, 2");
Shout("Test" + (1..16));







	//	figure 8.17 - codeDump tests and usage in Shout

this.codeDump.postcs;	// anything there yet? by default, this is nil.
this.codeDump = this.codeDump.addFunc(Shout.defaultCodeDumpFunc); // add Shout
this.codeDump.postcs	// should be there now
//!! test whether Shout works now
this.codeDump = this.codeDump.removeFunc(Shout.defaultCodeDumpFunc);
this.codeDump.postcs	// should be gone now

//!! should be off again

Shout { 
	...
	*add { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump
			.removeFunc(defaultCodeDumpFunc) // remove it first so it will 
										// only be in the list once
			.addFunc(defaultCodeDumpFunc); 
	}
	*remove { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump.removeFunc(defaultCodeDumpFunc); 
	}
	...	
}

// tests
Shout.add;
//!! test whether Shout works now - it should!
Shout.remove;
//!! test whether Shout works now - should be off.






	//	figure 8.18 - keeping Shout out of the way

Shout.close;
Shout("blabla");	// now typing is impossible, because the new window is in front.

Shout("blabla blabla");	// now one can type, because Shout window was already there.
Shout.close; 

	// this does not work, because the shout window gets put in front later:
d = Document.current; Shout(\bla); d.front; 
 

	// This can be wrapped around the call to this.makeWin in Shout:new:
Shout { 
	...
	*new { |message="ÁShout'er!"| 
		var currDoc;

		if (win.isNil or: { win.isClosed }) { 
			currDoc = Document.current;
			top = this.basicNew(message); 
				// wait a little before restoring front window
			defer ({ currDoc.front }, 0.1);
		} {
			top.setMessage(message);
		};
	}
	...	
}






	// figure 8.19 - a fixed serialization method.

serialize {|index, pos, extent = 8|
	var ranges, slice;

	// get ranges
	ranges = pos.collect{|pos, i| 
		((pos-(extent*0.5))..(pos+(extent*0.5)-1)) % this.shape[i] 
	};
	// get sub-slice
	slice = this.slice(index, *ranges);
	// trivial serialization of multidim. slice
	^slice.flat
}






	// figure 8.20 - flexible  serialization by lookup.

serialize {|index, pos, extent = 8, how = \hilbert|
	// [...]
	slice = this.slice(index, *ranges);
	// call function in serTypes dictionary
	^serTypes[how].(slice.asArray, extent)
}







// figure 8.21 Ñ some initial serialization methods, and adding an alternative. 

	*initClass {|numDims = 4|
		...
		// slice here is a 4d hypercube of extent <extent>
		serTypes = (
			hilbert: {|slice, extent|
				extent.isPowerOfTwo.not.if({
					"QCD:serialize: extent has to be a power of two".error
				});
				HilbertIndices.serialize(slice)
			},
			torus: {|slice, extent|
				slice.flat;
			},
			scramble: {|slice|
				slice.flat.scramble;
			}
		);
	}

	// add a new serialization type at runtime
QCD.serTypes.put(\star, {|slice|
	var starSize = slice.size div:2;
	var numDims = 4;
	var starShape;

	starShape = neighbours1.collect({ |nb|  
		(0..starSize).collect(_ * nb) 
	}).flatten(1).collect {|indexN| 
		indexN + (starSize.div(2)+1).dup(numDims) 
	};
	
	starShape.collect{|iA| slice.slice(*iA)}
});





///////// continued in ObjMod2_codefigures.rtf ///// 


