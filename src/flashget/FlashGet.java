package flashget;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * FlashGet program for download file from URL
 *
 * @author Kittitouch Ingkasompob
 */
public class FlashGet extends Application {
    private VBox root;
    Window primaryStage;
    private TextField urlField;

    /**
     * Set scene and title
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        root = initComponents();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("File downloader");
        primaryStage.show();
    }

    /**
     * Initialize for GUI
     */
    private VBox initComponents() {
        FlowPane pane = new FlowPane();
        VBox vBox = new VBox();
        pane.setHgap(8);
        vBox.setPrefSize(670, 130);
        vBox.setPadding(new Insets(10));
        Button download = new Button("Download");
        Button clear = new Button("Clear");
        urlField = new TextField();
        DownloadHandler downloadHandler = new DownloadHandler();
        download.setOnAction(downloadHandler);
        urlField.setMinWidth(400);
        EventHandler<ActionEvent> clearHandler = event -> urlField.clear();
        clear.setOnAction(clearHandler);
        Label label1 = new Label("URL to download");
        pane.getChildren().addAll(label1, urlField, download, clear);
        vBox.getChildren().addAll(pane);
        return vBox;
    }


    /**
     * Run the application
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Inner class for download button controller
     */
    public class DownloadHandler implements EventHandler<ActionEvent> {
        // Create list of tasks
        private ArrayList<Task<Long>> tasks = new ArrayList();
        // Create list of progressbar for bind with threads
        private ArrayList<ProgressBar> threadBarsLists = new ArrayList();
        // Create list that contains thread
        private ArrayList<DownloadThread> threads = new ArrayList<>();
        private Label sizeLabel;
        private Label name;

        /**
         *
         * @param event
         */
        @Override
        public void handle(ActionEvent event) {
            urlField.setStyle("-fx-text-inner-color: black;");
            threadBarsLists = new ArrayList(Arrays.asList(new ProgressBar(), new ProgressBar(), new ProgressBar(),
                    new ProgressBar(),new ProgressBar()));
            FlowPane progressPane = new FlowPane();
            FlowPane threadPane = new FlowPane();
            Label progressLabel = new Label("0");
            Button cancel = new Button("Cancel");
            cancel.setOnAction(this::stopTask);
            ProgressBar totalProgress = new ProgressBar();
            Label threadLabel = new Label("Threads: ");
            progressPane.setHgap(8);
            progressPane.setPadding(new Insets(5));
            threadPane.setPadding(new Insets(5));
            String urlName = urlField.getText().trim();
            int nThreads = 5;
            totalProgress.setPrefWidth(200);

            try {
                URL url = new URL(urlName);
                URLConnection connection = url.openConnection();
                long length = connection.getContentLengthLong();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Text (.txt)", "*.txt")
                        , new FileChooser.ExtensionFilter("Microsoft Word Documents (.docx)", "*.docx")
                        , new FileChooser.ExtensionFilter("HTML (.html)", "*.html")
                        , new FileChooser.ExtensionFilter("JPEG (.jpg)", "*.jpg", "*.jpeg")
                        , new FileChooser.ExtensionFilter("PDF (.pdf)", "*.pdf")
                        , new FileChooser.ExtensionFilter("PNG (.png)", "*.png")
                        , new FileChooser.ExtensionFilter("MP4 (.mp4)", "*.mp4")
                        , new FileChooser.ExtensionFilter("ZIP(.zip)", "*.zip")
                );
                String fileName = String.valueOf(fileChooser.showSaveDialog(primaryStage));
                if (fileName.equals("null")) {
                    return;
                }
                File file = new File(fileName);
                String newName = "";
                for (int i = 0; i < fileName.length(); i++) {
                    if (fileName.charAt(i) == 47) {
                        newName = fileName.substring(i+1);
                    }
                }

                name = new Label(newName);
                if (length < 200000) {
                    nThreads = 2;
                    createTask(nThreads, length, url, file);
                    totalProgress.progressProperty().bind(tasks.get(0).progressProperty().multiply(0.5).add(tasks.get(1).progressProperty().multiply(0.5)));
                }
                else {
                    createTask(nThreads, length, url, file);
                    totalProgress.progressProperty().bind(tasks.get(0).progressProperty().multiply(0.2).add(tasks.get(1).progressProperty().multiply(0.2)).add(tasks.get(2).progressProperty().multiply(0.2)).add(tasks.get(3).progressProperty().multiply(0.2)).add(tasks.get(4).progressProperty().multiply(0.2)));
                }
                sizeLabel = new Label(String.format("/ %d",length));
            } catch (MalformedURLException ex) {
                urlField.setStyle("-fx-text-inner-color: red;");
                urlField.setText("Invalid URL");
                return;

            } catch (IOException ex) {
                urlField.setStyle("-fx-text--inner-color: red;");
                urlField.setText("Invalid URL");
                return;
            }
            ChangeListener<String> messageListener = (subject, oldValue, newValue) -> progressLabel.setText(newValue);
            ExecutorService executor = Executors.newFixedThreadPool(nThreads);
            for (int i = 0; i < tasks.size(); i++) {
                threads.add(new DownloadThread((DownloadTask) tasks.get(i)));
                threads.get(i).start();
                tasks.get(i).messageProperty().addListener(messageListener);
                executor.execute(tasks.get(i));
            }
            progressPane.getChildren().addAll(name, totalProgress, progressLabel, sizeLabel, cancel);
            threadPane.getChildren().addAll(threadLabel, threadBarsLists.get(0), threadBarsLists.get(1), threadBarsLists.get(2), threadBarsLists.get(3), threadBarsLists.get(4));
            root.getChildren().addAll(progressPane, threadPane);
        }

        /**
         * Create the task and bind thread with progressBar
         * @param nThreads is number of thread to run
         * @param length is size of task
         * @param url is link to download
         * @param file is file to export
         *
         */
        public void createTask(int nThreads,long length, URL url, File file) {
            long value;
            long newValue = 0;
            for (int i = 0; i < nThreads; i++) {
                value = newValue;
                newValue = (length / nThreads) * (i + 1);
                tasks.add(new DownloadTask(value, newValue, url, file));
                threadBarsLists.get(i).progressProperty().bind(tasks.get(i).progressProperty());
            }
        }


        /**
         * Stop all task
         *
         * @param event
         */
        public synchronized void stopTask(ActionEvent event) {
            for (int i = 0; i < threads.size(); i++) {
                threads.get(i).stopThread();
            }
        }

    }

}


