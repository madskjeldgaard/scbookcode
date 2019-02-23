/* (IZ 2005-09-04)
Get the first part of a file name in a path as symbol.
Frequently used for naming buffers or other data loaded from file
after the name of the file in a path.

"./SCClassLibrary/IX/Sequences/agamemnon".fileSymbol
"./SCClassLibrary/IX/Sequences/agamemnon".basename.splitext.first.asSymbol

*/

+ String {
	fileSymbol {
		^this.basename.splitext.first.asSymbol
	}
}
