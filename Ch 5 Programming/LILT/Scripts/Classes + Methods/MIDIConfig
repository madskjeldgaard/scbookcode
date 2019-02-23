/* (IZ 2005-08-23 / 2005-10-25) { 

This Class is obsolete as of January 2007 - use MIDIListener with MIDIHandler instead.

Utility for saving the MIDI command types sent by all controls on a given device and assigning these to Parameters of scripts.

MIDIConfig.new(<name>, row1, row2 ... rown);
Each row is a number stating the number of items in the row. 
The rows items are named automatically as follows: 

<row number><item number> where: 
<row number> is numbered alphabetically A-Z, a-z (max 52 rows possible).
<item number> is numbered numerically
Thus 'B5' is the 5th item on the 2nd row. 

The items are represented by buttons named as follows:
<item name> <midi assignment>
Where <midi assignment> is named as follows: 
<channel><midi type><number>
Midi types are:
NN : note on
NO : note off
NX : note on-off
CC : Control change
PB : Pitch bend
AT : After touch

w = SCWindow("MIDI Device Configuration", Rect(0, 200, 12 * 74 + 8, 200).fromTop);
w.view.decorator = FlowLayout(w.view.bounds);
{ SCButton(w, Rect(0,0,70, 20)).states_([["B99 16CC127"]]).font_(Font("Helvetica", 10)); } ! 12;
w.front;
} */
