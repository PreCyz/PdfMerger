Simple JavaFX to merge PDF files.

Maven version:  3.8.7 <br />
Java version:   17 <br />
JavaFX version: 17.0.9 <br />

The jar file which is built with `mvn clean package` command is a fat jar. If your OS supports launching applications 
with double-click, then this is how you execute the jar file.<br /><br />

If you need more sophisticated ways then use below instruction
```
java -jar PdfMerger.jar
```

Or turbo complex way. Once the jar file is built with `mvn clean package` then with following assumptions: <br />
1. `%J17%` - is the path the Java 17 home directory. <br />
2. `%JFX17%` - is the path to the `libs` directory from JavaFX 17.0.9 framework. <br />
3. `PdfMerger.jar` is the jar file that should be executed. <br />

below command launches the program: 
```
%J17%\bin\java --module-path %JFX17% --add-modules=javafx.fxml,javafx.controls,javafx.media,javafx.web,javafx.swing --add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED -jar "PdfMerger.jar"
```
