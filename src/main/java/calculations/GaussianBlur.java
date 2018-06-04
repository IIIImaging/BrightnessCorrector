package calculations;

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

public class GaussianBlur {
	
	/**
	 * provides the option of attempting an blurr effect calculated by the gaussian bell curve on an 2D array representing an image
	 * @date 15.05.2018
	 * @author Sebastian Raßmann
	 */
	
	double sigma;
	int radius;
	double[][] kernel;
	
	public GaussianBlur(double sigma) {
		this.sigma = sigma;
		this.radius = (int) (3*sigma) + 1;
		this.kernel = getKernel();
	}

	public double[][] addBlurr(double[][] imageIn) {		
		int height = imageIn[0].length;
		int width = imageIn.length;
		double [][] imageOut = new double [width][height];
		
		int x,y, rx, ry, ix, iy;
		
		for(x = 0; x < width; x++) {
			for(y = 0; y < height; y++) {
				if(fitsOnImage(x,y,width,height)) {
					for(ix = 0, rx = x - radius; ix <= radius*2; ix++, rx++) {
						for(iy = 0, ry = y - radius; iy <= radius*2; iy++, ry++) {
						imageOut[x][y] += imageIn[rx][ry]*kernel[ix][iy]; 
						}
					}
				}
				else {
					double correction = 0;
					for(ix = 0, rx = x - radius;ix <= 2*radius; ix++, rx++) {
						if((rx >= 0) && (rx < width)) {
							for(iy = 0, ry = y - radius; iy <= 2*radius; iy++, ry++) {
								if((ry >= 0) && (ry < height)){
									imageOut[x][y] += imageIn[rx][ry]*kernel[ix][iy];
									correction += kernel[ix][iy];
								}
							}
						}
					}
					imageOut[x][y] /= correction;
				}
			}
		}	
		return imageOut;
	}

	private boolean fitsOnImage(int x, int y, int width,  int height) {
		if(x - radius >= 0 && x + radius < width && y - radius >= 0 && y + radius < height) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private double[][] getKernel() {
		double[][] kernel = new double [2*radius+1][2*radius+1];
		int x,ix, y,iy;
		double sum = 0;
		double prefactor = (1 / (2 * Math.PI * sigma * sigma));
		double twoSigmaSquare = (2 * sigma *sigma);
		double exp;
			for(ix = 0; ix  <=2*radius; ix++) {
				x = ix - radius;
				for(iy = 0; iy <= 2*radius; iy++) {
					y = iy - radius;
					exp = -(x*x+y*y) / twoSigmaSquare;
					kernel [ix][iy] = prefactor * Math.exp(exp);
					sum += kernel[ix][iy];
				}
			}
			for(double[] line : kernel) {
				for(@SuppressWarnings("unused") double v : line) {
					v /= sum;
				}
			}
		return kernel;
	}
}
