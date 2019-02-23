Figure 14.5

// single tap delay lines
DelayN.ar(in, maxdelaytime, delaytime, mul, add)
DelayL.ar(in, maxdelaytime, delaytime, mul, add)
DelayC.ar(in, maxdelaytime, delaytime, mul, add)

// allpass filters:
AllpassN.ar(in, maxdelaytime, delaytime, decaytime, mul, add)
AllpassL.ar(in, maxdelaytime, delaytime, decaytime, mul, add)
AllpassC.ar(in, maxdelaytime, delaytime, decaytime, mul, add)

// comb filters (delaylines with feedback):
CombN.ar(in, maxdelaytime, delaytime, decaytime, mul, add)
CombL.ar(in, maxdelaytime, delaytime, decaytime, mul, add)
CombC.ar(in, maxdelaytime, delaytime, decaytime, mul, add)

// buffer versions:

BufDelayN.ar(buf, in, delaytime, mul, add)
BufDelayL.ar(buf, in, delaytime, mul, add)
BufDelayC.ar(buf, in, delaytime, mul, add)

BufAllpassN.ar(buf, in, delaytime, decaytime, mul, add)
BufAllpassL.ar(buf, in, delaytime, decaytime, mul, add)
BufAllpassC.ar(buf, in, delaytime, decaytime, mul, add)

BufCombN.ar(buf, in, delaytime, decaytime, mul, add)
BufCombL.ar(buf, in, delaytime, decaytime, mul, add)
BufCombC.ar(buf, in, delaytime, decaytime, mul, add)


// special delay lines utilising PlayBuf:
Tap.ar(bufnum, numChannels, delaytime)
PingPong.ar(bufnum, inputArray, delayTime, feedback, rotate)


