/* IZ 2007-02-02 { SC3
pplot is not a Class but a method added to ArrayedCollection, Wavetable, Buffer, Function and SoundFile. Its purpose is to create plots of synth output in small windows mainly to serve as picture snapshots for inclusion in documentation, tutorials etc.

Examples (require the default Server to be running!):

{ SinOsc.ar(40, 0, 0.1) }.pplot(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot100(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot150(0.1);
// { SinOsc.ar(40, 0, 0.1) }.plot180b(0.1);
{ LFSaw.ar(400, 0.0, 0.1) }.plot180(0.1);
{ LFPulse.ar(40, 0.0, 0.5, 0.1) }.plot180(0.1);
{ Pulse.ar(40, 0.5, 0.1) }.plot180(0.1);
{ Pulse.ar(10, 0.5, 0.1) }.plot180(1.0);
{ Pulse.ar(100, 0.5, 0.1) }.plot500(0.1);
{ Pulse.ar(500, 0.5, 0.1) }.plot500(0.1);
{ LFSaw.ar(400, 0.0, 0.1) }.plot500(0.1);
{ WhiteNoise.ar(0.1) }.plot180(0.1);
{ PinkNoise.ar(0.1) }.plot180(0.1);
{ BrownNoise.ar(0.1) }.plot180(0.1);
{ GrayNoise.ar(0.1) }.plot180(0.1);
{ WhiteNoise.ar(0.1) }.plot500(0.01);
{ GrayNoise.ar(0.1) }.plot500(0.01);
{ GrayNoise.ar(0.1) }.plot500(0.005);
{ LFNoise0.ar(100, 0.1) }.plot180(0.1);
{ LFNoise1.ar(100, 0.1) }.plot180(0.1);
{ LFNoise2.ar(100, 0.1) }.plot180(0.1);
{ LFClipNoise.ar(400, 0.1) }.plot180(0.1);
{ LFDNoise3.ar(100, 0.1) }.plot180(0.1);
{ Saw.ar(300, 0.1) }.plot400(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot200(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot300(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot400(0.1);
{ SinOsc.ar([40,50], 0, 0.1) }.plot500(0.1);

({ var m;
	m = LFNoise0.ar(5, 20, 32);
	[m, 
	SinOsc.ar(m, 0, 0.1)
	] }.plot500(1)
)

} */
