Figure 14.3


// 2 channel panners:
 Pan2.ar( in, pos, level );
 LinPan2.ar( in, pos, level );
 Balance2.ar( left, right, pos, level );
 Rotate2.ar( x, y, pos );

// 4-channel panner:
 Pan4.ar( in, xpos, ypos, level );

// N-channel panner:
 PanAz.ar( numChans, in, pos, level, width, orientation );

// spread M channels over a stereo field:
 Splay.ar( inArray, spread, level, center, levelComp );

// spread M channels over N channels:
 SplayAz.ar( numChans, inArray, spread, level, width, center, orientation, levelComp );



