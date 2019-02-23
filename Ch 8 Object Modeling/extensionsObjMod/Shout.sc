Shout {
	classvar <top;
	classvar <>tag="//!!", <>width=1250, <>defaultCodeDumpFunc; 

	var <win, <txtView;
	
	*initClass { 
		defaultCodeDumpFunc = { |str| if (str.beginsWith(tag)) { Shout(str.drop(tag.size)) } };
	}

	*new { |message="ÁShout'er!"| 
		var currDoc;

		if (top.isNil or: { top.win.isClosed }) { 
			currDoc = Document.current;
			top = this.basicNew(message); 
			defer ({ currDoc.front }, 0.1);
		} {
			top.setMessage(message);
		};
	}

		// the method formerly known as *new
	*basicNew { |message="Shout this!"| ^super.new.makeWin(message) } 

	*close { try { top.win.close } }
	
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
		var messSize, fontSize;
		messSize = message.size;
		fontSize = (1.64 * width) / max(messSize, 32);
		
		defer { 
			txtView.font_(Font("Monaco", fontSize))
				.string_(message.asString);
		};
		this.animate;
	}
	
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

	*add { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump
			.removeFunc(defaultCodeDumpFunc) // remove it first so it will 
										// only be in the list once
			.addFunc(defaultCodeDumpFunc); 
	}

	*remove { var interp = thisProcess.interpreter; 
		interp.codeDump = interp.codeDump.removeFunc(defaultCodeDumpFunc); 
	}

} 
