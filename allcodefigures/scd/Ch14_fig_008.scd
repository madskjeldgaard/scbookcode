   Figure 14.8


// one channel input:
FreeVerb.ar(in, mix, room, damp, mul, add)
// 2 channel input and output:
FreeVerb2.ar(in, in2, mix, room, damp, mul, add)

// stereo reverb
#left, right = GVerb.ar(in, roomsize, revtime, damping, inputbw,
 spread, drylevel, earlyreflevel, taillevel, maxroomsize, mul, add)



