package pg.merger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @FXML
    private Button choosePdfsButton;
    @FXML
    private Button mergePdfsButton;
    @FXML
    private TextArea infoTextArea;

    private LinkedList<File> files;
    private String destinationDir;

    private final Window parent;

    public Controller(Window parent) {
        this.parent = parent;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choosePdfsButton.setOnAction(choosePdfsAction());
        mergePdfsButton.setOnAction(mergePdfsAction());
    }

    private EventHandler<ActionEvent> choosePdfsAction() {
        return actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.setTitle("Choose PDFs");
            files = new LinkedList<>(
                    Optional.ofNullable(fileChooser.showOpenMultipleDialog(parent)).orElseGet(Collections::emptyList)
            );
            if (!files.isEmpty()) {
                infoTextArea.setText(files.stream().map(File::getName).collect(Collectors.joining("\n")));
                destinationDir = files.getFirst().toPath().getParent().toString();
            }
        };
    }

    private EventHandler<ActionEvent> mergePdfsAction() {
        return actionEvent -> {
            if (files.isEmpty()) {
                infoTextArea.setText("Nothing to merge");
            } else {
                mergePdfs();
            }
        };
    }

    private void mergePdfs() {
        String fileName = String.format("merge-result-%s.pdf",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-h24mmss"))
        );
        String destinationFileName = String.format("%s%s%s", destinationDir, File.separator, fileName);
        try {
            PDFMergerUtility ut = new PDFMergerUtility();
            for (File file : files) {
                ut.addSource(file);
            }
            ut.setDestinationFileName(destinationFileName);
            ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            String fileNames = files.stream().map(File::getName).collect(Collectors.joining("\n"));
            infoTextArea.setText(String.format("[%d] files merged.%n%s%nFind the [%s] in here:%n%s",
                    files.size(), fileNames, fileName, destinationFileName
            ));
        } catch (IOException e) {
            logger.error("Something went wrong.", e);
            infoTextArea.setText("Something went wrong :(");
        }
    }
}
