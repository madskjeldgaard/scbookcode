Figure 18.8
// melodic expander
(
var note, keys, durs, now, score, chunk, expander, rangemap;

// 
thisThread.randSeed_(123);

// a simple note player

note = CtkSynthDef(\NRT_dut, {arg key, amp, dur;
		Out.ar(0, SinOsc.ar(key.midicps, 0, XLine.kr(amp, 0.00001, dur)))
		});

// first, make a melody - these will be used as midikeynums (easier to alter later)

keys = [ 72, 72, 79, 79, 81, 81, 79, 77, 77, 76, 76, 74, 74, 72 ];

// a list of durations

durs = [0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.5, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.5];

// create a var to store 'now' in

now = 0.0;

// create a CtkScore with the above melody

score = CtkScore.new;

keys.do({arg thiskey, inc;
	var thisdur;
	thisdur = durs[inc];
	score.add(note.new(now, thisdur).key_(thiskey).amp_(0.2).dur_(thisdur));
	now = now + thisdur;
	});

// first, create a function that will return a chunk of the melody the duration of the chunk
// sets the starttimes of the notes to a base of 0.0

chunk = {arg offset = 0;
	var size, start, end, duration = 0, chunk, copies;
	// the size of the current melody - 1 (for array access)
	size = score.notes.size;
	// the beginning of the chunk can come from the beginning of the melody to the second to 
	// last note
	start = 0.rrand(size-1);
	end = start.rrand(size);
	chunk = score.notes[start..end].collect({arg anote; 
		var newnote;
		newnote = anote.copy(duration + offset);
		duration = duration + anote.duration;
		newnote;
		});
	[chunk, duration];
	};

// now, create a function that will add those chunks to the score, and will keep doing this
// until the score is at least the desired length. Then check the score size, and truncate to 
// desired size.

expander = {arg len;
	var curchunk, chunkdur, insert, inserttime, insertdur, cursize, newnotes;	cursize = score.notes.size;
	while({
		cursize < len
		}, {		
		insert = 0.rrand(cursize - 1);
		inserttime = score.notes[insert].starttime;
		insertdur = score.notes[insert].duration;
		#curchunk, chunkdur = chunk.value(inserttime + insertdur);
		score.notes[(insert+1)..(cursize-1)].do({arg me; 
			me.setStarttime(me.starttime + chunkdur)});
		score = score.add(curchunk);
		(score.notes.size > len).if({
			score.notes.do({arg me, i;
				(i > (len - 1)).if({score.notes.remove(me)});
				})
			});
		cursize = score.notes.size;
		});
	};

// rangemap will place the melodic material within a certain range. The user passes 
// in an envelope that will describe the center pitch in an octave range

rangemap = {arg center;
	score.notes.do({arg me;
		me.key_(me.key.mapIntoRange(12, center[me.starttime]));
		})
	};
		
// expand it to 100 notes
expander.value(100);

// describe a new range of pitches
rangemap.value(Env([60, 96], [20]));

// finally, play the CtkScore

score.play;
)

