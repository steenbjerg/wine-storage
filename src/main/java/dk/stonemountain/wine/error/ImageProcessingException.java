package dk.stonemountain.wine.error;

import java.io.File;

public class ImageProcessingException extends RuntimeException {
    final String path;
    final long size;

    public ImageProcessingException(File image, Exception e) {
        super(String.format("Image File %s of size %s failed: %s", image.getAbsoluteFile(), image.length(), e.getMessage()), e);
        this.path = image.getAbsolutePath();
        this.size = image.length();
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }
}
