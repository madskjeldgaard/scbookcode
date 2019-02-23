Figure 10.5

(
~ampText = JSCStaticText( ~win, Rect( ~ampSlider.bounds.right + 2, 4, 50, 25 ));
~ampTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
    ~ampText.string = "% dB".format( value.ampdb.round( 0.1 ))}, \amp );
~speedText = JSCStaticText( ~win, Rect( ~speedSlider.bounds.right + 2, 32, 50, 25 ));
~speedTextUpd = UpdateListener.newFor( ~model, { arg upd, mod, value;
    ~speedText.string = "%\\%".format( (value * 100).round( 0.1 ))}, \speed );
)
// note: the sliders must be initially dragged to cause view updates!

