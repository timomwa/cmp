/* Copyright (c) Inmobia Mobile Technology, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Inmobia 
 * Mobile Technology. ("Confidential Information").  You shall not disclose such 
 * Confidential Information and shall use it only in accordance with the terms 
 * of the license agreement you entered into with Inmobia Mobile Technology.
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.inmobia.axiata.reports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * 
 * Utils, contains helper methods used by classes in the application
 * 
 * @author <a href="mailto:montell@inmobia.com">Montell Tome</a>
 * @version 1.0, August 29, 2011
 * @since jdk 1.6
 */
public class Utils {

	private static final Logger LOGGER = Logger.getLogger(Utils.class);
	/**
	 * WORKING_DIR, current directory
	 */
	public static final String USER_HOME = System.getProperty("user.home");
	/**
	 * PATH_SEPARATOR, file path separator Unix(/) Windows(\)
	 */
	public static final String PATH_SEPARATOR = System
			.getProperty("file.separator");
	/**
	 * formatter,used to format date
	 */
	private static SimpleDateFormat formatter;

	/**
	 * Creates and returns the reports directory
	 * 
	 * @return<code>File</code> <p>
	 */
	public static File createDir() {

		String dirPath = USER_HOME + PATH_SEPARATOR + "nacc";
		File statsDir = new File(dirPath);
		if (!statsDir.exists()) {
			statsDir.mkdir();
			LOGGER.info("Created directory " + statsDir.getAbsolutePath());
		}
		return statsDir;

	}

	/**
	 * 
	 * 
	 * Converts param to an <code>int</code>
	 * <p>
	 * 
	 * @param<code>String</code>param to convert to an <code>int</code>
	 * @return<code>int</code>converted param
	 */
	public static int toInt(String param) {

		int result = 0;
		if (isParamSet(param))
			result = Integer.valueOf(param);

		return result;

	}

	/**
	 * 
	 * Get's and returns current date with format yyyy-MM-dd
	 * 
	 * @return<code>String</code>
	 */
	public static String getCurDate() {
		formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(Calendar.getInstance().getTime());
	}

	/**
	 * 
	 * Generates and returns a new file name by appending the current date in
	 * front of the file name
	 * <p>
	 * 
	 * @return<code>String</code>
	 */
	public static String generateFileName() {
		formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(Calendar.getInstance().getTime()) + "_Giant.xls";
	}

	/**
	 * Checks if param is set
	 * 
	 * @param <code>String</code>param, parameter to check if it has been set
	 * @return<code>boolean</code> true if param is set and false otherwise
	 */
	public static boolean isParamSet(String param) {

		boolean isSet = false;
		if (param != null && param.trim().length() > 0)
			isSet = true;
		return isSet;

	}
}
