package com.mediate18.ocr.tools;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class CorrectMargins extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( CorrectMargins.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder or Zip file containing multiple images.");
		else {
			CorrectMargins processor = new CorrectMargins();
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
			PageImage rlsaImage = images[i].generateRLSAImage(true).generateRLSAImage(false);
			generatedFiles.add(rlsaImage.getFileName());
			LOGGER.info("* Cropping to margins");
			Rectangle margins = rlsaImage.getMargins();
			rlsaImage = rlsaImage.cropAndSave(margins);
			generatedFiles.add(rlsaImage.getFileName());
			images[i].cropAndSave(margins);
			this.finish();
		}
	}

}
