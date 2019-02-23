Figure 6.15. The definition of the stream created by Pseq.
	Routine({
		repeats.value.do({
			list.size.do({ arg i;
				item = list.wrapAt(i + offsetValue);
				inval = item.embedInStream(inval);
			});
		});
	});


