package com.mediate18.ocr.tools;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class ImagePreparation extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( ImagePreparation.class.getName() );

	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder or Zip file containing multiple images.");
		else {
			ImagePreparation processor = new ImagePreparation();
			processor.start(args);
		}
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		PageImage[] images = this.saveAllFiles ? image.splitVertical2AndSave() : image.splitVertical2();
		for (int i = 0; i < images.length; i++) {
			String fileName = images[i].getFileName();
			LOGGER.info("Processing file "+fileName);
			if (this.increaseContrast > 0) {
				LOGGER.info("* Increasing contrast");
				for (int j = 0; j < this.increaseContrast; j++)
					images[i].contrastImage(true);
				if (this.saveAllFiles) {
					images[i].setFileName(images[i].generateFilename("contrast"));
					images[i].writeImage(new ImageInfo(fileName));
				}
			}
			LOGGER.info("* Correcting skew");
			PageImage rlsaImage = images[i].generateRLSAImage(true).generateRLSAImage(false);
			if (!this.saveAllFiles) {
				File file = new File(rlsaImage.getFileName());
				file.delete();
			}
			double deg = rlsaImage.getSkewDegrees();
			if (deg == 0.0)
				LOGGER.info("-> Image is not skewed");
			else {
				if (this.saveAllFiles) {
					rlsaImage = rlsaImage.rotateAndSave(deg);
					images[i] = images[i].rotateAndSave(deg);
				} else {
					rlsaImage = rlsaImage.rotate(deg);
					images[i] = images[i].rotate(deg);
				}
			}
			LOGGER.info("* Cropping to margins");
			Rectangle margins = rlsaImage.getMargins();
			if (this.saveAllFiles)
				rlsaImage.cropAndSave(margins);
			images[i] = images[i].cropAndSave(margins);
			LOGGER.info("* Extracting lines");
			String base = FilenameUtils.getBaseName(fileName);
			String ext = FilenameUtils.getExtension(fileName);
			String path = FilenameUtils.getPathNoEndSeparator(fileName);
			String linePath = "/"+path+"/lines/"+base+"-line_extract_"+ext;
			new File(linePath).mkdirs();
			BufferedImage[] lines = images[i].getTextLines();
			for (int j = 0; j < lines.length; j++) {
				String jj = "0"+j;
				if (j > 9)
					jj = ""+j;
				BufferedImage line = lines[j];
				String newFileName = linePath+"/line"+jj+"."+ext;
				ImageIO.write(line, ext, new File(newFileName));
			}
		}
	}

}
