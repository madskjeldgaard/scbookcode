Figure 15.8
//do this first: 
MIDIIn.connect; 	// init for one port midi interface


//now:
m = OnlineMIDI();

m.analyse(3,1.0); //3 seconds window, step size of 1.0 seconds

m.data //poll current data

m.status = true; //prints analysis data as it goes
m.status= false;

//use analysis data to formulate responses

(
SynthDef(\beep2,{arg freq=440,amp=0.1, pan=0.0, dur=0.1; 
var source; 

source= SinOsc.ar(freq*[1,1.007],0,amp*0.5);

Out.ar(0,Pan2.ar(Mix(source)*Line.kr(1,0,dur, doneAction:2),pan))}).add;
)

//to echo each note you play on a MIDI keyboard with a sound; your SynthDef must have freq and amp arguments, and deal with duration and freeing the Synth itself.  
(
m.playinput= true; 
m.inputsynthdef= \beep2; 
)

//set a function that gets called after each window is analysed, to schedule events over the next second 
(
m.response = {|analysis|  
	var number;
	number= analysis.density; 
	//number= max(0,(10-(analysis.density))); //inverting number of notes playing
	
	if(analysis.iois.notEmpty, {
		{
		
		number.do{
		
		Synth(\beep2, [\freq, analysis.pitches.choose.midicps, \amp, 0.2*(rrand(analysis.volumemin, analysis.volumemax))]);
		
		analysis.iois.choose.wait; //could last longer than the next second, but still fun! 
		
		}; 
		
		}.fork;
	}); 
	
};
)


m.response= nil; //stop



