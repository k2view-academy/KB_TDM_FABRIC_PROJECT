/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.TDM.TDMUpgrade;

import com.k2view.cdbms.shared.Db;
import com.k2view.cdbms.shared.user.UserCode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.fnUpdateDistinctFieldData;
import static com.k2view.cdbms.usercode.common.TDM.SharedLogic.TDMDB_SCHEMA;
import static com.k2view.cdbms.usercode.lu.TDM.Globals.TDM_PARAMETERS_SEPARATOR;
import static com.k2view.cdbms.usercode.lu.TDM.TDM.Logic.fnGetParamType;

@SuppressWarnings({"DefaultAnnotationParam",  "unchecked"})
public class Logic extends UserCode {
    private static final String TDM = "TDM";
	public static void updateParamDistictValues() throws Exception {
		try {
		//log.info("Starting updateParamDistictValues");
		
		String insertDistintValuesSql = "INSERT INTO " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES " +
		    "(SOURCE_ENVIRONMENT, LU_NAME, FIELD_NAME, NUMBER_OF_VALUES, FIELD_VALUES, IS_NUMERIC, MIN_VALUE, MAX_VALUE) " +
		    "VALUES (?, ?, ? ,?, string_to_array(?, '" + TDM_PARAMETERS_SEPARATOR + "'), ?, ?, ?)";
		

		db(TDM).execute("truncate table " + TDMDB_SCHEMA + ".TDM_PARAMS_DISTINCT_VALUES");
		Db.Rows tableData = db(TDM).fetch("select table_name, '\"' || array_to_string(array_agg(column_name), '\",\"') || '\"' as columns " +
		    " FROM information_schema.columns where table_schema = '" + TDMDB_SCHEMA + "'" +
		    " and table_name like '%_params' and column_name like '%.%'" +
		    " group by table_name" +
		    " order by table_name");
		
		for (Db.Row tableRow : tableData) {
		    String tableName = tableRow.get("table_name").toString();
		    String luName = tableName.split("_params")[0].toUpperCase();
			//log.info("luName: " + luName + ", tableName: " + tableName);
            Db.Rows srcEnvList = db(TDM).fetch("select distinct source_environment from " + tableName);
            for (Db.Row srvEnvRec : srcEnvList) {
            
                String srcEnv = srvEnvRec.get("source_environment").toString();
                String[] columnsArr = tableRow.get("columns").toString().split(",");
                for (int idx = 0; idx < columnsArr.length; idx++) {
                    columnsArr[idx] = "array_to_string(" + columnsArr[idx] + ", '" + TDM_PARAMETERS_SEPARATOR + "') as " + columnsArr[idx];
                }
                String newSelClause = String.join(",", columnsArr);
                String query = "SELECT " + newSelClause + " FROM " +  TDMDB_SCHEMA + "." + tableName + " where source_environment = ?";
                Db.Rows tableRecords = db(TDM).fetch(query, srcEnv);
                List<String> columnNames = tableRecords.getColumnNames();
                Map<String, Map<String, Object>> fieldValues = new HashMap<>();
                for (Db.Row row : tableRecords) {
                    //ResultSet resultSet = row.resultSet();
                    for (String columnName : columnNames) {
                        //log.info("column_name: " + columnName);
                        if (row.get(columnName) != null) {
                            //log.info("column_name: " + columnName +", value: " + row.get(columnName));
                            String value = row.get(columnName).toString();
                            value = value.replace("{", "");
                            value = value.replace("}", "");
                            HashSet<String> values = new HashSet<String>(Arrays.stream(value.split(TDM_PARAMETERS_SEPARATOR)).collect(Collectors.toSet()));
                            String col = columnName.split("\\.")[1];
                            String columnType = fnGetParamType(luName, col);
                            fieldValues = fnUpdateDistinctFieldData(columnName,columnType, fieldValues, values);
                        }
                    }
                }
            
                for (String key : fieldValues.keySet()) {
                    Map<String, Object> fieldinfo = fieldValues.get(key);
                    Long numberOfValues = Long.parseLong(fieldinfo.get("numberOfValues").toString());
                    Boolean isNumeric  = Boolean.parseBoolean(fieldinfo.get("isNumeric").toString());
                    String minValue = fieldinfo.get("minValue").toString();
                    String maxValue = fieldinfo.get("maxValue").toString();
            
                    HashSet<String> valuesSet = (HashSet<String>)fieldinfo.get("fieldValues");
                    String newFieldvalues= String.join(TDM_PARAMETERS_SEPARATOR, valuesSet);
                    //newFieldvalues = "{" + newFieldvalues + "}";
                    //log.info("Loading - columnName:"  + key + ", numberOfValues: " + numberOfValues);
                    db(TDM).execute(insertDistintValuesSql, srcEnv, luName.toUpperCase(), key, numberOfValues, newFieldvalues, isNumeric, minValue, maxValue);
            
                }
            }
		}
		//log.info("Finished updateParamDistictValues");
		} catch (Exception e) {
		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw);
		    e.printStackTrace(pw);
		    String sStackTrace = sw.toString();
		    e.printStackTrace();
		    log.error("Failed - " + sStackTrace);
		}
	}
	
    public static void mergeSharedGlobalFiles(String SharedJavaLocation) throws Exception {
        //File mainGlobalsFile = new File(SharedJavaLocation + "/SharedGlobals.java");
        //File tdmGlobalsFile = new File(SharedJavaLocation + "/TDM/SharedGlobals.java");
        String newSharedGlobal = "/////////////////////////////////////////////////////////////////////////\n" +
        "// Shared Globals\n" +
        "/////////////////////////////////////////////////////////////////////////\n" +
        "\n" +
        "package com.k2view.cdbms.usercode.common;\n" +
        "\n" +
        "import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;\n" +
        "\n" +
        "public class SharedGlobals {\n" +
        "\n";

        Scanner tdmGlobalsFile = new Scanner(new File(SharedJavaLocation + "/TDM/SharedGlobals.java"));
        StringBuffer tdmBuffer = new StringBuffer();
        //Reading lines of the file and appending them to StringBuffer
        while (tdmGlobalsFile.hasNextLine()) {
            tdmBuffer.append(tdmGlobalsFile.nextLine() + "\n");
        }
        String tdmFileContents = tdmBuffer.toString();
        //closing the Scanner object
        tdmGlobalsFile.close();

        Scanner mainGlobalsFile = new Scanner(new File(SharedJavaLocation + "/SharedGlobals.java"));
        String candidateLines = "";

        while (mainGlobalsFile.hasNextLine()) {
            String line = mainGlobalsFile.nextLine()+System.lineSeparator();
            String currentGlobal = "";
            if (line.contains("@desc") ||  line.contains("@category")) {
                candidateLines += line;
            } else if (line.contains("public static")) {
                candidateLines += line;
                int startIndex = "public static String ".length();
                if (line.contains("public static final String ")) {
                    startIndex = "public static".length();
                }
                currentGlobal = line.substring(startIndex, line.indexOf("="));
                if (tdmFileContents.contains(currentGlobal)) {
                    String temp = (tdmFileContents.substring(tdmFileContents.indexOf(currentGlobal)));
                    String toReplace = temp.substring(0, temp.indexOf(";\n"));
                    tdmFileContents = tdmFileContents.replace(toReplace + ";\n", line.substring(startIndex));
                    candidateLines = "";
                } else {
                    newSharedGlobal += candidateLines;
                    candidateLines = "";
                }
            } else if (line.trim().isEmpty()) {
                candidateLines += line;
            } else {
                candidateLines = "";
            }
        }
        //closing the Scanner object
        mainGlobalsFile.close();
        newSharedGlobal += "\n}\n";
        //log.info("New Globals: \n\n\n" + newSharedGlobal + "\n\n\n");


        FileWriter fwTdm = new FileWriter(SharedJavaLocation + "/TDM/SharedGlobals.java", false);
        fwTdm.write(tdmFileContents);
        fwTdm.close();

        FileWriter fwMain = new FileWriter(SharedJavaLocation + "/SharedGlobals.java", false);
        fwMain.write(newSharedGlobal);
        fwMain.close();
    }

    public static void removeRelationTables(String SchemaLocation) throws Exception {
        
        String inputFile = SchemaLocation + "/vdb.k2vdb.xml";
        String outputFile = SchemaLocation + "/vdb.k2vdb2.xml";

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        String line;
        boolean skip = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("<TableProperties name=\"TDM_LU_TYPE_RELATION_EID\">")) {
                skip = true;
                continue;
            }
            if (line.trim().startsWith("</TableProperties>")) {
                if (skip){
                    skip = false;
                    continue;
                }

            }
            if (!skip) {
                writer.write(line + "\n");
            }
        }

        reader.close();
        writer.close();

        StringBuffer tdmBuffer = new StringBuffer();
        Scanner schemaFile = new Scanner(new File(SchemaLocation + "/vdb.k2vdb2.xml"));
        while (schemaFile.hasNextLine()) {
            tdmBuffer.append(schemaFile.nextLine() + "\n");
        }

        schemaFile.close();
        //String newSchema = tdmBuffer.toString().replaceAll("TableProperties name=\"TDM_LU_TYPE_RELATION_EID\">(.*?)<\\/TableProperties>$","");
        String newSchema = tdmBuffer.toString().replaceAll("<Table>TDM_LU_TYPE_RELATION_EID</Table>","");
        newSchema = newSchema.replaceAll("<Table>TDM_LU_TYPE_REL_TAR_EID</Table>","");
        newSchema = newSchema.replaceAll("<Node name=\"TDM_LU_TYPE_RELATION_EID\".*? viewType=\"Table\" />","");
        newSchema = newSchema.replaceAll("<Node name=\"TDM_LU_TYPE_REL_TAR_EID\".*? viewType=\"Table\" />","");

        FileWriter fwTdm = new FileWriter(SchemaLocation + "/vdb.k2vdb.xml", false);
        fwTdm.write(newSchema);
        fwTdm.close();

    }

    private static class FileContent {
        Map<String, String> fields;
        Set<String> imports;
        List<String> headerLines;

        public FileContent() {
            this.fields = new LinkedHashMap<>();
            this.imports = new TreeSet<>(); // TreeSet for unique, sorted imports
            this.headerLines = new ArrayList<>(); // ArrayList to maintain order of header lines
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public Set<String> getImports() {
            return imports;
        }

        public List<String> getHeaderLines() {
            return headerLines;
        }

        public void addImport(String importStatement) {
            this.imports.add(importStatement);
        }

    }

    public static void mergeJavaFiles(String oldFilePath, String newFilePath, String outputFilePath, String className,
            List<String> ignoreFieldsArray) {
        Set<String> ignoreFields = new HashSet<>(ignoreFieldsArray);
        // Read content (header, fields, and imports) from both files
        FileContent oldFileContent = readFileAndExtractContent(oldFilePath);
        FileContent newFileContent = readFileAndExtractContent(newFilePath);

        // --- Merge Imports ---
        Set<String> mergedImports = new TreeSet<>();
        mergedImports.addAll(oldFileContent.getImports());
        mergedImports.addAll(newFileContent.getImports());

        // --- Merge Fields ---
        Map<String, String> mergedFields = new LinkedHashMap<>();
        // 1. Add all fields from the old file content. This sets the base order and
        // initial values.
        mergedFields.putAll(oldFileContent.getFields()); // CHANGED: Populate with old fields first

        // 2. Iterate through new fields and add only those that are truly new (not in
        // old file).
        // Existing fields in newFileContent will be ignored here, preserving old values
        // and positions.
        for (Map.Entry<String, String> newFieldEntry : newFileContent.getFields().entrySet()) { // CHANGED: Iteration
                                                                                                // logic
            String newFieldName = newFieldEntry.getKey();
            String newFieldDeclaration = newFieldEntry.getValue();

            // If the field name does NOT exist in the old fields (i.e., in mergedFields),
            // add it.
            if (!mergedFields.containsKey(newFieldName)) { // CHANGED: Conditional add
                mergedFields.put(newFieldName, newFieldDeclaration); // Added to the end
            }
            // If it DOES exist, we do nothing. The old field's value and position are
            // preserved.
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            // 1. Write top-level header lines (comments, package declaration) from the old
            // file
            for (String headerLine : oldFileContent.getHeaderLines()) {
                writer.write(headerLine + "\n");
            }           

            // 2. Write merged import statements
            for (String importStmt : mergedImports) {
                writer.write(importStmt + "\n");
            }
            writer.write("\n"); // Add a blank line after imports for readability

            // 3. Write the class declaration
            writer.write("public class " + className + " {\n\n");

            // 4. Write all merged fields, excluding ignored ones
            for (String fieldName : mergedFields.keySet()) {
                if (!ignoreFields.contains(fieldName)) {
                    writer.write("    " + mergedFields.get(fieldName) + "\n");
                    writer.write("\n");
                }
            }

            writer.write("\n}\n"); // Close the class declaration

            log.info("Successfully merged fields into: " + outputFilePath);

        } catch (IOException e) {
            log.error("Error writing to output file: " + e.getMessage());
        }
    }

    private static FileContent readFileAndExtractContent(String filePath) {
        FileContent fileContent = new FileContent();
        StringBuilder classBodyContent = new StringBuilder();
        boolean inHeader = true; // NEW: Flag for the header parsing phase
        boolean inClassScope = false;
        int braceCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                // Phase 1: Capture Header Lines (comments, package declaration, etc.)
                if (inHeader) {
                    if (trimmedLine.startsWith("import ") || trimmedLine.startsWith("public class")
                            || trimmedLine.startsWith("class ")) {
                        inHeader = false; // Transition out of header phase, process this line in next phase
                        // Fall through to the next conditions to process this line as an import or
                        // class declaration
                    } else {
                        fileContent.headerLines.add(line); // Add the line as a header line
                        continue; // Move to the next line in the file
                    }
                }

                // Phase 2: Capture Import Statements (if not yet in class scope)
                if (!inClassScope) {
                    if (trimmedLine.startsWith("import ")) {
                        fileContent.addImport(trimmedLine);
                        continue; // Move to the next line in the file
                    }
                    // Detect the start of the class declaration
                    else if (trimmedLine.startsWith("public class") || trimmedLine.startsWith("class ")) {
                        inClassScope = true; // Transition into class scope
                        // Count braces on the class declaration line itself
                        for (char c : trimmedLine.toCharArray()) {
                            if (c == '{')
                                braceCount++;
                            if (c == '}')
                                braceCount--;
                        }
                        continue; // Skip adding the class declaration line itself to classBodyContent
                    }
                }

                // Phase 3: Capture Class Body Content (for field extraction)
                if (inClassScope) {
                    // Update brace count for the current line
                    for (char c : trimmedLine.toCharArray()) {
                        if (c == '{')
                            braceCount++;
                        if (c == '}')
                            braceCount--;
                    }

                    // If braceCount is 0 AND we've just encountered a closing brace, it's the end
                    // of the main class.
                    if (braceCount == 0 && trimmedLine.equals("}")) {
                        inClassScope = false; // Exit class scope
                        break; // Stop reading this file, as we're done with the main class body
                    }
                    // Append lines that are part of the class body (not the declaration or its
                    // final closing brace)
                    classBodyContent.append(line).append("\n");
                }
            }
            // Parse the collected class body content for field declarations
            fileContent.fields = extractFieldsFromString(classBodyContent.toString());
        } catch (IOException e) {
            log.error("Error reading file: " + filePath + " - " + e.getMessage());
        }
        return fileContent;
    }

    private static Map<String, String> extractFieldsFromString(String content) {
        Map<String, String> fields = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile(
                "((?:\\s*@\\w+\\s*\\(.*?\\)\\s*)*" + // Optional annotations (Group 1 part 1)
                        "\\s*(?:public|protected|private)?(?:\\s+static)?(?:\\s+final)?\\s+" + // Modifiers
                        "([\\w\\[\\].<>?]+)\\s+" + // Type (Group 2)
                        "(\\w+)\\s*" + // Field Name (Group 3)
                        "(?:=\\s*[^;]*)?" + // Optional initialization (non-greedy, stops at ';')
                        ";)", // Must end with a semicolon (Group 1 part 2)
                Pattern.DOTALL);

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String fullDeclarationWithAnnotations = matcher.group(1).trim();
            String fieldName = matcher.group(3);

            fields.put(fieldName, fullDeclarationWithAnnotations);
        }
        return fields;
    }
}
