/* (IZ 2005-10-17) {

Reload an existing buffer with a new bufnum. Only to be used when a server reboots.
Used by Samples in loadBuffer.

} */

+ Buffer {
	reRead { | argPath, onInfo, startFrame = 0, numFrames = -1 |
		bufnum = server.bufferAllocator.alloc(1);
		path = argPath ? path;
		this.doOnInfo = { | ... args | onInfo.(*args); };
		this.waitForBufInfo;
		this.allocRead(path, startFrame, numFrames, { |buf|
			["/b_query", buf.bufnum]
		});
	}
	// making bufnum writeable ...
//	setBufnum { | | } // not yet ...
}
