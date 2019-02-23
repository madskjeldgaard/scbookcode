Figure 15.3
b = Buffer.alloc(s,1024,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

//d=Buffer.read(s,"sounds/a11wlk01.wav");


(
x= {
	var in, fft, array;
	
	//in= PlayBuf.ar(1,d.bufnum,BufRateScale.kr(d.bufnum),1,0,1);
	
	in = SoundIn.ar(0); 
	
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

w = Window("Thirteen MFCC coefficients", Rect(200,400,300,300));

ms = MultiSliderView(w, Rect(10,10,260,280));

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



