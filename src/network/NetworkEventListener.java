package network;

import java.util.HashMap;

/**
 *
 * @author Alex
 */
public abstract class NetworkEventListener {

    public abstract void actionPerformed(String type, HashMap<String, String> data);

}
