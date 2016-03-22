package smartroad.Interfaces;

import smartroad.Segment;


public interface IPanel {
    
    /**
     * This method returns the ID of the Panel. 
     * @return id
     */
    public int getId();
    
    /**
     * This method returns segment which it belongs. 
     * @return segment
     */
    public Segment getSegment(); 
    
    /**
     * This method returns the text which is being displayed 
     * in the Panel. 
     * @return text
     */
    public String getText();
    
    /**
     * This method returns True if the Panel is enabled. 
     * If it is not, returns False. 
     * @return status
     */
    public boolean getStatus(); 
    
}
