package dk.stonemountain.wine.services;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.stonemountain.wine.domain.Wine;
import dk.stonemountain.wine.error.EntityNotFoundException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class WineService implements PanacheRepository<Wine> {
    private static final Logger LOG = LoggerFactory.getLogger(WineService.class);

	// public Wine update(Long id, Wine wineToSave) {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'update'");
	// }

	// public boolean delete(Long id) {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'delete'");
	// }

	// public Wine add(Wine wineToSave) {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'add'");
	// }

 //    public Long count() {
 //        // TODO Auto-generated method stub
 //        throw new UnsupportedOperationException("Unimplemented method 'count'");
 //    }

	public List<Wine> list(Page page, Sort sort) {
	    var query = findAll(sort).page(page);
		return query.list();
	}

	public InputStream getImage(Wine wine, boolean icon) {
	    try {
			return icon == false ? wine.image.getBinaryStream() : wine.icon.getBinaryStream();
		} catch (SQLException e) {
		    throw new EntityNotFoundException("Image could not be read", wine.id, Wine.class);
		}
	}

	public void putImage(Wine wine, File image) {
	    try {
    		LOG.debug("Loading input into blob: {}", image, Files.size(image.toPath()));
            wine.image = BlobProxy.generateProxy(new FileInputStream(image), Files.size(image.toPath()));
            var tempFile = Files.createTempFile("wine-" + wine.id + "-icon-" , ".png");
            resizePngToIcon(image, tempFile, 100);
            LOG.debug("Icon {} has size {}", tempFile, Files.size(tempFile));
            wine.icon = BlobProxy.generateProxy(new FileInputStream(tempFile.toFile()), Files.size(tempFile));
		} catch (IOException e) {
		    LOG.error("Blob operation failed", e);
		}
	}

	public static void resizePngToIcon(File input, Path output, int iconSize) throws IOException {
        BufferedImage src = ImageIO.read(input);
        if (src == null) {
            throw new IOException("Couldn't read image: " + input);
        }

        // Destination square canvas with alpha
        BufferedImage dst = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clear with full transparency
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, iconSize, iconSize);
            g.setComposite(AlphaComposite.SrcOver);

            // Scale to fit within iconSize while preserving aspect ratio
            double scale = Math.min(iconSize / (double) src.getWidth(), iconSize / (double) src.getHeight());
            int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(src.getHeight() * scale));
            int x = (iconSize - w) / 2;
            int y = (iconSize - h) / 2;

            g.drawImage(src, x, y, w, h, null);
        } finally {
            g.dispose();
        }

        ImageIO.write(dst, "png", output.toFile());
    }

}
