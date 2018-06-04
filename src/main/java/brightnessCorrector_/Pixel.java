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
import java.util.Comparator;

public class Pixel {
	
	/**represents a Pixel with a 2D Point and and a double intensity value
	 * @author Sebastian Rassmann
	 */
	
	Point p;
	double intensity;
	
	public Pixel (Point p, double intensity) {
		this.p = p;
		this.intensity = intensity;
	}

	public Pixel (int x, int y, double intensity) {
		this.p = new Point (x,y);
		this.intensity = intensity;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	/**sort in descending order, returns either 1 or - 1	 */
	
	public static class PixelIntensityComparator implements Comparator<Pixel> {
	    public int compare(Pixel p1, Pixel p2) {
	        if((p1.getIntensity() < p2.getIntensity())) {
	        	return 1;
	        }
	        else {
	        	return -1;
	        }
	    }
	}
	
	/**sort in ascending order. Pastes the difference value	to the Comparator if it is > 1. Performs about 10% worse the {@link #PixelIntensityComparator Digital Comparator}  */
	
	public static class HybridPixelIntensityComparator implements Comparator<Pixel> {
	    public int compare(Pixel p1, Pixel p2) {
	        double difference = p1.getIntensity() - p2.getIntensity();	    	
	        if(difference < -1 || difference > 1) {
	        	return (int) difference;
	        }else if(difference < 0 && difference > -1) {
	        	return -1;
	        }else{
	        	return 1;
	        }
	    }
	}
	
	public String printPixel () {
		return (this.p.x + "	|" + this.p.y + "	|" + this.intensity);
	}
}
