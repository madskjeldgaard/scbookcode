// figure 8.9 - using codeDump to shout 

this.codeDump = { |str, result, func| [str, result, func].printAll };

a = 1 + 2;	// code appears in post window

z.shoutTag = "//!!";
this.codeDump = { |str| if (str.beginsWith(z.shoutTag)) { z.shout(str.drop(z.shoutTag.size)) } };

//!! a comment with a 'shout tag' now gets shouted!




