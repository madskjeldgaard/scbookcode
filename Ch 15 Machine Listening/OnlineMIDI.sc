//SuperCollider is under the GNU GPL; and so is this class.  
//Nick Collins Oct 2007

//Class to demonstrate a simple interactive music system based on online MIDI note analysis

//Long held notes are not systematically accounted for

//This class doesn't confront the harder problems of symbolic analysis in this context; stream segregation, chord detection, metre tracking and quantisation, style, melodic and harmonic structure etc

//It is meant to provide an accessible example of online analysis of some parameters, ready for responses

//next?:
//100 milliseconds step size? (the fastest rate of human action without chunking). 
//beat tracking/expectancy field: ready for scheduling
//improve key tracking
//look at close notes which are possible chord notes or acciacaturas; gaps of around 50msec or less; must look at local trend and overall spread areas


OnlineMIDI {
	classvar major= #[0,2,4,5,7,9,11];
	classvar minor=#[0,2,3,5,7,8,11];
	//Krumhansl Kessler profiles, normalised	
	classvar kkminor= #[ 0.14221523253202, 0.060211188496967, 0.079083352055718, 0.12087171422152, 0.05841383958661, 0.079308020669512, 0.057065827903842, 0.10671759155246, 0.089418108290272, 0.060435857110762, 0.075039317007414, 0.071219950572905 ];
	classvar kkmajor = #[ 0.15195022732711, 0.053362048336923, 0.083273510409189, 0.055754965302704, 0.10480976310122, 0.097870303900455, 0.060301507537688, 0.12419239052405, 0.057190715482173, 0.087580760947595, 0.054797798516391, 0.068916008614501 ];


	var <data, <window;
	var windowsize, stepsize;
	var managemidinotes; 
	var <>status; 
	var <>routine; 
	var now, then; //time of last analysis and start of that analysis window

	//analysis results:
	var <pitches, <iois, <intervals; //straight data arrays
	var <articulation; //as dutycycle
	var <volumemax, <volumemin, <volumemean, <volumevar;
	var <pitchmax, <pitchmin, <pitchmean, <pitchvar;
	var <intervalmax, <intervalmin, <intervalmean, <intervalvar; //leapiness
	var <density; //notes per window 
	var <keyname, <key; //0-11 major 12-23 minor
	//var <ioipeak1; //max peak in ioi histogram in rhythmic range
	//var <clumpiness; //how spread out are the notes over the window? 
	var <>response; //function called for response
	var <>playinput, <>inputsynthdef; 
	
	*new{
	
	^super.new.initOnlineMIDI();
	}
	
	initOnlineMIDI {
	
		response=nil; //no function to start with
		
		playinput= false; //no input echo to start with
		
		status = true; 
		
		//use to maintain state showing which MIDI notes are currently active
		managemidinotes= Array.fill(128,{nil});
	
		//storing new notes in a time sorted list; older (smaller) times further back in the past
		//the custom sorting function here looks only at the first entry of each note's data, the start time  
		data= SortedList(8, {|a,b| (a[0]) < (b[0])});

			
		MIDIIn.noteOn = { arg src, chan, num, vel;  
			
			if (managemidinotes[num].notNil,{"ERROR: on before off!".postln;});
			
			managemidinotes[num] = [Main.elapsedTime,vel];
			
			//assumes inputsynthdef is a string or symbol, else behaviour undefined
			if(playinput && (inputsynthdef.notNil),{Synth(inputsynthdef, [\freq, num.midicps, \amp, 0.2*(vel/127.0)]) }); 
			
		};
		
		MIDIIn.noteOff = { arg src, chan, num, vel;  
			var duration, starttime;
			 
			if (managemidinotes[num].isNil,{"ERROR: off before on!".postln;});
			
			starttime= managemidinotes[num][0];
			
			vel=managemidinotes[num][1];
			
			duration= (Main.elapsedTime - starttime);
			
			//[starttime, duration, num, vel/127.0].postln;
			
			//velocity converted to a linear 0.0 to 1.0 
			data.add([starttime, duration, num, vel/127.0]); 
			
			managemidinotes[num]=nil;
			
		};
			
	}
	
	//will set up infinite analysis loop
	analyse {|length=3.0, step=1.0|
		var index;
		
		windowsize= length; 
		stepsize= step;
			
		//safety
		if (routine.notNil, {routine.stop;});
		
		routine= {
			
			
			inf.do {
				
				now = Main.elapsedTime;
				then = now- windowsize;
				
				//safety
				//must avoid empty arrays, and because of differences, size 1 also trouble
				if ((data.size)>1,{
				
				index= data.detectIndex({|a| (a[0])>then});
				
				if(index.isNil, {index= data.size});
				
				if((index < (data.size)), {
				//will return a SortedList with wrong sorting function if use data.copyRange
				//window= data.array.copyRange(index, data.size);
				//now fixed in SC source
				window= data.copyRange(index, data.size);
				
				//ANALYSE WINDOW
				this.analyseWindow;
				
				//or redo copy for 1 second further in the past
				//brutal dropping of old events; troublesome in general because you may lose notes whose duration extends into the working window, or which are acciacaturas or spread chord notes 
				
				////FIX ME! NEED remove or similar; not coming back as Sorted List with correct sort function
				//data= window; 
				//data.function= {|a,b| (a[0]) < (b[0])}; //annoying having to keep doing this
				
				response.value(this); //call the response function
				
				//new MIDI data can update the array during the timescale of window analysis, old data dropped at this stage now 
				data.array=data.array.copyRange(index, data.size); 
					
				});	
					
				});
					
				stepsize.wait;
			};
			
		}.fork;
		
	}
	
	//returns max, min, mean, stddev
	getStats {|array|
		var meanval;
		
		//"statting".postln;
		
		meanval= mean(array);
		
		^[minItem(array), maxItem(array), meanval, (sum((array-meanval).squared)/array.size).sqrt ]
		
	}
	
	//data of form [starttime, duration, midi pitch, velocity]
	
	analyseWindow {	
		var pchistogram= Array.fill(12, {0.01}); //set to equal non zero values because of normalizeSum later
		var lasttime, lastpitch, tmp, acount=0; 
		var vols;
		
		//window.postln;
		
		intervals = Array.fill(window.size-1,{});
		iois= Array.fill(window.size-1, {});
		
		articulation=0.0;
		
		//calculating many things at once
		pitches= window.collect {|val,i| var pitch, time, pc;
			
			time= val[0]; 
			pitch= val[2];  
			
			if(lastpitch.notNil, {intervals[i-1]=pitch-lastpitch; lastpitch=pitch;},{lastpitch=pitch;}); 
			
			if (lasttime.notNil, {tmp= time-lasttime; iois[i-1]=tmp; 
			
			//examining dutycycle when greater than 0.05
			if(tmp>0.05, {articulation= articulation + (val[1]/(max(tmp,0.1)));  acount= acount+1;});  
			
			lasttime= time;},{lasttime= time});
			
			pc= pitch%12;
			
			//pc.postln;
			
			pchistogram[pc] = pchistogram[pc]+1;
			
			pitch
		};
		
		//"get vols".postln;
		
		vols= window.collect {|val| val[3]};
		
		//this average will not represent the true state if have one note held all window and various smaller staccatos! 
		//0.8 is default playing mode; average articulation could be longer than note gaps for extreme legato and sustained notes  
		if(acount>0, { articulation = articulation/acount;},{articulation = 0.8;});
		
		density= pitches.size;
		
		#Êkeyname, key = this.findKey(pchistogram.normalizeSum);
		# pitchmax, pitchmin, pitchmean, pitchvar = this.getStats(pitches);
		# volumemax, volumemin, volumemean, volumevar = this.getStats(vols);
		# intervalmax, intervalmin, intervalmean, intervalvar = this.getStats(intervals);
		
		if (status, {this.postStatus});
		
	}
	
	//
	postStatus {
	
		Post << "pitches " << pitches << nl;
		Post << "iois " << iois << nl;
		Post << "intervals " << intervals << nl;
		 
	    [\articulation, articulation, \density, density, \keyname, keyname].postln;
	    [\key, key].postln;
	    [\volumestats, volumemax, volumemin, volumemean, volumevar].postln;
	    [\pitchstats, pitchmax, pitchmin, pitchmean, pitchvar].postln;
	    [\intervalstats, intervalmax, intervalmin, intervalmean, intervalvar].postln;
	    "".postln; 	
	}
	
	
	//not great, particularly on which leading note; should penalise E natural in Fsharp major say! 
	findKey {arg pchistogram;
		var keyscores, tmp, best;
		
		//"key finding".postln;
		
		keyscores=Array.fill(24,{0});
		
		24.do{arg i;
		var testkey, score, results;
		
		testkey= (i.div(2)+(if(i.odd,{major},{minor})))%12;
		
		//naive quick version
		//results=Array.fill(12,{-1});
		//results.put(testkey,1);
		//instead, adjust for Krumhansl Kessler profiles
		results= if(i.odd,{kkmajor},{kkminor});
		
		results= results.rotate(i.div(2));
		
		score=0;
		
		pchistogram.do({arg num,i;
		
		  score=score+(num*(results[i]));
		
		});
		
		keyscores[i]=score;
		};
		
		//highest score wins!
		tmp=0;
		best=(1000.neg);
		
		keyscores.do{arg val,j; if(val>best,{best=val; tmp=j});};
		
	^[\key, (["C","Db","D","Eb","E","F","F#","G","Ab","A","Bb","B"].at(tmp.div(2)))+(if(tmp.odd,"major","minor")),\keyarray,(tmp.div(2)+(if(tmp.odd,{major},{minor})))%12  ];
	}
	
}