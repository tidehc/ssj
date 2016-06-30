/*
 * SimpleFileReader.java
 * Copyright (c) 2016
 * Authors: Ionut Damian, Michael Dietz, Frank Gaibler, Daniel Langerenken, Simon Flutura
 * *****************************************************
 * This file is part of the Social Signal Interpretation for Java (SSJ) framework
 * developed at the Lab for Human Centered Multimedia of the University of Augsburg.
 *
 * SSJ has been inspired by the SSI (http://openssi.net) framework. SSJ is not a
 * one-to-one port of SSI to Java, it is an approximation. Nor does SSJ pretend
 * to offer SSI's comprehensive functionality and performance (this is java after all).
 * Nevertheless, SSJ borrows a lot of programming patterns from SSI.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package hcm.ssj.file;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import hcm.ssj.core.Log;
import hcm.ssj.core.Sensor;
import hcm.ssj.core.option.Option;
import hcm.ssj.core.option.OptionList;

/**
 * File reader for SSJ.<br>
 * Created by Frank Gaibler on 20.08.2015.
 */
public class SimpleFileReader extends Sensor
{
    /**
     *
     */
    public class Options extends OptionList
    {
        public final Option<String> filePath = new Option<>("filePath", LoggingConstants.SSJ_EXTERNAL_STORAGE, String.class, "file path");
        public final Option<String> fileName = new Option<>("fileName", null, String.class, "file name");
        public final Option<Boolean> loop = new Option<>("loop", true, Boolean.class, "");

        /**
         *
         */
        private Options()
        {
            addOptions();
        }
    }

    public final Options options = new Options();
    private File fileHeader;
    private File fileReal;
    private BufferedReader bufferedReader = null;
    private SimpleHeader simpleHeader = null;
    private boolean initialized = false;

    /**
     *
     */
    public SimpleFileReader()
    {
        _name = "SSJ_sensor_" + this.getClass().getSimpleName();
    }

    /**
     *
     */
    protected final void init()
    {
        if (!initialized)
        {
            initialized = true;
            if (options.filePath.get() == null)
            {
                Log.w("file path not set, setting to default " + LoggingConstants.SSJ_EXTERNAL_STORAGE);
                options.filePath.set(LoggingConstants.SSJ_EXTERNAL_STORAGE);
            }
            File fileDirectory = new File(options.filePath.get());
            if (!fileDirectory.exists())
            {
                Log.e("directory \"" + fileDirectory.getName() + "\" does not exist");
            }
            if (options.fileName.get() == null)
            {
                String defaultName = this.getClass().getSimpleName();
                Log.w("file name not set, setting to " + defaultName);
                options.fileName.set(defaultName);
            }
            this.fileHeader = new File(fileDirectory, options.fileName.get());
            setFiles();
        }
    }

    /**
     *
     */
    @Override
    protected boolean connect()
    {
        init();
        simpleHeader = null;
        bufferedReader = getFileConnection(fileReal, bufferedReader);
        return true;
    }

    /**
     * @return SimpleHeader
     */
    protected SimpleHeader getSimpleHeader()
    {
        if (simpleHeader == null)
        {
            try
            {
                SimpleXmlParser simpleXmlParser = new SimpleXmlParser();
                SimpleXmlParser.XmlValues xmlValues = simpleXmlParser.parse(
                        new FileInputStream(fileHeader),
                        new String[]{"stream", "info"},
                        new String[]{"sr", "dim", "byte", "type"}
                );
                simpleHeader = new SimpleHeader();
                simpleHeader._sr = xmlValues.foundAttributes.get(0)[0];
                simpleHeader._dim = xmlValues.foundAttributes.get(0)[1];
                simpleHeader._byte = xmlValues.foundAttributes.get(0)[2];
                simpleHeader._type = xmlValues.foundAttributes.get(0)[3];
                xmlValues = simpleXmlParser.parse(
                        new FileInputStream(fileHeader),
                        new String[]{"stream", "chunk"},
                        new String[]{"from", "to", "num"}
                );
                simpleHeader._from = xmlValues.foundAttributes.get(0)[0];
                simpleHeader._to = xmlValues.foundAttributes.get(0)[1];
                simpleHeader._num = xmlValues.foundAttributes.get(0)[2];
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.e("file could not be parsed");
            }
        }
        return simpleHeader;
    }

    /**
     *
     */
    private void setFiles()
    {
        String path = fileHeader.getPath();
        if (path.endsWith(LoggingConstants.TAG_DATA_FILE))
        {
            fileReal = fileHeader;
            fileHeader = new File(path.substring(0, path.length() - 1));
        } else if (fileHeader.getName().contains("."))
        {
            fileReal = new File(path + LoggingConstants.TAG_DATA_FILE);
        } else
        {
            fileHeader = new File(path + "." + LoggingConstants.FILE_EXTENSION);
            fileReal = new File(path + "." + LoggingConstants.FILE_EXTENSION + LoggingConstants.TAG_DATA_FILE);
        }
    }

    /**
     * @param reader BufferedReader
     * @return BufferedReader
     */
    private BufferedReader closeStream(BufferedReader reader)
    {
        if (reader != null)
        {
            try
            {
                reader.close();
                reader = null;
            } catch (IOException e)
            {
                Log.e("could not close reader");
            }
        }
        return reader;
    }

    /**
     *
     */
    @Override
    protected void disconnect()
    {
        bufferedReader = closeStream(bufferedReader);
        initialized = false;
    }

    /**
     * @param file   File
     * @param reader BufferedReader
     * @return BufferedReader
     */
    private BufferedReader getFileConnection(File file, BufferedReader reader)
    {
        if (reader != null)
        {
            reader = closeStream(reader);
        }
        try
        {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
        } catch (FileNotFoundException e)
        {
            Log.e("fileHeader not found");
        }
        return reader;
    }

    /**
     * @param reader BufferedReader
     * @return String
     */
    private String readLine(BufferedReader reader)
    {
        String line = null;
        if (reader != null)
        {
            try
            {
                line = reader.readLine();
            } catch (IOException e)
            {
                Log.e("could not read line");
            }
        }
        return line;
    }

    /**
     * @return String
     */
    protected String getData()
    {
        String data = readLine(bufferedReader);
        if (data == null && options.loop.get())
        {
            //start anew
            bufferedReader = getFileConnection(fileReal, bufferedReader);
            data = readLine(bufferedReader);
        }
        return data;
    }
}
