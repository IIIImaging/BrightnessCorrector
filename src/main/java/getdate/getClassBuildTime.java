package getdate;

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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class getClassBuildTime{
	
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("HH:mm:ss_yyyy/MM/dd");

/**
 * Handles files, jar entries, and deployed jar entries in a zip file (EAR).
 * @return The date if it can be determined, or null if not.
 */
	
public static Date getDate() {
    Date d = null;
    Class<?> currentClass = new Object() {}.getClass().getEnclosingClass();
    URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
    if (resource != null) {
        if (resource.getProtocol().equals("file")) {
            try {
                d = new Date(new File(resource.toURI()).lastModified());
            } catch (URISyntaxException ignored) { }
        } else if (resource.getProtocol().equals("jar")) {
            String path = resource.getPath();
            d = new Date( new File(path.substring(5, path.indexOf("!"))).lastModified() ); 
        }
    }
    return d;
}

public static String getDateString() {
    Date d = null;
    Class<?> currentClass = new Object() {}.getClass().getEnclosingClass();
    URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
    if (resource != null) {
        if (resource.getProtocol().equals("file")) {
            try {
                d = new Date(new File(resource.toURI()).lastModified());
            } catch (URISyntaxException ignored) { }
        } else if (resource.getProtocol().equals("jar")) {
            String path = resource.getPath();
            d = new Date( new File(path.substring(5, path.indexOf("!"))).lastModified() ); 
        }
    }
    return NameDateFormatter.format(d);
}

}