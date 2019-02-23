/*
 *	JSpeech
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	Replacement for (Cocoa) Speech
 *
 *	@version	0.58, 12-Jan-08
 *	@author	Hanns Holger Rutz
 */
JSpeechChannel{
	var <channel, <pitch, <volume, <pitchMod, <voice, <rate;
	var <wordDoneAction, <doneAction;
	var <paused = false, isActive;
	
	
	// ----------------- constructor -----------------

	*new{ arg chan;
		^super.newCopyArgs( chan );
	}
	
	// ----------------- public instance methods -----------------

	wordDoneAction_ { arg action;
		JSpeech.wordDoneActions.put( channel, action );
	}

	doneAction_ { arg action;
		JSpeech.doneActions.put( channel, action );
	}
		
	pitch_ { arg midinote;
		pitch = midinote;
		JSpeech.setSpeechPitch( channel, pitch );
	}
	
	volume_ { arg amp;
		volume = amp;
		JSpeech.setSpeechVolume( channel, volume );		
	}
	
	pitchMod_ {  arg mod;
		pitchMod = mod;
		JSpeech.setSpeechPitchMod( channel, pitchMod );
	}
	
	rate_ { arg ratein;
		rate = ratein;
		JSpeech.setSpeechRate( channel, rate );
	}
	
	voice_ { arg num;
		voice = num;
		JSpeech.setSpeechVoice( channel, voice );			
	}
	
	stop { arg when;
		if( when.isNumber.not, {
			when = JSpeech.stopMethods[ when ];
		});
		JSpeech.stop( channel, when );
	}
	
	pause { arg bool;
		paused = bool;
		JSpeech.pause( channel, bool.binaryValue );
	}
	
	isActive {
		^this.prIsActive( channel );
	}
		
	speak { arg string, force = false;
		if( force.not, {
			this.prSpeak( channel, string );
			^this
		});
		r {
			this.stop( 0 );
			0.5.wait;
			this.prSpeak( channel, string );
		}.play;
	}

	// ----------------- private instance methods -----------------
	
	prSpeak { arg channel, txt;
		var result = JSpeech.initialized;
//		_SpeakText
		if( result.not, { result = JSpeech.init });	
		if( result, {
			JSpeech.server.sendMsg( '/method', \speech, \speak, txt );
		});
	}
	
	prIsActive { arg chan;
//		_SpeechVoiceIsSpeaking
	}
}

JSpeech {
	classvar <>wordActions, <>doneActions, <>wordAction, <>doneAction, <channels;
	classvar <>initialized = false, <stopMethods;
	
	classvar <>server;
	
	classvar svolume = 1.0, srate = 120, spchrange = 10, spch = 80;

	// ----------------- public class methods -----------------

	*setSpeechVoice { arg chan, voice;
		"JSpeech.setSpeechVoice : not yet implemented".error;
//		_SetSpeechVoice
	}

	*setSpeechRate { arg chan, rate;
//		rate = (rate * 2).div( 3 );
//		rate = (rate.pow( 0.785 ) + 48.9).round( 0.1 );
//		rate = (rate.pow( 0.897 ) + 10.5).round( 0.1 );
		srate = ((rate - 20) / 1.75 + 25.72).round( 0.1 ); // hmmm, seems to be similar to cocoa speed this way
		if( initialized, { server.sendMsg( '/set', \speech, \rate, srate )});
	}

	*setSpeechPitch { arg chan, pitch;
		spch = pitch.midicps;
		if( initialized, { server.sendMsg( '/set', \speech, \pitch, spch )});
	}

	*setSpeechPitchMod { arg chan, pitchMod;
		spchrange = pitchMod / 10; // hmmm...
		if( initialized, { server.sendMsg( '/set', \speech, \pitchRange, spchrange )});
	}

	*setSpeechVolume { arg chan, volume;
		svolume = volume.ampdb.max(-96)/96+1; // hmmm, seems to be similar to cocoa linear volume this way
		if( initialized, { server.sendMsg( '/set', \speech, \volume, svolume )});
	}

	*pause { arg chan, paused = 0;
		if( initialized, {
			switch( paused.asInteger,
			0, {
				server.sendMsg( '/methodr', '[', \method, \speech, \getAudioPlayer, ']', \resume );
			},
			1, {
				server.sendMsg( '/methodr', '[', \method, \speech, \getAudioPlayer, ']', \pause );
			}, {
				("JSpeed.pause : illegal argument (paused = " ++ paused ++ ")").error;
			});
		});
	}

	//when: 0 kImmediate, 1 kEndOfWord, 2 kEndOfSentence
	*stop { arg chan, when = 0;
		"JSpeech.stop : not yet implemented".error;
//		_SetSpeechStopAt
	}

	*doWordAction { arg chan;
		wordAction.value( chan ); // backward compatibility
		wordActions[ chan ].value( channels[ chan ]);
	}

	*doSpeechDoneAction { arg chan;
		doneAction.value( chan );
		doneActions[ chan ].value( channels[ chan ]);
	}

	// ----------------- private class methods -----------------

	*init { arg num = 1;
		if( server.isNil, { server = SwingOSC.default });
		if( server.serverRunning.not, {
			"JSpeech.init : SwingOSC server is not running".error;
			^false;
		});
		if( initialized.not, {
			initialized = true;
			channels = Array.new( num );
			wordActions = Array.newClear( num );
			doneActions =  Array.newClear( num );
			num.do { arg i;
				channels.add( JSpeechChannel( i ));
			};
			stopMethods = (immediate: 0, endOfWord: 1, endOfSentence: 2);
			UI.registerForShutdown({ if( server.notNil and: { server.serverRunning }, {
				server.sendBundle( nil, [ '/method', \speech, \deallocate ], [ '/free', \speech ]);
			})});
		});
		server.sendBundle( nil,
			[ '/local', \speech, '[', '/methodr', '[', '/method', 'com.sun.speech.freetts.VoiceManager', \getInstance, ']',
				\getVoice, "kevin16", ']' ],
			[ '/method', \speech, \allocate ],
			[ '/set', \speech, \volume, svolume, \rate, srate, \pitchRange, spchrange, \pitch, spch ]
		);
		^true;
	}
}