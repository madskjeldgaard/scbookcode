Complementary_Grundgestalt {

	var <a, <b, <c, <perm;
	
	*new {arg transposition = 76;
		^super.new.initGrundgestalt(transposition);
	}
	
	initGrundgestalt {arg transposition;
		a = PitchClass.new(transposition);
		b = PitchClass.new(a.keynum + 1);
		c = PitchClass.new(b.keynum + 1);

		perm = Dictionary.new;

		perm.put(\012, PitchCollection.new([a, b, c]));
		perm.put(\120, PitchCollection.new([b, c, a]));
		perm.put(\201, PitchCollection.new([c, a, b]));

		perm.put(\210, PitchCollection.new([c, b, a]));
		perm.put(\102, PitchCollection.new([b, a, c]));
		perm.put(\021, PitchCollection.new([a, c, b]));
	}
	
	at {arg key;
		^perm.at(key);
	}
	
	arrayAt {arg key;
		^this.at(key).pitchCollection;
	}

}
Complementary_Instruments {

	classvar <protoNotes;
	
	*initClass {
		protoNotes = CtkProtoNotes.new(
			SynthDef.new(\NRT_shaper,
				{arg outbus = 0, inbus = 0, bufnum = 0, amp = 1, offSet = 0;
					Out.ar(outbus, 
						Shaper.ar(bufnum, In.ar(inbus, 1), amp, offSet));
				}
			),
			SynthDef.new(\NRT_sinosc,
				{arg outbus = 0, freq = 622.542, phase = 0, amp = 0, offSet = 0;
					Out.ar(outbus, 
						SinOsc.ar(freq, phase, amp, offSet)
					);
				}
			)
		);
	}
}
Complementary_Controllers {

	classvar <protoNotes;
	
	*initClass {
		protoNotes = CtkProtoNotes.new(
			SynthDef.new(\NRT_envgen,
				{arg outbus = 0, gate = 1, levelScale = 1, 
					levelBias = 0, timeScale = 1, doneAction = 0;
					Out.kr(outbus, 
						(((EnvGen.kr(Control.names([\env]).kr(Env.newClear(128)),
							gate, timeScale: timeScale, doneAction: doneAction)
						) * levelScale) + levelBias)
					);
				}
			),
			SynthDef.new(\NRT_cntlProduct,
				{arg outbus = 0, left = 1, right = 1;
					Out.kr(outbus, (left * right));
				}
			)
		)
	}
}
//Given an array of PitchClasses and a constant duration
//  builds a frequency envelope and an amplitude envelope
//  for that phrase
Complementary_Intro_Segment {

	var atomicDur, attackDur, releaseDur, shortenBy, 
		attackLevel, sustainLevel, releaseLevel;
	var pitchColl, <frequencyEnv, <amplitudeEnv;

	*new {arg pitchClasses, atomicDur, attackDur = 0.1, releaseDur = 0.1, 
		shortenBy = 0.0, attackLevel = 0, sustainLevel = 1, releaseLevel = 0;
		^super.newCopyArgs(atomicDur, attackDur, releaseDur, shortenBy, 
			attackLevel, sustainLevel, releaseLevel).initCompIntroSegment(pitchClasses);
	}
	
	initCompIntroSegment {arg pcArray;
		pitchColl = PitchCollection.new(pcArray.unbubble);
		//N.B. Env's require that their levels Array contain one more element
		// than their times Array. Since we want an Env with the number
		// of times equal to the number of elements in our pitchColl,
		// we'll prepend a duplicate of the first pitch in our frequencyEnv
		frequencyEnv = Env.new(
			Array.with(pcArray.at(0).keynum.midicps) ++
			Array.fill(pcArray.size, {arg i; 
					pcArray.at(i).keynum.midicps;
			}),
			Array.fill(pcArray.size, {arg i; atomicDur}), \step
		);
		amplitudeEnv = Env.new(
			[attackLevel, sustainLevel, sustainLevel, releaseLevel], 
			[attackDur, this.sustainDur, releaseDur], 
			[-4, 1, 4]
		);
	}
	
	sustainDur {
		^((this.size * atomicDur) - (attackDur + releaseDur + shortenBy));
	}
	
	size {
		^pitchColl.pitchCollection.size;
	}

	duration {
		^this.size * atomicDur;
	}
}
//Parses an array of PitchClass objects into segments
Complementary_Intro_Data {

	var pitchClasses, <atomicDur, <segments;
	
	*new {arg pcArray, noteDur;
		^super.new.initCompIntroData(pcArray, noteDur);
	}
	
	initCompIntroData {arg pcArray, noteDur;
		pitchClasses = pcArray; // or use PitchCollection
		atomicDur = noteDur;
		segments = Array.newClear(0);
		this.processPitchCollIntoSegments;
	}

	//loop through the pitchClasses to see if there are any repeated notes
	// if there are, split pitchClasses into segments at those points
	processPitchCollIntoSegments {
		var subSet;
		subSet = Array.newClear(0);

		(pitchClasses.size).do({arg ndx;
			if((subSet.size == 0), {
				//if subSet is empty we're starting a new phrase
				subSet = subSet.add(pitchClasses.at(ndx));
			}, { 
				//subSet isn't empty so we may be continuing a phrase
				if(subSet.at(subSet.size - 1) == pitchClasses.at(ndx), {
					//we've found a repeated note, so end the current segment,
					// and start a new one
					segments = segments.add(Complementary_Intro_Segment(subSet, atomicDur));
					subSet = Array.with(pitchClasses.at(ndx));
				}, {
					//This pitchClass is a continuation of the sequence...
					subSet = subSet.add(pitchClasses.at(ndx));
				});
			});
		});
		//be sure to add the last subSet to segments
		segments = segments.add(Complementary_Intro_Segment(subSet, atomicDur));
	}
	
	size {
		var size = 0;
		segments.do({arg seg; size = size + seg.size;});
		^size;
	}
	
	duration {
		^this.size * atomicDur;
	}
}
//A single part for the introduction of the Complementary Quartet
//  requires a Complementary_Intro_Data and an amplitude envelope
Complementary_Intro_Part {

	var data, weights, server, group, cntlGroup, procGroup, srcGroup, globalAmp, chebyshev, 
		sine, cntlProduct, <score;

	*new {arg starttime = 0.0, introData, globalAmpEnv, addAction = 0, target = 1, server, weights;
		^super.new.initCompIntroPart(starttime, introData, globalAmpEnv, 
			addAction, target, server, weights);
	}
	
	//given a Complementary_Intro_Data and amplitude envelope, build a score
	// for one part of the Complementary Quartet
	initCompIntroPart {arg starttime, introData, globalAmpEnv, 
		addAction = 0, target = 1, server, weights;
		var localStart;

		data = introData;
		weights = weights ?? {[1, 0, 1, 1, 0, 1]};
		server = server ?? {Server.default};


		group = CtkGroup.new(starttime, data.duration,
			addAction: addAction, target: target, server: server);

		cntlGroup = CtkGroup.new(starttime, data.duration,
			addAction: \head, target: group, server: server);

		procGroup = CtkGroup.new(starttime, data.duration,
			addAction: \tail, target: group, server: server);

		srcGroup = CtkGroup.new(starttime, data.duration,
			addAction: \before, target: procGroup, server: server);


		chebyshev = Complementary_WaveShaper.new(starttime, data.duration, weights, 
				addAction: \head, target: procGroup, server: server);

		//create a source to feed to the chebyshev wave-shaper instrument...
		sine = Complementary_Instruments.protoNotes[\NRT_sinosc]
			.new(starttime, (data.duration), \head, srcGroup, server);
		sine.outbus_(chebyshev.inbus);

		//apply dynamics globally...
		globalAmp = Complementary_EnvGen.new(starttime, data.duration, globalAmpEnv,
			addAction: \head, target: cntlGroup, server: server);

		//Mix the global amplitude envelope with the local amplitude envelope
		// once the local env is built in the segments loop below
		cntlProduct = Complementary_ControlProduct.new(starttime, data.duration,
			right: globalAmp.outbus, addAction: \head, target: cntlGroup, server: server);

		score = CtkScore.new(chebyshev.waveTable, group, cntlGroup, procGroup, srcGroup, 
			globalAmp.note, cntlProduct.note, chebyshev.note, sine);
		
		//now loop through the segments in data and add an EnvGen each for 
		// sine.amp and sine.freq
		data.segments.size.do({arg ndx;
			var localAmpEnvGen, localFreqEnvGen, segment;

			segment = data.segments.at(ndx);

			//first we need a starttime (localStart) that is local to each
			// segment
			if((ndx == 0),
				{localStart = starttime;},
				{localStart = localStart + data.segments.at(ndx - 1).duration;}
			);

			localAmpEnvGen = Complementary_EnvGen.new(localStart, segment.amplitudeEnv.times.sum, 
				segment.amplitudeEnv, addAction: \head, target: cntlGroup, server: server);

			//map the local amplitude env to the global cntlProduct's left parameter
			// set the time for this mapping relative to cntlProduct's starttime
			cntlProduct.left_(localAmpEnvGen.outbus, localStart - cntlProduct.starttime);

			localFreqEnvGen = Complementary_EnvGen.new(localStart, segment.duration, 
				segment.frequencyEnv, addAction: \tail, target: cntlGroup, server: server);

			//map the results of the global cntlProduct to
			// this segemnt's amplitude, relative to sine's starttime
			sine.amp_(cntlProduct.outbus, localStart - sine.starttime);
			sine.freq_(localFreqEnvGen.outbus, localStart - sine.starttime);

			score.add(localAmpEnvGen.note, localFreqEnvGen.note;);
		});
	}
}
//Wrapper class for Complementary_Instruments.protoNotes[\NRT_shaper], CtkBuffer, and associated
// CtkAudio busses
Complementary_WaveShaper {

	var <note, <waveTable;

	*new {arg starttime = 0.0, duration, weights, inbus, outbus, addAction, target, server;
		^super.new.initCompWaveShaper(starttime, duration, weights, inbus, outbus, 
			addAction, target, server);
	}

	initCompWaveShaper {arg starttime, duration, weights, inbus, outbus, 
		addAction, target, server;
		weights = weights ?? {[1, 0, 1, 1, 0, 1]};

		waveTable = CtkBuffer.new(size: 512, server: server).load(sync: true);
		waveTable.cheby(0.0, 1, 1, 1, weights.unbubble);

		inbus  = inbus ?? {CtkAudio.new(numChans: 1, server: server)};
		outbus = outbus ?? {CtkAudio.new(bus: 0, numChans: 1, server: server)};

		//Process Instrument, apply wave shape distortion to an input
		note = Complementary_Instruments.protoNotes[\NRT_shaper].new(starttime, 
			duration, addAction, target, server)
			.bufnum_(waveTable.bufnum)
			.outbus_(outbus)
			.inbus_(inbus);
	}
	
	inbus {
		^note.inbus;
	}
	
	outbus {
		^note.outbus;
	}
	
	inbus_ {arg ctkAudio, time = 0.0;
		note.inbus_(ctkAudio, time);
	}

	outbus_ {arg ctkAudio, time = 0.0;
		note.outbus_(ctkAudio, time);
	}
}
//Wrapper class for Complementary_Controllers.protoNotes[\NRT_envgen], Env, and associated
// CtkControl
Complementary_EnvGen {

	var <note;
	
	*new {arg starttime = 0.0, duration, env, control, 
		addAction = 1, target = 0, server;
		^super.new.initCEG(starttime, duration, env, control, addAction, target, server);
	}

	initCEG {arg starttime = 0.0, duration, envelope, control, addAction = 1, target = 0, server;
		server = server ?? {Server.default};
		control = control ?? {CtkControl.new(1, env[0], starttime, server: server)};
		note = Complementary_Controllers.protoNotes[\NRT_envgen]
				.new(starttime, duration, addAction, target, server)
					.env_(envelope).outbus_(control);
	}
	
	env {
		^note.env;
	}
	
	env_ {arg newEnv, time = 0.0;
		note.env_(newEnv, time);
	}

	outbus {
		^note.outbus;
	}
	
	outbus_ {arg ctkCntl, time = 0.0;
		note.outbus_(ctkCntl);
	}
}
//Wrapper class for Complementary_Controllers.protoNotes[\NRT_cntlProduct], and associated
// CtkControls
Complementary_ControlProduct {

	var <note;
	
	*new {arg starttime = 0.0, duration, left, right, outbus,  
		addAction = 1, target = 0, server;
		^super.new.initCEG(starttime, duration, left, right, outbus, 
			addAction, target, server);
	}

	initCEG {arg starttime = 0.0, duration, left, right, outbus, 
		addAction = 1, target = 0, server;
		server = server ?? {Server.default};

		left = left ?? {CtkControl.new(1, 1, starttime, server: server)};
		right = right ?? {CtkControl.new(1, 1, starttime, server: server)};
		outbus = outbus ?? {CtkControl.new(1, 1, starttime, server: server)};

		note = Complementary_Controllers.protoNotes[\NRT_cntlProduct]
				.new(starttime, duration, addAction, target, server)
					.left_(left)
					.right_(right)
					.outbus_(outbus);
	}
	
	left {
		^note.left;
	}
	
	right {
		^note.right;
	}
	
	starttime {
		^note.starttime;
	}
	
	outbus {
		^note.outbus;
	}
	
	left_ {arg ctkCntl, time = 0.0;
		note.left_(ctkCntl, time);
	}
	
	right_ {arg ctkCntl, time = 0.0;
		note.right_(ctkCntl, time);
	}

	outbus_ {arg ctkCntl, time = 0.0;
		note.outbus_(ctkCntl, time);
	}
}
