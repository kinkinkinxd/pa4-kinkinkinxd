package flashget;

/**
 * Class for download thread
 */
public class DownloadThread extends Thread {
    private volatile boolean isRunning = true;
    private Thread thread;
    private DownloadTask task;

    public DownloadThread(DownloadTask task) {
        this.task = task;
        thread = new Thread(this.task);
}

    @Override
    public void run() {
        while (isRunning) {
            task.call();
        }
    }

    public void stopThread() {
        isRunning = false;
        task.stopTask();
    }


}