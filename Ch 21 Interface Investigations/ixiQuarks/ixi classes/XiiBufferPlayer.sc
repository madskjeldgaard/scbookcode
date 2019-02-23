
XiiBufferPlayer {
	classvar classIDNum;
	var <>xiigui, <>win, params;
	
	var selbPool, poolName, ldSndsGBufferList;
	
	*new {arg server, ch, setting = nil;
		^super.new.initXiiBufferPlayer(server, ch, setting);
	}

	initXiiBufferPlayer {arg server, chnls, setting;

var outmeterl, outmeterr, responder, bufsec, session, quitButt;
var sndfiles;
var tracks; // = 8; // 8 or 16 is IDEAL !
var trigID;
var sliderList;
var bufferList, synthList, stMonoList, argList, globalList;
var rowspace = 110, lowRow = 0, virIndex = 0;
var windowSize;
var soundDir;
var glStartButt, glStopButt, volSlList, panSlList, pitchSlList, startButtList;
var reLoadSndsGBufferList, gBufferPoolNum;
var sfdropDownList, globalVolSlider, tracksButt;
var s, p, point;
var idNum, drawRadioButt, createResponder;

if(classIDNum.isNil, {classIDNum = 50}); // sendtrig id starts with 50
idNum = classIDNum;

tracks = XQ.pref.bufferPlayerTracks; //tracks;
s = server;
sliderList = List.new;
synthList = Array.fill(tracks, nil);
stMonoList = List.new;
argList = List.new;
globalList = Array.fill(tracks, 0);
volSlList = List.new;
panSlList = List.new;
pitchSlList = List.new;
startButtList = List.new;
sfdropDownList = List.new;

bufsec = 1;
gBufferPoolNum = 0;


p = [
Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
Point(24,43), Point(7,43), Point(1,36), Point(1,8)
];


xiigui = nil;
point = if(setting.isNil, {Point(260, 600)}, {setting[1]});
params = if(setting.isNil, {[1, 0, 0, 1]}, {setting[2]});

windowSize = Rect(point.x, point.y, 1015, (tracks/8).round(1)*270);

win = SCWindow.new("multibuffer player", windowSize, resizable:false);
win.drawHook = {
	Color.new255(255, 100, 0).set;
	Pen.width = 3;
	Pen.translate(48,48);
	Pen.scale(0.4,0.4);
	Pen.moveTo(1@7);
	p.do({arg point;
		Pen.lineTo(point+0.5);
	});
	Pen.stroke
};

selbPool = SCPopUpMenu(win, Rect(15, 5, 90, 16))
	.font_(Font("Helvetica", 9))
	.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
	.value_(0)
	.background_(Color.white)
	.action_({ arg item; var outbus;
		reLoadSndsGBufferList.value(selbPool.items[item.value]);
	});


glStartButt = SCButton(win,Rect(15, 110, 41, 18));
glStartButt.states = [["start",Color.black, Color.clear]];
glStartButt.canFocus_(false);
glStartButt.font_(Font("Helvetica", 9));
glStartButt.action = { arg butt;
	startButtList.size.do({arg i; 
		if(globalList[i] == 1, {
			startButtList[i].valueAction_(1);
		});
	});
};

glStopButt = SCButton(win,Rect(61, 110, 41, 18));
glStopButt.states = [["stop",Color.black, Color.clear]];
glStopButt.canFocus_(false);
glStopButt.font_(Font("Helvetica", 9));
glStopButt.action = { arg butt;
	startButtList.size.do({arg i; 
		if(globalList[i] == 1, {
			startButtList[i].valueAction_(0);
		});
	});	
};

globalVolSlider = OSCIISlider.new(win, 
		Rect(15, 165, 80, 10), "- vol", 0, 1.0, 0, 0.0001, \amp)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 	
					volSlList.size.do({arg i; 
						if(globalList[i] == 1, {
							volSlList[i].valueAction_( sl.value);
						});
					});	
				});
	
OSCIISlider.new(win, 
		Rect(15, 195, 80, 10), "- pan", -1.0, 1.0, 0.0, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
					panSlList.size.do({arg i; 
						if(globalList[i] == 1, {
							panSlList[i].valueAction_(sl.value);
						});
					});	
				});
OSCIISlider.new(win, 
		Rect(15, 225, 80, 10), "- pitch", 0, 2.0, 1.0, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 	
					pitchSlList.size.do({arg i; 
						if(globalList[i] == 1, {
							pitchSlList[i].valueAction_(sl.value);
						});
					});	
				});
				
SCStaticText(win, Rect(5, 100, 105, 160))
	.font_(Font("Helvetica", 16))
	.string_("")
	.background_(Color.new255(255, 100, 0, 20));

			
ldSndsGBufferList = {arg argPoolName;
	poolName = argPoolName.asSymbol;
	if( try{ XQ.globalBufferDict.at(poolName)[0] } != nil, {
		sndfiles = Array.fill(XQ.globalBufferDict.at(poolName)[0].size, { arg i;
					XQ.globalBufferDict.at(poolName)[0][i].path.basename});
	}, {
		sndfiles = [];
	});
};

ldSndsGBufferList.value(selbPool.items[0].asSymbol);

tracks.do({ arg i; 
	var trigID, ch, sf, glButt, startPos, endPos;

	trigID = idNum + (i * 2);
	argList.add([0,0,1,0,0]); // volume - pan - pitch - onOff - outbus 

	if((i>0)and:{(i%8)==0}, {lowRow=lowRow+270; virIndex= virIndex-8});

	sliderList.add( // the left volume signal
		outmeterl = SCRangeSlider(win, Rect(120+(virIndex+i*rowspace), 5+lowRow, 20, 100));
		outmeterl.background_(Color.new255(155, 205, 155)).knobColor_(Color.new255(103, 148, 103));
		outmeterl.lo_(0.0).hi_(0.01);
		outmeterl.canFocus_(false);
	);
	sliderList.add( // the right volume signal
		outmeterr = SCRangeSlider(win, Rect(142+(virIndex+i*rowspace), 5+lowRow, 20, 100));
		outmeterr.background_(Color.new255(155, 205, 155)).knobColor_(Color.new255(103, 148, 103));
		outmeterr.lo_(0.0).hi_(0.01);
		outmeterr.canFocus_(false);
	);
	
	stMonoList.add(
		SCStaticText(win, Rect(172+(virIndex+i*rowspace), 65+lowRow, 60, 16))
			.font_(Font("Helvetica", 9))
			.string_("oo");
	);

	SCStaticText(win, Rect(172+(virIndex+i*rowspace), 87+lowRow, 60, 16))
		.font_(Font("Helvetica", 9))
		.string_("global:");
	glButt = SCButton(win,Rect(206+(virIndex+i*rowspace), 89+lowRow, 12, 12));
	glButt.states = [	["",Color.black, Color.clear],
					["",Color.black, Color.new255(155, 205, 155)]];
	glButt.canFocus_(false);
	glButt.action = { arg butt;
		globalList[i] = butt.value;
};

	ch = SCPopUpMenu(win,Rect(120+(virIndex+i*rowspace), 111+lowRow , 50, 16))			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.action_({ arg ch; var outbus;
				outbus = ch.value * 2;
				argList[i][4] = outbus; //  (when synths are created)
				if(synthList[i] !=nil, { synthList[i].set(\out, outbus) });
			});
	
startButtList.add(SCButton(win,Rect(177+(virIndex+i*rowspace), 110+lowRow, 41, 18))
					.states_([	["play",Color.black, Color.clear],
								["stop",Color.black, Color.new255(155, 205, 155)]])
					.font_(Font("Helvetica", 9))
					.action_({ arg butt; var startPos, endPos;
					
					if(butt.value == 1, {
					
		trigID = idNum + (i * 2);
		startPos = XQ.globalBufferDict.at(poolName)[1][sfdropDownList[i].value][0];
		endPos = startPos + XQ.globalBufferDict.at(poolName)[1][sfdropDownList[i].value][1];
		synthList[i] = 
			if(XQ.globalBufferDict.at(poolName)[0][sfdropDownList[i].value].numChannels == 2, {
				Synth.new(\xiiBufPlayerSTEREO, 
					[ \bufnum, XQ.globalBufferDict.at(poolName)[0][sfdropDownList[i].value].bufnum, 
					  \trigID, trigID, // the bus for the gui update
					  \out, argList[i][4],
					  \vol, argList[i][0], 
					  \pan, argList[i][1],
					  \pitch, argList[i][2],
					  \onOff, argList[i][3],
					  \startPos, startPos,
					  \endPos, endPos
					  ], // the default out bus
					  s, \addToHead);
			}, {

				Synth.new(\xiiBufPlayerMONO, 
					[ \bufnum, XQ.globalBufferDict.at(poolName)[0][sfdropDownList[i].value].bufnum, 
					  \trigID, trigID, // the bus for the gui update
					  \out, argList[i][4],
					  \vol, argList[i][0], 
					  \pan, argList[i][1],
					  \pitch, argList[i][2],
					  \onOff, argList[i][3],
					  \startPos, startPos,
					  \endPos, endPos
					  ], // the default out bus
					  s, \addToHead);
			});
		},{
			synthList[i].free;
			synthList[i] = nil;			
		});
		argList[i][3] = butt.value;
		});
	);

sfdropDownList.add(SCPopUpMenu(win,Rect(120+(virIndex+i*rowspace), 135+lowRow , 100, 18))
	.items_(sndfiles)
	.value_(i)
	.font_(Font("Helvetica", 9))
	.background_(Color.white)
	.action_({ arg sf; var startPos, endPos; 
		stMonoList.at(i).string_(
			if(XQ.globalBufferDict.at(poolName)[0].at(sf.value).numChannels == 2, 
				{"stereo"}, {"mono"})
		);		
		synthList.at(i).set(\bufnum, XQ.globalBufferDict.at(poolName)[0][sf.value].bufnum);
		startPos = XQ.globalBufferDict.at(poolName)[1][sf.value][0];
		endPos = startPos + XQ.globalBufferDict.at(poolName)[1][sf.value][1];
	});
);

volSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 165+lowRow, 100, 10), "- vol", 0, 1.0, 0, 0.01, \amp)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; var globalActiveCounter = 0, volAll = 0;
				if(synthList[i] !=nil, { synthList[i].set(\vol, sl.value) });
				argList[i][0] = sl.value;
				tracks.do({arg i;
					if(globalList[i] == 1, {
						globalActiveCounter = globalActiveCounter + 1;
						volAll = volAll + argList[i][0];
						globalVolSlider.value_(volAll/globalActiveCounter);
					})
				});
			})
		);
panSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 195+lowRow, 100, 10), "- pan", -1.0, 1.0, 0.0, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 	
				if(synthList[i] !=nil, { synthList[i].set(\pan, sl.value) });
				argList[i][1] = sl.value;
			})
		);
pitchSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 225+lowRow, 100, 10), "- pitch", 0, 2.0, 1.0, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 	
				if(synthList[i] !=nil, { synthList[i].set(\pitch, sl.value) });
				argList[i][2] = sl.value;
			})
		);
	
	if(try {XQ.globalBufferDict.at(poolName)[0]} != nil, {
		{stMonoList.wrapAt(i).string_(
		if(XQ.globalBufferDict.at(poolName)[0].wrapAt(i).numChannels == 2, {"stereo"}, {"mono"}))
		}.defer;
	});
}); // end of channel loop

		reLoadSndsGBufferList = {arg arggBufferPoolName;
			
			poolName = arggBufferPoolName;
			sndfiles = Array.fill(XQ.globalBufferDict.at(poolName)[0].size, { arg i;
						XQ.globalBufferDict.at(poolName)[0][i].path.basename});
			
			if(try {XQ.globalBufferDict.at(poolName)[0]} != nil, {
				tracks.do({arg i; var startPos, endPos;
					sfdropDownList[i].items = sndfiles;
					sfdropDownList[i].value_(i);
					{stMonoList.at(i).string_(
						if(XQ.globalBufferDict.at(poolName)[0].wrapAt(i).numChannels == 2, 
							{"stereo"}, {"mono"})
						)
					}.defer;
					if(synthList[i] !=nil, { 
						synthList[i].set(\bufnum, 
							XQ.globalBufferDict.at(poolName)[0].wrapAt(i).bufnum);
					});
				});
			});
			
		};
		
		drawRadioButt = OSCIIRadioButton(win, Rect(15,138,14,14), "draw")
					.font_(Font("Helvetica", 9))
					.value_(1)
						.action_({arg val; if(val==1, {
								createResponder.value;
							}, {
								responder.remove;
							})
					});
					
		createResponder = {
			responder = OSCresponderNode(s.addr, '/tr', { arg time, responder, msg;
				{ 
				win.isClosed.not.if({ // if window is not closed, update GUI...
					if((msg[2]-idNum >= 0) && (msg[2] <= (idNum+(tracks*2))), {
						sliderList.at(msg[2]-idNum).hi_(1-(msg[3].ampdb.abs * 0.01)) 
					});
				});
				}.defer;
			}).add;
		};
		createResponder.value;

		win.front;
		win.onClose_({ 
			var t;
			responder.remove;
			synthList.size.do({arg i; synthList[i].free;});
			bufferList = nil;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		
		});
		classIDNum = classIDNum + (tracks*2) + 2; // increase the classvar in case the user opens another bp
	} // end of initXiiBufferPlayer

	updatePoolMenu {
		var pool, poolindex;
		pool = selbPool.items.at(selbPool.value);
		selbPool.items_(XQ.globalBufferDict.keys.asArray);
		poolindex = selbPool.items.indexOf(pool);        
		if(poolindex != nil, {
			selbPool.value_(poolindex);
		});
	}

	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}
	
}
