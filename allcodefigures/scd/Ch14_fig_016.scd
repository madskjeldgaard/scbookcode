Figure 14.16


// no interpolation
*arBufN { arg sound = 0, bufnum, location, speakerSpec, speedOfSound = 334, ampType = 'ws'; 
 var numChannels, distArray;

// WFSPoint is a 3D representation of a point in cartesian space

 speakerSpec = speakerSpec ? [WFSPoint.new(-2.7, 1.8, 0), WFSPoint.new(2.7,1.8, 0)]; //default 2 speakers

 if( speakerSpec.class == WFSConfiguration )
  { speakerSpec = speakerSpec.allSpeakers; };

 numChannels = speakerSpec.size; // speakerSpec = Array of WFSPoint objects

 location = location ? WFSPoint.new(0,0,0); // the location of the sound source

 distArray = Array.fill(numChannels, { |i| speakerSpec.at(i).dist(location)}); // distance of the sound source to each speaker (r_0)

 cosPhiArray = Array.fill(numChannels, { |i| speakerSpec.at(i).cosphi(location)}); // cosine phi_0 of the sound source to each speaker (cos phi_0)
 ^BufDelayN.ar(bufnum, sound, 
  distArray / speedOfSound, // delay
  WFSPan.wfsAmp(distArray,cosPhiArray);
 }

*wfsAmp{ arg inDist, inCosPhi, refDist=4.0, minDist = 0.1;
 // refDist is the reference line distance
 // minDist is the minimal distance to the speaker array (to avoid explosion at /0)

 // avoid explosion:
 inDist = inDist.max( minDist );

 ^(ampFactor * ((refDist/(refDist + inDist)).sqrt)*(inCosPhi/(inDist.sqrt)))
}



