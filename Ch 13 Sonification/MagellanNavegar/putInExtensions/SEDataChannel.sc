/*
	Filename: SEDataChannel.sc 
	created: 23.8.2005 

	Copyright (C) IEM 2005 
		Alberto deCampo [decampo@iem.at]
		Christopher Frauenberger [frauenberger@iem.at] 

	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation; either version 2 
	of the License, or (at your option) any later version. 

	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
	GNU General Public License for more details. 

	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 

	IEM - Institute of Electronic Music and Acoustics, Graz 
	Inffeldgasse 10/3, 8010 Graz, Austria 
	http://iem.at

	SonEnvir project
	http://sonenvir.at
	
	$LastChangedDate$
	$Rev$
*/

/*****************
	Root class for all types of data channels
*/
SEDataChan {
	
	// name of the channel
	var <>name;
		
	// the data as array of objects
	var <data;
	
	// the size of the data channel
	var <size, <>units;
	
	// does the data contain nil values?
	var <numNil;

	// statistical properties
	var <min, <max, <avg, <stdDev, <fillRatio;
	
	var <>spec;		// a controlspec for scaling this datachan to [0, 1];
	var <properties;	// a dict for keeping data properties.
	
	*new { |name, data| 
		^super.newCopyArgs(name, data).init
	} 
	init { 
		size = data.size;
		numNil = data.occurrencesOf(nil);
		properties = ();
	}
	// access method
	at { arg index;
		^data.at(index)
	}
	copyRange { |start, end|
		^data.copyRange(start, end)
	}
	// analysis method for numerical data which may contain
	// non-numbers, missing values etc. 
	analyse {			
		var sum = 0.0, count=0, stdDevSum = 0;
		
		min = max = data.detect({ |el| el.isNumber }); 
		
		data.do({ |el| 
			if (el.isNumber, { 
				min = min(min, el); 
				max = max(max, el); 
				sum = sum + el;
				count = count + 1;
			});
		}); 
		avg = if (count > 0, { sum / count }); 
	
		data.do({ |el| 
			if (el.isNumber, { stdDevSum = (el - avg).squared + stdDevSum });
		}); 
		stdDev = if (count > 0, { (stdDevSum / count).sqrt });
		fillRatio = if (count > 0, { (count / data.size) });
		
		^[ min, max, avg, stdDev ];
	}
	isFull { ^fillRatio == 1.0 }
	
	suggestSpec { ^ControlSpec(min, max, \lin, 0.0, avg, units) }

	makeSpec { |min,max,warp,step,default| 
		spec = this.suggestSpec;
		min !? { spec.minval_(min) };
		max !? { spec.maxval_(max) };
		warp !? { spec.warp_(warp) };
		step !? { spec.step_(step) };
		default !? { spec.default_(default) };
		^spec
	}
	storeArgs { ^[name, data] }
}


/*****************
	Numeric data Chan
*/
SENumDataChan : SEDataChan {	

	// removes constant
	removeDC { 
		if (avg.isNil) { this.analyse };
		data = data - avg;
	}
	
	// normalise data 
	norm { |min=0.0, max=1.0|
				
	}
		// we know data is all numbers, so this is faster:
	analyse { 
		fillRatio = 1.0; 		// must be
		min = data.minItem;
		max = data.maxItem;
		avg = data.meanF;
		stdDev = data.stdDev(avg);
		^[ min, max, avg, stdDev ];
	}
}

/*****************
	Time series
*/
SETimeDataChan : SENumDataChan {

	// the duration
	var <duration;

}

/*****************
	Time series with sample rate 
*/
SETimeSeriesChan : SENumDataChan {
	classvar <>headerFormat = "AIFF", <>sampleFormat = "float";
	// the sample rate
	var <>sampleRate, <buffer; 	

	// removes a constant drift in the data
	removeDrift {
	
	}
	
	*fromSF { |path, name, startFrame=0, endFrame, array|  
			// assume files are mono for now.
		var file;
		file = SoundFile.new; 
		file.openRead(path); 
		if (file.isOpen.not) { ("file" + path + "not found!").warn; ^nil };
		
				// support interleaved timeseries data? would make sense!
		if (file.numChannels != 1) { ("file" + path + "has" + file.numChannels + "channels!").warn };
		
		endFrame = endFrame ? file.numFrames; 
		array = array ?? { Signal.newClear(endFrame - startFrame) };
		file.readData(array);
		file.close; 
		^super.new(name, array).sampleRate_(file.sampleRate);
	}
	writeSF { |path, hFormat, sampleFormat|	// assume mono
		
	}
}
/*****************
	Time series with time stamps
*/
SETimeStampChan : SENumDataChan {

	// the time stamps
	var <>timeStamps;
	
		// read strings, turn them into numbers
	*fromStrings { 
	
	}	// or make strings from the times
	*fromNums { 
	
	}
		// data are in seconds and sorted ascending
	nearest { |time|
		^data.nearestInList(time);
	}
	checkSorting { 
	
	}
	atHMS { |hour=0,min=0,sec=0| 
		
	}
	atYMD { |year,month,day| 
		
	}
}

/*****************
	Ordinal data channel
*/
SECatDataChan : SEDataChan { 
	var <repertoire; 
	
	init { 
		super.init;
		repertoire = data.as(Set);
	}
}

/*****************
	Vector data channel
*/
SEVecDataChan : SEDataChan {

}

/*****************
	Spatial data channel
*/
SESpatialDataChan : SEVecDataChan {

}
