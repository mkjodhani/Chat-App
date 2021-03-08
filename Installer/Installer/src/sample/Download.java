package sample;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class Download
{
    private Object object= new Object();
    public String url_link;
    private URL url;
    private long size;
    InputStream inputStream ;
    BufferedOutputStream bufferedOutputStream ;
    private static final String FILE_NAME = "Gossip.jar";
    public long getTotalSize() {
        return totalSize;
    }

    long totalSize;
    boolean pause = false;
    private ProgressBar progressBar;
    private Object complete;
    private File file=null;
    private Button Button = null;
    HttpURLConnection httpURLConnection = null;
    URLConnection urlConnection = null;
    public Download(String url_link, ProgressBar progress, Button button) throws IOException {
        this.progressBar = progress;
        this.url_link = url_link;
        this.complete = false;
        this.Button = button;
        url = new URL(url_link);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("HEAD");
        totalSize =httpURLConnection.getContentLengthLong();
        urlConnection = url.openConnection();
    }

    public boolean getComplete() {
        return (boolean)complete;
    }

    public void downloadFile() throws InterruptedException
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                Button.setText("Pause");
            }
        });
        try
        {
            byte[] data = new byte[1024];
            int length =0;
            long current = 0;
            file = new File(Download.FILE_NAME);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            inputStream = urlConnection.getInputStream();
            while ((length = inputStream.read(data,0,1024)) != -1)
            {
                synchronized (this.object)
                {
                    while (pause)
                        object.wait();
                }
                current += length;
                bufferedOutputStream.write(data,0,length);
                long finalCurrent = current;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((double) finalCurrent /totalSize);
                    }
                });
            }
            bufferedOutputStream.close();
            Platform.runLater(new Runnable() {
                @Override
                public void run()
                {
                    Button.setText("Launch");
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    synchronized public void toggle() throws InterruptedException
    {
        synchronized (this.object)
        {
            pause  = !pause;
            if (!pause)
                object.notify();
        }
    }

}
