package com.mediate18.ocr.image;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import com.mediate18.ocr.util.ColorDetector;
import com.mediate18.ocr.util.ImageSplitter;
import com.mediate18.ocr.util.MarginDetector;
import com.mediate18.ocr.util.RLSA;
import com.mediate18.ocr.util.SkewDetector;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;

public class PageImage extends MagickImage {
	private static int MARGIN = 50;
	private PageImage rlsaImage = null;
	private Rectangle cropInfo = null;

	public PageImage(ImageInfo origInfo) throws MagickException {
		super(origInfo);
	}
	
	public PageImage(String fileName) throws MagickException {
		super(new ImageInfo(fileName));
	}
	
	public PageImage(MagickImage image) throws MagickException {
		super(new ImageInfo(image.getFileName()));
	}

	public PageImage generateRLSAImage(boolean bidirectional) throws MagickException {
		IplImage image = this.getIplImage();
        if (image != null) {
        	RLSA rls = new RLSA();
            IplImage rlsa = rls.runLengthSmoothingAlgorithm(image, bidirectional);
            String rlsaFileName = this.generateFilename("rlsa").replace("rlsa.rlsa", "rlsa");
            cvSaveImage(rlsaFileName, rlsa);
            image.release();
            rlsa.release();
            rlsaImage = new PageImage(rlsaFileName);
            rlsaImage.negateImage(1);
            rlsaImage.writeImage(new ImageInfo(rlsaFileName));
            if (cropInfo != null) {
            	rlsaImage.setCropInfo(cropInfo);
            } else {
            	Dimension dim = this.getDimension();
	            int x = Math.max(rls.getLastXStart() - MARGIN, 0);
	            int y = Math.max(rls.getLastYStart() - MARGIN, 0);
	            int xSize = Math.min(rls.getLastXEnd() - rls.getLastXStart() + (2 * MARGIN), dim.width);
	            int ySize = Math.min(rls.getLastYEnd() - rls.getLastYStart() + (2 * MARGIN), dim.height);
				rlsaImage.setCropInfo(new Rectangle(x, y, xSize, ySize));
            }
        }
		return rlsaImage;
	}
	
	public BitSet[] getBitMap() throws MagickException {
		Dimension dim = this.getDimension();
		int height = dim.height;
		int width = dim.width;
		BitSet[] data = new BitSet[height];
		for (int y = 0; y < height; y++) {
			data[y] = new BitSet(width);
			for (int x = 0; x < width; x++) {
				int c = ColorDetector.isNotWhite(this.getOnePixel(x+1, y+1));
				if (c == 1)
					data[y].set(x);
			}
		}
		return data;
	}
	
	public Rectangle getCropInfo() {
		return cropInfo;
	}
	
	public void setCropInfo(Rectangle margins) {
		cropInfo = margins;
	}
	
	public BitSet[] getEmptyBitMap() throws MagickException {
		Dimension dim = this.getDimension();
		int height = dim.height;
		int width = dim.width;
		BitSet[] data = new BitSet[height];
		for (int y = 0; y < height; y++) {
			data[y] = new BitSet(width);
		}
		return data;
	}
	
	public Rectangle getMargins() throws MagickException, IOException {
		return MarginDetector.detect(this);
	}
	
	public int[][] getPixelMap() throws MagickException, IOException {
		BufferedImage image = this.toBufferedImage();
		if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		    convertedImg.getGraphics().drawImage(image, 0, 0, null);
		    image = convertedImg;
		}
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				if (argb > -5000000)
					result[row][col] = 0;
				else
					result[row][col] = 1;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				if (argb > -5000000)
					result[row][col] = 0;
				else
					result[row][col] = 1;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}
		return result;
	}
	
	public double getSkewDegrees() throws MagickException {
		return SkewDetector.detect(this);
	}
	
	public BufferedImage[] getTextLines() throws MagickException, IOException {
		ImageSplitter splitter = new ImageSplitter();
		return splitter.splitLines(this);
	}
	
	public BufferedImage crop(Rectangle margins) throws MagickException, IOException {
		if (margins == null)
			margins = this.getMargins();
		BufferedImage src = this.toBufferedImage();
//		Dimension dim = this.getDimension();
//		System.out.println("x: "+margins.x+", y: "+margins.y+", w: "+margins.width+" ("+dim.width+"), h: "+margins.height+" ("+dim.height+")");
		BufferedImage img = src.getSubimage(margins.x, margins.y, margins.width, margins.height);
		BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = copyOfImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		return copyOfImage;
	}
	
	public PageImage cropAndSave(Rectangle margins) throws MagickException, IOException {
		BufferedImage copyOfImage = this.crop(margins);
		String fileName = this.generateFilename("cropped");
	    ImageIO.write(copyOfImage, FilenameUtils.getExtension(fileName), new File(fileName));
		return new PageImage(fileName);
	}
	
	public PageImage rotate(double deg) throws MagickException {
		MagickImage rotated = this.rotateImage(deg); // rotate image
		return new PageImage(rotated);
	}
	
	public PageImage rotateAndSave(double deg) throws MagickException {
		MagickImage rotated = this.rotateImage(deg); // rotate image
		String degStr = String.valueOf(deg).replace("\\.", "_");
		String fileName = this.generateFilename("rotated_"+degStr);
		rotated.setFileName(fileName); //give new location
		rotated.writeImage(new ImageInfo(this.getFileName())); //save
		return new PageImage(fileName);
	}
	
	public PageImage[] splitVertical2AndSave() throws MagickException, IOException {
		ImageSplitter splitter = new ImageSplitter();
		PageImage[] pages = splitter.splitVertical(this); // split image
		for (int i = 0; i < pages.length; i++) {
			pages[i].setFileName(this.generateFilename("split"+i)); //give new location
			pages[i].writeImage(new ImageInfo(this.getFileName())); //save
		}
		return pages;
	}
	
	public BufferedImage toBufferedImage() throws MagickException, IOException {
		return ImageIO.read(new File(this.getFileName()));
	}
	
	private String extension() throws MagickException {
		return FilenameUtils.getExtension(this.getFileName());
	}
	
	private IplImage getIplImage() {
		IplImage image = null;
        try {
        	if (this.extension().equals("png"))
        		image = cvLoadImage(this.getFileName());
        	else {
	        	Java2DFrameConverter converter1 = new Java2DFrameConverter(); 
	            OpenCVFrameConverter.ToIplImage converter2 = new OpenCVFrameConverter.ToIplImage(); 
	            BufferedImage img = ImageIO.read(new File(this.getFileName())); 
	            image = converter2.convert(converter1.convert(img));
        	}
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
        return image;
	}
	
	public String generateFilename(String modifier) throws MagickException {
		String fileName = this.getFileName();
		String basename = FilenameUtils.getBaseName(fileName);
		String ext = FilenameUtils.getExtension(fileName);
		String baseDir = FilenameUtils.getFullPathNoEndSeparator(fileName);
		if (!baseDir.endsWith("output")) {
			baseDir += "/output";
			File outDir = new File(baseDir);
			if (!outDir.exists())
				outDir.mkdirs();
		}
		return baseDir + "/" + basename + "." + modifier + "." + ext;
		
	}
	
	public static PageImage fromBufferedImage(BufferedImage image) throws MagickException {
		Object data = null;
		int w = image.getWidth();
		int h = image.getHeight();
		MagickImage original = new MagickImage();
		DataBuffer buff = image.getRaster().getDataBuffer();
		if(buff.getDataType() == DataBuffer.TYPE_BYTE) {
			data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			original.constituteImage(w,h,"RGB",(byte[]) data);
		}
		return new PageImage(original);
	}

}
