Figure 15.2
b = Buffer.alloc(s,1024,1);   //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000
	
(  //analyse loudness and poll result
x={
	var in, fft, loudness;
	
	in = SoundIn.ar(0); 
	
	fft = FFT(b.bufnum, in);
	
	loudness = Loudness.kr(fft); 
	
	loudness.poll(20); //poll for testing 20 times per second
	
	Out.ar(0,Pan2.ar(in)); 
}.play
)

x.free;
b.free;

