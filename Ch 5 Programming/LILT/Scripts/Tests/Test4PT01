/* IZ 2007-01-31 { SC3

} */


var script, max, chromaUpdateAction, chroma1 = 1, chroma2 = 2, chroma3 = 3;

script = ~script;
max = NetAddr("127.0.0.1", 10000);

chromaUpdateAction = {
	if (script.isRunning) {
		Post << "THIS IS THE CHROMA ACTION SENDING NOW: ";
		max.sendMsg(\chromakey, (chroma1 + chroma2 + chroma3).postln);
	}
};


~controlSpecs = [
[\chroma1, 0, 1, \linear, 0, 0.6, { | argChroma |
			~chroma1 = chroma1 = argChroma;
			chromaUpdateAction.value;
}],
[\chroma2, 0, 1, \linear, 0, 0.6, { | argChroma |
			~chroma2 = chroma2 = argChroma;
			chromaUpdateAction.value;
}],
[\chroma3, 0, 1, \linear, 0, 0.6, { | argChroma |
			~chroma3 = chroma3 = argChroma;
			chromaUpdateAction.value;
}]
];

                                               
~start =  {
       "I'm ON".postln;        
      [~chroma1, ~chroma2, ~chroma3].postln;
};


~stop =  {

"I'm OFF".postln;
};

