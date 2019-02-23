
// Figure 3.9 Gathering up files for multichannel cues 

// gather all your folder paths
//this will path match each folder in the collection, i.e. we will have a collection of collections of paths

~groupOfindivCueFolders = "sounds/*".pathMatch.collect{ | item |  (item.asSymbol++"*").pathMatch };

Post << ~groupOfindivCueFolders;  //see them all !

//check how many cues you will have in the end
~groupOfindivCueFolders.size; 

//automate the buffering process for all cues:
~bufferedCues = ~groupOfindivCueFolders.collect{|item, i| item.collect{| path |  Buffer.read(s,  path)}}; //now all our cue files are sitting in their buffers ! 

~bufferedCues[0];  //here is cue 1

// see it in the post window:
Post << ~bufferedCues[0];

// play them all in a Group, using our previous synthdef
// we use bind here to ensure they start simultaneously
(
s.bind({	
	~nowPlaying = Group.new(s); // a group to put all the channel synths in
	~bufferedCues[0].do({|cue| Synth("playbuf", [buf: cue], ~nowPlaying)})
});
)
// fade them out together by sending a release message to the group
~nowPlaying.release;




