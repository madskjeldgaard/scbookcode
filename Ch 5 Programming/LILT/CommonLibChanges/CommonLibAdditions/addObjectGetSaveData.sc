/* (IZ 2005-10-26) {
	my own scheme for saving and loading objects to files
	I may scrap it if I manage to work fully with the writeArchive scheme
	Now, all Objects return themselves except for certain classes in this Lilt library 
	TODO: See if the "getSaveData-fromSaveData" scheme can be replaced by a scheme 
	utilizing only the existing methods working with writeArchive / readArchive
} */

+ Object {
	getSaveData { ^this }
	*fromSaveData { |data| ^data }
}
