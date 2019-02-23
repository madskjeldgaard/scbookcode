//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//abstract base class 
//simple sample playback from 64 step sequencer
//took out pitches, lagties, will add in a pitched instrument base class

InfnoInstrument {
//classvar g_n=128, g_bars=8; //doubling standard size created
var <sixteenths; //,<pitches, <lagties; //don't use pitches if nil!  lag tie if not nil
var <originalsixteenths; //,<pitches, <lagties; //don't use pitches if nil!  lag tie if not nil

	*new {//|harmony, rhythm|
	
		^super.new.init; //(harmony, rhythm)
		}
	
//minimal creation
init {//|harmony, rhythm|
		
		sixteenths = Array.fill(64,{arg i; 0.0}); 
		
		//pitches = nil; //Array.fill(64,{arg i; nil}); 
		
		//lagties=nil;
	}

	crossover {|other, chance=0.02|
		
		if(originalsixteenths.isNil,{originalsixteenths= sixteenths;}); 
		
		sixteenths= originalsixteenths.collect{|val,j| if(chance.coin,{other.sixteenths[j]},val)};
		
		//Post << [\crossover, chance, sixteenths, originalsixteenths, sixteenths-originalsixteenths]<< nl; 
		
	}


	play {|infno, index, mix,n, indexing| 
		var latency;
		var synthdef, busnum, bufnum;
		//var args=[];
		
		busnum=infno.bus[index].index;
		synthdef=mix.synthdef[index];
		bufnum= mix.bufnum[index]; //assumes sample based, but not necessarily used
		
		
		//[\busnum, busnum, \synthdef, synthdef, \bufnum, bufnum].postln;
		
		//sixteenths.postln;
		
		{
			n.do{arg j; 
			
				var val, i;
				
				i= if(indexing.notNil, {indexing[j]},{j}); //allows stutters and permutations
				
				//i.postln;
				
				val= sixteenths[i];
				
				//if val is array, could have separate code for roll [number in 0.25 would do]
				
				 if(val>0.5,{
					 //this remains based on j, because indexing can't override it
					latency= infno.s.latency+ if(j%2==1,infno.groove, 0.0);
					
					infno.s.sendBundle(latency, ["/s_new", synthdef, -1, 0, infno.groupID, \out, busnum, \bufnum, bufnum]); //\note, pitches[i]++args
				}); 0.25.wait}
		}.fork(infno.clock);
	
	}
	
	
	/////////STANDARD on off step sequence rhythm generation methods
	//standard creation functions
	
	//pass in list of indices of possible influencing parts
	//common chance of avoiding straight loops 
	
	//order could also specify; follow/oppose and also allow for aggregates and probabilities? 
	makeSixteenths {|infno, rhythm, parts, additional|

		var bar, alternative, temp; 
		
		//bias[0..15]
		//rhythm.aggregate, infno.tempo
		bar= this.makeBar(infno, rhythm, parts, (0..15), additional);
		
		//one probability for each bar for an alternative to a straight loop
		alternative= [0.1,0.2,0.1,0.7];
		
		sixteenths= Array.fill(4, {|i| 
		
		temp=bar.copy;
		
		if(alternative[i].coin,{temp= this.makeBar(infno, rhythm, parts,((i*16)..(i*16+15)), additional);});
		
		//could call barVariation here for variety
		temp
		}).flatten;
		
		//this.filterAwkward1;
		
	}
	
	
	//depending on which parts there already; may return nil if no matches! 
	getRelations {|parts, relations, indices|
	
		var bias, temp, temp2;
	
		indices= indices ?? {(0..16)};
	
		//oppose from first once that is true
		//could also do an aggregate of all true ones
		block {|break|
			relations.do {|val|
				var which=val[0];
								
				if(which.size==0,{
					
					if (parts[which].notNil,{
					
					bias= parts[which].sixteenths.at(indices);
					
					if(val[1].coin,{bias= 1-bias;});
					
					break.value;
					});
					
				},{
					//...could do | (or) of parts, except don't know order of creation so have to check which are there 
					//take aggregate
					temp= false;
					temp2= Array.fill(64,0.0);
					
					//default to false and switch to true if at least one part is there! 
					which.do{|i| if(parts[i].notNil,{temp2= temp2+ (parts[i].sixteenths); temp=true;})};
					//{temp=false;} //otherwise would default to true and switch off if anything missing
					
					if(temp,{
					
					bias= temp2.min(1).at(indices);
					
					//[2,0,1,1,1,0,3].min(1)
					
					if(val[1].coin,{bias= 1-bias;});
					
					break.value;
					});
					
				});
		
			}
		};
		
		//CAN RETURN nil! 
	//if(bias.isNil,{bias=Array.fill(64,0.0);});
		
	^bias;
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	//over ride in each instrument; this is the makeBasicBar method from previously
	//makeBar {|agg, tempo, oppose|
	//
	//}
	
	//tweak sixteenths in one bar for params
	//barVariation {
	
	//}
	
	//call makeFillBar for appropriate subbar of 4 bar pattern 
	fill {
	
	}
	
	//simple variation (though not very sensitive to context)
	//should be different probabilities for different metrical layers- ie less for 1/16 phase offset
	onoffflip {|on=0.1,off=0.3|
	
	sixteenths= sixteenths.collect {|val| if(val>0.5,{if(on.coin,0,1)},{if(off.coin,1,0)})};
	
	}
	
	
	
	
	//filter pattern to avoid 16th note single offsets
	filterawkward1 {|pattern|  
		var tmp, tmp2;
		var halfsize;
		
		halfsize= pattern.size.div(2);

		//Array.fill(16,{|i| if(i.even, 1, 0)})  Array.fill(16,{|i| if(i.odd, 1, 0)})
		//tmp= (pattern & (#[1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0 ])).sum;
		//tmp2= (pattern & (#[ 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1 ])).sum;
		
		//& only works if these are integers; may need to revise later! 
		tmp= (pattern & (Array.fill(pattern.size,{|i| if(i.even, 1, 0)}))).sum;
		tmp2= (pattern & (Array.fill(pattern.size,{|i| if(i.odd, 1, 0)}))).sum;
		
		//cure; either reduce tmp2 out or increase tmp
		//get troublesome indices and 
		if(tmp2>tmp, {
		
		(tmp2-tmp).do{ pattern[2*((halfsize).rand)]=1;}; //quick hack, won't necessarily cure it! 
		
		}); 

		//look out for THREE bad offsets in a row; correct by adding in a 1 n even step near first two
		tmp=[0,0]; //Array.fill(2,0); //fixed since only need two at a time?//List();
		tmp2=0;
		pattern.do {|val, i|  
		
		if((i.even) && (val>0.5),{tmp2=0; }); //tmp=List();
		 
		if((i.odd) && (val>0.5),{tmp2=tmp2+1; 
		
		if(tmp2>2,{
		
		pattern[(tmp.choose)+([-1,1].choose)]=1;
		
		tmp2=1; 
		
		//tmp=List();
		
		}); 
		
		tmp[tmp2-1]=i; //tmp.add(i);
		});
		
		};
	
		^pattern;
		
	}
	

}