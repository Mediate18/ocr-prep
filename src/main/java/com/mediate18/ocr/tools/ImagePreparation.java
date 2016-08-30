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
		PageImage[] images = image.splitVertical2AndSave();
		for (int i = 0; i < images.length; i++) {
			String fileName = images[i].getFileName();
			generatedFiles.add(fileName);
			LOGGER.info("Processing file "+fileName);
			if (this.increaseContrast > 0) {
				LOGGER.info("* Increasing contrast");
				for (int j = 0; j < this.increaseContrast; j++)
					images[i].contrastImage(true);
				String newFileName = images[i].generateFilename("contrast");
				images[i].setFileName(newFileName);
				images[i].writeImage(new ImageInfo(fileName));
				generatedFiles.add(newFileName);
			}
			LOGGER.info("* Correcting skew");
			PageImage rlsaImage = images[i].generateRLSAImage(true).generateRLSAImage(false);
			generatedFiles.add(rlsaImage.getFileName());
			double deg = rlsaImage.getSkewDegrees();
			if (deg == 0.0)
				LOGGER.info("-> Image is not skewed");
			else {
				rlsaImage = rlsaImage.rotateAndSave(deg);
				generatedFiles.add(rlsaImage.getFileName());
				images[i] = images[i].rotateAndSave(deg);
				generatedFiles.add(images[i].getFileName());
			}
			LOGGER.info("* Cropping to margins");
			Rectangle margins = rlsaImage.getMargins();
			rlsaImage = rlsaImage.cropAndSave(margins);
			generatedFiles.add(rlsaImage.getFileName());
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
			this.finish();
		}
	}

}
