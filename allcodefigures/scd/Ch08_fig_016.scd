
	// figure 8.16 - converting setMessage

z.setMessage = { |z, str| 
	var messSize = str.size; 
	var fontsize = (1.64 * z.txtView.bounds.width) / max(messSize, 32);
	z.txtView.font_(Font.new("Monaco", fontsize));
	z.txtView.string_(str);
	z.animate;
};

Shout { 
	...
	setMessage { |message| 
		var messSize, fontSize;
		messSize = message.size;
		fontSize = (1.64 * width) / max(messSize, 32);
		
		defer { 
			txtView.font_(Font("Monaco", fontSize))
				.string_(message.asString);
		};
		this.animate;
	}
	...	
}

Shout("Test 1, 2");
Shout("Test" + (1..16));


