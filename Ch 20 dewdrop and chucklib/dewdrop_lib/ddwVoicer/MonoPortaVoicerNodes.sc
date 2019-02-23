
MonoPortaSynthVoicerNode : SynthVoicerNode {

	trigger { arg freq, gate = 1, args, latency;
		var bundle;
		if(freq.isNumber) {
			this.shouldSteal.if({
				bundle = this.setMsg([\freqlag, voicer.portaTime, \freq, freq,
					\gate, gate, \t_gate, gate] ++ args);
			}, {
				isReleasing.if({
					bundle = this.releaseMsg(-1.02);	// quick release
				});
					// triggerMsg() sets the synth instance var
				bundle = bundle ++ this.triggerMsg(freq, gate, args ++ [\freqlag, voicer.portaTime]);
				NodeWatcher.register(synth);
					// when the synth node dies, I need to set my flags
				Updater(synth, { |syn, msg|
					(msg == \n_end).if({
							// synth may have changed
						(syn == synth).if({
							isPlaying = isReleasing = false;
						});
						syn.releaseDependants;
					});
				});
			});
			target.server.listSendBundle(myLastLatency = latency, bundle);
			frequency = freq;	// save frequency for Voicer.release
//			lastTrigger = Main.elapsedTime;	// save time
			lastTrigger = thisThread.seconds;  // clock.beats2secs(thisThread.clock.beats);
			isPlaying = true;
			isReleasing = false;
		} {
			reserved = false;
		}
	}
	
	shouldSteal {
		^super.shouldSteal and: { isReleasing.not }
	}
	
	release { |gate = 0, latency, freq|
		voicer.lastFreqs.remove(freq);
		super.release(gate, latency, freq);
	}
}


// method defs are repeated between these 2 classes because of no multiple inheritance

MonoPortaInstrVoicerNode : InstrVoicerNode {

	trigger { arg freq, gate = 1, args, latency;
		var bundle;

		if(freq.isNumber) {
			this.shouldSteal.if({
				bundle = this.setMsg([\freqlag, voicer.portaTime, \freq, freq,
					\gate, gate, \t_gate, gate] ++ args);
			}, {
				isReleasing.if({
					bundle = this.releaseMsg(-1.02);	// quick release
				});
				bundle = bundle ++ this.triggerMsg(freq, gate, args ++ [\freqlag, voicer.portaTime]);
				NodeWatcher.register(synth);
					// when the synth node dies, I need to set my flags
				Updater(synth, { |syn, msg|
					(msg == \n_end).if({
							// synth may have changed
						(syn == synth).if({
							isPlaying = isReleasing = false;
						});
						syn.releaseDependants;
					});
				});
			});
			
			target.server.listSendBundle(myLastLatency = latency, bundle);
			
			frequency = freq;
			voicer.lastFreqs.add(freq);
			lastTrigger = Main.elapsedTime;
			isPlaying = true;
			isReleasing = false;
		} {
			reserved = false;
		}
	}

	shouldSteal {
		^super.shouldSteal and: { isReleasing.not }
	}

	release { |gate = 0, latency, freq|
		voicer.lastFreqs.remove(freq);
		super.release(gate, latency, freq);
	}
}
