//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//mix parameters, choice of sounds etc

//assumes 10 tracks

InfnoMix {

	var <pan, <amp, <rev, <other; //arrays covering each channel
	var <synthdef;  //which synthdef to use for each
	var <bufnum;    //which buffer numbers if necessary
	var <altsynthdef;
	
	*new {|infno|
	
	^super.new.init(infno)
	}
	
	//part volumes will depend on SynthDef? No, should try to get equivalences
	
	//kick, snare, hat, perc,   bass, l1, l2, l3,  chords, other2 = breakbeat
	init {|infno|
	
	//0.7, 0.7, 0.3, [0.6,0.5,0.5,0.3]
	
	//rrand(0.2,0.35)
	//old bass vol 0.55
	//amp= [1.0,0.8,0.26,rrand(0.2,0.32)]++[0.5]++Array.fill(3,{rrand(0.2,0.3)})++[rrand(0.2,0.3),0.0];
		amp= [rrand(0.9,0.93),rrand(0.7,0.8),rrand(0.26,0.31),rrand(0.2,0.32)]++[0.5]++Array.fill(3,{rrand(0.2,0.3)})++[rrand(0.24,0.31),0.0];
		
		pan= [0.0,0.2.rand2,(1.neg**(2.rand))*rrand(0.2,0.7),(1.neg**(2.rand))*rrand(0.1,0.9)]++[0.1.rand2,0.5.rand2, rrand(-1,-0.5), rrand(0.5,1)]++[0.3.rand2,0.0];
		
		rev= [0.05,rrand(0.05,0.25),rrand(0.1,0.5),rrand(0.1,0.5)]++[0.05,rrand(0.3,1),rrand(0.3,1),rrand(0.3,1)]++[rrand(0.2,0.8),0.0];
		
		synthdef=(\InfnoPerc.dup(4)) ++ [[\InfnoBass1, \InfnoBass2, "InfnoBass2.alpha", "InfnoBass2.beta", \InfnoBass3,\InfnoBass4, \InfnoBassSampleCRef].wchoose([0.1,0.1,0.1,0.1,0.1,0.1,0.4])]++Array.fill(3,{[\InfnoLead++(rrand(1,8)),\InfnoSampleCRef].choose})  ++ [[\InfnoChord1,\InfnoChord2, \InfnoChord3, \InfnoChord4, "InfnoChord4.alpha", "InfnoChord4.beta", \InfnoChord5, "InfnoChord5.alpha", "InfnoChord5.beta"].choose,nil];
		
		//to control alternative classes which share the same bus but use a different rendering method
		//leads all same
		if(0.3.coin,{synthdef[6]=synthdef[5]});
		if(0.3.coin,{synthdef[7]=synthdef[5]});
		
		altsynthdef= synthdef.copy;
		
		//mono bass
		altsynthdef[4] = [\InfnoMonoBass2, "InfnoMonoBass2.alpha", "InfnoMonoBass2.beta"].wchoose([0.3,0.3,0.4]); 
		
		3.do{|i|
		altsynthdef[5+i] = [\InfnoMonoLead1,\InfnoMonoSampleCRef].wchoose([0.5,0.5]); 
		};
		
		//\InfnoLead1, \InfnoLead2, \InfnoLead3
		//synthdef= Array.fill(infno.numtracks, nil)
		//will extend this code to allow more options
		
		bufnum= Array.fill(5,{arg i; infno.buffer[i].choose.bufnum})++Array.fill(3,{arg i; infno.buffer[5].choose.bufnum})++[0]; ////Array.fill(5,{0}); 
		
	}
	
	//
	setupBuses {|infno|
	//or make Bundle for each? 
	
	infno.s.sendBundle(infno.s.latency, ["/c_set"]++((infno.trackamp.collect{arg val,i; [val.index,amp[i]]}).flatten), ["/c_set"]++((infno.trackpan.collect{arg val,i; [val.index,pan[i]]}).flatten), ["/c_set"]++((infno.trackrev.collect{arg val,i; [val.index,rev[i]]}).flatten));
	
	
	/*
	infno.trackamp.do{arg val,i; val.set(amp[i])}; 
	infno.trackpan.do{arg val,i; val.set(pan[i])}; 
	infno.trackrev.do{arg val,i; val.set(rev[i])}; 	
	*/
	
	}
	
	
	/*
	*interpolate {|other|
	
	}
*/

}