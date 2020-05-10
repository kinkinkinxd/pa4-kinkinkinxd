package flashget;

/**
 * Class for create download thread
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
     * Stop thread
     */
    public void stopThread() {
        isRunning = false;
        task.stopTask();
    }


}