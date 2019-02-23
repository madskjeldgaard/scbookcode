/*
	Filename: SEData.sc 
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
	Root class for data sets, encapsulating the channels and handling the data source
*/
SEData {

	// dict keep all loaded instances available 
	classvar <all;
	
	// name of the data set
	var <>name;

	// the data channels;
	var <channels, <chansDict;
	
	// number of data channels
	var <>numChans, <>numFrames, <>chanNames;
	
	// the source object handling the data source
	var <>source;
	
	
	*new { |name, channels| 
		^super.newCopyArgs(name, channels).init
	}
	init {	
		numChans = channels.size;
		chanNames = channels.collect(_.name); 
		
		chansDict = (); 
		channels.do { |ch| chansDict.put(ch.name, ch) };
		
		numFrames = channels.first.size; 
		if (this.checkChans(numFrames)) { 
			^this 
		} {
			"SEData - wrong sizes?".postln;
			^nil
		}
	}
	
	checkChans { |nFrames| 
		var lengthsOK;
			// check data lengths for consistency:
		lengthsOK = channels.every({ |ch| ch.size == nFrames });
		
		^lengthsOK;
	}
	
	// access method to the channels
	atChan { arg key, range; 
		^chansDict[key]
	}
	atChans { |keys| 
		keys = this.checkNames(keys);
		^keys.collect { |key| this.atChan(key) };
	}
	// access method to the data as vectors
	atVector { arg index, keys;
		keys = this.checkNames(keys);
		^keys.collect { |key| this.atChan(key)[index] }
	}

	atEvent { |index, keys| 
		var event = ();
		keys = this.checkNames(keys);
		keys.do { |key| event.put(key, this.atChan(key)[index]) };
		^event
	}
	
	checkNames { |names| 
		var validNames;
		if (names.isNil) { ^chanNames };
		validNames = names.asArray.sect(chanNames);
		if (names != validNames) { 
			warn(this.class.asString + this.name + 
				"filtered bad chanNames:" + (names.asArray.difference(validNames)));
			names = validNames;
		};
		^names
	}
	
	// analyse the data at the top level (intra channel analysis)
	analyse {
		channels.do(_.analyse);
	}

	vectorsDo { |names, func| 
		this.numFrames.do { |index| func.value(this.atVector(index, names), index) }
	}
	
	vectorsCollect { |names, func| 
		^Array.fill(this.numFrames, { |index| func.value(this.atVector(index, names), index) });
	}
	
	*fromVect { |name, chanNames, vectors, classes| 
		var channels, numChs, datachans; 
		numChs = chanNames.size; 
		if (vectors.every { |vect| vect.size == numChs }.not, { "unequal sizes?".postln; });

		classes = (classes ? SEDataChan).asArray; 
		
		datachans = vectors.flop; 
		channels = chanNames.collect { |name, i| classes.clipAt(i).new(name, datachans[i]) };

		^this.new(name, channels)
	}
	*fromNamesData { |name, chanNames, dataArray, classes|
		var channels;
		if (dataArray.size != chanNames.size, { "unequal sizes?".postln; });
		classes = (classes ? SEDataChan).asArray;

		channels = chanNames.collect { |name, i| classes.clipAt(i).new(name, dataArray[i]) };

		^this.new(name, channels)
	}
	
	storeArgs { arg stream; 	// support storage as SC code.
		^[name, channels]
	}

	addChan { |channel| 
		channels = channels.add(channel);
		this.init;
	}
}


/*****************
	Convenience class for time series data
*/
SETimeData : SEData {


	// data at specific time
	atTime { arg range;
	
	}

}

/*****************
	Convenience class for 3D spatial data
*/
SESpatialData : SEData {

	// data at specific position
	atPosition { arg position;
	
	}


}
