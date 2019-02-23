/* IZ 2007-02-02 { SC3
Methods added to Classes not belonging to Lilt: 

======== 1. Core - Essential (Lilt relies on them to function) ========

Buffer:reRead : Re read audio buffer from file without creating new buffer object, but allocate new number (for example when the server reboots). This enables all scripts that are currently using a buffer to keep referencing it as the same object. This is much simpler than updating the scripts to new objects, but works consistently.

Collection:makeUniqueName(name) : if I contain name, add numbered suffix to the name, counting up, till unique name is constructed. Meaning if you add something named 'foo' and something ele named "foo" already exists in the collection, you will get a new symbol 'foo[2]' to use as name. Something like the numbering scheme for user-created Documents (Untitled, Untitled 2 etc.) All elements of the receiver must be Symbols for this to work. 

FlowLayout:rest : Calculate the width and height to fill the remaining of the current row on a FlowLayout, from the current position to the bottom right margin. Return these as array for ease of use when creating new Rects for views.

Function:update

Object midi stuff ...

Object:onBoot
Object:onQuit

Node, EventStreamPlayer:onStart
Node, EventStreamPlayer:onEnd

Rect:fromTop
Rect:fromRight
Rect:resize

SCWindow:resize (should compare with Rect:resize and see if SCWindow:resize can be dispensed with, or recoded to use Rect:resize)

String:fileSymbol

Synth coding utilities: 
	arString
	krString
	akrString
	args
	arDef
	krDef
	akrDef

==== 1b Integration Planned ====

(Not yet integrated into Lilt, but integration is planned)

Synth:addControl 

Synth:fx
Synth:send


	
======== 2. Utilities (Partly used by Lilt) ========

(These seem good ideas whether they are used by Lilt or not)
Method:report

Utilities for finding the largest and smallest element in a collection: 
Collection:largest, Collection:smallest

======== 3. Rarely Used But to Be Considered ========

(Make sure these are no longer used by Lilt. Consider removing them, or writer to the sc-dev list to see what people think about them:)

Function:playArgs(target, outbus, fadeTime, addAction, args);

Function !! n: Construct a Pfuncn to repeat a function n times. 

Pattern:test
Pattern:ptest

PointArray:size
PointArray:at
PointArray:xat
PointArray:yat

Buffer:xplay : Utility for simply playing a buffer directly with a method.

Server:isRunning

SequentialCollection:pplot
==== ???
Object:getSaveData
Object:fromSaveData


} */

