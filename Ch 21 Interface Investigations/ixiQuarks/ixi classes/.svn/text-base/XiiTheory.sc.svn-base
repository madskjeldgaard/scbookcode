
XiiTheory {

	var <>xiigui;
	var <>win, params;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiTheory(server, channels, setting);
		}

	initXiiTheory {
		var win, bounds, chord, chords, chordnames;
		var scale, scales, scalenames;
		var chordmenu, scalemenu, play;
		var fString, fundamental=60;
		var setting, point, k, scaleOrChord, scaleChordString;
		
		bounds = Rect(120, 5, 800, 222);
		
		point = if(setting.isNil, {Point(310, 250)}, {setting[1]});
		
		win = SCWindow("Theory", 
						Rect(point.x, point.y, bounds.width+20, bounds.height+10), resizable:false);
		
		k = MIDIKeyboard.new(win, Rect(10, 60, 790, 160), 4, 48);
		
		k.keyDownAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midiname);
								scaleChordString.string_((fundamental+chord).midiname.asString);
								k.showScale(chord, fundamental, Color.new255(103, 148, 103));
								chord.postln;
						});
		k.keyTrackAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midiname);
								scaleChordString.string_((fundamental+chord).midiname.asString);
								k.showScale(chord, fundamental, Color.new255(103, 148, 103));
						});
		k.keyUpAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midiname);
								scaleChordString.string_((fundamental+chord).midiname.asString);
								k.showScale(chord, fundamental, Color.new255(103, 148, 103));
						});
		
		GUI.staticText.new(win, Rect(300, 10, 100, 20)).string_("Fundamental :")
						.font_(Font("Helvetica", 9));
		fString = GUI.staticText.new(win, Rect(370, 10, 50, 20))
					.string_(fundamental.asString++"  :  "++fundamental.midiname)
					.font_(Font("Helvetica", 9));

		scaleOrChord = GUI.staticText.new(win, Rect(300, 30, 100, 20)).string_("Chord :")
						.font_(Font("Helvetica", 9));
		scaleChordString = GUI.staticText.new(win, Rect(340, 30, 150, 20))
						.string_(fundamental.asString++"  :  "++fundamental.midiname)
						.font_(Font("Helvetica", 9));
		
		chords = XiiMusicTheory.chords;
		scales = XiiMusicTheory.scales;
		
		chordnames = [];
		chords.do({arg item; chordnames = chordnames.add(item[0])});
		chord = chords[0][1];
		
		scalenames = [];
		scales.do({arg item; scalenames = scalenames.add(item[0])});
		scale = scales[0][1];
		
		chordmenu = GUI.popUpMenu.new(win,Rect(500,10,140,16))
				.font_(Font("Helvetica", 9))
				.items_(chordnames)
				.background_(Color.white)
				.action_({arg item;
					chord = chords[item.value][1];
					scaleOrChord.string_("Chord :");
					scaleChordString.string_((fundamental+chord).midiname.asString);
					k.showScale(chord, fundamental, Color.new255(103, 148, 103));
					//play.focus;
				});
		
		scalemenu = GUI.popUpMenu.new(win,Rect(500,31,140,16))
				.font_(Font("Helvetica", 9))
				.items_(scalenames)
				.background_(Color.white)
				.action_({arg item;
					chord = scales[item.value][1];
					scaleOrChord.string_("Scale :");
					scaleChordString.string_((fundamental+chord).midiname.asString);
					k.showScale(chord, fundamental, Color.new255(103, 148, 103));
					//play.focus;
				});
		
		play = GUI.button.new(win,Rect(680,31,60,20))
			.font_(Font("Helvetica", 9))
			.states_([["play", Color.black, Color.clear]])
			.action_({
				chord.postln;
				Task({
					chord.do({arg note;
						note = note + fundamental;
						Synth(\midikeyboardsine, [\freq, note.midicps]);
						0.4.wait;
					});
					0.6.wait;
					chord.do({arg note;
						note = note + fundamental;
						Synth(\midikeyboardsine, [\freq, note.midicps, \amp, 0.1]);
					});
				}).start;
			});
			win.front;
		
}
}
