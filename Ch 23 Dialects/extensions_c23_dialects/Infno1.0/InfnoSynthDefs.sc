//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//writes SynthDefs to disk if necessary

 + Infno {
 
 	//any SynthDefs to create? could come ready loaded in synthdefs directory
	*initClass {
	
	StartUp.add({
	
		
		SynthDef.writeOnce(\InfnoBass1, {
		 arg out=0,note=0,amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[0.25]),doneAction:2)*LPF.ar(LFSaw.ar((36+note).midicps, 0, amp*0.5),Line.kr(10000,1000,0.2))
			)
		
		
		});
		
		
		
		//modmult 2 also good
		SynthDef.writeOnce(\InfnoBass2, {
		 arg out=0,note=0,amp=1, modmult=1, index=2;
		var freq, modfreq;
		
		freq= (36+note).midicps;
		modfreq= freq*modmult;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[0.25]),doneAction:2)*LPF.ar(SinOsc.ar(SinOsc.ar(modfreq, 0, index*modfreq, freq), 0, amp),Line.kr(10000,1000,0.2))
			)
		
		
		}, variants: (alpha: [modmult: 2], beta: [modmult:0.5, index:3, amp:1.5]));
		
		
		SynthDef.writeOnce(\InfnoBass3, {
		 arg out=0,note=0,amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[0.5]),doneAction:2)*Mix(LFCub.ar((36+[0,0.05]+note).midicps, 0, amp))
			)
		});
		
		SynthDef.writeOnce(\InfnoBass4, {
		 arg out=0,note=0,amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[0.25]),doneAction:2)*
				
				amp * MoogFF.ar(Mix(LFSaw.ar(((note+36).midicps)*[0.99,0.995,1,1.01,1.02]))*0.2,Line.kr(10000,1000,0.25),Rand(0.1,3.4))
				
				//LFNoise2.kr(Rand(6,16),1.7,2)
				
			)
		});
		
		
		
		SynthDef.writeOnce(\InfnoBassSampleCRef, {
		 arg out=0,bufnum=0, note, trigger=1, startPos=0, loop=0, amp=1;
		 var length; //sr
		 var rate;
		 var env;
		 
		 //length= BufFrames.ir(bufnum);
		 //sr= SampleRate.ir;
		 
		 rate= (((60+note).midicps)/261.626);	
		 
		 //length= BufDur.ir(bufnum);
		 
			Out.ar(out,
				amp*EnvGen.ar(Env([0,1,1,0],[0.0001,0.2,0.1]),doneAction:2)*PlayBuf.ar(1,bufnum, BufRateScale.kr(bufnum)*rate, trigger, BufFrames.ir(bufnum)*startPos, loop)
			)
		
		
		});
		
		
		
		//could have gate and t_trigger via two separate envelopes but simpler this way
		//modmult 2 also good
		SynthDef.writeOnce(\InfnoMonoBass2, {
		 arg out=0,note=0,amp=1, modmult=1, index=2, t_trigger=1, lagtime=0.0;
		var freq, modfreq;
		
		freq= (36+note).midicps.lag(lagtime);
		modfreq= freq*modmult;
		
		//.lag(lagtime)
			Out.ar(out,
				EnvGen.ar(Env([0,1,1,0],[0.001,10,0.1]),t_trigger,doneAction:2)*LPF.ar(SinOsc.ar(SinOsc.ar(modfreq, 0, index*modfreq, freq), 0, amp),Line.kr(10000,1000,0.2))
			)
		
		}, variants: (alpha: [modmult: 2], beta: [modmult:0.5, index:3, amp:1.5]));
		
		
		
		
		
		SynthDef.writeOnce(\InfnoChord1, {
		 arg out=0,note, amp=1, sustain=0.75;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[sustain]),doneAction:2)*LFTri.ar(note.midicps, 0, amp)
			)
		
		
		});
		
		
		SynthDef.writeOnce(\InfnoChord2, {
		 arg out=0,note, amp=1, sustain=0.75;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],sustain*[0.1,0.9]),doneAction:2)*LPF.ar(HPF.ar(LFSaw.ar(note.midicps, 0, amp),Line.kr(100,5000,sustain)),1000,2)
			)
		
		
		});
		
		SynthDef.writeOnce(\InfnoChord3, {
		 arg out=0,note, amp=1, sustain=0.75;
		var freq;
		
		freq= note.midicps;
		
			Out.ar(out,
				EnvGen.ar(Env([1,0],[sustain]),doneAction:2)*BPF.ar(Mix(LFSaw.ar(freq+[-3,0,3], 0, 0.3*amp)),freq*2,Line.kr(0.7,0.3,sustain))
			)
		
		
		});
		
		
		SynthDef.writeOnce(\InfnoChord4, {
		 arg out=0,note, amp=1, sustain=0.75, form=1500, bw=800;
		var freq, sound, line;
		
		freq= note.midicps;
		line= Line.kr(1,0.5,sustain);
		
		sound= BPF.ar(Mix(Formant.ar(freq*[1,1.5], form*(1-(0.25*line)), bw*(line))*amp*0.5),freq*2*line,0.5);
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.005,sustain]),doneAction:2)*sound
			)
		
		}, variants: (alpha: [form: 3000], beta: [form:2500, bw:1000]));
		
		SynthDef.writeOnce(\InfnoChord5, {
		 arg out=0,note, amp=1, sustain=0.75, filtmult=2, modfreq=0.4;
		var freq, sound, line;
		
		freq= note.midicps;
		line= Line.kr(1,0.5,sustain);
		
		sound= BPF.ar(LFPulse.ar(freq,0,LFNoise1.kr(modfreq,0.4,0.6))*amp*0.5,freq*filtmult*line,0.5);
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.005,sustain]),doneAction:2)*sound
			)
		
		}, variants: (alpha: [filtmult:3, modfreq:3.1], beta: [filtmult:4, modfreq:34]));
			
			
		SynthDef.writeOnce(\InfnoKick1, {
		 arg out=0,startfreq=100, endfreq=70, amp=1;
		 var length; //sr
		 
			Out.ar(out,
				amp*EnvGen.ar(Env.perc(0.005,0.2,1),doneAction:2)*SinOsc.ar(Line.kr(startfreq, endfreq, 0.1)+LFNoise1.kr(900,30,30),pi*0.5,1)
			)
		});
		
		SynthDef.writeOnce(\InfnoKick2, {
		 arg out=0,startfreq=100, endfreq=70, amp=1;
		 var length; //sr
		 
			Out.ar(out,
				amp*SinOsc.ar(EnvGen.ar(Env.new([10000, 100, 1], [0.003, 0.1],
		[4, 0])), mul:EnvGen.ar(Env.perc(0,0.15), doneAction:2))
			)
		});	
			
		SynthDef.writeOnce(\InfnoPerc, {
		 arg out=0,bufnum=0, rate=1, trigger=1, startPos=0, loop=0, amp=1;
		 var length; //sr
		 
		 //length= BufFrames.ir(bufnum);
		 //sr= SampleRate.ir;
		 
		 length= BufDur.ir(bufnum);
		 
			Out.ar(out,
				amp*EnvGen.ar(Env([1,1],[length]),doneAction:2)*PlayBuf.ar(1,bufnum, BufRateScale.kr(bufnum)*rate, trigger, BufFrames.ir(bufnum)*startPos, loop)
			)
		
		
		});
		
		
		//chirp bell
		SynthDef.writeOnce(\InfnoPerc1, {
		 arg out=0,startfreq=500, endfreq=50, amp=1;
		 var length; //sr
		 
			Out.ar(out,
				amp*EnvGen.ar(Env.perc(0.005,0.1),doneAction:2)*SinOsc.ar(Line.kr(startfreq, endfreq, 0.1),pi*0.5,1)
			)
		
		});
		
		SynthDef.writeOnce(\InfnoLead1, {
		 arg out=0,note=60, amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.01,0.25],[-4,5]),doneAction:2)*(RLPF.ar(Pulse.ar(note.midicps, LFNoise1.kr(Rand(12,15.3),0.2,0.5), amp).distort,LFNoise2.kr(Rand(5,8.7),2000,2400), 0.1))
			)
		
		});
		
		
		SynthDef.writeOnce(\InfnoLead2, {
		 arg out=0,note=60, amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.01,0.125],[-4,5]),doneAction:2)*(RLPF.ar(Blip.ar(note.midicps,10,amp).distort,LFNoise2.kr(Rand(15,18.7),500,2400), 0.1))
			)
		
		});
		
		SynthDef.writeOnce(\InfnoLead3, {
		 arg out=0,note=60, amp=1;
		var freq;
		
		freq= note.midicps;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.01,0.125],[-4,5]),doneAction:2)*(LPF.ar(Formlet.ar(Blip.ar(freq,10,amp).distort,freq, 0.05,0.5).clip2(0.8),Line.kr(10000,500,0.05)))
			)
		
		});
		
		SynthDef.writeOnce(\InfnoLead4, {
		 arg out=0,note=60, amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.02,0.25]),doneAction:2)*(LFPar.ar((note).midicps, 0, amp).distort*2)
			)
		
		
		});
		
		SynthDef.writeOnce(\InfnoLead5, {
		 arg out=0,note=60, amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.02,0.25]),doneAction:2)*(LFPar.ar((note).midicps, 0, amp).clip2(0.3)*3)
			)
		
		
		});
		
		SynthDef.writeOnce(\InfnoLead6, {
		 arg out=0,note=60, amp=1;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.02,0.25]),doneAction:2)*(LPF.ar(LFSaw.ar((note).midicps, 0, amp).clip2(0.1)*8,Line.kr(10000,1000,0.15)))
			)
		
		});
		
		SynthDef.writeOnce(\InfnoLead7, {
		 arg out=0,note=60, amp=1;
		var freq;
		
		freq= (note).midicps;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.02,0.25]),doneAction:2)*(LPF.ar(Mix(LFSaw.ar(freq+[-2,0,2], 0, amp*0.4)),Line.kr(10000,1000,0.15)))
			)
		
		});
		
		SynthDef.writeOnce(\InfnoLead8, {
		 arg out=0,note=60, amp=1;
		var freq;
		
		freq= (note).midicps;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,0],[0.02,0.25]),doneAction:2)*(HPF.ar(Mix(SyncSaw.ar(freq+[-2,0,2], freq*Line.kr(0.1,1,0.25), 3*amp)),Line.kr(1000,10000,0.25)))
			)
		
		});
		
		//if add octaves, some samples sound great, especially the bells! resample the bell source? 
		SynthDef.writeOnce(\InfnoSampleCRef, {
		 arg out=0,bufnum=0, note, trigger=1, startPos=0, loop=0, amp=1;
		 var length; //sr
		 var rate;
		 var env;
		 
		 //length= BufFrames.ir(bufnum);
		 //sr= SampleRate.ir;
		 
		 rate= (((note).midicps)/261.626);	
		 
		 //length= BufDur.ir(bufnum);
		 
			Out.ar(out,
				amp*EnvGen.ar(Env([0,1,1,0],[0.0001,0.2,0.1]),doneAction:2)*PlayBuf.ar(1,bufnum, BufRateScale.kr(bufnum)*rate, trigger, BufFrames.ir(bufnum)*startPos, loop)
			)
		
		
		});
		
		
		SynthDef.writeOnce(\InfnoMonoLead1, {
		 arg out=0,note=60, amp=1, t_trigger=1, lagtime=0.0;
		
			Out.ar(out,
				EnvGen.ar(Env([0,1,1,0,0],[0.001,1,1,10]),t_trigger, doneAction:2)*(RLPF.ar(Pulse.ar(note.midicps.lag(lagtime), LFNoise1.kr(Rand(12,15.3),0.2,0.5), amp).distort,LFNoise2.kr(Rand(5,8.7),2000,2400), 0.1))
			)
		
		});
		
		//default to loop!
		SynthDef.writeOnce(\InfnoMonoSampleCRef, {
		 arg out=0,bufnum=0, note, startPos=0, loop=1, amp=1,t_trigger=1, lagtime=0.0;
		 var length; //sr
		 var rate;
		 var env;
		 
		 //length= BufFrames.ir(bufnum);
		 //sr= SampleRate.ir;
		 
		 rate= (((note).midicps)/261.626).lag(lagtime);	
		 
		 //length= BufDur.ir(bufnum);
		 
			Out.ar(out,
				amp*EnvGen.ar(Env([0,1,1,0,0],[0.0001,1,0.1,10]),t_trigger, doneAction:2)*PlayBuf.ar(1,bufnum, BufRateScale.kr(bufnum)*rate, t_trigger, BufFrames.ir(bufnum)*startPos, loop)
			)
		
		
		});


	}); 
	
	}
 

 }