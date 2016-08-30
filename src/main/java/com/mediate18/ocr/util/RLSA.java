package com.mediate18.ocr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import org.bytedeco.javacpp.opencv_core.IplImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class RLSA {
	private Properties prop = new Properties();
	private int lastXStart = -1;
	private int lastXEnd = 0;
	private int lastYStart = -1;
	private int lastYEnd = 0;
	
	public RLSA() {
		InputStream input = null;
		try {
			File jarPath = new File(RLSA.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	        String propertiesPath = jarPath.getParentFile().getAbsolutePath();
	        File customPropFile = new File(propertiesPath+"/rlsa.properties");
	        if (customPropFile.exists() && customPropFile.canRead())
	        	input = new FileInputStream(propertiesPath+"/rlsa.properties");
	        else
	        	input = getClass().getClassLoader().getResourceAsStream("rlsa.properties");
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getLastXStart() {
		return lastXStart;
	}
	
	public int getLastXEnd() {
		return lastXEnd;
	}
	
	public int getLastYStart() {
		return lastYStart;
	}
	
	public int getLastYEnd() {
		return lastYEnd;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public IplImage runLengthSmoothingAlgorithm(IplImage image, boolean bidirectional) {
        IplImage gry = image.clone();
        cvThreshold(gry, gry, 128, 255, CV_THRESH_BINARY_INV);
        lastXStart = -1;
    	lastXEnd = 0;
    	lastYStart = -1;
    	lastYEnd = 0;
        
        CvMat tmpImg = gry.asCvMat();
        ByteBuffer buffer = gry.getByteBuffer();

        CvScalar temp = new CvScalar();
        temp.val(255);

        int hor_thres = Integer.valueOf(prop.getProperty("horizontal_threshold1", "44")); // adjust for your text size
        if (!bidirectional)
        	hor_thres = Integer.valueOf(prop.getProperty("horizontal_threshold2", "44"));
        int zero_count = 0;
        int one_flag = 0;
        for (int i = 0; i < tmpImg.rows(); i++) {
            for (int j = 0; j < tmpImg.cols(); j++) {
                int ind = i * gry.widthStep() + j * gry.nChannels() + 1;
                double val = -1;
                if (ind < buffer.capacity()) {
                    val = (buffer.get(ind) & 0xFF);
                }
                if (val == 255) {
                    if (one_flag == 255) {
                        if (zero_count <= hor_thres) {
                        	if (lastXStart == -1 || lastXStart > j)
                        		lastXStart = j;
                        	if (lastXEnd < j)
                        		lastXEnd = j;
                        	if (lastYStart == -1 || lastYStart > i)
                        		lastYStart = i;
                        	if (lastYEnd < j)
                        		lastYEnd = i;
                            tmpImg.put(i, j, 255);
                            for (int n = (j-zero_count); n < j; n++) {
                                tmpImg.put(i, n, 255);
                            }
                        } else {
                            one_flag = 0;
                        }
                        zero_count = 0;
                    }
                    one_flag = 255;
                } else if (one_flag == 255) {
                    zero_count = zero_count + 1;
                }
            }
        }

        if (bidirectional) {
	        int ver_thres = Integer.valueOf(prop.getProperty("vertical_threshold", "44")); // adjustable
	        zero_count = 0;
	        one_flag = 0;
	        for (int i = 0; i < tmpImg.cols(); i++) {
	            for (int j = 0; j < tmpImg.rows(); j++) {
	                int ind = j * gry.widthStep() + i * gry.nChannels() + 1;
	                double val = -1;
	                if (ind < buffer.capacity()) {
	                    val = (buffer.get(ind) & 0xFF);
	                }
	                if (val == 255) {
	                    if (one_flag == 255) {
	                        if (zero_count <= ver_thres) {
	                        	if (lastXStart == -1 || lastXStart > i)
	                        		lastXStart = i;
	                        	if (lastXEnd < i)
	                        		lastXEnd = i;
	                        	if (lastYStart == -1 || lastYStart > j)
	                        		lastYStart = j;
	                        	if (lastYEnd < j)
	                        		lastYEnd = j;
	                            tmpImg.put(j, i, 255);
	                            for (int n = ((j-zero_count) >= 0) ? (j-zero_count) : 0; n < j; n++) {
	                                tmpImg.put(n, i, 255);
	                            }
	                        } else {
	                            one_flag = 0;
	                        }
	                        zero_count = 0;
	                    }
	                    one_flag = 255;
	                } else if (one_flag == 255) {
	                    zero_count = zero_count + 1;
	                }
	            }
	        }
        }
        
        return gry;
    }
	
}
