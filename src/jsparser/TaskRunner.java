package jsparser;

import java.util.Vector;


/**
 *
 * @author Alex
 */
public class TaskRunner extends Thread {

    public TaskRunner(Vector<Timer> timers) {
        this.timers = timers;
    }

    @Override
    public void run() {
        while (true) {
            if (timers.size() == 0) break;
            for (int i = 0; i < timers.size(); i++) {
                timers.get(i).check();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {}
        }
    }

    private Vector<Timer> timers;
    public volatile boolean stop = false;
    public volatile boolean started = false;

}
