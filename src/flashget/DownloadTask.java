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

    public DownloadTask(long start, long size, URL url, File dir) {
        this.start = start;
        this.size = size;
        this.url = url;
        this.dir = dir;
        this.count = 0;
    }

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
                } while (bytesRead < size);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

        } catch (MalformedURLException e) {
            e.getMessage();
        } catch (IOException e) {
            e.getMessage();
        }
        return count;
    }
}

