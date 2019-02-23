Figure 6.7. Chord events
// 2nd inversion - e loudest
( degree:  [-3,0,2],	sustain: 2, 	db: [-20, -20, -10] ).play			
// 2nd inversion - c loudest
 ( degree:  [-3,0,2],	sustain: 2, 	db: [-20, -10, -20] ).play	
// note "fattened" by three detuned copies
 ( degree: 0,		sustain: 2, 	detune: [0,3, 5] ).play			
// each detune is assigned to a different pitch, fat free.
 ( degree:  [-3,2,4],	sustain: 2, 	detune: [0,3, 5] ).play			
// detune rotates through each note in the chord
 ( degree:  [-3,2,4],	sustain: 2, 	detune: [0,0,0,3,3,3,5,5,5] ).play 	


