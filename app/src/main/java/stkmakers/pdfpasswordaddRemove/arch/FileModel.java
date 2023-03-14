package stkmakers.pdfpasswordaddRemove.arch;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FileModel {
    public FileModel() {}
    private File file;
    private boolean favorite;

    public FileModel(File file) {
        this.file = file;
    }
    private static final AtomicInteger count = new AtomicInteger(0);
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileModel fileModel = (FileModel) o;
        return favorite == fileModel.favorite && file.equals(fileModel.file);
    }
    @Override
    public int hashCode() {
        return Objects.hash(file, favorite);
    }

}
