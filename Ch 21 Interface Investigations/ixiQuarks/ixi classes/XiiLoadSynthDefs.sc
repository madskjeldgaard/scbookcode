
XiiLoadSynthDefs {	

	var s;
	
	*new { arg server;
		^super.new.initXiiLoadSynthDefs(server);
		}
		
	initXiiLoadSynthDefs { arg server;
		s = server;
		
		// --- the SoundScratcher --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		// the lag time is good around 4 seconds
		SynthDef.writeOnce(\xiiScratch1x2, {arg outbus=0, bufnum, pos=0, vol=0, gate=1;
			var signal, env;
			env = EnvGen.kr(Env.adsr(0.2, 0.2, 1, 0.21, 1, -4), gate, doneAction:2);
			signal = BufRd.ar(1, bufnum, Lag.ar(K2A.ar(pos), 4), 1) * env * vol;
			Out.ar(outbus, signal!2);
		});
		
		
		SynthDef(\xiiWarp, {arg outbus=0, bufnum = 0, freq=1, pointer=0, vol=0, gate=1;
			var signal, env;
			env = EnvGen.kr(Env.adsr(0.2, 0.2, 1, 0.21, 1, -4), gate, doneAction:2);
			signal = Warp1.ar(1, bufnum, pointer, freq, 0.09, -1, 8, 0.2, 2) * env * vol;
			Out.ar(outbus, signal!2);
		}).add;
		
		SynthDef.writeOnce(\xiiGrain, {arg outbus=0, bufnum, rate=1, pos=0, dur=0.05, vol=1, envType=0;
			var signal, env, sineenv, percenv;
			sineenv = EnvGen.kr(Env.sine(dur, vol), doneAction:2);
			percenv = EnvGen.kr(Env.perc(0.001, dur*2, vol), doneAction:2);
			env = Select.kr(envType, [sineenv, percenv]);
			signal = PlayBuf.ar(1, bufnum, rate, 1.0, pos) * env ;
			Out.ar(outbus, Pan2.ar(signal, Rand(-0.75, 0.75)));
		});
		
		/*
		// One day I'll use Josh's BufGrain
		SynthDef(\xiiGrain, {arg outbus=0, bufnum, t_trig=0, rate=1, pos=0, dur=0.05, vol=1, envType=0;
			var signal, env, sineenv, percenv;
			sineenv = EnvGen.kr(Env.sine(dur, vol));
			percenv = EnvGen.kr(Env.perc(0.001, dur*2, vol));
			env = Select.kr(envType, [sineenv, percenv]);
			signal = BufGrain.ar(t_trig, dur, bufnum, rate, pos) * env ;
			Out.ar(outbus, Pan2.ar(signal, Rand(-0.75, 0.75)));
		}).load(s);
		*/
		
		SynthDef.writeOnce(\xiiGrains, {arg 	outbus=0, bufnum, dur=0.3, trate=10, ratelow= 0.5, 
								ratehigh=1.5, left=0.3, right=0.4, vol=0.2, globalvol=1;
			var clk, pos, pan, rate;
			clk = Impulse.kr(trate);
			pos = TRand.kr(left, right, clk);
			rate = TRand.kr(ratelow, ratehigh, clk);
			pan = WhiteNoise.kr(0.6);
			Out.ar(outbus, TGrains.ar(2, clk, bufnum, rate, pos, dur, pan, vol)*globalvol);
		});
		
		SynthDef.writeOnce(\xiiGrainsSQ, {arg outbus=0, bufnum, dur=0.3, trate=10, rate, left=0.3, vol=0.2, globalvol=1;
			var clk, pos, pan;
			clk = Impulse.kr(trate);
			pan = WhiteNoise.kr(0.6);
			Out.ar(outbus, TGrains.ar(2, clk, bufnum, rate, left, dur, pan, vol) * globalvol);
		});
		

		// --- the XiiPlayer --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DiskIn
		
		
		SynthDef.writeOnce(\xiiPlayer1, { arg outbus = 0, bufnum = 0, vol=0;
			Out.ar(outbus, (DiskIn.ar(1, bufnum) * vol).dup);
		});

		SynthDef.writeOnce(\xiiPlayer2, { arg outbus = 0, bufnum = 0, vol=0;
			Out.ar(outbus, DiskIn.ar(2, bufnum) * vol);
		});


		// --- the AudioIn tool  --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DiskIn
	
		SynthDef.writeOnce(\xiiAudioIn, { arg out=0, volL, volR, panL, panR;
			var updateRate=40, ampl, ampr, left, right;
			left =  SoundIn.ar(0) * volL;
			right = SoundIn.ar(1) * volR;
			ampl = Amplitude.kr(left);
			ampr = Amplitude.kr(right);
			SendTrig.kr(Impulse.kr(updateRate), 800, ampl);
			SendTrig.kr(Impulse.kr(updateRate), 801, ampr);
			Out.ar(out, Pan2.ar(left, panL));
			Out.ar(out, Pan2.ar(right, panR));
		});


		// --- the bufferplayer --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		SynthDef.writeOnce(\xiiBufPlayerSTEREO, {arg trigID = 10, out=0, bufnum=0, vol=0.0, pan = 1, trig=0, 
				pitch=1.0, onOff=0, startPos=0, endPos= 1000;
			var updateRate=40, playbuf, ampl, ampr, signal;
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol;
			signal = Balance2.ar(playbuf[0], playbuf[1], pan);
			ampl = Amplitude.kr(signal.at(0));  
			ampr = Amplitude.kr(signal.at(1));
			SendTrig.kr(Impulse.kr(updateRate), trigID, ampl);
			SendTrig.kr(Impulse.kr(updateRate), trigID+1, ampr);
			Out.ar(out, signal);
		});
		
		SynthDef.writeOnce(\xiiBufPlayerMONO, {arg trigID = 10, out=0, bufnum=0, vol=0.0, pan = 0, trig=0, 
				pitch=1.0, onOff=0, startPos=0, endPos= 1000;
			var updateRate=40, playbuf, ampl;
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol;
			ampl = Amplitude.kr(playbuf);  
			SendTrig.kr(Impulse.kr(updateRate), trigID, ampl);
			SendTrig.kr(Impulse.kr(updateRate), trigID+1, ampl);
			Out.ar(out, Pan2.ar(playbuf, pan));
		});


		// --- the polymachine --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		SynthDef.writeOnce(\xiiPolyrhythm1x2, {arg outbus=0, bufnum=0, vol=1, pan = 1, 
								trig=0, pitch=1, startPos=0, endPos= -1;
			var playbuf, env;
			env = EnvGen.ar(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env;
			Out.ar(outbus, playbuf!2);
		});
		
		SynthDef.writeOnce(\xiiPolyrhythm2x2, {arg outbus=0, bufnum=0, vol=1, pan = 1, 
								trig=0, pitch=1, startPos=0, endPos= -1;
			var playbuf, env;
			env = EnvGen.ar(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env;
			Out.ar(outbus, playbuf);
		});
		
		SynthDef.writeOnce(\xiiPolyrhythm1x2Env, {arg outbus=0, bufnum=0, vol=1, pan = 1, trig=0, pitch=1, 
				startPos=0, endPos= -1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.1, 0.5, 1.0];
			var playbuf, env, killenv;
			env = EnvGen.kr(Env.new(levels, times)); // killenv kills because there is no loop:0 in the new LoopBuf
			killenv = EnvGen.kr(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			//playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, endPos, loop:0) * vol * env; // old loopbuf
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env * killenv;
			Out.ar(outbus, playbuf!2);
		});

		SynthDef.writeOnce(\xiiPolyrhythm2x2Env, {arg outbus=0, bufnum=0, vol=1, pan = 1, trig=0, pitch=1, 
				startPos=0, endPos= -1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.1, 0.5, 1.0];
			var playbuf, env, killenv;
			env = EnvGen.kr(Env.new(levels, times)); // killenv kills because there is no loop:0 in the new LoopBuf
			killenv = EnvGen.kr(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env * killenv; 
			//playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, endPos, loop:0) * vol * env;/ / old loopbuf
			Out.ar(outbus, playbuf);
		});

		
		SynthDef.writeOnce(\xiiPolyrhythmAudioStream2x2Env, {arg inbus=20, outbus=0, vol=1, 
				levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.07, 0.1, 0.062];
			var in, env, killenv;
			env = EnvGen.kr(Env.new(levels, times), doneAction:2);
			in = InFeedback.ar(inbus, 2) * vol * env; 
			Out.ar(outbus, in);
		});

		// --- the grainbox --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
		SynthDef.writeOnce(\xiiGranny, {arg out=0, trigRate=1, freq=1, centerPos=0.5, dur=0.05, 
						pan=0.2, amp = 0.4, buffer=0, cntrPosRandWidth=0.1, cntrPosRandFreq=10,
						 durRandWidth=0.1,  durRandFreq=10, revVol=0.1, delayTime=4,
						 decayTime=6, aDelTime=1, aDecTime=1, rateRandWidth=0.01, 
						 rateRandFreq=10, vol = 1;
			var fc, granny, outSignal, revSignal;
		
		granny = 
		TGrains.ar(
			2, 					// num channels
			Impulse.ar(trigRate), 	// trigger
			buffer,				// buffer
			freq + 
			TRand.kr(-1*rateRandWidth, rateRandWidth, Impulse.kr(rateRandFreq)), // rate  
			// cntrpos
			centerPos + 
			TRand.kr(-1*cntrPosRandWidth, cntrPosRandWidth, Impulse.kr(cntrPosRandFreq)),
			// duration of the grain
			dur + TRand.kr(-1*durRandWidth, durRandWidth, Impulse.kr(durRandFreq)), 				WhiteNoise.kr(pan),  	// pan
				amp, 				// amplitude
				2); 	// interpolation : (1 = no interp. 2 = linear interp. 4 = cubic interpol.) 
			
			revSignal = Mix.ar(granny) * revVol;
			revSignal = Mix.ar(CombL.ar(revSignal, 0.1, 
				{0.04.rand2 + 0.05}.dup(4) * delayTime,  decayTime));
			4.do({ revSignal = AllpassN.ar(revSignal, 0.150, [0.050.rand,0.051.rand] 
				* aDelTime, aDecTime) });
			Out.ar(out, granny + LeakDC.ar(revSignal) * vol);
		});
		
		// ------ the predators ------- !!!!!!!!!!!!!!!!!!!!!!!!!!!

		// - the sample player
		SynthDef.writeOnce(\xiiPrey1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = LoopBuf.ar(1, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		});
		
		SynthDef.writeOnce(\xiiPrey2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = LoopBuf.ar(2, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, bufplay*env);
		});
		
		SynthDef.writeOnce(\xiiAudioStream, {arg inbus=20, outbus=0, amp=1, pitchratio=1.0, timesc=1.0, 
				levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162];
			var in, env, killenv;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			in = InFeedback.ar(inbus, 2); 
			in = PitchShift.ar(in, 0.1, pitchratio, 0, 0.004) * amp * env;
			Out.ar(outbus, in);
		});

		SynthDef.writeOnce(\xiiCode, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		// - the bell synthesis
		SynthDef.writeOnce(\xiiBells, {arg outbus=0, freq=440, dur=1, amp=0.4, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, Rand(333,666)/freq, amp), doneAction:2);
		        x = Mix.ar([SinOsc.ar(freq, 0, 0.3), SinOsc.ar(freq*2, 0, 0.2)] ++
		        				Array.fill(6, {SinOsc.ar(freq*Rand(-10,10), 0, Rand(0.08,0.2))}));
		        //x = BPF.ar(x, freq, 4.91);
		        x = Pan2.ar(x, pan);
		        Out.ar(outbus, x*env);
		});
		
		// - harmonic sines
		SynthDef.writeOnce(\xiiSines, {arg outbus=0, freq=440, dur=1, amp=0.4, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq, amp), doneAction:2);
		        x = Mix.ar(Array.fill(8, {SinOsc.ar(freq*IRand(1,10),0, 0.12)}));
		        x = RLPF.ar(x, freq*14, Rand(0.04,1));
		        x = Pan2.ar(x,pan);
		        Out.ar(outbus, x*env);
		});
				
		// - synth1
		
		SynthDef.writeOnce(\xiiSynth1, {arg outbus, freq=440, dur=1, amp=0.4, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq, amp), doneAction:2);
		        x = Mix.ar([FSinOsc.ar(freq, pi/2, 0.5), Pulse.ar(freq, Rand(0.3,0.5))]);
		        x = RLPF.ar(x, freq*14, Rand(0.04,1));
		        x = Pan2.ar(x,pan);
		        Out.ar(outbus, x*env);
		});
		
		SynthDef.writeOnce(\xiiKs_string, { arg outbus, note, pan, rand, delayTime, noiseType=1;
			var x, y, env;
			env = Env.new(#[1, 1, 0],#[2, 0.001]);
			x = Decay.ar(Impulse.ar(0, 0, rand), 0.1+rand, WhiteNoise.ar); 
		 	x = CombL.ar(x, 0.05, note.reciprocal, delayTime, EnvGen.ar(env, doneAction:2)); 
			x = Pan2.ar(x, pan);
			Out.ar(outbus, LeakDC.ar(x));
		});
		
		SynthDef.writeOnce(\xiiImpulse, { arg outbus, pan, amp;
			var x, y, env, imp;
			env = Env.perc(0.0000001, 0.1);
			imp = Impulse.ar(1);
			x = Pan2.ar(imp * EnvGen.ar(env, doneAction:2), pan) * amp;
			Out.ar(outbus, LeakDC.ar(x));
		});
		
		
		SynthDef.writeOnce(\xiiRingz, {arg outbus, freq, pan, amp;
			var ring, trig;
			trig = (Impulse.ar(0.005, 180) * 0.01)
						* EnvGen.ar(Env.perc(0.001, 220/freq), doneAction:2);
			ring = Ringz.ar(trig, [freq, freq+2], 220/freq);
			ring = Pan2.ar(ring, pan) * amp;
			Out.ar(outbus, ring);
		});
		
		SynthDef.writeOnce(\xiiKlanks, { arg outbus=0, freq= 1.0, amp = 1, pan;
			var trig, klan, env;
			var  p, exc, x, s;
			trig = PinkNoise.ar( 0.11 );
			klan = Klank.ar(`[ Array.fill( 16, { Rand(10.0 ) }), nil, 
							Array.fill( 16, { 0.1 + Rand(2.0)})], trig, freq );
			klan = (klan * amp).softclip;
			klan = LPF.ar(klan, freq*2);
			env = EnvGen.ar(Env.perc(0.001, 340/freq), doneAction:2);
			Out.ar( outbus, Pan2.ar( klan * env, pan ));
		});
		
		// --------------- The Gridder ---------------------
		
		SynthDef.writeOnce(\xiiSine, {arg outbus=0, freq=440, phase=0, pan=0, amp=0.61;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = sum(SinOsc.ar([freq, freq+1], phase, 0.5*env*amp*AmpComp.kr(freq, 65)));
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		SynthDef.writeOnce(\xiiString, {arg outbus=0, freq=440, pan=0, amp=1;
			var pluck, period, string;
			pluck = PinkNoise.ar(Decay.kr(Impulse.kr(0.005), 0.05));
			period = freq.reciprocal;
			string = CombL.ar(pluck, period, period, 4);
			string = LeakDC.ar(LPF.ar(Pan2.ar(string, pan), 12000)) * amp;
			DetectSilence.ar(string, doneAction:2);
			Out.ar(outbus, string)
		});
				
		SynthDef.writeOnce(\xiiGridder, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		
		// -------------- TrigRecorder ----------------------
		
		SynthDef.writeOnce(\xiiTrigRecAnalyser1x1, {arg out=120, inbus=8, prerectime=1, sensitivity=0.8;
			var in, signal;
			in = InFeedback.ar(inbus, 1);
			signal = DelayN.ar(in, 2, prerectime); 
			Out.ar(out, signal);
			SendTrig.kr(Amplitude.kr(in) >= sensitivity, 666);
		});

		SynthDef.writeOnce(\xiiTrigRecAnalyser2x2, {arg out=120, inbus=8, prerectime=1, sensitivity=0.8;
			var in, signal;
			in = InFeedback.ar(inbus, 2);
			signal = DelayN.ar(in, 2, prerectime); 
			Out.ar(out, signal);
			SendTrig.kr(Amplitude.kr(Mix.ar(in)) >= sensitivity, 666);
		});
		
		SynthDef.writeOnce(\xiiTrigRecorderRec1x1, {arg bufnum, inbus=120;
			DiskOut.ar(bufnum, In.ar(inbus, 1));
		});
		
		SynthDef.writeOnce(\xiiTrigRecorderRec2x2, {arg bufnum, inbus=120;
			DiskOut.ar(bufnum, In.ar(inbus, 2));
		});

				
		// -------------- Recorder vumeter ----------------------
		SynthDef.writeOnce(\xiiVuMeter, {arg inbus = 0, amp = 1.0, rate = 15, rel = 1;
			var signal, amplitude;
			signal = InFeedback.ar(inbus, 1) * amp;
			amplitude = AmplitudeMod.kr(signal, 0.01, rel);
			SendTrig.kr(Impulse.kr(rate), 820, amplitude);
		});
		
				
		// -------------- StratoSampler Synths ----------------------
		SynthDef.writeOnce(\xiiStratoSamplerRec,{ arg  inbus=0, bufnum=0, reclevel=1.0, prelevel=0.0;
		    var ain;
		    ain = InFeedback.ar(inbus, 1);     // In
		    RecordBuf.ar(ain, bufnum, recLevel: reclevel, preLevel: prelevel);
		}, [0.2, 0.2 , 0.2, 0.2]);
	   
	   SynthDef.writeOnce(\xiiStratoSamplerPlay,{ arg outbus=0, bufnum,  endloop=1000, amp=1.0; 
			var signal;
			signal = LoopBuf.ar(1, bufnum, 1, 1, 0, 0, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});
		
		
		// ----------------- Mushrooms --------------------
		
		// -------------- BufferOnsets Synth ----------------------
	   SynthDef.writeOnce(\xiiBufOnset,{ arg outbus=0, bufnum,  rate=1.0, endloop=1000, amp=1.0; 
			var signal;
			signal = LoopBuf.ar(1, bufnum, rate, 1, 0, 0, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});
		
				// - the sample player
		SynthDef.writeOnce(\xiiMush1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(1, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.perc(0.001,0.2), timeScale: timesc, doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		});
		
		SynthDef.writeOnce(\xiiMush2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(2, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.perc(0.001,0.2), timeScale: timesc, doneAction:2);
			Out.ar(outbus, bufplay*env);
		});
		
		SynthDef.writeOnce(\xiiMushTime,{ arg outbus=0, bufnum, rate=1.0, startloop=0, endloop=1000, amp=1.0; 
			var signal;	
			signal = LoopBuf.ar(1, bufnum, rate, 1, startloop, startloop, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});

		SynthDef.writeOnce(\xiiMushFFT, {arg outbus=0, thresh, bufnum, fftbuf, trackbuf,rate=1.0, amp=1, 
								startloop=0, endloop=1000;
			var sig, onsets, pips, am;
			sig = LoopBuf.ar(1, bufnum, rate, 1, startloop, startloop, endLoop:endloop);
			am = Amplitude.kr(sig);
			onsets = OnsetsDS.kr(sig, fftbuf, trackbuf, thresh*8, \complex);
			6.do{ am = max(am, Delay1.kr(am))}; // get the max power over the last 6 control periods
			SendTrig.kr(onsets, 840, am);
			Out.ar(outbus, ((sig * amp)).dup);
		});
		
		// the default code synth of the  mushrooms
		SynthDef.writeOnce(\xiiMushroom, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		SynthDef.writeOnce(\xiiLoopBufXSndFileView1x1, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = Pan2.ar(LoopBuf.ar(1, bufnum, 1, 1, start, start, end), 0.0) * vol;
			Out.ar(out, z);
		});
		
		SynthDef.writeOnce(\xiiLoopBufXSndFileView2x2, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = LoopBuf.ar(2, bufnum, 1, 1, start, start, end) * vol;
			Out.ar(out, z);
		});

		SynthDef.writeOnce(\xiiPlayBufXSndFileView1x1, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = Pan2.ar(PlayBuf.ar(1, bufnum, 1, 1, start, start, end), 0.0) * vol;
			Out.ar(out, z);
		});
		
		SynthDef.writeOnce(\xiiPlayBufXSndFileView2x2, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = PlayBuf.ar(2, bufnum, 1, 1, start, start, end) * vol;
			Out.ar(out, z)
		});
		
		// theory synthdef
		SynthDef.writeOnce(\midikeyboardsine, {arg freq, amp = 0.25;
			Out.ar(0, (SinOsc.ar(freq,0,amp)*EnvGen.ar(Env.perc, doneAction:2)).dup)
		});

	}
}	
