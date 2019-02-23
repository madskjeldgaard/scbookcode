Figure 15.6
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



