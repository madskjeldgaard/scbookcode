//redFrik - released under gnu gpl license

//--changes 080807
//added standardizePath here and there
//fixed standardizePath in importSoundFile

//--changes 071216
//replaced bittest method with standard &s.  faster and also avoids a bug on older sc versions
//added jukebox gui example file
//fixed parsing bug if number of envelope points in the xm file was >12
//--first release 071214

//--todo:
// save xm
// ripPattern (how to translate into say a pbind?)
// .next method that step through slots
// 16bit adpcm compressed i don't know.  need to find a file that uses it
// stripped i don't know either.  need to find a stripped xm file
// volume effects porta, vefxVolumeSlideUp, vefxVolumeSlideDown


//--known issues:
// some effects (which?) placed in the first row in the first pattern might behave a bit strange.
// not sure about the E4x effect.  which vibrato types to use.

//----------------------------------------------------------------------------------------

//--a class that can parse & play back .xm files (fasttracker2)
RedXM {
	var
		<isPaused,
		<isPrepared,								//flag true if server booted, samples loaded and synths sent
		
		<server,									//server in use
		<outBusses,								//array with bus indexes for output
		<>muteTracks= #[],							//array with track indexes to mute
		
		clk,										//tempoclock. passed in or created internally
		clockPassedIn,							//flag
		<>index= 0,								//index of current pattern
		<>row= 0,									//index of current slot, in current pattern
		<>tick= 0,								//counter modulo speed
		speed,									//integer global speed
		customSpeed,								//integer that when not nil overrides speed
		<bpm,
		<>root= 77.785,							//main tuning for frequency calculations
		<>volume= 64,								//global volume (0-64)
		<numTracks,								//integer
		
		<>patterns,								//array with RedXMPattern objects
		<>instruments,							//array with RedXMInstrument objects
		<tracks,									//array with RedXMTrack objects
		<>order,									//array with which patterns to play in which order
		
		<magic,									//string
		<name,									//string
		<tracker,									//string
		<version,									//string
		<restart,									//index of where the song should restart after reaching the end
		<frequencyTable,							//'amiga' or 'linear'
		
		task;									//runs the main loop
	
	*read {|path|
		^super.new.read(path)
	}
	
	//--check if file exists & then parse
	read {|path|
		path= path.standardizePath;
		if(File.exists(path), {
			this.prRead(path);
			isPrepared= false;
		}, {("RedXM: file"+path+"not found").error});
	}
	
	numInstruments {^instruments.size}
	numPatterns {^patterns.size}
	soloTrack {|index| muteTracks= (0..numTracks-1).reject{|x| x==index}}
	muteTrack {|index| muteTracks= [index]}
	unmuteAll {muteTracks= []}
	
	//--find all sample data, load to server and return an array of buffers
	ripSamples {|server|
		^instruments.select{|x| x.notEmpty}.collect{|x|
			x.samples.select{|y| y.notEmpty}.collect{|y|
				y.ripSample(server);
			}
		}.flat;
	}
	
	//--load sample data into buffers & send required synthdef
	prepareForPlay {|group, action|
		if(isPrepared.isNil, {
			"RedXM: read a file first".error;
			^this
		});
		if(isPrepared.not, {
			group= group ? Server.default.defaultGroup;
			server= group.server;					//get server from group
			Routine.run{
				var halt= Condition.new;
				server.bootSync(halt);				//wait for the server to boot
				SynthDef(\redXMplayer, {|out= 0, bufnum, t_trig= 0, offset= 0, loop= 0, loopStart= 0, loopEnd= 0, rate= 0, vol= 0, pan= 0, interp= 2|
					var len= BufFrames.kr(bufnum)-1;
					var i= RedPhasor.ar(t_trig, rate, offset, len, loop, loopStart, loopEnd);
					var z= BufRd.ar(1, bufnum, i, 0, interp);
					Out.ar(out, Pan2.ar(z, pan, vol));
				}, #[\ir, 0, \tr, 0, 0, 0, 0, 0, 0.01, 0.01, 0]).store;
				instruments.do{|x| x.load(server)};
				server.sync(halt);					//wait for all buffers to load
				isPrepared= true;
				action.value;
			}
		}, {"RedXM: already prepared - just play".warn});
	}
	
	//--playback methods
	play {|out= 0, clock, quant= 0, loop= false|
		if(isPrepared.not, {
			"RedXM: must .prepareForPlay first".error;
			^this;
		});
		if(out.isSequenceableCollection, {
			outBusses= out;
		}, {
			outBusses= out.dup(numTracks);
		});
		if(clock.isKindOf(TempoClock), {				//if clock passed in as an argument
			clk= clock;
			CmdPeriod.doOnce({this.stop});
			clockPassedIn= true;
		}, {										//else create a new one
			clk= TempoClock(bpm/60);
			clk.permanent= true;
			clockPassedIn= false;
		});
		isPaused= false;
		customSpeed= nil;							//following gets reset when .play
		index= 0;
		row= 0;
		tick= 0;
		task= Task{
			this.prPlay(index);					//this is what you override for custom playroutine
		};
		task.play(clk, true, quant);
	}
	stop {
		if(this.isPlaying, {tracks.do{|x| x.syn.free}});
		task.stop;
	}
	pause {
		isPaused= true;
		task.pause;
	}
	resume {
		isPaused= false;
		task.resume;
	}
	save {|path|
		this.prSave(path.standardizePath);
	}
	free {
		if(isPrepared==true, {
			isPrepared= false;
			instruments.do{|x| x.free};
			if(this.isPlaying, {this.stop});
			if(clockPassedIn==false, {clk.stop});
		});
	}
	isPlaying {
		^task.isPlaying;
	}
	clock {
		^clk;
	}
	speed_ {|val|
		customSpeed= val;
	}
	speed {
		^customSpeed ? speed;
	}
	revertSpeed {
		customSpeed= nil;
	}
	
	//----------------------------------------------------------------------------------------
	
	//--read and parse - private
	
	//  this method reads the file from disk.  not to mess with.
	//  general settings, patterns, instruments and sample data.
	prRead {|path|
		var songLen, headLen, tempSkip, tempNumPatterns, tempNumInstruments;
		var file= File(path, "r");
		
		//--read xm header
		magic= {file.getChar}.dup(17).join;			//id text string
		if(magic!="Extended Module: ", {				//check if this is a proper .xm file
			"RedXM: strange magic string - might not work properly".warn;
		});
		name= {file.getChar}.dup(20).select{|x| x.ascii!=0}.join;
		tempSkip= file.getInt8&0xFF;
		if(tempSkip!=0x1A, {
			if(tempSkip==0, {
				"RedXM: stripped XM format not yet supported (1)".error;
				file.close;
				^this;
			}, {
				"RedXM: not a valid XM file".error;
				file.close;
				^this;
			});
		});
		tracker= {file.getChar}.dup(20).select{|x| x!=$ }.join;
		version= [file.getInt8&0xFF, file.getInt8&0xFF];
		if(version[0]<1 and:{version[1]<4}, {
			if(version==[0, 0], {
				"RedXM: stripped XM format not yet supported (2)".error;
				file.close;
				^this;
			}, {
				"RedXM: too old version (less than 1.4) - will not work properly".warn;
			});
		});
		version= ""++version[0]++"."++version[1];		//convert to string
		headLen= file.getInt32LE;					//total length of following header data
		songLen= file.getInt16LE;					//song length (1-256)
		restart= file.getInt16LE;					//song restart position
		numTracks= file.getInt16LE;					//number of mixing channels
		tempNumPatterns= file.getInt16LE;			//number of patterns
		tempNumInstruments= file.getInt16LE;			//number of instruments (0-128)
		frequencyTable= #[\amiga, \linear][file.getInt16LE&0x01];//only bit0 used.  freq.table
		speed= file.getInt16LE.max(1);				//default tempo (number of ticks)
		bpm= file.getInt16LE;						//default bpm
		order= {file.getInt8&0xFF}.dup(256).keep(songLen);//which order to play patterns (form)
		if(restart>=order.size, {restart= 0});
		
		//--read patterns header
		patterns= {
			var pheadLen= file.getInt32LE;			//pattern header length.  usually 9
			var type= file.getInt8;					//packing type. always 0 -unused
			var numRow= file.getInt16LE;			//number of rows in pattern (1-256)
			var patLen= file.getInt16LE;			//pattern length
			var pos= file.pos, slots= [];
			if(patLen==0, {						//if this pattern not stored
				slots= nil.dup(64);				//fill up with empty space
			}, {
				
				//--read pattern data
				while({file.pos-pos<patLen}, {
					slots= slots.add({
						var note, inst, volu, efxn, efxp;
						var byte= file.getInt8&0xFF;
						if(byte&0x80!=0, {			//check for slot compression
							if(byte&0x01!=0, {note= file.getInt8&0xFF});
							if(byte&0x02!=0, {inst= file.getInt8&0xFF});
							if(byte&0x04!=0, {volu= file.getInt8&0xFF});
							if(byte&0x08!=0, {efxn= file.getInt8&0xFF});
							if(byte&0x10!=0, {efxp= file.getInt8&0xFF});
						}, {						//slot not compressed
							note= byte;			//(0-96, 97)
							inst= file.getInt8&0xFF;//(1-128)
							volu= file.getInt8&0xFF;//(0-64, 255)
							efxn= file.getInt8&0xFF;//(0-26)
							efxp= file.getInt8&0xFF;//(0-255)
						});
						RedXMSlot(note, inst, volu, efxn ? 0, efxp ? 0);
					}.dup(numTracks));
				});
			
			});
			if(slots.size!=numRow, {
				"RedXM: number of slots missmatch".warn;
			});
			RedXMPattern(slots);					//one for each unique index in order
		}.dup(tempNumPatterns);
		
		//--read instruments header
		instruments= {
			var iLen= file.getInt32LE;				//instrument header size
			var iNam= {file.getChar}.dup(22)			//instrument name
				.select{|x| x.ascii!=0}.join;
			var iTyp= file.getInt8;					//instrument type -unused
			var nSmp= file.getInt16LE;				//number of samples
			var	smpLen, smpKey, volEnv, panEnv, volNum, panNum,
				volSus, volSta, volEnd, panSus, panSta, panEnd,
				volTyp, panTyp, vibTyp, vibSwe, vibDep, vibRat;
			var autoVib, fadeOut, samples= [];
			
			//--read extra instrument header
			if(nSmp>0, {
				smpLen= file.getInt32LE;			//sample header size
				smpKey= {file.getInt8&0xFF}.dup(96);	//sample number for all notes
				volEnv= {file.getInt16LE}.dup(24);	//points for volume envelope
				panEnv= {file.getInt16LE}.dup(24);	//points for panning envelope
				volNum= file.getInt8&0xFF;			//number of volume points
				panNum= file.getInt8&0xFF;			//number of panning points
				volSus= file.getInt8&0xFF;			//volume sustain point
				volSta= file.getInt8&0xFF;			//volume loop start point
				volEnd= file.getInt8&0xFF;			//volume loop end point
				panSus= file.getInt8&0xFF;			//panning sustain point
				panSta= file.getInt8&0xFF;			//panning loop start point
				panEnd= file.getInt8&0xFF;			//panning loop end point
				volTyp= file.getInt8&0xFF;			//volume type.  bit0: on, bit1: sustain, bit2: loop
				panTyp= file.getInt8&0xFF;			//panning type.  bit0: on, bit1, sustain, bit2: loop
				vibTyp= file.getInt8&0xFF;			//vibrato type
				vibSwe= file.getInt8&0xFF;			//vibrato sweep
				vibDep= file.getInt8&0xFF;			//vibrato depth
				vibRat= file.getInt8&0xFF;			//vibrato speed
				fadeOut= file.getInt16LE;			//volume fadeout time
				file.seek(22, 1);					//skip -unused
				
				volEnv= if(volNum==0, {nil}, {RedXMEnvelope(volEnv, volNum.min(12), volSus, volSta, volEnd, volTyp)});
				panEnv= if(panNum==0, {nil}, {RedXMEnvelope(panEnv, panNum.min(12), panSus, panSta, panEnd, panTyp)});
				autoVib= RedXMVibrato(vibTyp, vibSwe, vibDep, vibRat);
				
				//--read sample header
				samples= {
					var len= file.getInt32LE;		//sample length
					var loopSta= file.getInt32LE;	//sample loop start
					var loopLen= file.getInt32LE;	//sample loop length
					var vol= file.getInt8&0xFF;		//volume
					var tun= file.getInt8;			//finetune (signed)
					var typ= file.getInt8&0xFF;		//type of loop (bits0-1) and 8/16bits format (bit4)
					var pan= file.getInt8&0xFF;		//panning
					var not= file.getInt8;			//relative note number (signed)
					var frm= file.getInt8&0xFF;		//format.  0x00= regular, 0xAD= 4bit adpcm
					var nam= {file.getChar}.dup(22)	//sample name
						.select{|x| x.ascii!=0}.join;
					if(typ&0x10==0x10, {			//check bit4 if 16bit format
						len= len.div(2);
						loopSta= loopSta.div(2);
						loopLen= loopLen.div(2);
					});
					RedXMSample(
						len,
						loopSta,
						loopLen,
						vol,
						tun,
						typ&0x03,
						if(typ&0x10==0x10, 16, 8),
						pan,
						not,
						frm,
						nam
					);
				}.dup(nSmp);
				
				//--read sample data
				samples.do{|sample|
					var old= 0, table;
					if(sample.bitRate==16, {		//test if 16bit format
						if(sample.format==0xAD, {"RedXM: looks like adpcm compressed".warn});
						sample.data= {			//convert from signed delta to float
							old= (old+file.getInt16LE).wrap(-32768, 32767);
							old/32768;
						}.dup(sample.length);
					}, {							//8bit format
						sample.data= {			//convert from signed delta to float
							old= (old+file.getInt8).wrap(-128, 127);
							old*256/32768;
						}.dup(sample.length);
					});
				};
				
			}, {
				file.seek(iLen-29, 1);				//skip empty instruments
			});
			
			RedXMInstrument(iNam, samples, volEnv, panEnv, autoVib, fadeOut, smpKey);
		}.dup(tempNumInstruments);
		
		//--replace all instruments indices (1-128) with objects
		patterns.do{|pat|
			pat.slots.do{|tracks|
				tracks.do{|slot|
					if(slot.instrument.notNil, {
						slot.instrument= instruments[slot.instrument-1];
					});
				};
			};
		};
		
		//--finish reading
		if(file.pos!=file.length, {
			"RedXM: filepos mismatch at the end - no good".warn;
		});
		file.close;
	}
	
	
	//--write the thing back to disk
	prSave {|path|
		"save xm yet todo!!!".postln;
	}

	
	//----------------------------------------------------------------------------------------

	//--playback - to override

	//  a 'correct' playback routine with all effects implemented.
	//  override this in subclasses or copy&paste into a function.
	//  the reason it is so long is that it should be fairly easy
	//  to rewrite as a single function. see helpfile for example.
	prPlay {
		var pattern, slots;
		var delayPattern= 0;
		tracks= {|i|								//one per track (ie channel or column)
			var track= RedXMTrack.new;
			track.syn= Synth(\redXMplayer, [\out, outBusses.wrapAt(i)]);
		}.dup(numTracks);
		
		//--main loop
		while({index<(order.size-1)}, {				//loop ticks
			
			server.makeBundle(nil, {
				
				//--first tick
				if(tick==0, {						//slots update every 0 tick, rest sub efxs
					pattern= patterns[order[index]];
					if(pattern.isNil, {("RedXM: tried to access pattern index:"+index).error});
					slots= pattern.slots[row];		//find current slots - one for each track
					slots.do{|slot, i|				//for each channel or column
						var track= tracks[i];
						track.trig= false;
						if(slot.note.notNil, {
							if(slot.note<97, {
								track.offset= 0;
								track.previousNote= track.note;
								track.note= slot.note;
								track.trig= true;
							}, {
								track.released= true;
							});
						});
						if(slot.instrument.notNil and:{slot.instrument.notEmpty}, {
							track.reset;
							track.ins= slot.instrument;
							track.vol= slot.instrument.vol;
							track.pan= slot.instrument.pan;
							track.released= false;
						});
						
						//--volume column effects - non tick based
						if(slot.volume.notNil, {
							case
								{slot.volume==0} {
								}
								{slot.volume<=0x50} {// - set volume (01-40)
									track.vol= slot.volume-0x10;
								}
								{slot.volume<=0x6F} {// - volume slide down
									//"track.vefxVolumeSlideDown need to test this".warn;
									track.vefxVolumeSlideDown= slot.volume&0xF;
								}
								{slot.volume<=0x7F} {// - volume slide up
									//"track.vefxVolumeSlideUp need to test this".warn;
									track.vefxVolumeSlideUp= slot.volume&0xF;
								}
								{slot.volume<=0x8F} {// - fine volume slide down (Dx)
									track.vefxFineVolumeSlideDown= slot.volume&0xF/7.5;
								}
								{slot.volume<=0x9F} {// - fine volume slide up (Ux)
									track.vefxFineVolumeSlideUp= slot.volume&0xF/7.5;
								}
								{slot.volume<=0xAF} {// - set vibrato speed (Sx)
									if(slot.volume&0xF!=0, {
										track.efxVibratoSpeed= slot.volume&0xF;
									});
								}
								{slot.volume<=0xBF} {// - vibrato (Vx)
									if(slot.volume&0xF!=0, {
										track.efxVibratoDepth= slot.volume&0xF/7.5;
									});
								}
								{slot.volume<=0xCF} {// - set panning (Px)
									track.pan= slot.volume&0xF*17;
								}
								{slot.volume<=0xDF} {// - panning slide left (Lx)
									track.vefxPanningSlideLeft= slot.volume&0xF;
								}
								{slot.volume<=0xEF} {// - panning slide right (Rx)
									track.vefxPanningSlideRight= slot.volume&0xF;
								}
								{slot.volume<=0xFF} {// - tone porta
									//"voleffect tone porta need to test this".warn;
								};
						});
						
						//--non tick based effects
						switch(slot.efxindex,
							0x01, {				//1xx - porta up
								if(slot.efxparam!=0, {
									track.efxPortaUp= slot.efxparam/0x10;
								});
							},
							0x02, {				//2xx - porta down
								if(slot.efxparam!=0, {
									track.efxPortaDown= slot.efxparam/0x10;
								});
							},
							0x03, {				//3xx - tone porta
								if(slot.efxparam!=0, {
									track.efxTonePortaSpeed= slot.efxparam/0x10;
								});
								if(slot.note.notNil and:{slot.note<97}, {
									track.efxTonePortaNote= slot.note;
									track.note= track.previousNote;
								});
								track.trig= false;
							},
							0x04, {				//4xy - vibrato
								if(slot.efxparam!=0, {
									track.efxVibratoSpeed= slot.efxparam.rightShift(4);
									track.efxVibratoDepth= slot.efxparam&0xF/7.5;
								});
							},
							0x05, {				//5xx - tone porta + volume slide
								if(slot.efxparam!=0, {
									track.efxVolumeSlideUp= slot.efxparam.rightShift(4);
									track.efxVolumeSlideDown= slot.efxparam&0xF;
									track.efxTonePortaSpeed= slot.efxparam/0x10;
								});
								if(slot.note.notNil and:{slot.note<97}, {
									track.efxTonePortaNote= slot.note;
									track.note= track.previousNote;
								});
								track.trig= false;
							},
							0x06, {				//6xx - vibrato + volume slide
								if(slot.efxparam!=0, {
									track.efxVibratoSpeed= slot.efxparam.rightShift(4);
									track.efxVibratoDepth= slot.efxparam&0xF/7.5;
									track.efxVolumeSlideUp= slot.efxparam.rightShift(4);
									track.efxVolumeSlideDown= slot.efxparam&0xF;
								});
							},
							0x07, {				//7xy - tremolo
								if(slot.efxparam!=0, {
									track.efxTremoloSpeed= slot.efxparam.rightShift(4);
									track.efxTremoloDepth= slot.efxparam&0xF/32;
								});
							},
							0x08, {				//8xx - set panning
								track.pan= slot.efxparam;
							},
							0x09, {				//9xx - sample offset
								if(slot.efxparam!=0, {
									track.efxSampleOffset= slot.efxparam*256;
								});
								track.offset= track.efxSampleOffset;
							},
							0x0A, {				//Axy - volume slide
								if(slot.efxparam!=0, {
									track.efxVolumeSlideUp= slot.efxparam.rightShift(4);
									track.efxVolumeSlideDown= slot.efxparam&0xF;
								});
							},
							0x0B, {				//Bxx - position jump
								index= slot.efxparam;
								row= -1;
							},
							0x0C, {				//Cxx - set volume
								track.vol= slot.efxparam;
							},
							0x0D, {				//Dxx - pattern break
								index= index+1;
								row= slot.efxparam.rightShift(4)*10+(slot.efxparam&0x0F)-1;
							},
							0x0E, {
								switch(slot.efxparam.rightShift(4),
									0x1, {		//E1x - fine porta up
										if(slot.efxparam&0x0F!=0, {
											track.efxFinePortaUp= slot.efxparam&0x0F/0x10;
										});
										track.note= track.note+track.efxFinePortaUp;
									},
									0x2, {		//E2x - fine porta down
										if(slot.efxparam&0x0F!=0, {
											track.efxFinePortaDown= slot.efxparam&0x0F/0x10;
										});
										track.note= track.note-track.efxFinePortaDown;
									},
									0x3, {		//E3x - set gliss control
										track.efxGlissando= slot.efxparam&0xF;
									},
									0x4, {		//E4x - set vibrato control
										track.efxVibratoWave= slot.efxparam&0xF;
									},
									0x5, {		//E5x - set finetune
										if(slot.note.notNil, {
											track.note= track.note+(slot.efxparam&0xF/7.5-0.5);
										});
									},
									0x6, {		//E6x - set loop begin/loop
										if(slot.efxparam&0xF==0, {
											track.efxSetLoop= row;
										}, {
											if(track.efxSetLoopIndex<=0, {
												track.efxSetLoopIndex= slot.efxparam&0xF;
											}, {
												track.efxSetLoopIndex= track.efxSetLoopIndex-1;
											});
											if(track.efxSetLoopIndex>0, {
												row= track.efxSetLoop-1;
											});
											
										});
									},
									0x7, {		//E7x - set tremolo control
										track.efxTremoloWave= slot.efxparam&0xF;
									},
									0xA, {		//EAx - fine volume slide up
										if(slot.efxparam&0xF!=0, {
											track.efxFineVolumeSlideUp= slot.efxparam&0xF;
										});
										track.vol= (track.vol+track.efxFineVolumeSlideUp).min(64);
									},
									0xB, {		//EBx - fine volume slide down
										if(slot.efxparam&0xF!=0, {
											track.efxFineVolumeSlideDown= slot.efxparam&0xF;
										});
										track.vol= (track.vol-track.efxFineVolumeSlideDown).max(0);
									},
									0xC, {		//ECx - note cut
										if(slot.efxparam&0xF==0, {
											track.trig= false;
											track.released= true;
										});
									},
									0xD, {		//EDx - note delay
										if(slot.efxparam&0xF>0, {
											track.trig= false;
										});
									},
									0xE, {		//EEx - pattern delay
										delayPattern= slot.efxparam&0xF;
									}
								);
							},
							0x0F, {				//Fxx - set tempo/bpm
								if(slot.efxparam!=0, {
									if(slot.efxparam<0x20, {
										speed= slot.efxparam;
									}, {
										clk.tempo_(slot.efxparam/60);
									});
								});
							},
							0x10, {				//Gxx - set global volume
								volume= slot.efxparam;
							},
							0x11, {				//Hxx - global volume slide
								if(slot.efxparam!=0, {
									track.efxGlobalVolumeSlide= slot.efxparam;
								});
							},
							0x14, {				//Kxx - key off
								if(slot.efxparam&0xF==0, {
									track.trig= false;
									track.released= true;
								});
							},
							0x15, {				//Lxx - set envelope position
								track.volEnvIndex= slot.efxparam;
								track.panEnvIndex= slot.efxparam;
							},
							0x19, {				//Pxx - panning slide
								if(slot.efxparam!=0, {
									track.efxPanningSlide= slot.efxparam;
								});
							},
							0x1B, {				//Rxx - multi retrig note
								if(slot.efxparam!=0, {
									track.efxMultiRetrigNote= slot.efxparam;
								});
							},
							0x1D, {				//Txx - tremor
								if(slot.efxparam!=0, {
									track.efxTremor= slot.efxparam;
									track.efxTremorVol= track.vol;
								});
							},
							0x21, {
								switch(slot.efxparam.rightShift(4),
									0x1, {		//X1xx - extra fine porta up
										if(slot.efxparam&0xF!=0, {
											track.efxExtraFinePortaUp= slot.efxparam&0xF/64;
										});
										track.note= track.note+track.efxExtraFinePortaUp;
									},
									0x2, {		//X2xx - extra fine porta down
										if(slot.efxparam&0xF!=0, {
											track.efxExtraFinePortaDown= slot.efxparam&0xF/64;
										});
										track.note= track.note-track.efxExtraFinePortaDown;
									}
								);
							},
							{
								track.efxVibrato= 0;
								track.efxTremolo= 0;
							}
						);
						if(track.trig, {
							track.syn.set(
								\offset, track.offset,
								\t_trig, 1
							);
						});
					};
					
					//--position updates - row and pattern
					if(row==(pattern.numRows-1), {	//check if time to jump in order
						index= index+1;			//go to next section in the form/order
						row= 0;
					}, {
						row= row+1;				//jump to next row
					});
					if((customSpeed ? speed)>1, {tick= 1});
				
				}, {
					
					//--tick based effects
					slots.do{|slot, i|				//for each channel or column
						var track= tracks[i];
						
						//--volume column effects - tick based
						if(slot.volume.notNil, {
							case
								//{slot.volume==0} {
								//}
								{slot.volume<=0x50} {
								}
								{slot.volume<=0x6F} {// - volume slide down
									track.vol= (track.vol-track.vefxVolumeSlideDown).max(0);//??need to test
								}
								{slot.volume<=0x7F} {// - volume slide up
									track.vol= (track.vol+track.vefxVolumeSlideUp).min(64);//??need to test
								}
								{slot.volume<=0x8F} {// - fine volume slide down (Dx)
									track.vol= (track.vol-track.vefxFineVolumeSlideDown).max(0);
								}
								{slot.volume<=0x9F} {// - fine volume slide up (Ux)
									track.vol= (track.vol+track.vefxFineVolumeSlideUp).min(64);
								}
								{slot.volume<=0xAF} {// - set vibrato speed
								}
								{slot.volume<=0xBF} {// - vibrato (Vx)
									switch(track.efxVibratoWave,
										0, {track.efxVibrato= sin(track.efxVibratoIndex%64/64*2pi)},
										1, {track.efxVibrato= (track.efxVibratoIndex%64/64).round*2-1},
										2, {track.efxVibrato= (track.efxVibratoIndex%64/64)*2-1},
										3, {track.efxVibrato= (1-(track.efxVibratoIndex%64/64))*2-1}
									);
									track.efxVibrato= track.efxVibrato*track.efxVibratoDepth;
									track.efxVibratoIndex= track.efxVibratoIndex+track.efxVibratoSpeed;
								}
								{slot.volume<=0xCF} {// - set panning (Px)
								}
								{slot.volume<=0xDF} {// - panning slide left (Lx)
									track.pan= (track.pan-track.vefxPanningSlideLeft).max(0);
								}
								{slot.volume<=0xEF} {// - panning slide right (Rx)
									track.pan= (track.pan+track.vefxPanningSlideRight).min(255);
								}
								{slot.volume<=0xFF} {// - tone porta//??need to test
								};
							});
						
						switch(slot.efxindex,
							0x00, {				//0xx - arpeggio
								if(slot.efxparam>0, {
									switch(tick%3,
										1, {track.note= track.note+(slot.efxparam&0xF)},
										2, {track.note= track.note+(slot.efxparam.rightShift(4))-(slot.efxparam&0xF)},
										0, {track.note= track.note-(slot.efxparam.rightShift(4))}
									);
								});
							},
							0x01, {				//1xx - porta up
								track.note= track.note+track.efxPortaUp;
							},
							0x02, {				//2xx - porta down
								track.note= track.note-track.efxPortaDown;
							},
							0x03, {				//3xx - tone porta
								if(track.note<track.efxTonePortaNote, {
									if(track.efxGlissando>=1, {
										track.note= track.note+1;
									}, {
										track.note= (track.note+track.efxTonePortaSpeed).min(track.efxTonePortaNote);
									});
								}, {
									if(track.note>track.efxTonePortaNote, {
										if(track.efxGlissando>=1, {
											track.note= track.note-1;
										}, {
											track.note= (track.note-track.efxTonePortaSpeed).max(track.efxTonePortaNote);
										});
									});
								});
							},
							0x04, {				//4xy - vibrato
								switch(track.efxVibratoWave,
									0, {track.efxVibrato= sin(track.efxVibratoIndex%64/64*2pi)},
									1, {track.efxVibrato= (track.efxVibratoIndex%64/64).round*2-1},
									2, {track.efxVibrato= (track.efxVibratoIndex%64/64)*2-1},
									3, {track.efxVibrato= (1-(track.efxVibratoIndex%64/64))*2-1}
								);
								track.efxVibrato= track.efxVibrato*track.efxVibratoDepth;
								track.efxVibratoIndex= track.efxVibratoIndex+track.efxVibratoSpeed;
							},
							0x05, {				//5xx - tone porta + volume slide
								if(track.note<track.efxTonePortaNote, {
									if(track.efxGlissando>=1, {
										track.note= track.note+1;
									}, {
										track.note= (track.note+track.efxTonePortaSpeed).min(track.efxTonePortaNote);
									});
								}, {
									if(track.note>track.efxTonePortaNote, {
										if(track.efxGlissando>=1, {
											track.note= track.note-1;
										}, {
											track.note= (track.note-track.efxTonePortaSpeed).max(track.efxTonePortaNote);
										});
									});
								});
								if(track.efxVolumeSlideUp>0, {
									track.vol= (track.vol+track.efxVolumeSlideUp).min(64);
								}, {
									track.vol= (track.vol-track.efxVolumeSlideDown).max(0);
								});
							},
							0x06, {				//6xx - vibrato + volume slide
								switch(track.efxVibratoWave,
									0, {track.efxVibrato= sin(track.efxVibratoIndex%64/64*2pi)},
									1, {track.efxVibrato= (track.efxVibratoIndex%64/64).round*2-1},
									2, {track.efxVibrato= (track.efxVibratoIndex%64/64)*2-1},
									3, {track.efxVibrato= (1-(track.efxVibratoIndex%64/64))*2-1}
								);
								track.efxVibrato= track.efxVibrato*track.efxVibratoDepth;
								track.efxVibratoIndex= track.efxVibratoIndex+track.efxVibratoSpeed;
								if(track.efxVolumeSlideUp>0, {
									track.vol= (track.vol+track.efxVolumeSlideUp).min(64);
								}, {
									track.vol= (track.vol-track.efxVolumeSlideDown).max(0);
								});
							},
							0x07, {				//7xx - tremolo
								switch(track.efxTremoloWave,
									0, {track.efxTremolo= sin(track.efxTremoloIndex%64/64*2pi)},
									1, {track.efxTremolo= (track.efxTremoloIndex%64/64).round*2-1},
									2, {track.efxTremolo= (1-(track.efxTremoloIndex%64/64))*2-1},
									3, {track.efxTremolo= (track.efxTremoloIndex%64/64)*2-1}
								);
								track.efxTremolo= track.efxTremolo*track.efxTremoloDepth;
								track.efxTremoloIndex= track.efxTremoloIndex+track.efxTremoloSpeed;
							},
							0x0A, {				//Axx - volume slide
								if(track.efxVolumeSlideUp>0, {
									track.vol= (track.vol+track.efxVolumeSlideUp).min(64);
								}, {
									track.vol= (track.vol-track.efxVolumeSlideDown).max(0);
								});
							},
							0x0E, {
								switch(slot.efxparam.rightShift(4),
									0x9, {		//E9x - retrig note
										if(tick%(slot.efxparam&0xF)==0, {
											track.syn.set(\t_trig, 1);
										});
									},
									0xC, {		//ECx - note cut
										if(tick>=(slot.efxparam&0xF), {
											track.vol= 0;
											track.released= true;
										});
									},
									0xD, {		//EDx - note delay
										if(tick==(slot.efxparam&0xF), {
											track.syn.set(\t_trig, 1);
										});
									}
								);
							},
							0x11, {				//Hxx - global volume slide
								if(track.efxGlobalVolumeSlide.rightShift(4)>0, {
									volume= (volume+(track.efxGlobalVolumeSlide.rightShift(4))).min(64);
								}, {
									volume= (volume-(track.efxGlobalVolumeSlide&0xF)).max(0);
								});
							},
							0x14, {				//Kxx - key off
								if(tick>=(slot.efxparam), {
									track.vol= 0;
									track.released= true;
								});
							},
							0x19, {				//Pxx - panning slide
								if(track.efxPanningSlide.rightShift(4)>0, {
									track.pan= (track.pan+(track.efxPanningSlide.rightShift(4))).min(255);
								}, {
									track.pan= (track.pan-(track.efxPanningSlide&0xF)).max(0);
								});
							},
							0x1B, {				//Rxx - multi retrig note
								if(tick+1%track.efxMultiRetrigNote==0, {
									track.syn.set(\t_trig, 1);
								});
							},
							0x1D, {				//Txx - tremor
								if(track.efxTremorIndex%((track.efxTremor.rightShift(4))+(track.efxTremor&0xF)+2)>(track.efxTremor.rightShift(4)), {
									track.efxTremorVol= track.vol;
									track.vol= 0;
								}, {
									track.vol= track.efxTremorVol;
								});
								track.efxTremorIndex= track.efxTremorIndex+1;
							}
						);
					};
					
					//--position updates - ticks within each slot
					tick= tick+1%(customSpeed ? speed);
				});								//end ticks switch
				
				//--frames - all ticks
				tracks.do{|track, i|
					var rate, vol, pan, vEnv, pEnv, vib= 0, swp, smp;
					if(track.ins.notNil, {
						if(muteTracks.includes(i), {
							track.syn.set(\vol, 0);
						}, {
							smp= track.ins.sample(track.note);
							vol= track.vol*volume/4096;//4096= 64*64
							pan= track.pan/127.5-1;
							
							//--volume envelope
							if(track.ins.volEnv.notNil, {
								vEnv= track.ins.volEnv;
								if(vEnv.on, {
									vol= vol*(vEnv.at(track.volEnvIndex)/64);
									if(vEnv.sustain and:{track.released.not}, {
										if(track.volEnvIndex==vEnv.sustainIndex, {
											track.volEnvIndex= track.volEnvIndex-1;
										});
									});
									track.volEnvIndex= (track.volEnvIndex+1).min(0xFFFF);
									if(vEnv.loop, {
										if(track.volEnvIndex==vEnv.loopEndIndex, {
											track.volEnvIndex= vEnv.loopStartIndex;
										});
									});
									if(track.released, {
										vol= vol*(track.fade/0x8000);
										track.fade= (track.fade-(track.ins.fadeOut)).max(0);
									});
								}, {
									if(track.released, {
										track.vol= 0;
										vol= 0;
									});
								});
							}, {
								if(track.released, {
									track.vol= 0;
									vol= 0;
								});
							});
							
							//--panning envelope
							if(track.ins.panEnv.notNil, {
								pEnv= track.ins.panEnv;
								if(pEnv.on, {
									pan= pan+(pEnv.at(track.panEnvIndex)-32/32*(1-pan.abs).abs);
									if(pEnv.sustain and:{track.released.not}, {
										if(track.panEnvIndex==pEnv.sustainIndex, {
											track.panEnvIndex= track.panEnvIndex-1;
										});
									});
									track.panEnvIndex= (track.panEnvIndex+1).min(0xFFFF);
									if(pEnv.loop, {
										if(track.panEnvIndex==pEnv.loopEndIndex, {
											track.panEnvIndex= pEnv.loopStartIndex;
										});
									});
								});
							});
							
							//--automatic vibrato
							if(track.ins.vibrato.depth>0 and:{track.released.not and:{track.ins.vibrato.speed>0}}, {
								switch(track.ins.vibrato.wave,
									0, {vib= sin(track.vibratoIndex%64/64*2pi)},
									1, {vib= (track.vibratoIndex%64/64).round*2-1},
									2, {vib= (track.vibratoIndex%64/64)*2-1},
									3, {vib= (1-(track.vibratoIndex%64/64))*2-1}
								);
								swp= track.ins.vibrato.sweep.max(0.25)*(track.ins.vibrato.speed*0.25);
								vib= vib*(track.ins.vibrato.depth/64)*(track.vibratoIndex.min(swp)/swp);
								track.vibratoIndex= track.vibratoIndex+(track.ins.vibrato.speed*0.25);
							});
							
							vol= vol*(1+track.efxTremolo);
							rate= (track.note+smp.note+(smp.tuning/128)+vib-track.efxVibrato-root).midiratio;
							track.syn.set(
								\rate, rate,
								\vol, vol,
								\pan, pan,
								\bufnum, smp.buffer.bufnum,
								\loop, smp.loop,
								\loopStart, smp.loopStart,
								\loopEnd, smp.loopEnd
							);
						});
					});
				};
			});									//end makeBundle
			if(delayPattern>0, {
				(delayPattern*speed).do{(1/4/6).wait};
				delayPattern= 0;
			});
			(1/4/6).wait;
		});
		1.wait;
		tracks.do{|x| x.syn.free};
	}
}


//----------------------------------------------------------------------------------------
	
//--a type of event class.  usually 64 of them per pattern
RedXMSlot {
	var <>note, <>instrument, <>volume, <>efxindex, <>efxparam;
	*new {|note, instrument, volume, efxindex, efxparam|
		^super.newCopyArgs(note, instrument, volume, efxindex, efxparam)
	}
}

//--a collection of events.  form part of the song
RedXMPattern {
	var <>slots;
	*new {|slots|
		^super.newCopyArgs(slots)
	}
	numRows {^slots.size}							//perhaps optimise later
	//ripPattern {}		//todo!!!
}

//--keeps settings, counters and envelopes for each channel or column
RedXMTrack {
	var	<>syn, <>ins, <>note= 0, <>previousNote, <>vol= 64, <>pan= 127, <>offset= 0,
		<>volEnvIndex, <>panEnvIndex, <>vibratoIndex, <>trig, <>released, <>fade,
		<>efxPortaUp= 0, <>efxPortaDown= 0, <>efxSampleOffset= 0,
		<>efxFinePortaUp= 0, <>efxFinePortaDown= 0, <>efxFineVolumeSlideUp= 0, <>efxFineVolumeSlideDown= 0,
		<>efxTonePortaSpeed= 0, <>efxTonePortaNote= 0,
		<>efxVibrato= 0, <>efxVibratoSpeed= 0, <>efxVibratoDepth= 0, <>efxVibratoIndex= 0, <>efxVibratoWave= 0,
		<>efxTremolo= 0, <>efxTremoloSpeed= 0, <>efxTremoloDepth= 0, <>efxTremoloIndex= 0, <>efxTremoloWave= 0,
		<>efxSetLoop= 0, <>efxSetLoopIndex= 0, <>efxGlissando= 0, <>efxGlobalVolumeSlide= 0,
		<>efxExtraFinePortaUp= 0, <>efxExtraFinePortaDown= 0,
		<>efxVolumeSlideUp= 0, <>efxVolumeSlideDown= 0,
		<>efxPanningSlide= 0, <>efxMultiRetrigNote= 0, <>efxTremor= 0, <>efxTremorIndex= 0, <>efxTremorVol= 0,
		<>vefxVolumeSlideDown= 0, <>vefxVolumeSlideUp= 0, <>vefxFineVolumeSlideDown= 0, <>vefxFineVolumeSlideUp= 0,
		<>vefxPanningSlideLeft= 0, <>vefxPanningSlideRight= 0;
	reset {
		volEnvIndex= 0;
		panEnvIndex= 0;
		vibratoIndex= 0;
		efxTremorIndex= 0;
		efxVibratoIndex= 0;
		fade= 0x8000;
	}
}

//--keeps samples and envelopes.  usually just one sample per instrument
RedXMInstrument {
	var <>name, <>samples, <>volEnv, <>panEnv, <>vibrato, <>fadeOut, <>keyMap, >lastSample;
	*new {|name, samples, volEnv, panEnv, vibrato, fadeOut, keyMap|
		^super.newCopyArgs(name, samples, volEnv, panEnv, vibrato, fadeOut, keyMap).lastSample_(samples[0])
	}
	numSamples {^samples.size}
	isEmpty {^samples.size==0}
	notEmpty {^samples.size>0}
	sample {|note|
		^lastSample= samples.clipAt(keyMap.clipAt(note));
	}
	vol {^lastSample.volume}
	pan {^lastSample.pan}
	load {|server|								//load samples onto server
		server= server ? Server.default;
		if(server.serverRunning.not, {
			"RedXM: boot server to load samples".error;
			^this
		});
		samples.do{|x| x.load(server)};
	}
	free {samples.do{|x| x.free}}					//free samples
}

//--for instrument volume and panning
RedXMEnvelope {
	var <env, <sustainPoint, <loopStartPoint, <loopEndPoint, <>on, <>sustain, <>loop, <levels, <times;
	*new {|array, numPoints, sustainPoint, loopStartPoint, loopEndPoint, type|
		^super.new.initRedXMEnvelope(array, numPoints, sustainPoint, loopStartPoint, loopEndPoint, type);
	}
	initRedXMEnvelope {|argArr, argNum, argSus, argSta, argEnd, argTyp|
		levels= argArr[{|i| i*2+1}.dup(argNum)];
		times= argArr[{|i| i*2}.dup(argNum)];
		env= Env(levels, times.drop(1).differentiate);
		sustainPoint= argSus;
		loopStartPoint= argSta;
		loopEndPoint= argEnd;
		on= argTyp&1==1;							//boolean test bit 0
		sustain= argTyp&2==2;						//boolean test bit 1
		loop= argTyp&4==4;							//boolean test bit 2
	}
	at {|time|
		^env[time];
	}
	sustainIndex {
		^times[sustainPoint];
	}
	loopStartIndex {
		^times[loopStartPoint];
	}
	loopEndIndex {
		^times[loopEndPoint];
	}
}

//--for instrument auto vibrato
RedXMVibrato {
	var <>wave, <>sweep, <>depth, <>speed;
	*new {|wave, sweep, depth, speed|
		^super.newCopyArgs(wave, sweep, depth, speed)
	}
}

//--a type of buffer class
RedXMSample {
	var <>length, <>loopStart, <>loopLen, <>volume, <>tuning, <>loopType, <>bitRate, <>pan, <>note, <format, <>name,
		<>data, <buffer;
	*new {|length, loopStart, loopLen, volume, tuning, loopType, bitRate, pan, note, format, name|
		^super.newCopyArgs(length, loopStart, loopLen, volume, tuning, loopType, bitRate, pan, note, format, name)
	}
	load {|server, action|
		if(buffer.notNil, {this.free});
		buffer= Buffer.loadCollection(server, data, 1, action);
	}
	isEmpty {^data.size==0}
	notEmpty {^data.size>0}
	free {buffer.free}
	//length {^buffer.numFrames ? 0}
	loop {^if(loopLen>2, {loopType}, {0})}
	loopEnd {
		if(this.loop>0, {^(loopLen+loopStart).min(this.length)});
		^this.length
	}
	
	//--convert & load data into a float buffer
	ripSample {|server, action|
		server= server ? Server.default;
		if(server.serverRunning.not, {
			"RedXM: boot server to rip".error;
			^this
		});
		^Buffer.loadCollection(server, data, 1, action);
	}
	
	//--import soundfile into sample object via a buffer
	//  remember to manually update name, tuning, volume, loopstart, looplen
	importSoundFile {|path, server, action|
		path= path.standardizePath;
		server= server ? Server.default;
		if(server.serverRunning.not, {
			"RedXM: boot server to import soundfile".error;
			^this
		});
		Routine.run{
			var halt= Condition.new;
			var file, buf;
			if(File.exists(path), {
				file= SoundFile.openRead(path);
				if(file.numChannels>1, {
					"RedXM: multichannel soundfile - only first channel used".warn;
				});
				file.close;
				buf= Buffer.readChannel(server, path, channels:[0]);
				server.sync(halt);
				this.prImport(buf, action);
			}, {
				"RedXM: file not found".error;
			});
		}
	}
	
	//--import buffer into sample object
	//  remember to manually update name, tuning, volume, loopstart, looplen
	importBuffer {|buffer, action|
		if(buffer.server.serverRunning.not, {
			"RedXM: boot server to import buffer".error;
			^this
		});
		this.prImport(buffer, action);
	}
	
	//--private
	prImport {|buf, action|
		buf.loadToFloatArray(0, -1, {|arr|
			data= arr;
			loopStart= 0;
			if(this.loop>0, {loopLen= data.size});
			this.load(buf.server, action);
		});
	}
}
