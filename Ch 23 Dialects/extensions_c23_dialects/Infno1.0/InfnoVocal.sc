//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//integration with infno main mixer may require more care, separate reverb units but want all through final limiter! 
//use Out.ar(0,Pan.ar(vocalbusindex, 0.0)) in Mixer

InfnoVocal {
	var <bus, <group; 
	//just one synth with all source, fx and output for now
	var <synth; //, fxsynth, finalsynth;
	var w, vol, reverb, floor, compress, vocode; //sliders for controls
	
	*new {arg group, bus;
		
		^super.new.init(group, bus);
	}
	
	show {
			
		w.front;
		
	}
	
	free {
		w.close;
		group.free; 
		bus.free;
	
	}
	
	init{arg grp, bs;
		
		var temp;
		var color1, color2; 
		
		group= grp ?? {Group.new};
		bus= bs ?? {Bus.audio(Server.default,1)}; //mono on bus 0; but wouldn't be stereo out eventually...
		
		
		//bus= Bus.audio(Server.default,1); //defaults to default server and 1 channel
		
		synth= Synth.head(group, \infnovocal, [\out, bus.index]);
	
		//make GUI
		
		w= SCWindow("Vocals", Rect(800,150,200,120));
		
		color1= Color.yellow(0.97,0.9);
		color2= Color.red(0.8,0.5);
		//SCView.dumpInterface
		w.view.backColor_(color1);
		
		vol= SCSlider(w,Rect(0,0,150,25)); 
		temp=SCStaticText(w, Rect(150,0,50,25)).string_(" [VOL] ");
		
		vol.knobColor_(color2).background_(color1);
		temp.background_(color1).stringColor_(color2);
		
		vol.value_(0.0);
		vol.action_({|slid| synth.set(\amp, slid.value)});
		
		reverb= SCSlider(w,Rect(0,30,150,25)); 
		temp=SCStaticText(w, Rect(150,30,50,25)).string_(" [REV] ");
		
		reverb.knobColor_(color2).background_(color1);
		temp.background_(color1).stringColor_(color2);
		
		reverb.value_(0.333); //defaults to 1
		reverb.action_({|slid| synth.set(\rev, (slid.value)*1.5)});
		
		floor= SCSlider(w,Rect(0,60,150,25)); 
		temp=SCStaticText(w, Rect(150,60,50,25)).string_(" [nFL] ");
		
		floor.knobColor_(color2).background_(color1);
		temp.background_(color1).stringColor_(color2);
		
		floor.value_(0.1); //defaulting to 0.01 effectively
		floor.action_({|slid| synth.set(\threshold, (slid.value)*0.1)});
		
		compress= SCSlider(w,Rect(0,90,150,25)); 
		temp=SCStaticText(w, Rect(150,90,50,25)).string_(" [comp] ");
		
		compress.knobColor_(color2).background_(color1);
		temp.background_(color1).stringColor_(color2);
		
		compress.value_(0.5); //defaults to 2 
		compress.action_({|slid| synth.set(\compression, slid.value*2+1)});
	
	}
	
	
	//SynthDef
	*initClass {
	
	StartUp.add({
		
		SynthDef.writeOnce(\infnovocal,{arg out=0,amp=0.0,threshold=0.01, pan=0.0, compression=2, rev=0.5;  
		var input,inputAmp,gate;
		var temp, reverb, signal;
		
		input = AudioIn.ar(1)*amp; 
		inputAmp = Amplitude.kr(input);
		//threshhold = 0.02;	// noise gating threshold
		gate = Lag.kr(inputAmp > threshold, 0.01);
		signal= (input * gate);
		
		temp= compression*0.7+0.3; //gives max possible volume before overload
		
		signal= CompanderD.ar(temp*signal,0.3,1.0, compression); //kneee at -10dB
		
		//add vocoder control later
		
		reverb= FreeVerb.ar(signal, 0.33, rev, 0.5);
		//reverb=signal; 
		
		Out.ar(out,reverb);//Pan2.ar(reverb, pan)
		
		});
		
		});

	
	
	}

}