
//Figure 1: Immediate machine listening example using Pitch and Amplitude UGens. The original detected pitch appears in your left ear and an octave up in the right.
(
x={
	var in, amp, freq, hasFreq, out;
	in = SoundIn.ar(0);;
	amp = Amplitude.ar(in);
	# freq, hasFreq = Pitch.kr(in);
	LFTri.ar(freq*[1,2]) * amp;
}.play
)

x.free;



//Figure 2: Loudness 

b = Buffer.alloc(s,1024,1);   //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

(  //analyse loudness and poll result
x={
	var in, fft, loudness;
	
	in = SoundIn.ar(0);; 
	
	fft = FFT(b.bufnum, in);
	
	loudness = Loudness.kr(fft); 
	
	loudness.poll(20); //poll for testing 20 times per second
	
	Out.ar(0,Pan2.ar(in)); 
}.play
)

x.free;
b.free;




//Figure 3: MFCC 

b = Buffer.alloc(s,1024,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

//d=Buffer.read(s,"sounds/a11wlk01.wav");


(
x= {
	var in, fft, array;
	
	//in= PlayBuf.ar(1,d.bufnum,BufRateScale.kr(d.bufnum),1,0,1);
	
	in = SoundIn.ar(0);; 
	
	fft = FFT(b.bufnum, in);
	
	array = MFCC.kr(fft); 
	
	array.size.postln; 
	
	Out.kr(0,array); 
	
	Out.ar(0,Pan2.ar(in)); 
}.play
)


c= Bus.new('control', 0, 13); 

//poll coefficients
c.getn(13,{arg val; {val.plot;}.defer}); 


//Continuous graphical display of MFCC values; free routine before closing window

(
var ms; 

w = GUI.window.new("Thirteen MFCC coefficients", Rect(200,400,300,300));

ms = GUI.multiSliderView.new(w, Rect(10,10,260,280));

ms.value_(Array.fill(13,0.0));
ms.valueThumbSize_(20.0);
ms.indexThumbSize_(20.0);
ms.gap_(0);

w.front;

r = {
	
	inf.do{
		
		c.getn(13,{arg val; {ms.value_(val*0.9)}.defer}); 
		
		0.04.wait; //25 frames per second
	};

}.fork;

)


//tidy up
(
r.stop;
b.free;
c.free;
x.free;
w.close;
)




//Figure 4: Onsets

// Prepare the buffer
b = Buffer.alloc(s, 512);

(
x = {
	var sig, chain, onsets, pips, trigger;
	
	sig = SoundIn.ar(0);; 
	
	chain = FFT(b, sig);
	
	// - move the mouse left/right to change the threshold:
	onsets = Onsets.kr(chain, MouseX.kr(0,1), \complex);
	
	trigger= SendTrig.kr(onsets);
	
	pips = SinOsc.ar(880, 0, EnvGen.kr(Env.perc(0.001, 0.1, 0.2), onsets));
	
	Out.ar(0, ((sig * 0.1) + pips).dup);
}.play;
)

(
// register to receive message
a= OSCresponder(s.addr,'/tr',{ arg time,responder,msg;
	[time,responder,msg].postln;
}).add;
)

a.remove; //Free the OSCresponder
x.free; // Free the synth
b.free; // Free the buffer




//Figure 5: BeatTrack


b = Buffer.alloc(s,1024,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

//track audio in (try clapping a beat or beatboxing, but allow up to 6 seconds for tracking to begin); events will be spawned at quarter, eighth and sixteenth note rates
(
x= SynthDef(\help_beattrack2,{	
	var trackb,trackh,trackq,tempo;
	var source;
	var bsound,hsound,qsound;
	
	source = SoundIn.ar(0);;
	
	#trackb,trackh,trackq,tempo = BeatTrack.kr(FFT(b.bufnum, source));
	
	bsound = Pan2.ar(LPF.ar(WhiteNoise.ar*(Decay.kr(trackb,0.05)),1000),0.0);
	
	hsound = Pan2.ar(BPF.ar(WhiteNoise.ar*(Decay.kr(trackh,0.05)),3000,0.66),-0.5);
	
	qsound = Pan2.ar(HPF.ar(WhiteNoise.ar*(Decay.kr(trackq,0.05)),5000),0.5);
	
	Out.ar(0, bsound+hsound+qsound);
}).play;
)


x.free;
b.free; // Free the buffer




//Figure 6: KeyTrack

//straight forward test file with few transients; training set in e minor from MIREX2006 
//You will need to substitute your own soundfile to load here
d=Buffer.read(s,"/Users/nickcollins/Desktop/ML/training_wav/78.wav")


b = Buffer.alloc(s,4096,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,8192,1); //for sampling rates 88200 and 96000

(
x= {
	var in, fft; 
	var key;
	
	in = PlayBuf.ar(1,d.bufnum,BufRateScale.kr(d.bufnum),1,0,1);
	
	fft = FFT(b.bufnum, in);
	
	key = KeyTrack.kr(fft, 2.0, 0.5);
	
	key.poll; //write out detected key
	
	Out.ar(0,Pan2.ar(in)); 
}.play
)

x.free;
b.free;



//Figure 7: Simple melodic transcription
//see melodytranscription.rtf


//Figure 8: OnlineMIDI

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



