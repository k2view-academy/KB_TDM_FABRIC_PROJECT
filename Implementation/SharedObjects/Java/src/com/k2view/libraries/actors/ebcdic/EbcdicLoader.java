package com.k2view.libraries.actors.ebcdic;


import com.google.common.io.CharStreams;
import com.google.common.io.LineReader;
//import com.google.gson.stream.JsonReader;
//import com.google.gson.stream.JsonToken;
import com.k2view.broadway.actors.builtin.AbstractParser.AbstractTextParser;
import com.k2view.broadway.actors.builtin.LinesParser;
import com.k2view.broadway.model.Actor;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.fabric.common.ParamConvertor;
import com.k2view.fabric.common.Util;
import com.k2view.cdbms.interfaces.FabricInterface;
import com.k2view.cdbms.interfaces.jobs.local.LocalFileSystemInterface;
import com.k2view.cdbms.lut.InterfacesManager;

import net.sf.JRecord.Common.CommonBits;
import net.sf.JRecord.Common.Constants;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.External.CopybookLoader;
import net.sf.JRecord.IO.AbstractLineReader;
import net.sf.JRecord.IO.AbstractLineWriter;
import net.sf.JRecord.JRecordInterface1;
import net.sf.JRecord.Numeric.ICopybookDialects;
import net.sf.JRecord.Option.ICobolSplitOptions;
import net.sf.JRecord.Option.JRecordConstantVars;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings({"all"})
public class EbcdicLoader implements Actor {

    private final String INTERFACE          = "interface";
    private final String FILE               = "file";
    private final String COPYBOOK_INTERFACE = "copybook_interface";
    private final String COPYBOOK           = "copybook";
    private final String DATA               = "data";
    private final String FILE_FONT          = "font";
    private final String FILE_DIALECT       = "dialect";
    private final String FILE_ORGANIZATION  = "organization";
    private final String FILE_SPLIT         = "split";

    private transient ICobolIOBuilder iob;
    private transient AbstractLineWriter writer = null;


    public EbcdicLoader() {
    }

    public void action(Data input, Data output, Context context) throws NoSuchFieldException, IllegalAccessException, IOException {

        // File Definition
        FabricInterface fileInterface = InterfacesManager.getInstance().getInterface(input.string(INTERFACE));
        String fileDir    = ((LocalFileSystemInterface) fileInterface).getDir();
        String fileFilter = ((LocalFileSystemInterface) fileInterface).getFileFilter();
        String filePath   = fileDir + "/" + (!Util.isEmpty(input.string(FILE)) ? input.string(FILE) : fileFilter);

        // Copybook Definition
        FabricInterface copybookInterface = InterfacesManager.getInstance().getInterface(input.string(COPYBOOK_INTERFACE));
        String copybookDir    = ((LocalFileSystemInterface) copybookInterface).getDir();
        String copybookFilter = ((LocalFileSystemInterface) copybookInterface).getFileFilter();
        String copybookPath   = copybookDir + "/" + (!Util.isEmpty(input.string(COPYBOOK)) ? input.string(COPYBOOK) : copybookFilter);

        if(this.writer == null) {
            this.iob = JRecordInterface1.COBOL
                    .newIOBuilder(copybookPath)
                    .setDialect((int) ICopybookDialects.class.getDeclaredField(input.string(FILE_DIALECT)).get(ICopybookDialects.class))
                    .setFont(input.string(FILE_FONT).toString().toUpperCase())
                    .setFileOrganization((int) Constants.class.getDeclaredField(input.string(FILE_ORGANIZATION)).get(Constants.class))
                    .setSplitCopybook((int) ICobolSplitOptions.class.getDeclaredField(input.string(FILE_SPLIT)).get(ICobolSplitOptions.class));

            this.writer = iob.newWriter(filePath);
        }

        AbstractLine line = this.iob.newLine();
        createRecordCopybook(line,input.object(DATA));
        this.writer.write(line);
    }

    private void createRecordCopybook(AbstractLine line,Map<String,Object> data) throws IOException {

        // For every entry in the input Map
        for (Map.Entry<String, Object> entry : data.entrySet()) {

            // Get the Key & Value
            String key = entry.getKey();
            Object value = entry.getValue();

            // If value is a Map of its own
            if(value instanceof Map){
                // Recursively call the same function with that child map
                createRecordCopybook(line,(Map<String,Object>)value);

            // Else - meaning the value is not a map of its own
            } else {
                // If the value is null
                if(value == null){
                    // Mark it as Low Value (to be used as Null in the Ebcdic Parser)
                    line.getFieldValue(key).setToLowValues();
                // Else - meaning the value is not null
                } else {
                    // Set the value in the line
                    line.getFieldValue(key).set(value);
                }
            }

        }
    }

    public void close() throws Exception {
        this.writer.close();
        this.writer = null;
    }

}
