package com.mediate18.ocr.tools;

import java.io.IOException;
import java.util.logging.Logger;

import com.mediate18.ocr.image.PageImage;

import magick.ImageInfo;
import magick.MagickException;

public class RunLengthSmoothing extends OCRTool {
	private static final Logger LOGGER = Logger.getLogger( RunLengthSmoothing.class.getName() );
	
	public static void main(String[] args) throws MagickException, IOException {
		if (args.length == 0)
			LOGGER.severe("No input file provided! It can be either a single image file or a folder or Zip file containing multiple images.");
		else {
			RunLengthSmoothing processor = new RunLengthSmoothing();
			processor.start(args);
		}
	}

	@Override
	protected void run(PageImage image) throws MagickException, IOException {
		PageImage[] images = this.saveAllFiles ? image.splitVertical2AndSave() : image.splitVertical2();
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
			images[i].generateRLSAImage(true).generateRLSAImage(false);
			this.finish();
		}
	}

}
