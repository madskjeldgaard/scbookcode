/* IZ 2007-05-14 { SC3

Tone: 
Represents a Tone in the Tone-Net of octaves - fifths - thirds - sevenths
Calculates the frequency corresponding to the Tone instance. 

Chord: Represents a list of Tones.


Tone(0, 0, 0).play;

} */

VogelTone {
	classvar <>reference_frequency = 440;
	classvar <>gradus_weights = #[1, 3, 5, 7];
	var <octaves = 0, <fifths = 0, <thirds = 0, <sevenths = 0;
	var <freq;
	*new { | octaves = 0, fifths = 0, thirds = 0, sevenths = 0 |
		^this.newCopyArgs(octaves, fifths, thirds, sevenths).init;
	}
	init {
		freq = reference_frequency * product([2, 3, 5, 7] ** this.vector);
	}
	vector { ^[octaves, fifths, thirds, sevenths] }
	gs { | argTone |	// GRADUS SUAVITATIS (VOGEL VERSION)
		argTone = argTone ?? { Tone.new };
		// return the gradus suavitatis of this and argTone
		^sum((this.vector + argTone.vector).abs * gradus_weights);
	}
	play { | dur = 1.0, amp = 0.1 |
		Synth("smoothsine", [\freq, freq, \dur, dur, \amp, amp]);
	}
}

Chord : List {
	var <reference_tone, <upper_reference_tone, <lower_reference_tone;
	var <center;
	var <consonance_grade;
	*new { | ... vectors |
		^super.newUsing(vectors collect: VogelTone(*_)).init;
	}
	init {
		var vectors, flopped_vectors, upper_gs, lower_gs;
		vectors = this.vectors;
		flopped_vectors = vectors.flop;
		lower_reference_tone = flopped_vectors collect: _.smallest;
		lower_gs = VogelTone(*sum(vectors -.1 lower_reference_tone)).gs / this.size;
		upper_reference_tone = flopped_vectors collect: _.largest;
		upper_gs = VogelTone(*sum(vectors -.1 upper_reference_tone)).gs / this.size;
		if (lower_gs <= upper_gs) {
			consonance_grade = lower_gs;
			reference_tone = lower_reference_tone;
		}{
			consonance_grade = upper_gs;
			reference_tone = upper_reference_tone;
		}
	}
	vectors { ^this collect: _.vector }
	play { | dur = 1.0, amp = 0.1 |
		array do: _.play(dur, amp);
	}
}


