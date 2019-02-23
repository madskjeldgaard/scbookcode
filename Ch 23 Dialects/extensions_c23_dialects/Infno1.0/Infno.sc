//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//New version; less changes at this level but play functions taken out for now to be reconstructed later

//assumes SynthDefs already in place

//advanced- multiply Infnos at once with mix between capability

Infno {
	var <s, <numtracks;
	var <mastergroup, <group, <fxgroup, <vocalgroup;
	var <groupID; //shortcut for convenience later
	var <bus;
	var <buffer;
	var <clock;
	var mixer, limiter, reverb;
	var masterfx;
	var <trackamp, <trackpan, <trackrev; //, trackfx2;
	var <tempo, <groove;	//groove can be a whole 64 step delay time sequence for complex warping effects
	var <fxlist;
	var <>dynamicprogramming=false;
	var vocal;
	var <lookupclass, <lookupindex; //and tracknames array? 
	var <>lastSeed, <>lastPlay, <>lastArgs, <>lastDyn; //holds random seed/state data for track storage
	
	*new {|s|
	
		^super.new.init(s)
		}
	
	//if not showing straight away
	window {
		vocal.show;
	}
	
	init {|server|
		var condition;
		
		//later use InfnoPerc
		lookupclass=(\kick: InfnoKick, \snare:InfnoSnare, \hat:InfnoHat, \perc:InfnoHat, \perc2:InfnoPerc, \bass:InfnoBass, \lead1:InfnoLead, \lead2:InfnoLead, \lead3:InfnoLead, \chord:InfnoChordal, \monobass:InfnoMonoBass, \monolead1:InfnoMonoLead, \monolead2:InfnoMonoLead, \monolead3:InfnoMonoLead); //(, , \chord:InfnoChord);
		
		//can share bus/locations? 
		lookupindex=(\kick: 0, \snare:1, \hat:2, \perc:3, \perc2:3, \bass:4, \lead1:5, \lead2:6, \lead3:7, \chord:8, \monobass:4, \monolead1:5, \monolead2:6, \monolead3:7); //, \perc2:9
		
		s = server ? Server.default;
	
		mastergroup=Group.new;
		group= Group.head(mastergroup);
		fxgroup= Group.after(group);
		vocalgroup= Group.before(fxgroup);
		
		groupID= group.nodeID;
		
		//WANT TO AVOID TOO MUCH DEPENDENCE ON INSTRUMTNE ORDER; will use search to check for appropriate instrument dependencies?
		//buses
		//percussion will be panned when rendered down, panning can change
		//16 kick
		//17 snare
		//18 hat
		//19 extra perc
		//20 extra perc 2
		//20 bass
		//21 lead1
		//22 lead2
		//23 lead3
		//24 chord
		//25 fx 1 etc
		
		numtracks=10;
		
		//made stereo so reverb rendering also stereo- else would need additional reverb unit for each mono bus pre pan!
		bus= Array.fill(10,{Bus.audio(s,1)});

		tempo=2;
		groove=0.0;
		
		clock= TempoClock(tempo);
	
		condition= Condition.new;
			
		vocal= InfnoVocal(vocalgroup); //vocalbus= Bus.audio(Server.default,1); //bus set up automatically
		
		//vocal.show; //to make window appear
		
		Routine.run({
		
		"loading Buffers".postln;	
		
		this.loadBuffers;
		
		s.sync(condition); //Condition.new could be made automatically
		
		"loaded Buffers".postln;
		
		"preparing FX".postln;	
		
		this.setupMixer;
		
		s.sync(condition);
		
		"ready".postln;	
		
		});
		
	}
	
	stop {
		
		clock.clear;
		
		//to cover case of monophonic sustaining tones; keeps fx going though
		group.freeAll;
		
	}
	
	//free all resources
	free {
	
		this.stop;
		vocal.free;
		mastergroup.free; 
		
		bus.do{|val| val.free;}; 
		buffer.do{|val| val.free;};
	
	}
	
	//will be using samples for lead/bass etc soon
	// /Volumes/data/audio/infno/ on my computer, 
	//made symbolic link ln -s /Volumes/data/audio/infno/ infno
	//in sounds directory so now sounds/infno and can copy infno sounds directory in SC app for making independent
	loadBuffers {
		var num, prefix, filename, basepath;
		
		//number of each type to load
		num= [53,58,22,18,8,11];		//1 kick, 1 snare, 1 hat
		prefix=["kick","snare","hat","perc", "bass", "lead"];
		
		basepath= Infno.filenameSymbol.asString.dirname; 
		//Document.current.path.dirname; //samples directory should be next to this
		
		filename= num.collect{arg val,i; Array.fill(val, {arg j; basepath++"/infnosamples/"++prefix[i]++"/"++prefix[i]++((j+1).asString)++".wav"})};

		//always mono for now, later could have a stereo option, but simplest if keep all rendering mono to begin with
		buffer= filename.collect{arg val; val.collect{arg fname; Buffer.read(s,fname,1)}; };
	
	}
	
	
	play {|which ... args|
	
		lastSeed = thisThread.randData;
		lastPlay= which;
		lastArgs= args;
		lastDyn= dynamicprogramming; 
		
		this.performList(which, args);
	}
	
	getLast {
	
	^[lastSeed, lastPlay, lastArgs, lastDyn];
	
	}
	
	playOld {|which|
	
		if((which.isNil) && (lastSeed.notNil),{
		
		dynamicprogramming = lastDyn; 
		thisThread.randData_(lastSeed);
		this.performList(lastPlay, lastArgs);
		},{ //play specified
		
		dynamicprogramming = which[3]; 
		thisThread.randData_(which[0]);
		lastSeed= which[0]; lastPlay= which[1]; lastArgs= which[2];
		this.performList(which[1], which[2]);
		});
		
	}
		
	//more code than before...	
	demo1 {|creationlist, inputtempo=2.2|
		var activity;
		//var template; //array of templates for hypermeasures- structral data for harmony, metre and rhythmic seeds
		var hypermeasure; //array of hypermeasures
		var mix; //array of mixes
		var variants; 
		
		tempo= inputtempo; //rrand(1.9,rrand(2.7,3));
		groove= if(tempo<2.5,{if(0.5.coin,{rrand(0.05,0.1)*(tempo.reciprocal)},{0.0})},{0.0});
		clock.tempo_(tempo);
	
	//	template= hypermeasure = Array.fill(1, {arg i; 
//		("making template"+(i.asString)).postln;
//		
//		//high level structural data
//		InfnoTemplate(2.2, dynamicprogramming);
//		
//		});
	
		//generalise somewhat later? 
		//would be defaults
		//creationlist= [\kick, \snare, \hat, \perc, \bass]; //[, \perc, , \lead1, \lead2, \lead3, \chord];
		 
		activity= 0.5; //mid level of activity
	
		//these act as master hypermeasures
		hypermeasure = Array.fill(1, {arg i; 
		("making hypermeasure"+(i.asString)).postln;
		
		//create materials from these directives, in order of instrumental parts given by creationlist
		InfnoHyperMeasure(this,creationlist, activity);
		
		});
		
		//can calculate differential of current activity and new activity to have a guide function...
		//variants = Array.fill(2,{var hm; hm= hypermeasure[0].deepCopy; if(0.5.coin,{hm.compose(creationlist, activity)},{hm.subtlevariation(activity)})});
	
		variants = Array.fill(2,{var hm; hm= hypermeasure[0].deepCopy;});
	
		//variants[0].postln;
		//over time need a structure for which parts play, and which hypermeasure
		
		//single Mix object, later will be multiple
		mix= InfnoMix(this);
		
		mix.setupBuses(this);
		
		
		this.addFX;
		
		
		//now render
		//won't always want to play percussion part = 3 in particular

		{
		
		1.0.wait;
		
		4.do{
		variants[0].play(this,mix);
		16.wait;
		};
		4.do{
		variants[1].play(this,mix); //, [0,1,2,3,4]
		16.wait;
		};
		}.fork(clock)
	
	}
	
	popTrack {|whichform, withintro, inputtempo|
	
		this.synthpop(whichform, withintro, inputtempo); 
	
	}


//single track, making an arrangement
	//pop structure? riff, verse, chorus, [riff], verse, chorus, bridge, riff, verse, chorus, chorus
	//riff, verse, riff, verse, chorus, verse, chorus, bridge, chorus, chorus, riff 
	synthpop {|whichform, withintro, inputtempo|
		var creationlist;
		var arrangement;
		var instrumentation,perc,rhythmsection,trance,strippeddown1,chords,instrumentationoptions;
		var mix, hypermeasure, activity=1.0;
		var fillchance, fillchance2; 
		var bridge, doublebridge=0.5.coin;
		var versen=64, chorusn= 64, bridgen=64, riffn=64, intron=64; 
		var dochorusmix, chorusmix; 
		var whichpattern; 
		
		//thisThread.randData.postln;
		
		//pixies like shorter choruses, also shorter bridge sections
		//rare to start with
		if(0.2.coin,{
		if(0.01.coin,{versen= [32,48,56].wchoose([0.1,0.8,0.1])}); 
		if(0.025.coin,{chorusn= [32,48,56].wchoose([0.1,0.8,0.1])}); 
		if(0.025.coin,{bridgen= [32,48].wchoose([0.9,0.1])}); 
		if(0.05.coin,{riffn= 32;}); 
		if(0.05.coin,{intron= [32,48].wchoose([0.9,0.1])}); 
		}); 
		
		bridge= 0.6.coin; 
		
		//[\bridge,bridge].postln;
		
		whichform= whichform ? {2.rand};
		
		fillchance= [0,rrand(0.0,0.4),rrand(0.0,1.0),1.0].wchoose([0.2,0.2,0.5,0.1]);
		fillchance2= fillchance*[rrand(0.0,1.0),rrand(1.0,2.0),1.0.rand].choose; 
		
		tempo= inputtempo ?? {rrand(1.8,rrand(2.6,3))};
		groove= if(tempo<2.5,{if(0.5.coin,{rrand(0.05,0.1)*(tempo.reciprocal)},{0.0})},{0.0});
		clock.tempo_(tempo);

		//choose groove amount based on clock tempo chosen
		
		//these chosen first since they may have a big bearing on decisions for material- if high speed, need to be careful with 16 offbeats

		//verse, chorus, bridge
		hypermeasure = Array.fill(7, {arg i; 
		("making hypermeasure"+(i.asString)).postln;
		
		creationlist=([\kick, \snare, \hat, [\perc, \perc2].choose, [\bass,\monobass].wchoose([0.9,0.1]),\chord]++Array.fill(3,{|i| (([\lead,\monolead].wchoose([0.9,0.1]))++(i+1)).asSymbol})).scramble;
		
		//is there a way to force the bridge to be more harmonically adventurous? 
		//seeing topdown choice of form impacting on low level decisions! 
		InfnoHyperMeasure(this,creationlist, activity);
		
		});
	
		//over time need a structure for which parts play, and which hypermeasure
		
		//single Mix object, later will be multiple
		mix= InfnoMix(this);
		
		mix.setupBuses(this);
		
		dochorusmix= 0.3.coin; 
		if(dochorusmix, {chorusmix= InfnoMix(this);}); 
		
		//now render
	
		//could have end mode 8= just play first beat of pattern as last chord stab
		//other contrived ends- rit
		//simpler- could have riff using verse but with more lead lines, verse less busy
		arrangement= [[0,1,2,1,2,3,0,1,2,2],
		[4,1,4,1,2,1,2,3,2,2,4]
		].at(whichform.value); 
		
		withintro= withintro ?? {0.3.coin};
		
		if(withintro,{arrangement= [5]++arrangement;}); 

		this.addFX;
		
		//fills are 64 index blocks, usually with fill only in last segment; 
		//want fill only for rhythmic parts? 
		
		
		perc= [0,1,2,3];
		rhythmsection=[0,1,2,3,4];
		strippeddown1=[[0,2,4],[0,1,4],[3.rand,4]];
		trance=[[4,8],[4,5,8],[2,5,8]];
		chords=[8];
		
		instrumentationoptions=[
		{ //intro riff presentation
			rhythmsection++Array.fill(rrand(1,3),{|i| i+5})++chords
		}];
		
		//riff length = [2,4].choose
		//have 4 for now, and use same materials as verse 

		{
			if(dynamicprogramming,6.0,1.0).wait;
		
			arrangement.do{arg section,j;
			
		switch(section,
			0,{
			"RIFF!".postln;
				//1 for thematic unity
				this.playHyperMeasureN(2, hypermeasure[1],mix, instrumentationoptions[0].value, fillchance2.coin, riffn);
			},
			4,{
			"extra RIFF!".postln;
				//own riff
				this.playHyperMeasureN(2, hypermeasure[0],mix, instrumentationoptions[0].value, fillchance2.coin, riffn);
			},
			1,{
			"VERSE!".postln;
				if(0.3.coin && (j>2),{
				instrumentation= [strippeddown1,trance].choose.choose;
				}, { //most of the time
				instrumentation = rhythmsection; 
				
				if(0.2.coin,{instrumentation= instrumentation++[rrand(5,6)];});
				if(0.4.coin,{instrumentation= instrumentation++[8];});
				});
				
				//reduce arrangement for first repeat
				
				hypermeasure[1].variation(hypermeasure[4], [rrand(0.0,0.5),rrand(0.0,0.1)].choose);
				this.playHyperMeasureN(1,hypermeasure[1],mix,instrumentation, fillchance2.coin, versen);
				
				hypermeasure[1].variation(hypermeasure[4], [rrand(0.0,0.5),rrand(0.0,0.1)].choose);
				this.playHyperMeasureN(1,hypermeasure[1],mix,instrumentation, fillchance2.coin, versen);
				
				//instrumentation = rhythmsection ++[rrand(5,7)];
				if(0.8.coin,{instrumentation= instrumentation++[rrand(5,6)];}); //hold back use of 7
				if(0.6.coin,{instrumentation= instrumentation++[8];});
				
				if(bridge,{
				
				if(0.5.coin,{instrumentation= instrumentation++[0,2];});
				if(0.3.coin,{instrumentation= instrumentation++[7];});
				
				"BRIDGE!".postln;
				hypermeasure[5].variation(hypermeasure[1], [rrand(0.0,0.5),rrand(0.0,0.1)].choose);
				this.playHyperMeasureN(1, hypermeasure[5],mix, instrumentation, fillchance.coin, bridgen);
				
				if(doublebridge,{
				hypermeasure[6].variation(hypermeasure[5], [rrand(0.0,1.0),rrand(0.4,1.0)].choose);
				this.playHyperMeasureN(1, hypermeasure[6],mix, instrumentation, fillchance.coin, bridgen);
				}); 
				
				},{
				hypermeasure[1].variation(hypermeasure[4], [rrand(0.0,0.5),rrand(0.0,0.1)].choose);
				this.playHyperMeasureN(2, hypermeasure[1],mix, instrumentation, fillchance.coin, versen);
				});
				
			},
			2,{
			if(dochorusmix, {chorusmix.setupBuses(this);}); 
		
			"CHORUS!".postln;
				instrumentation = rhythmsection ++[5,6,7];
				if(0.8.coin,{instrumentation= instrumentation++[8];});
				
				this.playHyperMeasureN(2, hypermeasure[2],if(dochorusmix,{chorusmix},{mix}), instrumentation, fillchance.coin, chorusn);
		
				if(dochorusmix, {mix.setupBuses(this);}); 
		
			},
			3,{
			"MIDDLE8!".postln;
				if(0.3.coin,{
				instrumentation= [perc,trance].wchoose([0.2,0.8]).choose;
				}, { 
				instrumentation = rhythmsection ++[5,6,7];
				if(0.8.coin,{instrumentation= instrumentation++[8];});
				});
			
				if(0.5.coin,{
				this.playHyperMeasureN(2,hypermeasure[3],mix, instrumentation,fillchance2.coin, [intron, bridgen].choose);},{
				this.playHyperMeasureN(1,hypermeasure[3],mix, instrumentation, (fillchance*0.5).coin, [intron, bridgen].choose);
				this.playHyperMeasureN(1,hypermeasure[4],mix, instrumentation, fillchance.coin, [intron, bridgen].choose);
				});
			},
			5,{
			
			whichpattern = [3,4,5,6].choose;
			"INTRO!".postln;
				
				instrumentation = [8];
				
				if(0.6.coin,{instrumentation= instrumentation++[rrand(5,7)];});
				
				if(0.5.coin,{instrumentation= instrumentation++[4];});
				
				if(0.3.coin,{instrumentation= instrumentation++[2];});
				
				if(0.3.coin,{instrumentation= instrumentation++[3];});
				 
				if(0.1.coin,{instrumentation= instrumentation++[0];}); 
			
				this.playHyperMeasureN(rrand(1,2),hypermeasure[whichpattern],mix, instrumentation,false, intron);
				
			}
			);
			
			};
			
		}.fork(clock);

			
	}
	

//novel song structure- chooses ordering of 3-8 sections with various arrangements and even changes of mix
	novelForm {arg inputtempo; 
	
		var arrangement,instrumentation, numsections,nummixes, numinstrumentations;
		var temp;
		var currentmix=0;
		var creationlist;
		var perc,rhythmsection,trance,strippeddown1,chords;
		var mix, hypermeasure, activity=1.0;
		var steps, rearrange,stepsizes, length;
		
		tempo= inputtempo ?? {rrand(1.8,rrand(2.6,3.1))};
		groove= if(tempo<2.5,{if(0.5.coin,{rrand(0.05,0.1)*(tempo.reciprocal)},{0.0})},{0.0});
		clock.tempo_(tempo);

		//song sections
		numsections= rrand(3,rrand(3,8));
		
		hypermeasure = Array.fill(numsections, {arg i; 
		("making hypermeasure"+(i.asString)).postln;
		creationlist=([\kick, \snare, \hat, [\perc, \perc2].choose, [\bass,\monobass].wchoose([0.8,0.2]),\chord]++Array.fill(3,{|i| (([\lead,\monolead].wchoose([0.9,0.1]))++(i+1)).asSymbol})).scramble;
		InfnoHyperMeasure(this,creationlist, activity);
		});
	
		//over time need a structure for which parts play, and which hypermeasure
		
		//multiple Mix objects
		nummixes= rrand(1,rrand(1,7));
		mix= Array.fill(nummixes, {InfnoMix(this)}); 
		mix[0].setupBuses(this);
		
		//now render
	
		numinstrumentations= rrand(4,14);
		//from sparse to fuller? 
		
		
		perc= [0,1,2,3];
		rhythmsection=[0,1,2,3,4];
		strippeddown1=[[0,2,4],[0,1,4],[3.rand,4]];
		trance=[[4,8],[4,5,8],[2,5,8]];
		chords=[8];
	
		
		instrumentation= Array.fill(numinstrumentations,{var onoff;  
		
		onoff=[];
		
		if(0.9.coin,{onoff= onoff++([perc,perc.scramble.at((0..rrand(1,2)))].choose)});
		
		if(0.8.coin,{onoff= onoff++[4]});
		
		if(0.8.coin,{onoff= onoff++([5,6,7].at((0..rrand(0,2))))});
		
		if(0.8.coin,{onoff= onoff++chords});
		
		if(0.4.coin,{onoff= (0..8)});
		
		if(0.3.coin,{onoff= rhythmsection++Array.fill(rrand(1,3),{|i| i+5})++chords});
		
		if((onoff.isEmpty) || (0.05.coin),{onoff= [perc,strippeddown1.choose,trance.choose].choose;});
		
		onoff});
		
		
		stepsizes=Array.fill(numsections,{
	
			if (0.8.coin,64,{
			[rrand(2,5)*4,16,32,48,rrand(15,24)*2].wchoose([0.1,0.2,0.5,0.1,0.1]);
			});
		
		});
		
		//need to check for repeats? generate and test schema, only a memory of one
		temp=111;
		
		arrangement=Array.fill(rrand(5,43),{ var nextsection; nextsection=numsections.rand; while({nextsection==temp}, {nextsection= numsections.rand;});  temp=nextsection; [nextsection, nummixes.rand, numinstrumentations.rand]});
		
		this.addFX;

		//riff length = [2,4].choose
		//have 4 for now, and use same materials as verse 

		{
			"WAITING IN PREPARATION".postln;
			if(dynamicprogramming,8.0,2.0).wait;
			
			arrangement.do{arg section;
			
			mix[section[1]].setupBuses(this);
			
			steps= stepsizes[section[0]];
		
			
				//shoudl put into separate generation code
			//0.2 about right!
			if(0.2.coin,{
			
			rearrange=(0..(steps-1));
			
			switch(2.rand,
			0,{ //roll at end
			//temp= rrand(steps-1-rrand(1,7), steps-2);
			temp= rrand((steps-rrand(8,16)).max(0), steps-2);
			
			rearrange[(temp..(steps-1))]=temp;
			},
			1,{ //permute blocks of 2 to others nearby
			
			rearrange= Array.fill(steps.div(2),{arg i; temp= rrand(max(i-1,0), min(i+1,steps.div(2)-1)); [2*temp,2*temp+1]}).flatten;
			});
			//more ideas- permute off to on, random stutters throughout- chance of retaining previous after on SQ 
			
			//rearrange.postln;
			},{rearrange=nil});
		
		
			length= steps*0.25;
			
			rrand(1,2).do {
			
			hypermeasure[section[0]].play(this,mix[section[1]], instrumentation[section[2]], steps,rearrange);
			
			
			length.wait;
			
			};
			
			};
		}.fork(clock);

			
	}




	techno {|numsections, inputtempo|
		var arrangement,instrumentation,nummixes, numinstrumentations;
		var temp;
		var currentmix=0;
		var creationlist;
		var perc,rhythmsection,trance,strippeddown1,chords;
		var mix, hypermeasure, activity=1.0;
		var steps, rearrange,stepsizes, length;
		var numrepeats, repeat; 
		var instrwalk=2, instrchance=0.2, instrlast;
		var tmp,tmp2; 
		
		numsections= numsections ?? {rrand(1,4)};
		
		if(numsections<2, {numsections==2}); //because of while loop later 
		
		InfnoKick.fourfloorchance = 0.8;
		
		tempo= inputtempo ?? {rrand(2,rrand(2.5,3.3))};
		groove= if(tempo<2.5,{if(0.3.coin,{rrand(0.05,0.1)*(tempo.reciprocal)},{0.0})},{0.0});
		clock.tempo_(tempo);

		//song sections
		//numsections= rrand(3,rrand(3,8));
		
		hypermeasure = Array.fill(numsections, {arg i; 
		("making hypermeasure"+(i.asString)).postln;
		creationlist=([\kick, \snare, \hat, [\perc, \perc2].choose, [\bass,\monobass].wchoose([0.9,0.1]),\chord]++Array.fill(3,{|i| (([\lead,\monolead].wchoose([0.9,0.1]))++(i+1)).asSymbol})).scramble;
		InfnoHyperMeasure(this,creationlist, activity);
		});
	
		//over time need a structure for which parts play, and which hypermeasure
		
		//multiple Mix objects
		nummixes= [1,2,3].wchoose([0.7,0.2,0.1]); //rrand(1,rrand(1,7));
		mix= Array.fill(nummixes, {InfnoMix(this)}); 
		mix[0].setupBuses(this);
		
		//now render
	
		numinstrumentations= rrand(4,24);
		//from sparse to fuller? 
		
		//removed immutables since had problems with remove later
		perc= [0,1,2,3];
		rhythmsection=[0,1,2,3,4];
		strippeddown1=[[0,2,4],[0,1,4],[3.rand,4]];
		trance=[[4,8],[4,5,8],[2,5,8]];
		chords=[8];
	
		
		instrumentation= Array.fill(numinstrumentations,{var onoff;  
		
		onoff=[];
		
		if(0.95.coin,{onoff= onoff++([perc,perc.scramble.at((0..rrand(1,2)))].choose)});
		
		if(0.8.coin,{onoff= onoff++[4]});
		
		if(0.5.coin,{onoff= rhythmsection});
		
		if(0.6.coin,{onoff= onoff++([5,6,7].at((0..rrand(0,2))))});
		
		if(0.5.coin,{onoff= onoff++chords});
		
		if(0.1.coin,{onoff= (0..8)});
		
		if(0.2.coin,{onoff= rhythmsection++Array.fill(rrand(1,3),{|i| i+5})++chords});
		
		if((onoff.isEmpty) || (0.05.coin),{onoff= [perc,strippeddown1.choose,trance.choose].choose;});
		
		onoff});
		
		
		stepsizes=Array.fill(numsections,{
	
			//if (0.8.coin,64,{
			//[rrand(2,5)*4,16,32,48,rrand(15,24)*2].wchoose([0.1,0.2,0.5,0.1,0.1]);
			//});
			
			if (0.8.coin,32,{[16,64].choose});
		
		});
		
		//need to check for repeats? generate and test schema, only a memory of one
		temp=numsections.rand; //111;
		repeat=0;
		instrlast=instrumentation.choose; //numinstrumentations.rand;
		
		//arrangement=Array.fill(rrand(5,43),{ var nextsection; nextsection=numsections.rand; while({nextsection==temp}, {nextsection= numsections.rand;});  temp=nextsection; [nextsection, nummixes.rand, numinstrumentations.rand]}); 
		
		//alter existing instrumentation by one voice for layer in and out
		
		arrangement=Array.fill(rrand(5,43),{	 var nextsection; 
			
			nextsection=temp;
			
			//increasingly specialist if higher numbers
			if((repeat*nextsection*0.125).coin,{nextsection=numsections.rand; });
			
			if(nextsection==temp, {repeat=repeat+1},{repeat=1;});  
			
			if(instrchance.coin,{
			instrwalk=rrand(1,4);
			});
			
			if(instrwalk>0,{
				instrwalk= instrwalk-1;
				
				tmp= (0..8); 

				//[\tmp, tmp, \instrlast, instrlast].postln;

				instrlast.do{|val| tmp.remove(val)};
				
				if(tmp.isEmpty,{tmp=(0..8);}); 
				
				tmp = tmp.scramble;
				
				//[\tmp, tmp, \instrlast, instrlast].postln;
								
				//alter instrlast from last time by addition or removal
				if((instrlast.size>8) || ((instrlast.size>3) && (0.2.coin)),{
				
				//[\instlasttest, instrlast, instrlast.class].postln;
				
				instrlast.remove(instrlast.choose)
				},{instrlast= instrlast++(tmp.choose)});
				
				//instrumentation[numinstrumentations.rand]
			}, {instrlast= instrumentation.choose}); 
			
			//[\instrlast, instrlast].postln;
			
			temp=nextsection; [nextsection, [0,nummixes.rand].wchoose([0.9,0.1]), instrlast]
		});
		
		this.addFX;
		
		if(0.5.coin,{
		this.addglitchFX;
		});

		//riff length = [2,4].choose
		//have 4 for now, and use same materials as verse 

		{
			"WAITING IN PREPARATION".postln;
			if(dynamicprogramming,8.0,2.0).wait;
			
			arrangement.do{arg section;
			
			mix[section[1]].setupBuses(this);
			
			steps= stepsizes[section[0]];
		
			
				//shoudl put into separate generation code
			//0.2 about right!
			if(0.2.coin,{
			
			rearrange=(0..(steps-1));
			
			switch(2.rand,
			0,{ //roll at end
			//temp= rrand(steps-1-rrand(1,7), steps-2);
			temp= rrand((steps-rrand(8,16)).max(0), steps-2);
			
			rearrange[(temp..(steps-1))]=temp;
			},
			1,{ //permute blocks of 2 to others nearby
			
			rearrange= Array.fill(steps.div(2),{arg i; temp= rrand(max(i-1,0), min(i+1,steps.div(2)-1)); [2*temp,2*temp+1]}).flatten;
			});
			//more ideas- permute off to on, random stutters throughout- chance of retaining previous after on SQ 
			
			//rearrange.postln;
			},{rearrange=nil});
		
		
			length= steps*0.25;
			
			numrepeats= [2,4].choose;
			
			if (steps== 16, {numrepeats=[1,2].choose}); 
			if (steps== 64, {numrepeats=[1,2].choose});
			
			numrepeats.do {
			
			//instrumentation[section[2]]
			hypermeasure[section[0]].play(this,mix[section[1]], section[2], steps,rearrange);
			
			
			length.wait;
			
			};
			
			};
		}.fork(clock);
	
	InfnoKick.fourfloorchance = 0.2;		
	
	}

	
	
	playClubSet {
	
	
	}
	
	//keep going and going - amortised calculations?
	playInf {
	
	
	}
	
	
	//convenience function- repeat whole HyperMeasureN times
	playHyperMeasureN {|n, hm, mix, which, fill=false, steps=64|
	
		
		n.do{|i|
		
		if(fill && (i==(n-1)),{
		hm.fill(this,mix,which,steps);
		},
		{
		hm.play(this,mix,which,steps);
		});
		
		//16.wait;
		(steps*0.25).wait;	
		}
		
	}
	
	//makes SynthDef now, may add lags in later and gentle close etc.
	setupMixer {
	
		trackamp= Array.fill(numtracks,{Bus.control(s,1)});
		trackpan= Array.fill(numtracks,{Bus.control(s,1)});
		trackrev= Array.fill(numtracks,{Bus.control(s,1)});
		//trackfx2= Array.fill(numtracks,{Bus.control(s,1)});
		
		mixer= SynthDef(\infnoMixer,{|revamount=0.9| 
			var drymix, reverb, revmix;
			var vocals;
			
			drymix= 0.0;
			drymix= Array.fill(numtracks,{arg i; Pan2.ar(In.kr(trackamp[i].index,1)*In.ar(bus[i].index,1),In.kr(trackpan[i].index,1)) });
		
			revmix= drymix*Array.fill(numtracks, {arg i; In.kr(trackrev[i].index,1)});
		
			revmix= FreeVerb.ar(Mix(revmix), 1.0);
		
			//was going too loud and pushing limiter, reduced overall amp
			
			//add back in Pan2.ar(In.ar(vocal.bus.index,1),0.0) + 
			Out.ar(0,Limiter.ar(LeakDC.ar(Pan2.ar(In.ar(vocal.bus.index,1),0.0) + (0.5*(Mix(drymix)+(revamount*revmix)))),0.99,0.01));
		}).play(fxgroup,nil,\addToTail);
		
		//can't have Limiter here once start adding more FX since required to go after any masterfx
	
		//values will be set by InfnoMix object
	
	}
	
	//add FX unit to head of fxgroup, processing by ReplaceOut or Out the contents of bus[i]
	//reverse allocate- to melody parts first then to hat, snare, then bass, then kick last
	//not more than two on any one?	actually worked out via instructions for each
	addFX {
		var delay, tmp;		
		var fxchance, fxnum;
		var index; 
		var weightsnow;
		
		//kill previous
		if(fxlist.notNil,{fxlist.do{arg val; val.free; }});
	
		fxchance=[0.05,0.2,0.3,0.4,0.1,0.5,0.5,0.5, 0.5,0.5];
		fxnum=[1,1,2,2, 1,2,2,2, 2,2];
		
		//allowedfx= get weights for particular sounds
		
		fxlist= List[];
		
		fxnum.do{arg val, i;  
		
		//[\comb, \resonz, \bitchrunch, \allpass, \distort, \am, \ringmod, \delay, \flange, \filter]
		//no delay for kick, snare or bass
		weightsnow= if((i<2) || (i==4),{
		#[0.0,0.1,0.2,0.1,0.09,0.1,0.2,0.01,0.01,0.19];},{
		#[0.25,0.2,0.2,0.1,0.05,0.05,0.05,0.05,0.05,0.0];
		
		});
		
		index= bus[i].index;
		
		val.do{arg k;
			if(fxchance[i].coin,{
				
			//[i,k].postln;	
				
		fxlist.add(SynthDef(\infnoFX++(2*i+k),{  //|revamount=0.9| 
			var in, processed;
			
			in= In.ar(index);
			
			processed= [
			{
			
			tmp= exprand(0.5,5);
			delay= tempo.reciprocal*(if (groove<0.01,{[0.25,0.5,1].wchoose([0.3,0.6,0.1])},{[0.5,1].wchoose([0.7,0.3])}));
			
			//"combn".postln;
			
			//compensate for volume increase via Limiter? 
			//Limiter.ar(,0.99,0.005)
			
			CombN.ar(in, delay, delay, tmp, [0.8,rrand(0.1,0.8),exprand(0.05,0.8)].choose, if(0.5.coin,{in},0.0));
			},
			{
			var range, centre;
			
			//"limiterresonz".postln;
			
			centre= [2000,1000,3000,5000].choose;
			range= ([250,500,1000,1500,2000,centre].choose).min(centre-100);
			
			//2000, 2050
			//limiter for safety
			Limiter.ar(Resonz.ar(in*([4,8].choose), SinOsc.kr([tempo*[2,1,0.5,0.25,0.125].choose,0].choose, pi.rand, range, centre), 0.4),0.99,0.005)},
			{
			
			//"bitchrunch".postln;
			
			in.round(0.5**([8,6,rrand(5,10),rrand(3,7)].wchoose([0.4,0.2,0.2,0.2])))
			},
			{
			tmp=in;
			
			//"allpass".postln;
			
			//lower bound control needed, was 0.05.rand 
			rrand(3,8).do{tmp= AllpassL.ar(tmp,0.05,rrand(0.005,0.05), rrand(0.5,3))};
			tmp 
			
			},
			//distort
			{
			var lvl, drive, ampcompensation;
			var distortion; 
			//"distort".postln;
			
			distortion= if(0.2.coin,{
			lvl= [1, rrand(0.0,1.0)].choose;
			ampcompensation= 1.0-(0.9*lvl);
			
			if(in>0.0,lvl-in,(lvl.neg)-in)
			
			},{
			
			drive=rrand(1.0,40.0);
			//drive= 4.6742153167725.postln; 
			lvl = abs(in);
			ampcompensation= ((drive.max(10)-10)).neg.dbamp;
						
			//ampcompensation.postln;

			(in*(lvl + drive)/((in.squared) + ((drive - 1) * lvl)  + 1));
		
			//Limiter.ar(ampcompensation*(in*(lvl + drive)/((in ** 2) + ((drive - 1) * lvl)  + 1)), 0.99,0.01);
//			
			});
			
			Limiter.ar(ampcompensation*distortion,0.99,0.001);
			
			//signal rounding/clip distortion? 
			
			//in
			},
			{//AM at rhythmic rates
			
			//"am".postln;
			
			tmp= tempo* if (groove<0.01,{[8,4,2,1].wchoose([0.15,0.4,0.4,0.05])},{[1,2,0.5].wchoose([0.45,0.45,0.1])});
			
			if(0.5.coin,{in*SinOsc.ar(tmp,[0,pi*0.5].choose,0.5,0.5)},{in*LFSaw.ar(tmp,[0,pi*0.5].choose,0.5,0.5)});
			
			},
			{//ring mod + low pass
			//"ringmodlpf".postln;
			
			tmp= [rrand(0,15),rrand(15,45),exprand(40,400),exprand(100,1000)].choose;
			
			LPF.ar(in*SinOsc.ar(tmp),[1000,4000,10000].choose);
			},
			{
			//"delayn".postln;
			
			delay= tempo.reciprocal*(if (groove<0.01,{[0.25,0.5,1].wchoose([0.3,0.6,0.1])},{[0.5,1].wchoose([0.7,0.3])}));
			
			//don't lose original signal! 
			DelayN.ar(in, delay, delay, [1,rrand(0.1,1.0),exprand(0.05,1.0)].choose, in)
			},
				//flange
			{
			var delayTime;
		
			//"flange".postln;
			
			delay = 0.025;
			tmp= ((2**(rand(-7,-4))))*(tempo.reciprocal)*(if (groove<0.01,{[0.25,0.5,1].wchoose([0.3,0.6,0.1])},{[0.5,1].wchoose([0.7,0.3])}));
			
			//tempo*[0.25,0.125,0.33, rrand(0.05,0.2)].choose
			delayTime = SinOsc.kr(tmp,
			mul: delay/2  *([0.5, rrand(0.1,0.8)].choose), 
			add: delay/2); 
					
	 		//Limiter.ar(,0.99,0.005);
	 		CombC.ar(in,delay,delayTime,[0.5,exprand(0.25,2)].choose, [1,rrand(0.1,1.0),exprand(0.05,1.0)].choose, if(0.2.coin,{in},0.0))
	 		
			},
			//filter
			{var filtfreq;
			
			//"filter".postln;
			
			filtfreq= [1000,10000,exprand(1000,10000)].wchoose([0.2,0.2,0.6]);
			
			if(0.5.coin,{
			[LPF,HPF].choose.ar(in,filtfreq);
			},{
			//rq
			tmp= [exprand(0.01,1.0),0.5,rrand(0.01,0.5)].choose;
			[BPF,BRF,RLPF].choose.ar(in,filtfreq,tmp, (5*(1-tmp))+1)
			});
			
			}
			].wchoose(weightsnow).value;   //[0.4,0.2,0.2,0.1,0.05,0.05]
			
			//processed.postln; //for debug
			
			ReplaceOut.ar(index,processed);
		}).play(fxgroup,nil,\addToHead));
			
			
			});
		
		};
		
		};
		
	
		
		
	
	}
	
	
	//time locked bit chrunch modifications
	//more extreme filters
	//rhythmic delays
	//distortions
	//pitchshift
	//self ring mod + amp compensation
	//self ring mod + delay + compression CompB
	
	//deliberately add fx in quest of raw glitch alternative dsp sound
	addglitchFX {
		var delay, tmp;		
		var fxchance, fxnum;
		var index; 
		var weightsnow;
		
		//don't kill previous
		//if(fxlist.notNil,{fxlist.do{arg val; val.free; }});
	
		fxchance=[0.05,0.7,0.95,0.95, 0.1,0.95,0.95,0.95, 0.5,0.5]; //Array.fill(10,{1.0}); //rrand(0.3,0.7)//[0.5,0.2,0.3,0.4,0.1,0.5,0.5,0.5, 0.5,0.5];
		
		//was [1,2]
		fxnum=Array.fill(10,{[0,1].wchoose([0.8,0.2])}); //rrand(1,2)//[1,1,2,2, 1,2,2,2, 2,2];
		
		//allowedfx= get weights for particular sounds
		
		//fxlist= List[];
		
		fxnum.do{arg val, i;  
		
		//[\comb, \resonz, \bitchrunch, \allpass, \distort, \am, \ringmod, \delay, \flange, \filter]
		//no delay for kick, snare or bass
		//weightsnow= if((i<2) || (i==4),{
//		#[0.0,0.1,0.2,0.1,0.09,0.1,0.2,0.01,0.01,0.19];},{
//		#[0.25,0.2,0.2,0.1,0.05,0.05,0.05,0.05,0.05,0.0];
//		
//		});
		
		//was 10
		//weightsnow= Array.rand(8,0.01,1.0).normalizeSum;
		
		//[\moog, \res, \bitchrunch,\glitch, \ringmoddelay, \ps, \misc, \filter]
		
		weightsnow= [
		[0,0,1,1,0,0,1,0]/3.0, //kick
		[1,1,1,1,0,0,1,1]/6.0, //snare
		[1,1,1,1,0,0,1,1]/6.0, //hat
		[1,1,1,1,0,0,1,1]/6.0, //perc
		[0,0,1,1,0,1,1,0]/4.0, //bass
		[1,1,0,1,1,1,1,1]/7.0, //lead1
		[1,1,0,1,1,1,1,1]/7.0, //lead2
		[1,1,0,1,1,1,1,1]/7.0, //lead3
		[1,1,0,0,1,1,1,1]/6.0, //chords
		[1,1,0,0,1,1,1,1]/6.0, //other
		][i];
		
		index= bus[i].index;
		
		val.do{arg k;
			if(fxchance[i].coin,{
				
			//[i,k].postln;	
				
		fxlist.add(SynthDef(\infnoGlitchFX++(2*i+k),{  //|revamount=0.9| 
			var in, processed;
			
			in= In.ar(index);
			
			processed= [
		{
		var gain;
		
		gain = rrand(0.1,3);
//"moog".postln;
MoogFF.ar(in,exprand(100,2000),gain, gain.max(1.0).reciprocal)
},
			{
			var range, centre;
			
			//"limiterresonz".postln;
			
			centre= [2000,1000,3000,5000].choose;
			range= ([250,500,1000,1500,2000,centre].choose).min(centre-100);
			
			//2000, 2050
			//limiter for safety
			//Limiter.ar(,0.99,0.005)
			//*([4,8].choose)
			
			(([Resonz,RLPF, RHPF].choose).ar(in,centre,range/centre))
			
			},
			{
			var minbits, maxbits, rate, range, centre; 
			//"bitchrunch".postln;
			
			minbits = [8,6,rrand(5,10),rrand(3,7)].wchoose([0.4,0.2,0.2,0.2]);
			maxbits= min(minbits*2,16);
			
			centre= 0.5*(maxbits+minbits);
			range= maxbits-centre;
			
			rate= tempo* if(groove<0.01,{[2,1,0.75,0.5,0.25].choose},{[0.5,1,2,4].choose});
			
			in.round(0.5**([SinOsc,LFSaw,LFCub].choose.kr(rate, [0,pi*0.5,pi].choose, range, centre)));
			},
			//distort
			{
			in*[{Dust.ar(exprand(10,1000),0.5,0.5)},{LFNoise0.kr(exprand(1,100),0.5,0.5)},{Decay.ar((in>0.1),0.05,0.25)}].choose;
			
			},
			{//ring mod + low pass
			//"ringmodlpf".postln;
			
			//tmp= [rrand(0,15),rrand(15,45),exprand(40,400),exprand(100,1000)].choose;
			//SinOsc.ar(tmp)
			
			delay= tempo.reciprocal*(if (groove<0.01,{[0.25,0.5,1].wchoose([0.3,0.6,0.1])},{[0.5,1].wchoose([0.7,0.3])}));
			
			Limiter.ar(LPF.ar(in*DelayN.ar(in,delay, delay)*2,[1000,4000,10000].choose),0.99,0.01);
			},
			{//pitchshift
			
			//"ps".postln;
			
			PitchShift.ar(in, exprand(0.005,0.1),[0.25,0.5,1,2,4,rrand(0.5,2),exprand(0.25,4)].choose,[0,rrand(0.0,0.1)].choose,[0,exprand(0.001,0.05)].choose);
			},
				//flange
			{
			//"here".postln;
			
			tmp = [{in.distort},{
			
			//"clip2".postln;
			in.clip2(rrand(0.01,0.25))},{
			//"compander".postln;
			CompanderD.ar(2*in*in,0.1,1,0.25)},{
			
			//"resample".postln;
			in*Latch.ar(Impulse.ar(SampleRate.ir * (2**(5.rand))))}].choose;
			
			tmp
			},
			//filter
			{var filtfreq;
			
			//"filter".postln;
			
			filtfreq= [1000,10000,exprand(1000,10000)].wchoose([0.2,0.2,0.6]);
			
			if(0.5.coin,{
			[LPF,HPF].choose.ar(in,filtfreq);
			},{
			//rq
			tmp= [exprand(0.01,1.0),0.5,rrand(0.01,0.5)].choose;
			Limiter.ar([BPF,BRF,RLPF].choose.ar(in,filtfreq,tmp, (5*(1-tmp))+1),0.99,0.01);
			});
			
			}
			].wchoose(weightsnow).value;   //[0.4,0.2,0.2,0.1,0.05,0.05]
			
			//processed.postln; //for debug
			
			ReplaceOut.ar(index,processed);
		}).play(fxgroup,nil,\addToHead));
			
			
			});
		
		};
		
		};
		
	}
	
	
}