Figure 10.7

(
~ampSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
    if( source !== ~ampSlider, {
        ~ampSlider.value = value;
    })}, \amp );
~speedSliderUpd = UpdateListener.newFor( ~model, { arg upd, mod, value, source;
    if( source !== ~speedSlider, {
        ~speedSlider.value = ~speedSpec.unmap( value );
    })}, \speed );
)

