package render;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

/**
 *
 * @author Alex
 */
public class Util {

    public static String getInstallPath() {
        if (installPath == null) {
            URL location = Util.class.getProtectionDomain().getCodeSource().getLocation();
            String programPath = location.getPath().substring(1).replace('/', File.separatorChar);
            try {
                programPath = URLDecoder.decode(programPath, "utf8");
            } catch (Exception e) {}
            int slashes = 3;
            if (programPath.endsWith(".jar")) {
                slashes--;
            }
            for (int i = 0; i < slashes; i++) {
                int index = programPath.lastIndexOf(File.separatorChar);
                programPath = programPath.substring(0, index);
            }
            installPath = programPath;
        }
        return installPath;
    }

    public static String installPath;
}
