package com.mediate18.ocr.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.mediate18.ocr.image.PageImage;

import magick.MagickException;

public abstract class OCRTool {
	private static final Logger LOGGER = Logger.getLogger( OCRTool.class.getName() );
	protected static List<String> ALLOWED = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 3551467596496765208L;

	{
	    add("png");
	    add("jpg");
	    add("zip");
	}};
	protected static ZipFile fis;
	protected int increaseContrast = 0;
	protected boolean saveAllFiles = false;
	protected List<String> generatedFiles = new ArrayList<String>();

	protected void start(String[] args) throws IOException, MagickException {
		if (args.length > 1) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-c"))
					this.increaseContrast = Integer.valueOf(args[i+1]);
				else if (args[i].equals("--saveAll") || (args[i].equals("-s") && args[i+1].equals("true")))
					this.saveAllFiles = true;
			}
		}
		String inputFileName = args[args.length-1];
		File inputFile = new File(inputFileName);
		if (!inputFile.exists())
			System.err.println("File or folder not found: "+inputFileName);
		else if (!inputFile.canRead())
			System.err.println("File or folder not readable: "+inputFileName);
		else if (inputFile.isDirectory()) {
			File[] listOfFiles = inputFile.listFiles();
		    for (int i = 0; i < listOfFiles.length; i++) {
		    	if (listOfFiles[i].isFile()) {
		    		String ext = FilenameUtils.getExtension(listOfFiles[i].getAbsolutePath());
		    		if (ALLOWED.contains(ext))
		    			if (ext.equals("zip"))
		    				this.processZipFile(listOfFiles[i].getAbsolutePath());
		    			else
		    				this.processImageFile(listOfFiles[i].getAbsolutePath());
		    	}
		    }
		} else {
			String ext = FilenameUtils.getExtension(inputFileName);
    		if (ALLOWED.contains(ext))
    			if (ext.equals("zip"))
    				this.processZipFile(inputFile.getAbsolutePath());
    			else
    				this.processImageFile(inputFile.getAbsolutePath());
		}
	}
	
	protected void processImageFile(String path) throws MagickException, IOException {
		run(new PageImage(path));
	}

	protected void processZipFile(String path) throws IOException, MagickException {
		fis = new ZipFile(path);
		String folder = FilenameUtils.getFullPath(path);
		for (Enumeration<?> e = fis.entries(); e.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			String fileName = entry.getName();
			String filePath = folder+fileName;
			String ext = FilenameUtils.getExtension(filePath);
			if (!fileName.startsWith("_") && !fileName.startsWith(".") && ALLOWED.contains(ext) && !ext.equals("zip")) {
				LOGGER.info("Image file: "+filePath);
				InputStream in = this.getEntry(entry);
				BufferedImage bf = ImageIO.read(in);
				File outputFile = new File(filePath);
				ImageIO.write(bf, ext, outputFile);
				this.generatedFiles.add(filePath);
				PageImage image = new PageImage(filePath);
				image.setFileName(filePath);
				run(image);
			}
		}
	}
	
	private ByteArrayInputStream getEntry(ZipEntry entry) throws IOException {
	    int entrySize = (int)entry.getSize();
        BufferedInputStream bis = new BufferedInputStream(fis.getInputStream(entry));
        byte[] finalByteArray = new byte[entrySize];
        int bufferSize = 2048;
        byte[] buffer = new byte[2048];
        int chunkSize = 0;
        int bytesRead = 0;
        while (true) {
            //Read chunk to buffer
            chunkSize = bis.read(buffer, 0, bufferSize); //read() returns the number of bytes read
            if (chunkSize == -1) {
                //read() returns -1 if the end of the stream has been reached
                break;
            }
            //Write that chunk to the finalByteArray
            //System.arraycopy(src, srcPos, dest, destPos, length)
            System.arraycopy(buffer, 0, finalByteArray, bytesRead, chunkSize);
            bytesRead += chunkSize;
        }
        bis.close(); //close BufferedInputStream
        return new ByteArrayInputStream(finalByteArray);
	}
	
	protected abstract void run(PageImage image) throws MagickException, IOException;
	
	protected void finish() {
		if (!this.saveAllFiles) {
			for (String file : generatedFiles) {
				File f = new File(file);
				if (f.exists())
					f.delete();
			}
		}
	}

}
