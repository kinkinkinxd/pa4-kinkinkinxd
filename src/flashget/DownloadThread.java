package flashget;

/**
 * Class for create download thread
 *
 * @author Kittitouch Ingkasompob
 */
public class DownloadThread extends Thread {
    private volatile boolean isRunning = true;
    private Thread thread;
    private DownloadTask task;

    /**
     * Constructor for download thread
     */
    public DownloadThread(DownloadTask task) {
        this.task = task;
        thread = new Thread(this.task);
}

    /**
     * Call task
     */
    @Override
    public void run() {
        while (isRunning) {
            task.call();
        }
    }

    /**
     * Stop thread from running and stop task
     */
    public void stopThread() {
        isRunning = false;
        task.stopTask();
    }


}