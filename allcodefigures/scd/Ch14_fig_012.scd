Figure 14.12


// 3D encoding:
PanB.ar(in, azimuth, elevation, gain)
// 2D encoding:
PanB2.kr(in, azimuth, gain)
// 2D encoding of a stereo signal:
BiPanB2.kr(inA, inB, azimuth, gain)

// decoding (2D):
DecodeB2.kr(numChans, w, x, y, orientation)

// rotating (in the horizontal plane):
Rotate2.kr(x, y, pos)


/** From AmbisonicUGens in sc3-plugins: **/

// encoding (3D):
BFEncode1.ar(in, azimuth, elevation, rho, gain, wComp)
BFEncode2.ar(in, point_x, point_y, elevation, gain, wComp)

// encoding of a stereo signal (3D)
BFEncodeSter.ar(l, r, azimuth, width, elevation, rho, gain, wComp)

// decoding (3D):
BFDecode1.ar(w, x, y, z, azimuth, elevation, wComp, mul, add)

// manipulating (3D):
BFManipulate.ar(w, x, y, z, rotate, tilt, tumble)
// rotate is rotation around the z-axis, tilt around the x-axis, and tumble around the y-axis


