package flashget;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Kittitouch Ingkasompob
 */
public class DownloadTask extends Task<Long>{
    private long size;
    private final int BUFFERSIZE = 16 * 1024;
    private int bytesRead = 0;
    private long start;
    private URL url;
    private File dir;
    private long count;
    private volatile boolean running = true;


    /**
     * Constructor for download task
     * @param start is
     * @param size is
     * @param url is
     * @param fileName is
     */
    public DownloadTask(long start, long size, URL url, File fileName) {
        this.start = start;
        this.size = size;
        this.url = url;
        this.dir = fileName;
        this.count = 0;
    }

    /**
     * get task from URL and download
     * @return download progress
     */
    @Override
    public synchronized Long call() {
        updateProgress(0, size);
        try {
            URLConnection connection = url.openConnection();
            byte[] buffer = new byte[BUFFERSIZE];
            try (InputStream in = connection.getInputStream();
                 RandomAccessFile out = new RandomAccessFile(dir, "rwd")){
                out.seek(start);
                do {
                    int n = in.read(buffer);
                    if (n < 0) break; // n < 0 means end of the input
                    out.write(buffer, 0, n); // write n bytes from buffer
                    bytesRead += n;
                    count += n;
                    updateValue(count);
                    updateProgress(bytesRead, size);
                    updateMessage(String.format("%d", count));
                } while (bytesRead < size && running);
            } catch (IOException ex) {
                return null;
            }

        } catch (MalformedURLException e) {
            return null;

        } catch (IOException e) {
            return null;
        }
        return count;
    }

    /**
     * Stop task from running
     */
    public void stopTask() {
        running = false;
    }

}

