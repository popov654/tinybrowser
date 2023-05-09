package cssparser;

import java.util.LinkedHashMap;
import java.util.Vector;

/**
 *
 * @author Alex
 */
public class Styles {
    public LinkedHashMap<String, String> styles = new LinkedHashMap<String, String>();
    public LinkedHashMap<String, String> runtimeStyles = new LinkedHashMap<String, String>();
    public Vector<QuerySelector> stateStyles = new Vector<QuerySelector>();
    public Vector<QuerySelector> beforeStyles = new Vector<QuerySelector>();
    public Vector<QuerySelector> afterStyles = new Vector<QuerySelector>();
    public Vector<QuerySelector> specialStyles = new Vector<QuerySelector>();
    public SelectorGroup group;
}
