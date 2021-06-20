package proyectofinalacr.utils;

import java.io.File;

public class FilesUtil {

    public static File returnFile(String name, File directory) {
        File fileSearched=null;

        File[] list = directory.listFiles();
        if(list!=null)
            for (File file : list) {
                if (!file.isDirectory() && name.equalsIgnoreCase(file.getName())) {
                    fileSearched=file;

                }
            }
        return fileSearched;
    }

    public static boolean isFileOnDirectory(String name, File directory) {
        File fileSearched=null;

        File[] list = directory.listFiles();
        if(list!=null)
            for (File file : list) {
                if (!file.isDirectory() && name.equalsIgnoreCase(file.getName())) {
                    fileSearched=file;

                }
            }
        return fileSearched!=null;
    }
}
