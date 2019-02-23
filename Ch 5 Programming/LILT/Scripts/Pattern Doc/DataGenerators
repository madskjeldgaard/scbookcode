/* (IZ 2005-09-04)

- Data Generators

Pwhite(lo, hi, length)
Produce random values between lo and hi
Pbind(\midinote, Pwhite(30, 90, 256), \dur, 0.07).play;

Pbrown(lo, hi, step, length)
Produce values between lo and hi, each time varying from the previous value by step.
Pbind(\midinote, Pbrown(30, 90, 5, 256), \dur, 0.07).play;

Pgeom(start, grow, length)
Produce values changing geometrically (multiplied) by grow.
Pbind(\freq, Pseq([Pgeom(100, 1.1, 40), Pgeom(100, 1.2, 20), Pgeom(7000, 0.9, 40)]), \dur, 0.07).play(SystemClock);

Pseries(start, step, length);
Produce values changing arithmetically (addition) by step;
Pbind(\midinote, Pseq([Pseries(50, 1, 30), Pseries(80, -2, 25), Pseries(30, 3, 20)]), \dur, 0.07).play(SystemClock);

*/
/* The present script also makes a session with example scripts each on a separate script window.
The Pgeom example shows how to pause a stream in a script and resume it from last time paused. 
*/

var session;
~script.placeGui(0, 0);
~start = {
	session = Session.newAdd("Data Generators");
	session.addScript(
		Script("Pwhite Example", nil, 
		"~start = { Pbind(\\midinote, Pwhite(30, 90, 256), \\dur, 0.07).play(SystemClock); };"
	));
	session.addScript(
		Script("Pbrown Example", nil, 
		"~start = { Pbind(\\midinote, Pbrown(30, 90, 5, 256), \\dur, 0.07).play(SystemClock); };"
	));
	session.addScript(
		Script("Pgeom Example", nil, 
		"~start = {\
			if (~stream.isNil) {\
				~stream = Pbind(\\freq, Pseq([Pgeom(100, 1.1, 40), Pgeom(100, 1.2, 20), \
				Pgeom(7000, 0.9, 40)]), \\dur, 0.07).play(SystemClock);\
			}{\
				if(~stream.wasStopped.not) { ~stream.reset };\
				~stream.resume;\
			}\
		};\
		~stop = {\
			~stream.pause;\			
		};"
	));
	session.addScript(
		Script("Pseries Example", nil, 
		"~start = { Pbind(\\midinote, Pseq([Pseries(50, 1, 30), Pseries(80, -2, 25), Pseries(30, 3, 20)]), \\dur, 0.07).play(SystemClock); };"
	));
	Session.current.scriptGroups.selection.makeGui;
	Session.current.scriptGroups.selection.list.do { |s| s.makeGui };
};

~stop = {
	session.remove;
};

