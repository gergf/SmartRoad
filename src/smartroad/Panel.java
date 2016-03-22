
package smartroad;

import smartroad.Interfaces.IPanel;

/**
 *
 */
public class Panel implements IPanel{

    private int id; 
    private Segment segment; /* What it belongs */
    private boolean status; /* Enabled = true Disabled = false */
    private String text;
    
    /**
     * This constructor creates a Panel with no set up. 
     * @param id
     * @param seg 
     */
    public Panel(int id, Segment seg){
        this.id = id; 
        this.segment = seg; 
        this.status = false;
        this.text = ""; 
    }
    
    /**
     * This constructor creates a Panel already set up. 
     * @param id
     * @param seg
     * @param enabled
     * @param text 
     */
    public Panel(int id, Segment seg, boolean enabled, String text){
        this.id = id; 
        this.segment = seg; 
        this.status = enabled;
        this.text = text; 
    }
    
    /* Getters */
    @Override
    public int getId() {
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
        return this.status; 
    }
    
    /* Setters */
    
    /**
     * It changes the status of the Panel to the opposite one. 
     */
    public void setStatus(){
        this.status = !this.status; 
    }
    
    /* Methods */

    
    
}
