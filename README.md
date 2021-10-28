Simple JavaFX to merge PDF files.

Maven version:  3.6.1 <br />
Java version:   11 <br />
JavaFX version: 11.0.2 <br />

Once the jar file is buid with `mvn clean install` then with following assumptions: <br />
1. `%J11%` - is the path the Java 11 home directory. <br />
2. `%JFX11%` - is the path to the `libs` directory from JavaFX 11.0.2 framework. <br />
3. `PdfMerger.jar` is the jar file that should be executed. <br />

below command launches the program: 
```
%J11%\bin\java --module-path %JFX11% --add-modules=javafx.fxml,javafx.controls,javafx.media,javafx.web,javafx.swing --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED -jar "PdfMerger.jar"
```
