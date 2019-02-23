Figure 18.1
/*
This example is adapted and extracted from the Non-Realtime Synthesis helpfile itself, accessible from the Main SuperCollider help page.
*/
(
var f, c, d;
// open a file for writing raw OSC data to
f = File("~/test.osc".standardizePath,"w");
// start a sine oscillator at 0.2 seconds.
c = [ 0.2, [\s_new, \default, 1001, 0, 0]];
// convert the bundle to raw OSC
d = c.asRawOSC;
f.write(d.size); // each bundle is preceded by a 32 bit size.
f.write(d); // write the bundle data.
f.close;
)


