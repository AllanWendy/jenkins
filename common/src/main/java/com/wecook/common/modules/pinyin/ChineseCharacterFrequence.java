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

/**
 * 
 */
package com.wecook.common.modules.pinyin;

import android.content.Context;

import java.util.Properties;

/**
 * Get usage frequency of a Chinese character from statistics-based dictionaries
 * 
 * @author Li Min (xmlerlimin@gmail.com)
 * 
 */
class ChineseCharacterFrequence
{
    /**
     * the dictionary filename of GB2312-based frequence
     */
    private static final String GB_FREQ_TBL = "/data/gb_freq_iso8859_1.tbl";

    /**
     * the dictionary filename of Big5-based frequence
     */
    private static final String BIG5_FREQ_TBL = "/data/big5_freq_iso8859_1.tbl";

    /**
     * the total sample number in GB2312-based dictionary
     */
    private static final double GB_TOTAL_SAMPLE_NO = 258852642d;

    /**
     * the total sample number in Big5-based dictionary
     */
    private static final double BIG5_TOTAL_SAMPLE_NO = 162611044d;

    /**
     * the default occurrency if there is no match in the table
     */
    private static final double MINIMUM_OCCURRENCE = 1d;

    /**
     * the GB2312-based dictionary table
     */
    private static Properties gbFrequenceTable;

    /**
     * the Big5-based dictionary table
     */
    private static Properties big5FrequenceTable;

    /**
     * @param ch
     *            the input Chinese character
     * @return the frequency of this char in GB2312-based articles
     */
    static double getGbFrequence(Context context, char ch)
    {
        return getFrequenceFromTable(ch, getGbFrequenceTable(context), GB_TOTAL_SAMPLE_NO);
    }

    /**
     * @param ch
     *            the input Chinese character
     * @return the frequency of this char in BIG5-based articles
     */
    static double getBig5Frequence(Context context, char ch)
    {
        return getFrequenceFromTable(ch, getBig5FrequenceTable(context), BIG5_TOTAL_SAMPLE_NO);
    }

    /**
     * @param ch
     *            the input Chinese character
     * @param frequenceTable
     *            the frequence dictionary
     * @param totalSampleNumber
     *            the total sample number in this statistics
     * @return the GB2312-based or Big5-based frequence if the input characters
     *         exists in only one table; if this character appears in both
     *         table, return the higher frequence
     * @throws NumberFormatException
     */
    private static double getFrequenceFromTable(char ch,
            Properties frequenceTable, double totalSampleNumber)
            throws NumberFormatException
    {
        String charFrequenceStr = (String) frequenceTable.get(String.valueOf(ch));

        if (null != charFrequenceStr)
        {
            return Double.parseDouble((charFrequenceStr)) / totalSampleNumber;
        } else
        {
            return MINIMUM_OCCURRENCE / totalSampleNumber;
        }
    }

    /**
     * @return Returns the gbFrequenceTable.
     */
    private static Properties getGbFrequenceTable(Context context)
    {
        if (null == gbFrequenceTable)
        {
            final String resourceName = GB_FREQ_TBL;
            setGbFrequenceTable(ResourceHelper.loadProperties(context, resourceName));
        }
        return gbFrequenceTable;
    }

    /**
     * @param gbFrequenceTable
     *            The gbFrequenceTable to set.
     */
    private static void setGbFrequenceTable(Properties gbFrequenceTable)
    {
        ChineseCharacterFrequence.gbFrequenceTable = gbFrequenceTable;
    }

    /**
     * @return Returns the big5FrequenceTable.
     */
    private static Properties getBig5FrequenceTable(Context context)
    {
        if (null == big5FrequenceTable)
        {
            final String resourceName = BIG5_FREQ_TBL;
            setBig5FrequenceTable(ResourceHelper.loadProperties(context, resourceName));
        }

        return big5FrequenceTable;
    }

    /**
     * @param big5FrequenceTable
     *            The big5FrequenceTable to set.
     */
    private static void setBig5FrequenceTable(Properties big5FrequenceTable)
    {
        ChineseCharacterFrequence.big5FrequenceTable = big5FrequenceTable;
    }    
}
