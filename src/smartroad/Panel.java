
package smartroad;

import java.util.UUID;

import smartroad.Interfaces.IPanel;

/**
 *
 */
public class Panel implements IPanel{

    private String id; 
    private Segment segment; /* What it belongs */
    private boolean enabled; /* Enabled = true Disabled = false */
    private String text;
    private String standarText; 
    
    /**
     * This constructor creates a Panel with no set up. 
     * @param id
     * @param seg 
     */
    public Panel(Segment seg){
        this.id = UUID.randomUUID().toString(); 
        this.segment = seg; 
        this.enabled = false;
        this.text = "Circule con precaucion"; 
        this.standarText = this.text;
        /* Add the panel to the segment */
        seg.addPanel(this);
    }
    
    /**
     * This constructor creates a Panel already set up. 
     * @param id
     * @param seg
     * @param enabled
     * @param text 
     */
    public Panel(String id, Segment seg, boolean enabled, String text){
        this.id = id; 
        this.segment = seg; 
        this.enabled = enabled;
        this.text = text; 
        this.standarText = this.text;
    }
    
    /* Getters */
    @Override
    public String getId() {
        return this.id; 
    }
    
    @Override
    public Segment getSegment() {
        return this.segment; 
    }
    
    @Override
    public String getText() {
        return this.text; 
    }
    
    @Override
    public boolean getStatus() {
        return this.enabled; 
    }
    
    /* Setters */
    @Override 
    public void setStatus(boolean enabled){
        this.enabled = enabled; 
    }
    
    @Override
    public void setText(String newText){
    	this.text = this.standarText = newText; 
    }
    
    public void showSegmentCloseText(){
    	this.text = "The segment has been closed";
    }
    
    public void backToStadarText(){
    	this.text = this.standarText;
    }
    
    /* Methods */

    
    
}
