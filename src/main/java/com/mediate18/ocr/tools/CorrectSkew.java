package com.mediate18.ocr.tools;

import java.io.IOException;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class CorrectSkew extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( CorrectSkew.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder or Zip file containing multiple images.");
		else {
			CorrectSkew processor = new CorrectSkew();
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
			}
			this.finish();
		}
	}

}
