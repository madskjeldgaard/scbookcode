SchwelleSurround{

	var <bus, <synth;
	var <group, <server;
	var <controlmid;
	var <controldir;
	var <controltop;
	var <controlgroup;
	var <outbus;

	*new{ |out,group,server|
		^super.new.init( out, group, server );
	}

	init{ |out,gr,s|
		server = s ? Server.local;
		group = Group.new( gr, \addToTail );
		this.initSynthDefs( server );
		controlmid = Bus.control( server, 4 );
		controltop = Bus.control( server, 4 );
		controldir = Bus.control( server, 8 );
		controlgroup = Group.new( group, \addToTail ); 
		bus = Bus.audio( server, 8 ); // these are the buses the room mix for the input to the surround mix
		outbus = out ? Bus.new(\audio,0,8);
		
		synth = Synth.newPaused( \schwellesurround, [\out,outbus.index,\in,bus.index], group,\addToTail );
		NodeWatcher.register(synth,true);
		synth.busMap( \ampsdir, controldir );
		synth.busMap( \ampsmid, controlmid );
		synth.busMap( \ampstop, controltop );
		controldir.setn( [ 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 ] );
	}
	
	start{
		synth.run;
	}
	
	stop{
		synth.run(false);
	}
	
	movetop{ |attack=0.99,decay=0.01,timescale=1|
		Synth.new( \surroundmove, [\out, controltop.index, \attack, attack, \decay, decay, \ts, timescale], controlgroup, \addToTail );
	}
	
	movemid{ |attack=0.99,decay=0.01,timescale=1|
		Synth.new( \surroundmove, [\out, controlmid.index, \attack, attack, \decay, decay, \ts, timescale], controlgroup, \addToTail );
	}
	
	breathmid{ |freq=1.0,phases,mul=0.5,add=0.5|
		phases = phases ? Array.fill( 4, 0.5 );
		Synth.new( \surroundbreathmid, [\out, controlmid.index, \freq, freq, \mul, mul, \add, add, \phases, phases ].flatten, controlgroup, \addToTail );
	}
	
	randomroom{ |rate=1.0,attack=0.99,decay=0.01,timescale=1|
		controldir.setn( Array.fill( 8, 0 ) );
		Synth.new( \surroundrandom, [\out, controldir.index, \rate, rate, \attack, attack, \decay, decay, \ts, timescale ], controlgroup, \addToTail );

	}
	
	randomtop{ |rate=1.0,attack=0.99,decay=0.01,timescale=1|
		controltop.setn( Array.fill( 4, 0 ) );
		Synth.new( \surroundrandom, [\out, controltop.index, \rate, rate, \attack, attack, \decay, decay, \ts, timescale ], controlgroup, \addToTail );

	}
	
	toptoall{ |dur=5.0|
		controldir.setn( Array.fill( 8, 0 ) );
		Synth.new( \surroundtoptoall, [\outdir, controldir.index, \outtop, controltop.index, \outmid, controlmid.index, \dur, dur ], controlgroup, \addToTail );
	}
	
	stopmove{
		controlgroup.freeAll;
	}
	
	initSynthDefs{ |s|
		SynthDef( \schwellesurround, { arg out=0, in=0, amp=1, lag=1, lagmid=0.1, lagtop=0.1, lagdir=0.1, gate=1;
			var envcut, inputs, dir, mid, top, ampsmid, ampsdir, ampstop;
			ampsdir = Control.names([\ampsdir]).kr( Array.fill(8, 1.0 ));
			ampsmid = Control.names([\ampsmid]).kr( Array.fill(4, 1.0 ));
			ampstop = Control.names([\ampstop]).kr( Array.fill(4, 1.0 ));
			inputs = In.ar( in, 8 );
			dir = inputs * ampsdir.lag(lagdir);
			dir[4] = dir[4] + (ampsmid[0].lag(lagmid) * inputs[0]) + (ampsmid[1].lag(lagmid) * inputs[1]);
			dir[5] = dir[5] + (ampsmid[2].lag(lagmid) * inputs[2]) + (ampsmid[3].lag(lagmid) * inputs[3]);
			dir[7] = dir[7] + (ampstop[0].lag(lagtop) * inputs[0]) + (ampstop[1].lag(lagtop) * inputs[1]);
			dir[6] = dir[6] + (ampstop[2].lag(lagtop) * inputs[2]) + (ampstop[3].lag(lagtop) * inputs[3]);
			envcut = EnvGen.kr( Env.cutoff(1,1,4), gate, doneAction: 2 );
			Out.ar( out, dir * amp.lag(lag) * envcut );
		}).load(s);
		
		SynthDef( \surroundbreathmid, { arg out=0, freq=1, mul=0.5, add=0.5;
			var phases;
			phases = Control.names([\phases]).kr( Array.fill(4, 0.5 ));
			Out.kr( out, SinOsc.kr( freq, phases, mul, add ) );
		}).load(s);
		
		SynthDef( \surroundmove, { arg out=0, ts=1, attack=0.5, decay = 0.5;
			Out.kr( out, Array.fill( 4, EnvGen.kr( Env.perc( attack, decay ), timeScale: ts, doneAction: 2 ) ) );
		}).load(s);
		
		SynthDef( \surroundrandom, { arg out=0, rate=1, ts=1, attack=0.5, decay = 0.5;
			Out.kr( out, Array.fill( 4, { EnvGen.kr( Env.perc( attack, decay ), Dust.kr( rate ), timeScale: ts ) } ) );
		}).load(s);

		SynthDef( \surroundtoptoall, { arg outdir=0, outmid=0, outtop=0, dur=5.0;
			var fade;
			fade = XLine.kr( 0.00001, 1, dur, doneAction: 2 );
			Out.kr( outtop, Array.fill( 4, 0.5 ) );
			Out.kr( outmid, Array.fill( 4, fade ) );
			Out.kr( outdir, Array.fill( 4, fade ) );
		}).load(s);
		
	}
}