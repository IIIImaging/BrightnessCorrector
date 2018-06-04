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

import ij.IJ;
import ij.ImagePlus;
import exceptions.ChoosenNumberOfChannelsDoesNotMatchImpException;

public class StackAsArray {
	
	/**
	 * contains the image as an 5D Array and provides useful method to access and process the new image
	 * @date 20.04.2018
	 * @author Sebastian Raßmann
	 */
	
	boolean[] usedchannels;				//[0] is empty!!!!
	boolean compressImage = false;
	int compressionFactor = 1;
	int numberOfUsedChannels = 0;
	int channels, width, height, cWidth, cHeight, slices, frames, bitdepth;
	double [][][][][] image;				// 5 dimensional representing:  c - z - t - x - y  (color channel - line - column - depth - time)
	
	double[][] compressedSlice;
	
	double [][][] originalmin, originalmax;
	double newmin, newmax;
	
	int current_slice, current_frame, current_channel;
	double rangeinslice;
	
	boolean stackisset = false;
	boolean maxminset = false;
	
	
	public StackAsArray() {
		super();
	}
	
	/**creates StackAsArray Class out of an ImagePlus for processing */
	
	public StackAsArray(ImagePlus imp, boolean [] usedChannels, int compressionFactor, int channels) throws ChoosenNumberOfChannelsDoesNotMatchImpException  {
		this.channels = channels;
		if(channels != imp.getNChannels()) {
			IJ.log("channels defined: " + channels + "     from IJ: " + imp.getNChannels());
			throw new ChoosenNumberOfChannelsDoesNotMatchImpException("Number of channels choosen by the user does not match number of channels of the Image Plus ");
		}
		if(compressionFactor > 1) {
			this.compressImage = true;
			this.compressionFactor = compressionFactor;
			}
		usedchannels = usedChannels;
		this.width = imp.getWidth();
		this.height = imp.getHeight();
		this.slices = imp.getNSlices();
		this.frames = imp.getNFrames();
		this.bitdepth = imp.getBitDepth();
		image = new double [channels][slices][frames][width][height];
		this.cHeight = (int) height/this.compressionFactor + 1;
		this.cWidth = (int) width/this.compressionFactor + 1;
		copyToArrayAndGetLimits(imp);
	}

	/**sets the Indices for accessing the correct slices (channel, slice, frame) for other methods
	 */
	
	public void setIndices (int channel, int slice, int frame) {
		this.stackisset = true;
		this.current_channel = channel;
		this.current_frame = frame;
		this.current_slice = slice;
		this.rangeinslice = getOrignalRange();
		if(compressImage)compressSlice();
	}
	
	private void compressSlice() {
		int compressedHeight = (int) (height/compressionFactor) +1;
		int compressedWidth = (int) (width/compressionFactor) +1;
		compressedSlice = new double [compressedWidth][compressedHeight];
		int ix, iy, jx, jy;
		for(ix = 0; ix < compressedWidth; ix++) {
			for(iy = 0; iy < compressedHeight; iy++) {
				double sum = 0;
				for(jx = 0; jx < compressionFactor; jx++) {
					for(jy = 0; jy < compressionFactor; jy++) {
						if(ix*compressionFactor + jx < width && iy*compressionFactor + jy < height) {
							sum += image [current_channel][current_slice][current_frame][ix*compressionFactor + jx][iy*compressionFactor + jy];
						}					
					}
				}
				compressedSlice[ix][iy] = sum / (compressionFactor*compressionFactor);
			}
		}
	}
	

	/**sets range for the {@link #setToOriginalRange (double intensity) setToOriginalRange} Command
	 */
	
	public void setNewRange (double min, double max) {		
		this.maxminset = true;
		this.newmin = min;
		this.newmax = max;
	}
	
	public double [][] getSliceArray (int channel, int slice, int frame){
		return image [channel][slice][frame];
	}
	
	public double [][] getCompressedSliceArray (){		
		if(stackisset){
			if(compressionFactor==1)return image [current_channel][current_slice][current_frame];
			else return compressedSlice;
		}else {
			return zeroArray();
		}
	}
	
	/** returns the 2D array of the original picture's intensities at the position set by the {@link #setIndices(byte, int, int) setIndices} Command
	 */
	
	public double [][] getSliceArray (){
		if(stackisset){
			return image [current_channel][current_slice][current_frame];
		}else {
			return zeroArray();
		}
	}

	/** returns the value of pixel of the original picture the position set by the {@link #setIndices(byte, int, int) setIndices} Command and the x,y params
	 */
	public double getPixelValue (int x, int y){
		if(stackisset){
		return image [current_channel][current_slice][current_frame][x][y];
		}else {
			return 0;
		}
	}
	
	public double getOriginalMax (){
		if(stackisset){
		return originalmax [current_channel][current_slice][current_frame];
		}else {
			return 0;
		}
	}
	
	public double getOriginalMin (){
		if(stackisset){
		return originalmin [current_channel][current_slice][current_frame];
		}else {
			return 0;
		}
	}
	
	public double getOrignalRange () {
		if(stackisset){
		return getOriginalMax () - getOriginalMin ();
		}else {
			return 0;
		}
	}
	
	/**
	 * resets the intensity to the original range in the slice set by the {@link #setIndices(byte, int, int) setIndices} Command. 
	 * the algorithm subtracts the inserted intensity from the minimum value ( @param min ), divides it by the image's range ( @param max - @param min ), multiplies it by the original range and adds the original minimum
	 */
	
	public double setToOriginalRange (double intensity, double min, double max) {		
		if(stackisset){
		return ( (intensity - min) * ( rangeinslice / (max - min) ) ) +  this.getOriginalMin();
		}else {
			return 0;
		}
	}
	
//	double newvaluessetto0 = image[x][y] - image_min;
//	double oldrange = originalStack.getOriginalMax() - originalStack.getOriginalMin();
//	double newrange = image_max - image_min;
//	image[x][y] = newvaluessetto0 * (oldrange/newrange) +  originalStack.getOriginalMin();

	
	/**
	 * resets the intensity to the original range in the slice set by the {@link #setIndices(byte, int, int) setIndices} Command and the min and max value in the calculated slice set by {@link #setNewRange (double min, double max) setNewRange}.
	 * The algorithm subtracts the inserted intensity from the minimum value, divides it by the image's range (max - min), multiplies it by the original range and adds the original minimum
	 */
	
	public double setToOriginalRange (double intensity) {		
		if(stackisset && maxminset){
			return ( (intensity - newmin) * ( rangeinslice / (newmax - newmin) ) ) +  this.getOriginalMin();
		}else {
			return 0;
		}
	}

	private double[][] zeroArray() {
		double [][] array = new double [width][height];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				array[x][y] = 0;
			}
		}
		IJ.log("Zero Array created");
		return array;
	}
	
	/**
	 * fills an 5D double Array with data from Hyperstack and calculates the range. NOTE: indices of array are 0 based, whilst IJ's are 1 based
	 * @param imp ImagePlus from where voxel data is taken
	 */
	
	private void copyToArrayAndGetLimits(ImagePlus imp) {
		
		int c, x, y, z, t, stackindex;
		double voxelvalue;
		originalmax = new double[channels][slices][frames];
		originalmin = new double[channels][slices][frames];
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		for(c = 0; c < channels; c++) {
			for(t = 0; t < frames; t++) {
				for(z = 0; z < slices; z++) {
					stackindex = imp.getStackIndex(c+1, z+1, t+1)-1;
					for(x = 0; x < width; x++) {
						for(y = 0; y < height; y++) {
							voxelvalue = imp.getStack().getVoxel(x, y, stackindex);	
							image [c][z][t][x][y] = voxelvalue;
							if(voxelvalue < min) {
								min = voxelvalue;
							}
							if(voxelvalue > max) {
								max = voxelvalue;
							}
						}
					}
					originalmin [c][z][t] = min;
					originalmax [c][z][t] = max;
				}
			}
		}	
	
	}	
}
	
	
	
	
	
	


