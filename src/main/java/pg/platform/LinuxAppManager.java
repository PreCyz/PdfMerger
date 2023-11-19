package pg.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class LinuxAppManager implements AppManager {

    private static final Logger logger = LoggerFactory.getLogger(LinuxAppManager.class);

    @Override
    public void launchFileManager(String path) {
        try {
            List<String> command = new LinkedList<>();
            command.add("xdg-open");
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
            command.add("x-www-browser");
            command.add(pdfPath);
            new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.warn("Can not open browser.", ex);
        }
    }
}
