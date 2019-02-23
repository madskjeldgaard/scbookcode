/* 	Magellan Single Country Examples

Server.default = s = Server.internal.boot;
p = ProxySpace.push(s);

1. 	run ./1_MagellanFinalPrep.scd first to load data.
2.	run this entire file to try what single countries sound like
3.	run the piece from file 5_FinalPlayer.scd


//////// some countries to try line by line

	// little water: 
q.setPxFromData(\Afghanistan); 	// no gini
q.setPxFromData(\Laos); 		// gini known
	
	// low population density:
q.setPxFromData(\Mongolia); 	

	// high pop density
q.setPxFromData(\Hong_Kong); 	
q.setPxFromData(\China); 		


q.setPxFromData(\Denmark); 			// lowest gini
q.setPxFromData(\Switzerland); 		// slightly higher gini
q.setPxFromData(\United_States); 	// much higher gini

	// very poor, no gini
q.setPxFromData(\East_Timor); 	
q.setPxFromData(\Cape_Verde); 
	// very rich, no gini
q.setPxFromData(\Brunei); 

	// poor, known gini
q.setPxFromData(\Mozambique); 		// low gini
q.setPxFromData(\Lesotho); 			// VERY high gini
q.setPxFromData(\Sierra_Leone); 		// highest of all ginis



*/
(
	// adapt to number of speakers available!
q.numSpeakers = 8;		
 
~single = { |rootFreq = 220, outProp = 4.5, inProp = 2.2, 
	attack=0.0, decay = 1.0, dens = 2,
	 amp = 0.2, x = 0, y = 1, step=0.33333| 
	 
	var freqs, exciter, resonator, ampcomp, pos; 
	
	freqs = rootFreq * [ 1/outProp, 1/inProp, 1, inProp, outProp]; 
	
	exciter = (Dust2.ar(dens * [ 1,2,4,2,1] * 0.07) 	// five individual rd triggers for 
												// each component, weighted for center
			+ Dust2.ar(dens * 0.3))					// + some common attacks
				.clip2(0.5)	
				.lag(0.0003) 
				* (dens ** -0.5)				// amplitude compensation for density
			+ PinkNoise.ar(0.002)				// soft background noise 'halo' for fusion
			* (decay ** -0.5);					// amplitude compensation for decay time
				
	resonator = Formlet.ar( exciter, freqs, 
		Ramp.kr(attack, step), decay, 
		AmpComp.kr(freqs.max(50))
	).softclip.sum; 
	
		// convert x, y to angle for panaz, 
		// and rotate to look west.
	pos = atan2(Ramp.kr(y, step), Ramp.kr(x, step)) * (-pi.reciprocal) + 0.5; 

	PanAz.ar(q.numSpeakers, LeakDC.ar(resonator, 0.95), pos, Ramp.kr(amp, step));
};

q.setPxFromData = { |q, name, proxy| 
		var cntIndex = q.worlddata.atChan('Country').data.indexOf(name); 
		var dataVect = q.worlddata.atVector(cntIndex, 
			[\GDP_per_capita, \Density, \Drinking_total, \prop10, \prop20]);
		
		var gdp, dens, drink, prop10, prop20; 
		[name, cntIndex, dataVect].postln; 
		#gdp, dens, drink, prop10, prop20 = dataVect; 
		
		gdp = gdp.expexp(400, 58900, 200, 2200); 
		dens = dens.expexp(1.8, 16204.5, 1.8, 170); 
		drink = (drink ? 100).linlin(13, 100, 0.2, 1.5);  // 1.5 secs max
		proxy = proxy ?Ê~single; 
		proxy.set(			
			\rootFreq, gdp, 
			\outProp, (prop10 ? 1.2).sqrt, 	// unknown ginis form quartertone cluster
			\inProp, (prop20 ? 1.1).sqrt, 	 
			\decay, drink, 				
			\dens, dens 
		);
};

~single.playN(vol: 0.25);

q.makeButsWin = {
	var w, btns;
	w = Window( "Select a Country", Rect( 128, 400, 1020, 480 ));

	w.view.decorator = FlowLayout( w.view.bounds, 4@4, 1@1 );
	w.view.background = Color( 0.6, 0.8, 0.8 );

	btns = q.worlddata.atChan(\Country).data.collect({ arg name;
		// the same is true for all other widgets
		Button( w, Rect( 0, 0, 100, 24 ))
			.states_([[ name, Color.black, Color.rand(0.6, 1.0) ]])
			.action_({ q.setPxFromData(name); });
	});
	w.front;
};
q.makeButsWin;

Spec.add(\rootFreq, \freq);
Spec.add(\outProp, [1, 100, \exp, 0.01, 10]);
Spec.add(\inProp, [1, 100, \exp, 0.01, 10]);
Spec.add(\attack, [0.0001, 0.1, \exp, 0.0001, 0.01]);
Spec.add(\decay, [0.1, 100, \exp, 0.01, 1]);
Spec.add(\dens, [1, 1000, \exp, 0.01, 10]);
Spec.add(\x, \pan); Spec.add(\y, \pan);
Spec.add(\step, [0.001, 1, \exp, 0.001, 0.1]);

NodeProxyEditor(~single, 12);

	// a window to try changing the directions
q.dirwin = Window("Check Directions", Rect(350,0,240,240)).front;
StaticText(q.dirwin, Rect(70, 0, 100, 20)).string_("West").align_(\center);
StaticText(q.dirwin, Rect(70, 220, 100, 20)).string_("East").align_(\center);
StaticText(q.dirwin, Rect(0, 50, 20, 100)).string_("S\no\nu\nt\nh").align_(\center);
StaticText(q.dirwin, Rect(220, 50, 20, 100)).string_("N\no\nr\nt\nh").align_(\center);

Slider2D(q.dirwin, Rect(20,20,200,200))
	.x_(0.5).y_(1)
	.action = { |sl2d| ~single.set(\x, sl2d.x * 2 - 1, \y, sl2d.y * 2 - 1) };

	// show the speaker setup and sound levels in a SpatioScope if there
q.makeSpatio = { 
	var spatClass = 'SpatioScope'.asClass; 

if (spatClass.notNil) { 
	q.spatio = spatClass.rings([8], 0.8, -0.625pi, bounds: Rect(0, 0, 220, 220));
	q.spatio.parent.bounds_(q.spatio.parent.bounds.moveTo(600,0));
} { 
"	//The SpatioScope class (not installed) could be useful here.
	// To install it, do :
	Quarks.install(\"SpeakersCorner\");".postln;
}
};
q.makeSpatio;
)

