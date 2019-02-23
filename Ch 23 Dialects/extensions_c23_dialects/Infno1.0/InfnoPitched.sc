//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//set amount of lag when initially create synth? 
//can set lags as you go, but triggers to reset envelope; synth always hangs around? 
//leave lags for now

InfnoPitched : InfnoInstrument {

var <pitches; //, <lagties;
var <>basenote; 

//used by InfnoMonoBass and InfnoMonoLead	
var <lags;
var <defaulttimings;
var <predelay;	

	*new {
		^super.new.initInfnoPitched; 
	}

initInfnoPitched {
		
		basenote=0;
		pitches = Array.fill(64,{arg i; 60}); 
		
		//pitches = nil; //Array.fill(64,{arg i; nil}); 
		
		//lagties=nil;
	}


	//for using lagties- uses set as necessary, but would have to use alternative synthdefs that aren't released and have gates... 
	play {|infno, index, mix,n=64, indexing| 
		var latency;
		var synthdef, busnum, bufnum;
		//var args=[];
		//var synth;
		
		busnum=infno.bus[index].index;
		synthdef=mix.synthdef[index];
		bufnum= mix.bufnum[index]; //assumes sample based, but not necessarily used
		
		//[\busnum, busnum, \synthdef, synthdef, \bufnum, bufnum].postln;
//		sixteenths.postln;
//		pitches.postln;
//		
		{
			n.do{arg j; 
			
				var val, i;
				
				i= if(indexing.notNil, {indexing[j]},{j}); //allows stutters and permutations
				
				//i.postln;
				
				val= sixteenths[i];
				
				 if(val>0.5,{
				 
					 //this remains based on j, because indexing can't override it
					latency= infno.s.latency+ if(j%2==1,infno.groove, 0.0);
					
					infno.s.sendBundle(latency, ["/s_new", synthdef, -1, 0, infno.groupID, \out, busnum, \bufnum, bufnum, \note, pitches[i]+basenote]) //++args  //\lag, 0.05, 
				}); 0.25.wait}
		}.fork(infno.clock);
	
	}
	
	//monoplay
	
	
		//[ioi, instruction, originalindex] //use originalindex to look up pitch etc
	//could predelay or just set at time required	
	precalculatelagtimings {arg pattern, tempo;
	var where, tmp;
	var lagtimes, lastevent;
	//need distance in seconds between actual events
	
	where=List();
	
	pattern.do{|val,i|   if((val>0.5),{where.add(i)});};
	
	//where contains indices of lag instruction points to be adjusted in time
	
	lastevent=0.0;
	
	lagtimes= where.collect{|val,i| var lagnow, adjustment, alloweddist, requireddist, last, now, newevent; lagnow= lags[val];  
	
	now=if(((lagnow)<0) || (i==0),{[val*0.25,-1,val]},{
	
	last= where[i-1];
	alloweddist= (val-last)*0.25;
	
	//convert to beats
	requireddist= if(predelay,{lagnow*tempo},{0.0});
	
	//minimum gap of a demisemiquaver could be instituted
	adjustment= min(requireddist,alloweddist); //-0.125
	
	[((val*0.25)- adjustment),lagnow,val]
	
	});
	
	newevent=now[0];
	
	now[0]= now[0]- lastevent; //make ioi
	
	lastevent=newevent;
	
	now
	};	
	
	^lagtimes;	
	}
	
	
	
	//or call through to common play function in base class 
	
		//for using lagties- uses set as necessary, but would have to use alternative synthdefs that aren't released and have gates... 
	monoplay {|infno, index, mix,n=64, indexing| 
		var latency;
		var synthdef, busnum, bufnum;
		var nodeID;
		var data;
		
		indexing= indexing ?? {(0..(n-1))};
		
		busnum=infno.bus[index].index;
		
		//if (this.class==InfnoMonoBass) would differentiate it!  actually just use alternatives...will work for lead too!
		synthdef=mix.altsynthdef[index];//this is where it differs from the standard bass
		
		bufnum= mix.bufnum[index]; //assumes sample based, but not necessarily used
		
		//CORRECTED FOR n not 64 
		//first in array is wait till synth initialise; last is wait until release
		data=if((indexing.isNil) && (n==64),{defaulttimings},{this.precalculatelagtimings(sixteenths.at(indexing).copyRange(0,(n-1)), infno.tempo)  });
		
		//"monoplay test".postln;
		//n.postln;
		
		//Post << data << nl; 
		
		
		//[\synthdef, synthdef, \index, index].postln;
		
		{
		
		data.do {|val,i|
		var lagtype, pitch;
		
		val[0].wait;
		
		lagtype= val[1];
		pitch=pitches[val[2]];
		
		//this remains based on j, because indexing can't override it
		latency= infno.s.latency+ if((val[2])%2==1,infno.groove, 0.0);
					
		if(i==0,{
		
		//first note created
		nodeID= infno.s.nextNodeID;
		infno.s.sendBundle(latency, ["/s_new", synthdef, nodeID, 0, infno.groupID, \out, busnum, \bufnum, bufnum, \note, pitch+basenote]);
		
		},{
		
		//set message
		
		if(lagtype<0,{ //retrigger
		
		infno.s.sendBundle(latency, ["/n_set", nodeID, \lagtime, 0.0, \t_trigger, 1, \note, pitch+basenote]);

		},{ //set lag
		
		infno.s.sendBundle(latency, ["/n_set", nodeID, \lagtime, lagtype, \note, pitch+basenote]);
		
		});
		
		});
		
		//release instruction; 	if last add 0.25 for release message trigger of -1.2 gives 0.2 sec release
		if(i==(data.size-1),{
		infno.s.sendBundle(latency+0.25+(if(lagtype<0,0,lagtype)), ["/n_set", nodeID, \t_trigger, -1.2]);
		
		});
		
		}
		
		}.fork(infno.clock);
	
	}



}