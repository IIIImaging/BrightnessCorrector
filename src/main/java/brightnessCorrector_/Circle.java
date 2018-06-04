package brightnessCorrector_;

/** ===============================================================================
* BrightnessCorrector_.java Version 0.0.1
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation 
* (http://www.gnu.org/licenses/gpl.txt )
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/gpl-3.0.html>.
*  
* Copyright (C) 2018: Sebastian Raßmann, Jan N. Hansen, and Jan F. Jikeli;
* 		research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany
* 		(http://www.iii.uni-bonn.de/en/wachten_lab/).
* 
* Funding: DFG priority program SPP 1926
*    
* For any questions please feel free to contact me (rassmann@uni-bonn.de).
*
* =============================================================================== */

import java.awt.Point;
import java.util.ArrayList;

public class Circle {
	
	private int radius, diameter, listSize;
	private Point center;
	private Point[] leftBound, rightBound;				//right/leftmost points still inside of the circle
	private ArrayList<Pixel> brightestPixels;
	private double threshold = 0;						// lowest brightness value in the Array
	private int consideredPixels = 1; 					// defines the number of Pixels considered, by standard 1 so only the brightest pixel is considered
	private int imageHeight, imageWidth;
	private int resetCounter = 0;
		
	public Circle () {
		super();
	}
	
	public Circle (int radius, Point start, int consideredPixels, boolean enlargeList){
		this.center = new Point(start.x, start.y);
		this.radius = radius;
		diameter = 2*radius+1;
		leftBound = new Point[diameter];
		rightBound = new Point[diameter];
		int y = 0;
		for(int i = 0; i < diameter; i++) {
			y = start.y + (i - radius);
			leftBound[i] = new Point(start.x-calculateX(i - radius), y);
			rightBound[i] = new Point(start.x+calculateX(i - radius), y);
		}
		this.consideredPixels = consideredPixels;
		if(enlargeList) {
			this.listSize = consideredPixels + diameter;
		}
		else {
			this.listSize = consideredPixels + 1;
		}
		this.brightestPixels = new ArrayList<Pixel> (listSize);
	}
	
	/** calculates the highest pixel in the set location. Method has to be called before the Move-methods can be called! */
 	
 	public double initializePixelList (double [][] image) {
 		this.brightestPixels = new ArrayList<Pixel>(listSize);
 		threshold = 0;
 		imageHeight = image[0].length;
		imageWidth = image.length;
 		int x,y,i;
		for(i = 0; i < diameter; i++) {
			y = leftBound[i].y;
			if(y >= 0 && y < imageHeight) {			
				for(x = leftBound[i].x; x <= rightBound[i].x; x++) {
					if(x >= 0 && x < imageWidth) {
						insertPixelToList(x , y, image[x][y]);
					}
				}
			}
		}
		resetCounter++;
		return average();
	}

	/** moves the Circle and return the new average highest intensity value. Method can only be used after calling the {@link #initializePixelList(double[][]) calculateBrightestPixels} Command*/
 	
	public double moveRight(double[][] image) {
		moveRight();
		if(checkPixelsForBounds()) {return initializePixelList(image);}
		for(int i = 0; i < diameter; i++) {
			if(pixelIsOnImage(rightBound[i])) {	
				insertPixelToList(rightBound[i].x, rightBound[i].y, image[rightBound[i].x][rightBound[i].y]);	
			}
		}
		return average();
	}
	
	/** moves the Circle and return the new average highest intensity value. Method can only be used after calling the {@link #initializePixelList(double[][]) calculateBrightestPixels} Command*/
	
	public double moveLeft(double[][] image) {
		moveLeft();
		if(checkPixelsForBounds()) {return initializePixelList(image);}
		for(int i = 0; i < diameter; i++) {
			if(pixelIsOnImage(leftBound[i])) {
				insertPixelToList(leftBound[i].x, leftBound[i].y, image[leftBound[i].x][leftBound[i].y]);	
			}
		}
		return average();
	}
	
	/** moves the Circle and return the new average highest intensity value. Method can only be used after calling the {@link #initializePixelList(double[][]) calculateBrightestPixels} Command*/
	
	public double moveDownward(double[][] image) {
		moveDownward();
		if(checkPixelsForBounds()) {return initializePixelList(image);}
		for(int i = diameter-1; i >= radius; i--) {
			if(pixelIsOnImage(leftBound[i])) {
				insertPixelToList(leftBound[i].x,leftBound[i].y,image[leftBound[i].x][leftBound[i].y]);
			}
			if(pixelIsOnImage(rightBound[i])) {
				insertPixelToList(rightBound[i].x,rightBound[i].y,image[rightBound[i].x][rightBound[i].y]);
			}
		}	
		return average();
	}
	
	public void moveRight() {
		center.x ++;
		for(Point p : leftBound) {p.x++;}
		for(Point p : rightBound) {p.x++;}
	}
	
	public void moveLeft() {
		center.x --;
		for(Point p : leftBound) {p.x--;}
		for(Point p : rightBound) {p.x--;}
	}
	
	public void moveDownward() {
		center.y ++;
		for(Point p : leftBound) {p.y++;}
		for(Point p : rightBound) {p.y++;}
	}
	
	
	/**checks if the Pixels in the list are still inside the circle and deletes them if so. Return true if the list has shrunken a lot */
	
	private boolean checkPixelsForBounds() {
		for(int i = 0; i < brightestPixels.size(); i++) {
			if(distanceFromCenter(brightestPixels.get(i).p) > radius + 0.01) {
				brightestPixels.remove(i);
			}
		}
		if(brightestPixels.size() < listSize-radius/2) {return true;}
		else {return false;}
	}
	
	/** checks if the pixel's intensity is higher the lowest in the list and adds the pixel at the right position. Refreshes the lower border afterwards*/
	
	private void insertPixelToList(int x, int y, double intensity) {	
		if ((brightestPixels.size() >= listSize) && (intensity < threshold)) {
			return;
		}		
		if(brightestPixels.size() < listSize) {			//list has to be filled
			if(intensity > threshold && brightestPixels.size() != 0){	//appends at the right location in the list
				for(int p = 0; p < brightestPixels.size(); p++) {
					if(intensity >= brightestPixels.get(p).getIntensity()){		
						brightestPixels.add(p, new Pixel (x, y, intensity));
						break;
					}
				}
				threshold = brightestPixels.get(brightestPixels.size()-1).intensity;
				return;
			}
			else{
				brightestPixels.add(new Pixel (x, y, intensity));	//appends in the end of the list
				threshold = brightestPixels.get(brightestPixels.size()-1).intensity;
				return;
			}
		}
		else{															//appends in the right location and removes the Pixel with lowest brightness
			brightestPixels.remove(brightestPixels.size()-1);
			for(int p = 0; p < brightestPixels.size(); p++) {
				if(intensity >= brightestPixels.get(p).getIntensity()){
					brightestPixels.add(p, new Pixel (x, y, intensity));
					break;
				}
			}
		}
		threshold = brightestPixels.get(brightestPixels.size()-1).intensity;
		return;
}
	
	public int getResetCounter() {
		return resetCounter;
	}
		
	private boolean pixelIsOnImage (Point p) {
		if(p.x >= 0 && p.x < imageWidth && p.y > 0 && p.y < imageHeight) {
			return true;
		}else {
			return false;
		}
	}

	public String printList() {
		StringBuffer buf = new StringBuffer("\n");
		for(Pixel p : brightestPixels) {
			buf.append("Pixel @ 		" + p.printPixel() +"       distance to center : " + distanceFromCenter(p.p) + "\n");
		}
		return buf.toString();
	}
	
	private int calculateX (int y) {
		return (int) (Math.sqrt(radius*radius - y*y));
	}

	private double distanceFromCenter(Point p) {
		return Math.sqrt(((center.x-p.x)*(center.x-p.x))+((center.y-p.y)*(center.y-p.y)));		
	}
	
	private double average() {
		if(consideredPixels == 1) {
			return brightestPixels.get(0).intensity;
		}
		else {
			double value = 0;
			for(int i = 0; i < consideredPixels; i++) {
				value = value + brightestPixels.get(i).intensity;
			}
		return value/consideredPixels;
		}
	}	
}