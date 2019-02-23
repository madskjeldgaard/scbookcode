Figure 15.7
//Example uses internal server to demonstrate shared busses

(
s=Server.internal; 
Server.default=s; 

s.doWhenBooted({

b = Buffer.alloc(s, 512);

//this SynthDef will make no sound, just analyses input
SynthDef(\pitchandonsets,
{
	var in, amp, freqdata, chain, onsets, trigger;
	
	in = SoundIn.ar(0);
	amp = RunningSum.rms(in, 64); //get rms amplitude value per control block
	freqdata = Pitch.kr(in); 
	
	//allow synchronous polling, Internal Server only
	SharedOut.kr(0,freqdata);	
	SharedOut.kr(2,amp);
	
	chain = FFT(b, in);
	
	// - move the mouse left/right to change the threshold:
	onsets = Onsets.kr(chain, MouseX.kr(0,1), \complex);
	
	trigger = SendTrig.kr(onsets);

}).send(s); 
});

)



(
var freqlist=List(), amplist=List(); 
var notelist= List(), numnotes=10; //will hold the last 10 notes 
var lasttime, started=false; 
var maxlength=0.5, maxkperiods, waittime;

maxkperiods = ((maxlength*(s.sampleRate))/(s.options.blockSize)).asInteger;
waittime = (s.options.blockSize)/(s.sampleRate);


// register to receive message
a= OSCresponder(s.addr,'/tr',{ arg time,responder,msg;
	var newnote;
		
	if(started,{	
		
	//finalise previous note as [starttime, ioi= inter onset interval, dur, medianpitch, maxamp]	
	newnote = 	[lasttime, time-lasttime, (time-lasttime).min(maxlength), if(freqlist.notEmpty, {freqlist.median.cpsmidi},{nil}),amplist.maxItem.ampdb];
	
	newnote.postln;
		
	notelist.addFirst(newnote);
	
	//remove oldest note if over size
	if(notelist.size>numnotes,{notelist.pop}); 
	
	},{started = true;}); 
	
	//reset lists for collection
	freqlist = List();
	amplist = List(); 
	lasttime = time;
		
}).add;

x= Synth(\pitchandonsets); 

//poll values
{	
	
	inf.do{  
		var freq, hasfreq, rmsamp;
		 
		freq = s.getSharedControl(0);
		hasfreq = s.getSharedControl(1);
		rmsamp = s.getSharedControl(2);
		
		//don't allow notes of longer than 500 control periods or so
		if((hasfreq>0.5) and: (amplist.size<maxkperiods), {freqlist.add(freq)});
		
		if(amplist.size<maxkperiods, {amplist.add(rmsamp)});
		 
		//poll every control period, intensive
		(waittime).wait;
	}; 
	
}.fork;

)



(
a.remove; //Free the OSCresponder
x.free; // Free the synth
b.free; // Free the buffer
)




