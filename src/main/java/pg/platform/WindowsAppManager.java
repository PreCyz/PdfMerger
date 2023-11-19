package pg.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

class WindowsAppManager implements AppManager {

    private static final Logger logger = LoggerFactory.getLogger(WindowsAppManager.class);

    @Override
    public void launchFileManager(String path) {
        try {
            LinkedList<String> command = new LinkedList<>();
            command.addFirst("explorer");
            command.add(path);
            new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.warn("Can not open file manager.", ex);
        }
    }

    @Override
    public void openPdf(String pdfPath) {
        try {
            LinkedList<String> command = new LinkedList<>();
            command.addFirst("explorer");
            command.add(pdfPath);
            new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.warn("Can not open browser.", ex);
        }
    }
}
