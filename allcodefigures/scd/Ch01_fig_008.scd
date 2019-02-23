1.8 Playback Buffers

[~houston.bufnum, ~houston.numChannels, ~houston.path, ~houston.numFrames];

[~chooston.bufnum, ~chooston.numChannels, ~chooston.path, ~chooston.numFrames];

(  // phasing
{
	var rate, trigger, frames;
	frames = ~houston.numFrames; // or use ~chooston.numFrames

	rate = [1, 1.01];
	trigger = Impulse.kr(rate);
	PlayBuf.ar(1, ~houston, 1, trigger, frames * Line.kr(0, 1, 60)) * 
	EnvGen.kr(Env.linen(0.01, 0.96, 0.01), trigger) * rate;
}.play;
)


