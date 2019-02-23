

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Contents

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2008, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== The contents of this tutorial ==========

// 	01 - Language Basics
//	02 - Server Basics
//	03 - Controlling the Server
//	04 - Additive Synthesis
//	05 - Subtractive Synthesis
//	06 - AM, RM and FM Synthesis
//	07 - Envelopes - MIDI Keyboard
//	08 - Buffers and Samples
//	09 - Granular Synthesis
// 	10 - Physical Modelling
// 	11 - Fast Fourier Transform (FFT)
// 	12 - Audio Effects and Filters
//	13 - Busses, Nodes, Groups and Signal Flow
// 	14 - Musical Patterns on SCServer
// 	15 - Musical Patterns in the SCLang
// 	16 - Tuning Systems and Scales
// 	17 - Graphical User Interfaces


/*

This tutorial is not about programming SuperCollider, there are other tutorials
that address that question. This tutorial is more about how to explore digital
sound and synthesis _using_ Supercollider as our tool. 

It's far from perfect as it was written very quickly and amongst overload of other 
work, but we thought we would provide it to the public anyway. We use it in our
workshops (see www.ixi-software.net/workshops)

If you find any mistakes, have questions or comments, please send us an email.
: -> thor[]ixi-audio[]net 
: -> www.ixi-audio.net

For info on downloading and installing/compiling SuperCollider go here:
http://supercollider.sourceforge.net


The SC language is based on a language called Smalltalk. 
-> check some good books on Smalltalk here:
 http://www.iam.unibe.ch/~ducasse/FreeBooks.html

A file with all SC helpfiles could be helpful (as you can search a PDF file):
http://www.semiotiche.it/andrea/sw/sc/theSuperColliderHelpBook.pdf


*/



// An index into the content of each tutorial (thanks Martin)


// - ixi tutorial index

'01 - Language Basics' [ixi_01]
// 	1) Comments, posting and help
// 	2) Variables
// 	3) Functions
// 	4) Lists and Arrays
//	5) Dataflow
//	6) Looping and iterating
//	7) Peaking under the hood
// 	8) Creating classes

'02 - Server Basics' [ixi_02]
// 	1) Booting the server
// 	2) The play and scope functions
// 	3) SynthDefs
//	4) Getting values back to the language 
//	5) Tasks
// 	6) Patterns
// 	7) TempoClocks

'03 - Controlling the Server' [ixi_03]
// 	1) Tasks
// 	2) Patterns
// 	3) TempoClocks
// 	4) GUI

'04 - Additive Synthesis Basics' [ixi_04]
// 	1) Creating complex waves out of sines
// 	2) Bell Synthesis
// 	3) Stupid harmonics GUI
// 	4) Some Additive SynthDefs with routines playing them
// 	5) Polishook patch
// 	6) Using Control

'05 - Subtractive Synthesis Basics' [ixi_05]
// 	1) Noise sources
// 	2) Common filter types
// 	3) Bell synthesis

'06 - AM, RM and FM Synthesis' [ixi_06]
// 	1) LFO (Low Frequency Oscillators)
// 	2) Amplitude modulation (AM) synthesis
// 	3) Ring modulation (RM) synthesis
// 	4) Frequency modulation (FM) synthesis

'07 - Envelopes' [ixi_07]
// 	1) Envelope generator
// 	2) Envelope types
// 	3) Triggers and gates
//	4) MIDI keyboard example

'08 - Buffers and Samples' [ixi_08]
//	1) Allocating a buffer
// 	2) Reading a buffer
//	3) Streaming a buffer
// 	4) Record into buffer
// 	5) Fill a buffer
//	6) Pitch and Time changes in playback
// 	7) Using BufWr and BufRd

'09 - Granular Synthesis' [ixi_09]
// 	1) TGrains
//	2) Warp
// 	3) Custom built grainular synthesis
// 	4) The messaging style
// 	5) Munger

'10 - Physical Modelling' [ixi_10]
// 	1) Karplus-Strong synthesis
// 	2) A glass synthesis using biquad filter (SOS)
// 	3) Waveguide Flute
// 	4) Some useful filters
// 	5) The MetroGnome
// 	6) TBall examples
// 	7) The STK synthesis kit

'11 - Fast Fourier Transform' [ixi_11]
// 	1) Fast Fourier Transform examples

'12 - Audio Effects' [ixi_12]
// 	1) Delays
// 	2) Phaser (Phase Shifting)
// 	3) Flanger
// 	4) Chorus
// 	5) Reverb
// 	6) Tremolo
// 	7) Distortion
// 	8) Compressor
// 	9) Limiter
// 	10) Sustainer
// 	11) Noise gate
// 	12) Normalizer
// 	13) Limiter (Ugen)
// 	14) Amplitude
// 	15) Pitch
// 	16) Filters
// 	17) Making Audio Unit plugins

'13 - Busses, Nodes, Groups and Signal Flow' [ixi_13]
// 	1) Busses in SC (Audio and Control Busses)
// 	2) Nodes
// 	3) Groups

'14 - Musical Patterns on SCServer' [ixi_14]
//	1) Stepper and Select
//	2) PulseCount and PulseDivider
//	3) Demand UGens

'15 - Musical Patterns in the SCLang' [ixi_15]
//	1) The SynthDefs
//	2) A survey of patterns usage
//	3) TempoClock and patterns
//	4) Popcorn
//	5) Clocks in SuperCollider
//	6) Using the TempoClock

'16 - Tuning Systems and Scales' [ixi_16]
// 	1) The SynthDefs
// 	2) Equal Temperanment
// 	3) Just Intonation
// 	4) Pythagorean Tuning
// 	5) Scales
// 	6) The Scala Library
// 	7) Using Samples

'17 - Graphical User Interface' [ixi_17]
// 	1) SuperCollider and GUIs
// 	2) A Sine Controlling GUI
//	3) ControlSpec - Scaling/mapping values
// 	4) Other Views
//	5) Further exploration
  

