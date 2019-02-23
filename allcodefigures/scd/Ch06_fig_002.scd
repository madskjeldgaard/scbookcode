Figure 6.2. Example of a key/value Array and a note Event=
a = [ 
	type:			\note,		
	instrument: 		'sine', 
	freq:			400, 
	amp:			0.1, 
	pan:			0, 
	ar: 			2, 
	dr: 			4, 
	sustain:		2
];

e = (
	type:			\note,		
	instrument: 		'sine', 
	freq:			400, 
	amp:			0.1, 
	pan:			0, 
	ar: 			2, 
	dr: 			4, 
	sustain:		2
);
e.play;					// play the note

