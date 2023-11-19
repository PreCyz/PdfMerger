package pg.merger;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class Controller implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @FXML
    private Button choosePdfsButton;
    @FXML
    private Button mergePdfsButton;
    @FXML
    private TextArea infoTextArea;
    @FXML
    private RadioButton sortByCreationTimeAscRadioButton;
    @FXML
    private RadioButton sortByCreationTimeDescRadioButton;
    @FXML
    private RadioButton sortByNameRadioButton;
    @FXML
    private TableView<TableContent> filesTableView;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;

    private ArrayList<File> files;
    private String destinationDir;

    private final Window parent;

    public Controller(Window parent) {
        this.parent = parent;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choosePdfsButton.setOnAction(choosePdfsAction());
        mergePdfsButton.setOnAction(mergePdfsAction());

        ToggleGroup toggleGroup = new ToggleGroup();
        sortByCreationTimeAscRadioButton.setToggleGroup(toggleGroup);
        sortByCreationTimeDescRadioButton.setToggleGroup(toggleGroup);
        sortByNameRadioButton.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener(radioChangeListener());

        TableColumn<TableContent, ?> column = filesTableView.getColumns().get(0);
        TableColumn<TableContent, String> nameColumn = new TableColumn<>();
        nameColumn.setText(column.getText());
        nameColumn.setPrefWidth(column.getPrefWidth());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        nameColumn.setEditable(false);

        column = filesTableView.getColumns().get(1);
        TableColumn<TableContent, String> vcsTypeColumn = new TableColumn<>();
        vcsTypeColumn.setText(column.getText());
        vcsTypeColumn.setPrefWidth(column.getPrefWidth());
        vcsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        vcsTypeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        vcsTypeColumn.setEditable(false);

        column = filesTableView.getColumns().get(2);
        TableColumn<TableContent, String> path = new TableColumn<>();
        path.setText(column.getText());
        path.setPrefWidth(column.getPrefWidth());
        path.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        path.setCellFactory(TextFieldTableCell.forTableColumn());
        path.setEditable(false);

        filesTableView.getColumns().clear();
        filesTableView.getColumns().addAll(nameColumn, vcsTypeColumn, path);
        filesTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        filesTableView.getSelectionModel().selectedIndexProperty().addListener(rowSelectedChangeListener());

        upButton.setDisable(true);
        upButton.setOnAction(moveUpActionEventHandler());

        downButton.setDisable(true);
        downButton.setOnAction(moveDownActionEventHandler());
    }

    private ChangeListener<Toggle> radioChangeListener() {
        return (observableValue, oldValue, newValue) -> {
            if (newValue.equals(sortByCreationTimeAscRadioButton)) {
                files = files.stream().sorted(creationTimeComparator(false)).collect(toCollection(ArrayList::new));
            } else if (newValue.equals(sortByCreationTimeDescRadioButton)) {
                files = files.stream().sorted(creationTimeComparator(true)).collect(toCollection(ArrayList::new));
            } else {
                files = files.stream().sorted(Comparator.comparing(File::getName)).collect(toCollection(ArrayList::new));
            }
            filesTableView.setItems(getTableViewItems());
            infoTextArea.clear();
            infoTextArea.setText("Table view sorted.");
        };
    }

    private ChangeListener<? super Number> rowSelectedChangeListener() {
        return (ChangeListener<Number>) (observableValue, oldVal, newVal) -> {
            upButton.setDisable(false);
            downButton.setDisable(false);
            if (newVal.intValue() == 0) {
                upButton.setDisable(true);
            } else if (newVal.intValue() == files.size() - 1) {
                downButton.setDisable(true);
            }
        };
    }

    private EventHandler<ActionEvent> moveUpActionEventHandler() {
        return actionEvent -> {
            int focusedIndex = filesTableView.getSelectionModel().getFocusedIndex();
            int moveIndex = focusedIndex - 1;
            swapElements(focusedIndex, moveIndex);
        };
    }

    private void swapElements(int originIndex, int dstIndex) {
        ArrayList<File> tmpFiles = new ArrayList<>(files);
        File file1 = tmpFiles.get(originIndex);
        File file2 = tmpFiles.get(dstIndex);
        tmpFiles.set(dstIndex, file1);
        tmpFiles.set(originIndex, file2);
        files = new ArrayList<>(tmpFiles);
        filesTableView.setItems(getTableViewItems());
        filesTableView.getSelectionModel().select(dstIndex);
    }

    private ObservableList<TableContent> getTableViewItems() {
        return FXCollections.observableList(
                files.stream()
                        .map(file -> new TableContent(file.getName(), file.getParent(), getCreationDate(file)))
                        .toList()
        );
    }

    private EventHandler<ActionEvent> moveDownActionEventHandler() {
        return actionEvent -> {
            int focusedIndex = filesTableView.getSelectionModel().getFocusedIndex();
            int moveIndex = focusedIndex + 1;
            swapElements(focusedIndex, moveIndex);
        };
    }

    private EventHandler<ActionEvent> choosePdfsAction() {
        return actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.setTitle("Choose PDFs");
            files = new ArrayList<>(
                    Optional.ofNullable(fileChooser.showOpenMultipleDialog(parent)).orElseGet(Collections::emptyList)
            );
            if (!files.isEmpty()) {
                infoTextArea.clear();
                infoTextArea.setText("Files loaded.");
                destinationDir = files.get(0).toPath().getParent().toString();
                filesTableView.setItems(getTableViewItems());
            }
        };
    }

    private String getCreationDate(File file) {
        LocalDateTime creationDate = LocalDateTime.now().plusYears(10);
        try {
            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long milliseconds = attributes.creationTime().to(TimeUnit.MILLISECONDS);
            if (milliseconds > Long.MIN_VALUE && milliseconds < Long.MAX_VALUE) {
                creationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
            }
        } catch (IOException exception) {
            infoTextArea.clear();
            infoTextArea.setText("Exception handled when trying to get file attributes: " + exception.getMessage());
        }
        return creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private EventHandler<ActionEvent> mergePdfsAction() {
        return actionEvent -> {
            if (files.isEmpty()) {
                infoTextArea.clear();
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
            String fileNames = files.stream().map(File::getName).collect(joining("\n"));
            infoTextArea.clear();
            infoTextArea.setText(String.format("[%d] files merged.%n%s%nFind the [%s] in here:%n%s",
                    files.size(), fileNames, fileName, destinationFileName
            ));
        } catch (IOException e) {
            logger.error("Something went wrong.", e);
            infoTextArea.clear();
            infoTextArea.setText("Something went wrong :( " + e.getMessage());
        }
    }

    private Comparator<File> creationTimeComparator(boolean isDesc) {
        return (o1, o2) -> {
            try {
                int multipler = isDesc ? 1 : -1;
                BasicFileAttributes f1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
                BasicFileAttributes f2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
                return multipler * f1.creationTime().compareTo(f2.creationTime());
            } catch (IOException e) {
                infoTextArea.clear();
                infoTextArea.setText("Could not calculate creation date: " + e.getMessage());
                return 0;
            }
        };
    }

}
