package com.mainfolder.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class Implementationclass {

    // Here I using Apache Commons Math3 dependency for Calculating the Mathematical Operations
    public int[][] getImagePixelsdata(MultipartFile file, String sheetname) throws Exception {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            int width = image.getWidth();
            int height = image.getHeight();
            int[][] pixelValues = new int[height][width];

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = image.getRGB(j, i);
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;
                    int greyscale = (red + green + blue) / 3;
                    pixelValues[i][j] = greyscale;
                }
            }
            RealMatrix matrix = new Array2DRowRealMatrix(pixelValues.length, pixelValues[0].length);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    matrix.setEntry(i, j, pixelValues[i][j]);
                }
            }
            return pixelValues;
        } catch (IOException e) {
            return null;
        }
    }

    // Here I am Using Apache Commons Math3 dependencies for Simple calculations
    public double[] calculateMean(int[][] val) {
        double[] means = new double[val.length];
        for (int i = 0; i < val.length; i++) {
            Mean mean = new Mean();
            for (int j = 0; j < val[i].length; j++) {
                mean.increment(val[i][j]);
            }
            means[i] = mean.getResult();
        }
        return means;
    }

    // Using Apache Commons Math3 dependencies for Simple calculations
    public double[] calculateStdDev(int[][] val) {
        double[] stdDevs = new double[val.length];
        for (int i = 0; i < val.length; i++) {
            StandardDeviation stdDev = new StandardDeviation();
            for (int j = 0; j < val[i].length; j++) {
                stdDev.increment(val[i][j]);
            }
            stdDevs[i] = stdDev.getResult();
        }
        return stdDevs;
    }

    public double[] maxarr(double[] arr1, double[] arr2) {
        double[] result = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            result[i] = FastMath.max(arr1[i], arr2[i]);
        }
        return result;
    }
    public double[] minarr(double[] arr1, double[] arr2) {
        double[] result = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            result[i] = FastMath.min(arr1[i], arr2[i]);
        }
        return result;
    }
    public int[][] correlationMatrix(int[][] depth, int[][] intensity) {
        int height = depth.length;
        int width = depth[0].length;
        int[][] result = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = depth[i][j] - intensity[i][j];
            }
        }
        return result;
    }

    public boolean[] getBool(int[][] val) {
        boolean[] result = new boolean[val.length];
        for (int i = 0; i < val.length; i++) {
            result[i] = true;
            for (int j = 0; j < val[i].length; j++) {
                if (val[i][j] == 0) {
                    result[i] = false;
                    break;
                }
            }
        }
        return result;
    }

    public boolean[] getNoise(boolean[] arr1, boolean[] arr2) {
        boolean[] result = new boolean[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            result[i] = arr1[i] && arr2[i];
        }
        return result;
    }

	public static int[][] getGreenScaleImg(boolean[] greenScaleDf, int[][] intensity) {
		int rows = intensity.length;
		int cols = intensity[0].length;
		int k = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (k < greenScaleDf.length && !greenScaleDf[k]) {
					intensity[i][j] = 255;
					k++;
				}
			}
		}
		return intensity;
	}

    public void createOutputImage(int[][] greenScaleImg, boolean[] green_scale_df, String outputFolder) {
        int height = greenScaleImg.length;
        int width = greenScaleImg[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	int lightGreyThreshold = 190;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = greenScaleImg[y][x];
				if (pixel >= lightGreyThreshold) {
					image.setRGB(x, y, (0 << 16) | (255 << 8) | 0); 
				} 
				else {
					image.setRGB(x, y, (pixel << 16) | (pixel << 8) | pixel);
				}
			}
		} 
        try {
            ImageIO.write(image, "jpg", new File(outputFolder + "\\outputImage.jpg"));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
