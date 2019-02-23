

 Figure 7.3

p = ProxySpace.push;
~x = { SinOsc.kr(4) }; 
~y = { SinOsc.kr(13) };
~z = { SinOsc.ar(~x * ~y % 0.4 * 500 + 600) * 0.2 };
~z.play;

	
// now ~x and ~y can be replaced
~x = { SinOsc.kr(0.4) }; 
~y = { SinOsc.kr(1.3) }; 

p.clear(2).pop; // release environment (2 sec fadeout)


