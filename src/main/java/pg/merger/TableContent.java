package pg.merger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableContent {
    private StringProperty fileName;
    private StringProperty filePath;
    private StringProperty creationDate;

    public TableContent(String fileName, String filePath, String creationDate) {
        this.fileName = new SimpleStringProperty(fileName);
        this.filePath = new SimpleStringProperty(filePath);
        this.creationDate = new SimpleStringProperty(creationDate);
    }

    public String getFileName() {
        return fileName.get();
    }

    public String getFilePath() {
        return filePath.get();
    }

    public String getCreationDate() {
        return creationDate.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    public void setCreationDate(String creationDate) {
        this.creationDate.set(creationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableContent that = (TableContent) o;

        if (!fileName.equals(that.fileName)) return false;
        if (!filePath.equals(that.filePath)) return false;
        return creationDate.equals(that.creationDate);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + filePath.hashCode();
        result = 31 * result + creationDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TableContent{" +
                "fileName=" + fileName +
                ", filePath=" + filePath +
                ", creationDate=" + creationDate +
                '}';
    }
}
