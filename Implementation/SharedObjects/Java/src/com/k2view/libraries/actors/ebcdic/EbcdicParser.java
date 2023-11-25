package com.k2view.libraries.actors.ebcdic;


import com.google.common.io.CharStreams;
import com.google.common.io.LineReader;
//import com.google.gson.stream.JsonReader;
//import com.google.gson.stream.JsonToken;
import com.k2view.broadway.actors.builtin.AbstractParser.AbstractTextParser;
import com.k2view.broadway.actors.builtin.LinesParser;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.cdbms.shared.utils.UserCodeDescribe;
import com.k2view.fabric.common.Util;
import com.k2view.cdbms.interfaces.FabricInterface;
import com.k2view.cdbms.interfaces.jobs.local.LocalFileSystemInterface;
import com.k2view.cdbms.lut.InterfacesManager;

import net.sf.JRecord.Common.CommonBits;
import net.sf.JRecord.Common.Constants;
import net.sf.JRecord.Common.FieldDetail;
import net.sf.JRecord.Common.IFieldDetail;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.External.CopybookLoader;
import net.sf.JRecord.IO.AbstractLineReader;
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

@SuppressWarnings({"all"})
public class EbcdicParser extends AbstractTextParser {
    public EbcdicParser() {
    }

    protected Iterator<Object> parser(Reader reader, Data input, Context context) throws IOException, NoSuchFieldException, IllegalAccessException {
        return new EbcdicParser.EbcdicStreamReader(reader, input);
    }

    static class EbcdicStreamReader implements Iterator<Object>, AutoCloseable {

        private final String INTERFACE           = "interface";
        private final String FILE                = "file";
        private final String COPYBOOK_INTERFACE  = "copybook_interface";
        private final String COPYBOOK            = "copybook";
        private final String FILE_FONT           = "font";
        private final String FILE_DIALECT        = "dialect";
        private final String FILE_ORGANIZATION   = "organization";
        private final String FILE_SPLIT          = "split";

        private final transient AbstractLineReader reader;
        private final transient ICobolIOBuilder iob;
        private Object nextValue;
        AbstractLine nextLine;

        //private String dataFile     = "D:/K2View/K2Files/sample-customer-data.ebcdic";
        //private String copybookName = "D:/K2View/K2Files/cust-record-copybook.txt";

        EbcdicStreamReader(Reader reader, Data input) throws IOException, NoSuchFieldException, IllegalAccessException {

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


            this.iob = JRecordInterface1.COBOL
                    .newIOBuilder       (copybookPath)
                    .setDialect         ((int)ICopybookDialects.class.getDeclaredField(input.string(FILE_DIALECT)).get(ICopybookDialects.class))
                    .setFont            (input.string(FILE_FONT).toString().toUpperCase())
                    .setFileOrganization((int)Constants.class.getDeclaredField(input.string(FILE_ORGANIZATION)).get(Constants.class))
                    .setSplitCopybook   ((int)ICobolSplitOptions.class.getDeclaredField(input.string(FILE_SPLIT)).get(ICobolSplitOptions.class));

            this.reader = this.iob.newReader(filePath);
        }


        public boolean hasNext() {
            while(true) {
                if (this.nextLine == null) {
                    AbstractLineReader copyLine = reader;
                    try {
                        this.nextLine = copyLine.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return this.nextLine != null;
            }
        }

        public Object next() {
            Map<String,Object> resultMap = new LinkedHashMap<>();
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {

                try {
                    ArrayList copybookLayout = new ArrayList<>(this.iob.getLayout().getFieldNameMap().entrySet());
                    Collections.sort(copybookLayout, new Comparator<Map.Entry<String, FieldDetail>>() {
                        @Override
                        public int compare(Map.Entry<String, FieldDetail> o1, Map.Entry<String, FieldDetail> o2) {
                            return o1.getValue().getPos() - o2.getValue().getPos();
                        }
                    });

                    //this.iob.getLayout().getFieldNameMap().entrySet().forEach(entry ->
                    copybookLayout.forEach(entry -> {
                        String key        = ((Map.Entry<String, FieldDetail>) entry).getKey();
                        FieldDetail value = ((Map.Entry<String, FieldDetail>) entry).getValue();
                        updateObject(
                                resultMap,
                                value.getGroupName().substring(1) + key,
                                (this.nextLine.getFieldValue(key).isLowValues()) ? null : 
                                (this.nextLine.getFieldValue(key).isNumeric()) ? this.nextLine.getFieldValue(key).asLong() : this.nextLine.getFieldValue(key).asString()
                        );
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.nextLine = null;
                return resultMap;
            }
        }

        public static void updateObject(Map<String,Object> map, String path, Object value) {

            // Get the next level of the Path
            String newPath = path.split("\\.")[0];

            // If this is a new level
            if(!newPath.equals(path)) {

                // If this is a new child (it might have been created by another field
                if (!map.containsKey(newPath))
                    // Create a new child map
                    map.put(newPath, new LinkedHashMap<>());

                // Recursively call the next layer down
                updateObject((Map)map.get(newPath),path.substring(path.indexOf(".")+1),value);

            // Else - meaning this is the last level
            } else {

                // Update the value in the map
                map.put(path,value);
            }
        }

        public void close() throws Exception {
            this.reader.close();
        }
    }
}