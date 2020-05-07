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
    private Window primaryStage;
    private TextField urlField;

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
        vBox.setPrefSize(600, 130);
        vBox.setPadding(new Insets(10));
        Button download = new Button("Download");
        Button clear = new Button("Clear");
        urlField = new TextField();
        DownloadHandler downloadHandler = new DownloadHandler();
        download.setOnAction(downloadHandler);
        urlField.setPrefWidth(300);
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
     *
     */
    public class DownloadHandler implements EventHandler<ActionEvent> {
        private ArrayList<Task> tasks = new ArrayList();
        private ArrayList<ProgressBar> threadBarsList = new ArrayList();
        private Label sizeLabel;
        private Label name;




        @Override
        public void handle(ActionEvent event) {
            urlField.setStyle("-fx-background-color: white;");
            threadBarsList = new ArrayList(Arrays.asList(new ProgressBar(), new ProgressBar(), new ProgressBar(),
                    new ProgressBar(),new ProgressBar()));
            FlowPane progressPane = new FlowPane();
            FlowPane threadPane = new FlowPane();
            Label progressLabel = new Label("0");
            Button cancel = new Button("Cancel");
            Button play = new Button("\u25B6");
            Button pause = new Button("\u25A0");
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
                urlField.setStyle("-fx-background-color: red;");
                urlField.setText("Invalid URL");
                return;

            } catch (IOException ex) {
                urlField.setStyle("-fx-background-color: red;");
                urlField.setText("Invalid URL");
                return;
            }
            ChangeListener<String> messageListener = (subject, oldValue, newValue) -> progressLabel.setText(newValue);
            ExecutorService executor = Executors.newFixedThreadPool(nThreads);
            for (int i = 0; i < tasks.size(); i++) {
                new Thread(tasks.get(i)).start();
                tasks.get(i).messageProperty().addListener(messageListener);
                executor.execute(tasks.get(i));
            }
            progressPane.getChildren().addAll(name, totalProgress, progressLabel, sizeLabel, play, pause, cancel);
            threadPane.getChildren().addAll(threadLabel, threadBarsList.get(0), threadBarsList.get(1), threadBarsList.get(2), threadBarsList.get(3), threadBarsList.get(4));
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
                threadBarsList.get(i).progressProperty().bind(tasks.get(i).progressProperty());
            }
        }



        /**
         * Stop all task
         *
         * @param event
         */
        public void stopTask(ActionEvent event) {
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).cancel();

            }
//            root.getChildren().removeAll(this.output, this.thread);


        }

    }

}


