/**
 * This file is part of pinyin4j (http://sourceforge.net/projects/pinyin4j/) 
 * and distributed under GNU GENERAL PUBLIC LICENSE (GPL).
 * 
 * pinyin4j is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 * pinyin4j is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with pinyin4j.
 */

package com.wecook.common.modules.pinyin;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class for file resources
 * 
 * @author Li Min (xmlerlimin@gmail.com)
 * 
 */
class ResourceHelper
{
    /**
     * @param resourceName
     * @return resource (mainly file in file system or file in compressed
     *         package) as BufferedInputStream
     */
    static BufferedInputStream getResourceInputStream(Context context, String resourceName)
    {
        try {
			return new BufferedInputStream(context.getAssets().open(resourceName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    /**
     * @param resourceName
     * @return load a Properties hashtable according to resourceName
     */
    static Properties loadProperties(Context context, String resourceName)
    {
        try
        {
            Properties properties = new Properties();
            InputStream in = context.getAssets().open(resourceName);
            properties.load(in);

            return properties;

        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            return null;

        } catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
