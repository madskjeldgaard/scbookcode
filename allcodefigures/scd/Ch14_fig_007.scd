Figure 14.7


// convolving two signals with each other:
Convolution.ar( in, kernel, framesize, mul, add )

// convolving one signal with a buffer:
Convolution2.ar( in, kernel, trigger, framesize, mul, add )
// as above with linear interpolation:
Convolution2L.ar( in, kernel, trigger, framesize, crossfade, mul, add )
// as above, with two buffers:
StereoConvolution2L.ar( in, kernelL, kernelR, trigger, framesize, crossfade, mul, add )

// time based convolution (highly inefficient for audio rate)
Convolution3.ar( in, kernel, trigger, framesize, mul, add )
Convolution3.kr( in, kernel, trigger, framesize, mul, add )

// partitioned convolution
PartConv.ar(in, fftsize, irbufnum, mul, add)

