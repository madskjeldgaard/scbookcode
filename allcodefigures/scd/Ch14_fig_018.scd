Figure 14.18

(
b = Buffer.read(s, "sounds/a11wlk01.wav");
SynthDef("grain",{ arg i_out=0, i_sampbufnum, dur = 0.05, 
 pointer, offset = 0.005, amp = 1.0, loop = 1;
 var thisStart, thisDur, grain;
 thisStart = pointer + IRand(0, offset); // adds random time offset
 grain = EnvGen.ar(Env.sine, 1.0, amp, 0.0, dur, 2) 
  * PlayBuf.ar(1,i_sampbufnum, BufRateScale.ir(i_sampbufnum),
   1, thisStart,loop);
 OffsetOut.ar(i_out,grain); // use OffsetOut for precise sub-block timing
}).add;
)

(
x = {
var numGrains = 32; // approximate number of simultaneous grains
var numChannels = 2; // adjust for your setup
var dur = 0.05, durRand = 0.05, thisDur;
var start, now;
var numGrainsRecip;
numGrainsRecip = numGrains.reciprocal; // save some divides by converting to reciprocal

start = Main.elapsedTime;
loop({ 
 now = Main.elapsedTime - start;
 thisDur = dur + durRand.rand;
 s.bind({Synth("grain", [i_out: numChannels.rand, i_sampbufnum: b, dur: thisDur, 
  pointer: now * b.sampleRate, amp: numGrainsRecip]);
 }); // send as a bundle for precise sub-block timing
 (thisDur * numGrainsRecip).wait;
})
}.fork;
)

x.stop; b.free;


