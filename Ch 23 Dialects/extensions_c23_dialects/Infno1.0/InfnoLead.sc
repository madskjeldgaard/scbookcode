//This file is part of Infno. Copyright (C) 2007  Nick M.Collins. Distributed under the terms of the GNU General Public License.

//some similiarities to InfnoBass
//but with dynamic programming and online selection of pitches from harmony

//further algorithm- choose from harmony, smooth out preferring close movements and occasional leaps?
//add loop condition to make sure it wraps round nicely? 
//extra sweep to concatenate repeat notes in a row

//other lets a counter melody be generated- penalty for same pitch as counter + alternative rhythm

//set dynamic programming/search parameters 

InfnoLead : InfnoPitched {
	
	//<iois, <times;
	//var <>basenote;

	*new {|infno, harmony, rhythm, parts, index|
		
		var basenote = [60,60,72][index];
		
		^super.new.basenote_(basenote).initLead(infno, harmony, rhythm, parts, index)
		}
	
	//can have alternative functions here later
	initLead {|infno, harmony, rhythm, parts, index|
	
	//work out the other - even call back to infno if necessary! 
	var other; 
	
	//other can be nil
	other = switch (index,
	0, {if(0.4.coin, {parts[4]},nil)},
	1, {parts[5]},
	2, {parts[rrand(4,6)]});

	//[\seedtest2, thisThread.randData].postln;

	this.makeLead1(infno, harmony, rhythm, parts, other);
	
	}
	
	//follow structuring of bass line version
	makeBasic {|parts,indices|
		var basic;
		var allevents;
		var temp;
		
		basic= Array.fill(16,{arg i;  [rrand(0.8,1.0), rrand(0.0,0.4), rrand(0.5,1.0), rrand(0.0,0.7)][i%4]});
		
		allevents= this.getRelations(parts, [[[0,1,2,3],0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{1.0})});
		
		//could add a weight control basic= basic + (weight*addition);
		basic = basic + ([
		{ //follow snare, hat or perc
		allevents= this.getRelations(parts, [[rrand(1,3),0]], indices);
		//could be nil if nothing composed yet! 
		
		if(allevents.isNil,{allevents= Array.fill(16,{arg i; if(i%2==0, 1, 0)});});
		
		allevents
		},
		{
		allevents;
		},
		{	//antirhythm - check not all 1! 

		temp=allevents.sum;
		
		if(temp>15.5,{
		allevents= [Array.fill(8,{[0.0,0.2.rand].choose}),Array.fill(8,{1.0.rand})].lace(16);
		});
		
		1-allevents;
		},
		{
		Array.fill(16,{arg i; if(i%4==0, 1, 0)});
		},
		{
		Array.fill(16,{arg i; if(i%4==2, 1, 0)});
		},
		{
		Array.fill(16,{arg i; if(i%4==3, 1, 0)});
		}
	
		].choose.value);
		
		basic= basic.normalizeSum;
		
		^basic;
	}


	makeLead1 {|infno, harmony, rhythm, parts, other|
	
		var basic, basic2, activity, tmp, indices;
	
		indices= (0..15);
		
		//can have more calls than slots, perfectly safe
		activity= [rrand(2,6), rrand(3,10), rrand(5,19)].wchoose([0.4,0.4,0.2]);
	
		if((other.notNil) && (0.5.coin),{
		//create in opposition to other line
		
		activity= [(activity*2)/64, ((64-(other.sixteenths.sum))*rrand(0.2,0.6))/64.0].choose; //probability of note
		
		sixteenths = other.sixteenths.collect({arg val, i; if(val<0.5, {
		
		if(activity.coin, 1, 0)
		
		},0); }); 
		
		},
		{	
		basic= this.makeBasic(parts,(0..15));	
		
		//one way if don't normalize so much sixteenths = Array.fill(64,{arg i; if(basic[i%16].coin,1,0)});
		
		sixteenths = Array.fill(64,{arg i; 0});  //if 0.0 then fails 
		
		//first three bars, activity notes a bar? 
		3.do{arg j; activity.do{tmp= indices.wchoose(basic); sixteenths[j*16+tmp]=1}; };
		
		//change activity?
		if(0.4.coin,{activity= [rrand(2,7), rrand(3,12), rrand(6,23)].wchoose([0.3,0.4,0.3]);});
		
		//different for final bar
		basic2= this.makeBasic(parts,(48..63));	
		
		activity.do{tmp= indices.wchoose(basic2); sixteenths[48+tmp]=1}; 
		});
		
	//	"pattern test in LEAD".postln;
//		sixteenths.do{|val| val.class.postln};
//		
		//use safety feature to avoid too much in the way of awkward offsets?
		if(0.95.coin,{ //was 0.4.coin
		sixteenths= this.filterawkward1(sixteenths);
		});
		
		
		//tmp = harmony.get16thChord;
		//pitches = Array.fill(64,{arg i; if(sixteenths[i]>0.5, {tmp[i].choose}, 0)}); 
		
		this.dynamicprogramming1(harmony, other, infno.dynamicprogramming);
		
		//leave for now
		//ioi representation and hence tuplets are problematic for groove factor 
		//make tuplet: find x in a row, substitute tuplet over their iois
		
	
	}


	//Aug 18 added parameters for each run which can be changed...

	//five point contours following melodic similiarity literature suggestion?
	//generate pitches - see workbook Feb 23rd 2007
	
	//for each of next 24, select best path from previous 24 to that point
	dynamicprogramming1 {|harmony, other, dynprog=true|
	
		var chords;
		var low, high, mid; //range
		var chordnotes, num;
		var contour, contour2; //swapchance, contour, direction;
		var oldscore, newscore; 
		var oldpath, newpath;
		var notepenaltybase, nowpenalty;
		var which, minscore, minindex, prevnote; 
		var minscore2, minindex2;
		var cost, contourcost;
		var centre, changecontourchance;
		var tmp, tmp2;
		var loopnumber;
		var costsarray, basescale, scales, scaleindex,scalenow, allowedcentres, costweights, repetitioncosts; 
		var dynprogchoicenoise;
		
		//pitches = Array.fill(64,{0.0});
		
		//parameters for each run:
		
		//costs 0=chromatic 1=diatonic 2,3,4=chordtone based on metrical position 2=minim 3=crotchet 4=other 
		//5= repetition cost multiplier 6=tritone step cost 7=cost noise 8=cost from previous multiplier
		//9=being on another's notes cost
		costsarray=[
		rrand(8.0,25.0),rrand(4.0,10.0),rrand(-1.0,0.5).max(0.0),rrand(-0.5,2.5).max(0.0),rrand(1.0,4.0),
		[6,rrand(1.0,10.0),rrand(3.0,8.0), 0].wchoose([0.4,0.3,0.27,0.03]), //repetition cost mult
		rrand(0.0,20.0), //tritone cost
		[2.0.rand,exprand(0.01,3.0),5.0.rand].wchoose([0.8,0.1,0.1]), //cost noise
		[rrand(0.0,0.5),rrand(0.5,1.5),exprand(0.01,2.0)].wchoose([0.1,0.8,0.1]), //cost from previous
		rrand(0.0,10.0)]; 
		
		//prior defaults [[15,6,3,2,0, 6, 10, 2.0, 0.5, 5]].choose; //wchoose([]).value;
		basescale= [{[0,2,4,5,7,9,11, 12, 14, 16, 17, 19, 21, 23]}, {[0,2,3,5,7,8,11, 12, 14, 15, 17, 19, 20, 23]},{var tmp; tmp=1.neg; (Array.fill(rrand(10,14),{tmp=tmp+rrand(1,3); tmp})%24).asSet.asArray.sort}].wchoose([0.95,0.04,0.01]).value;
		//can add scale generating functions
		
		allowedcentres=[{[12,19,7, 16, 9].wchoose([0.4, 0.2,0.1,0.2,0.1])},{Array.fill(5,{24.rand}).wchoose([0.4, 0.2,0.1,0.2,0.1])}].wchoose([0.9,0.1]); //stores function to use later
		dynprogchoicenoise=[0,rrand(0.0,1.0),exprand(0.01,5.0)].wchoose([0.9,0.07,0.03]); //allows selection of secondbest path etc
		//costweights=Array.fill(3,{rrand(0.5,1.5)});		
		//transition, contour, note
		costweights=Array.fill(3,{rrand(0.1,1.1)});	
		
		//falling lines caused by transition cost
		//if set transition to zero just get held notes a lot of the time
		//costweights= [0.0,1.0,1.0]; 
		
		repetitioncosts=Array.fill(4,{|i| costsarray[5]*(i+1)}); //convenience
		
		//2 octave range, could extend later
		//mid=0;
		//low=12.neg;
		//high=12;
		
		//maximum penalty chromatic
		notepenaltybase = Array.fill(24, {costsarray[0]});
		
		//penalty diatonic underlying everything; more complicated schema now where use best match scale for each chord 
		if(0.2.coin,{
		notepenaltybase.put(basescale,costsarray[1]);
		});
	
		scales= harmony.getScales;
	
	
		//add on distance from midline penalty (might only apply some moves)
		
		//+((abs(i-12)*0.25)) +((abs(i-12)))
		//midline penalty superseeded by contour
		//if(0.1.coin,{
//		notepenaltybase= notepenaltybase.collect{arg val, i; val};
//		});
		
	
		chords = harmony.get16thChord;
		
		num= sixteenths.sum;
		pitches = Array.fill(num,{0.0}); //initialised only for dynprog
		
		//direction=0;
		
		//could get weights from a database of melody lines
		//done as chromatic for now - step of tone or leap of fifth up or fourth down
		//contour2= Array.fill(num, {[0,2,-2,7,-5].wchoose([0.35,0.25,0.25,0.07,0.08])});
		
		//dangerous because an abrupt leap? may interact weirdly with other factors?
		changecontourchance = [rrand(2.0,4.0)/num,rrand(2.5,8.0)/num,exprand(0.05,0.9),rrand(0.5,0.9)].wchoose([0.5,0.1,0.3,0.1]); 
		
		//better to have absolute contour- skeleton of tones, distance calculated from these over time?
		//centre=[12,19,7, 5,4, 17,16, 24,0].wchoose([0.4, 0.1, 0.1,  0.075, 0.075,  0.075, 0.075,  0.05,0.05]);
		
		contour= Array.fill(num, {arg i; if(i==0 || (changecontourchance.coin), {centre=allowedcentres.value;
		});  centre;}); 
		
		//contour.postln;
		
		loopnumber= if(dynprog,24,1);
		
		//variation in variables and path calculation as needed
		if(dynprog,{
		//work out for every chromatic
		oldscore= Array.fill(24,{0.0}); //Array.fill(24,{arg i; abs((i-12)) });
		newscore= Array.fill(24,{0.0});
		
		//start empty
		oldpath= Array.fill(24,{List[]});//Array.fill(num,{Array.fill(24)});
		newpath= Array.fill(24,{});
		},{
		oldpath= Array.fill(1,{pitches}); //trick to keep a reference Array.fill(num,{0.0});
		
		});
		
		which= 0;
		
		//Post << chords << nl;
		
		sixteenths.do{arg val, i;  if(sixteenths[i]>0.5, {
			
			//next chord decision
			chordnotes= chords[i];
			
			//chords[i].postln;
			
			
			chordnotes= (chordnotes) ++ (chordnotes+12); //allowed over two octaves
			
			
			//notepenaltybase.postln;
			
			nowpenalty= notepenaltybase.copy;
			
			//chordnotes.postln;
			//nowpenalty.postln;
			
			//use best matching scale to set up penalty
			scaleindex= scales[1][i];
			scalenow= scales[0][scaleindex]; //only in range 0 to 11 at this point
			scalenow = scalenow++(scalenow+12);
			nowpenalty.put(scalenow,costsarray[1]);
			
				//penalty reduction for chord tones based on how important metrically
			//tmp = if(i%4==0,{if(i%8==0,6,4)},{2});
			tmp = if(i%4==0,{if(i%8==0,0,2)},{3}); //was 4 by default
			
			//tmp = if(i%4==0,{if(i%8==0,costsarray[2],costsarray[3])},{costsarray[4]}); //was 4 by default
		
			chordnotes.do{arg val; nowpenalty[val]= tmp; };
				
				//avoid same note at same time
			//extended to other notes this beat
			if(other.notNil, {
			tmp=i.div(4)*4;
			
			4.do{arg k; tmp2= (other.pitches[tmp+k])%12; if (other.sixteenths[tmp+k]>0.5, {
			
			nowpenalty[tmp2]= nowpenalty[tmp2]+(costsarray[9]); 
			nowpenalty[tmp2+12]= nowpenalty[tmp2+12]+(costsarray[9]); //cover two octaves! 
			
			}); };
			
			});
			
			//chordnotes.do{arg val; nowpenalty[val]= nowpenalty[val]-tmp};
		
			//nowpenalty.postln;
			//"".postln;
			
			
			//calculate best paths TO each chromatic taking into account
			//1. nowpenalty
			//2. distance to previous
			//3. contour penalty
			
			
			minscore2= 10000;
			minindex2=0;
			prevnote= if(which==0,{24.rand},{pitches[which-1]});
		
			//new path is to j, from k; if not full dynamic programming, only one k! 
			24.do {arg j;
				
				minscore= 10000;
				minindex=0;
				
				//over all 24 active paths if dynprog, else just running best
				loopnumber.do{arg k; 
					
					//"here".postln;
					//which.postln;
					
					if(dynprog,{
					prevnote= if(which==0, {k}, {oldpath[k].last});
					cost= oldscore[k];},{
					//prevnote=if(which==0,{24.rand},{pitches[which-1]});
					cost=0.0;
					});
					
					//always same step cost; should vary it? 
					//this is the line which tends to contribute most to avoid leaps etc and have downwards falls
					cost=cost + ((costsarray[8])*(abs(prevnote-j))); //1.0* for non dyn prog previous
					
					//can improve by calculating as diatonic
					//contourcost = abs((j-prevnote)-contour2[which]);
					//cost= cost+(contourcost);
					//cost.postln;
					
					//fixed penalty for same note as last time, plus some small random noise?
					//shoudl really go up if keep choosing same note - increasing novelty requirement
					
					if(j==prevnote, {cost=cost+(repetitioncosts[0])});
					if(abs(j-prevnote)==6, {cost=cost+(costsarray[6])}); //tritone
					
					//increasing penalty for repetition
					if(which>1, {if(j==(oldpath[k][which-2]), {cost=cost+(repetitioncosts[1]);}) });
					if(which>2, {if(j==(oldpath[k][which-3]), {cost=cost+(repetitioncosts[2]);}) });
					if(which>3, {if(j==(oldpath[k][which-4]), {cost=cost+(repetitioncosts[3]);}) });
					
					//["here",cost].postln;
					
					cost=cost+rrand(0.0,costsarray[7]); //1.0
					
					//favours low k since they will be tested first? 
					if(cost<minscore,{minscore=cost; minindex= k;});
					
				};
				
				//cost for being at state j, independent of previous
				contourcost= abs(j-(contour[which])); 
				
				cost=(costweights[0]*minscore)+(costweights[1]*contourcost) + (costweights[2]*nowpenalty[j]);
				
				if(dynprog,{
				newpath[j]=oldpath[minindex].copy.add(j);
				//oldscore[minindex]+ 
				newscore[j]= cost; //minscore+ contourcost;
				}, {
				
				if(cost<minscore2,{minscore2=cost; minindex2= j;});
			
				});
				
			};
			
			if(dynprog,{
			oldpath= newpath;
			oldscore=newscore;
			},{
			
			//oldpath[0].postln;
			pitches[which]= minindex2;
			//oldpath[0][which]=minindex2; //auto update! 
			});
			
			which=which+1;
					
		}) };
	
		if(dynprog,{   //could add randomisation addition to score here to allow selection of second best!
		minscore= 10000;
		minindex=0;
		
		//noise in final choice to allow second best or worst path choice
		oldscore= oldscore+Array.fill(oldscore.size,{dynprogchoicenoise.rand});
		
		oldscore.do{arg cost, j; 
			if(cost<minscore,{minscore=cost; minindex= j;});
			};
			
		//get from winner
		pitches= oldpath[minindex];
		});
		
		which=1.neg;
		pitches = Array.fill(64,{arg i; if(sixteenths[i]>0.5, {which=which+1; pitches[which]}, 0)}); 
		
	}
	

}