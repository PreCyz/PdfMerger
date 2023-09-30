package pg.merger;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @FXML
    private Button choosePdfsButton;
    @FXML
    private Button mergePdfsButton;
    @FXML
    private TextArea infoTextArea;
    @FXML
    private CheckBox sortByCreationDateAscCheckBox;
    @FXML
    private CheckBox sortByCreationDateDescCheckBox;
    @FXML
    private CheckBox sortByNameCheckBox;

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
        sortByCreationDateDescCheckBox.selectedProperty().addListener(sortByCreationTimeChangeListener(true));
        sortByCreationDateAscCheckBox.selectedProperty().addListener(sortByCreationTimeChangeListener(false));
        sortByNameCheckBox.selectedProperty().addListener(sortByNameChangeListener());
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

    private ChangeListener<Boolean> sortByCreationTimeChangeListener(boolean isDesc) {
        return (observableValue, oldValue, newValue) -> {
            if (newValue) {
                files = files.stream().sorted(creationTimeComparator(isDesc)).collect(Collectors.toCollection(LinkedList::new));
                infoTextArea.clear();
                infoTextArea.setText(files.stream().map(File::getName).collect(Collectors.joining("\n")));
                sortByNameCheckBox.setSelected(false);
                if (isDesc) {
                    sortByCreationDateAscCheckBox.setSelected(false);
                } else {
                    sortByCreationDateDescCheckBox.setSelected(false);
                }
            }
        };
    }

    private Comparator<File> creationTimeComparator(boolean isDesc) {
        return (o1, o2) -> {
            try {
                int multipler = isDesc ? 1 : -1;
                BasicFileAttributes f1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
                BasicFileAttributes f2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
                return multipler * f1.creationTime().compareTo(f2.creationTime());
            } catch (IOException e) {
                return 0;
            }
        };
    }

    private ChangeListener<? super Boolean> sortByNameChangeListener() {
        return (observableValue, oldValue, newValue) -> {
            if (newValue) {
                files = files.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toCollection(LinkedList::new));
                infoTextArea.clear();
                infoTextArea.setText(files.stream().map(File::getName).collect(Collectors.joining("\n")));
                sortByCreationDateDescCheckBox.setSelected(false);
                sortByCreationDateAscCheckBox.setSelected(false);
            }
        };
    }
}
