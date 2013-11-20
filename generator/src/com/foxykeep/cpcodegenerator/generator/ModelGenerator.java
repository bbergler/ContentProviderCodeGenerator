package com.foxykeep.cpcodegenerator.generator;

import com.foxykeep.cpcodegenerator.FileCache;
import com.foxykeep.cpcodegenerator.model.FieldData;
import com.foxykeep.cpcodegenerator.model.TableData;
import com.foxykeep.cpcodegenerator.util.PathUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelGenerator {

    public static void generate(String fileName, String classPackage, ArrayList<TableData> classDataList,
			String modelFolder, String classesPrefix, String providerFolder) {


		final StringBuilder sb = new StringBuilder();
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new FileReader(new File("res/model_class.txt")));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			final String modelClass = sb.toString();

			final StringBuilder sbFields = new StringBuilder();

			final StringBuilder sbMethods = new StringBuilder();

			final StringBuilder sbImplements = new StringBuilder();

			final StringBuilder sbImports = new StringBuilder();

			for (TableData tableData : classDataList) {
				sbFields.setLength(0);
				sbMethods.setLength(0);
				sbImports.setLength(0);
                String defaultValuePostString = "";

				for (FieldData fieldData : tableData.fieldList) {
                    String accessModifier="    public ";
					if(!fieldData.annotation.isEmpty())
						sbFields.append("    "+fieldData.annotation+"\n");
                    if(fieldData.constructor) {
                          accessModifier+="final ";
                    }
                    if(!fieldData.default_value.isEmpty()){
                        defaultValuePostString = " = " + fieldData.default_value;
                    }

					if(fieldData.getModelType()!=null)
					sbFields.append(accessModifier + fieldData.getModelType() + " " + fieldData.getModelName() + defaultValuePostString + ";\n");
					else if(fieldData.type.contains("enum|"))
					{
						String name =fieldData.getModelName().substring(0, 1).toUpperCase()+fieldData.getModelName().substring(1);
						sbFields.append(accessModifier + name + " " + fieldData.getModelName() + ";\n");
						sbMethods.append(buildEnum(fieldData));
					}
					else
						sbFields.append(accessModifier+ fieldData.type + " " + fieldData.getModelName() + defaultValuePostString + ";\n");
				}
                sbImports.append("import android.content.ContentValues;\n");
                sbImports.append("\n");
                tableData.importList.add(classPackage+"."+providerFolder+"."+classesPrefix+"Content."+tableData.dbClassName);
                Collections.sort(tableData.importList);

				for (String imports : tableData.importList) {
					sbImports.append("import "+imports+";\n");
				}
				sbImplements.setLength(0);
                sbMethods.append(generateCtor(tableData.modelName,tableData.fieldList, tableData.dbClassName));
				sbMethods.append(generateToContentValues(tableData.fieldList,tableData.dbClassName)+"\n");
				sbMethods.append(generateToString(tableData.fieldList,tableData.modelName)+"\n");

				FileCache.saveFile(
						PathUtils.getAndroidFullPath(fileName, classPackage, modelFolder) + tableData.modelName
								+ ".java",
						String.format(modelClass, classPackage, modelFolder,sbImports.toString(), tableData.modelName,sbImplements.toString(), sbFields.toString(),
								sbMethods.toString()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private static String generateCtor(String modelName, List<FieldData> fieldList, String dbClassName) {
        String args="";
        String body="";
        for (FieldData fieldData : fieldList) {
            if(fieldData.constructor)
            {
               if(args.length()>0) args+=", ";
               args+=fieldData.getModelType()+" "+fieldData.dbName;
                body+="      this."+fieldData.dbName+" = "+fieldData.dbName+";\n";
            }
        }

           String ctor ="";
        if(args.length()>0)
        ctor+="    public "+modelName+"("+args+")"+" {\n"+body+"\n    }\n\n";

        return ctor;
    }

    private static String buildEnum(FieldData fieldData) {
		String name =fieldData.dbName.substring(0, 1).toUpperCase()+fieldData.dbName.substring(1);
		String enumCode ="    public enum "+name+" {\n        ";
		enumCode+=fieldData.type.split("\\|")[1]+";\n    }\n\n";
		return enumCode;
	}

	private static String generateToContentValues(List<FieldData> fieldList, String dbClassName) {
		String s="    public ContentValues getContentValues() {\n";
		s+="        ContentValues cv = new ContentValues();\n";
		for (FieldData fieldData : fieldList) {
//			
//			cv.put(OfferContent.Columns.NAME.getName(), name);
			String name =fieldData.dbName.toUpperCase();
			if(fieldData.dbIsModelOnly)
				continue;
			if(name.equals("_ID")){
				name="ID";
				if(!fieldData.annotation.contains("@SerializedName(\"id\")"))
				continue;
			}
			String value=fieldData.getModelName();
			if(!fieldData.customValue.isEmpty())
				value=fieldData.customValue;
			s+="        cv.put("+dbClassName+".Columns."+name+".getName(), " +value+");\n";

		}
		s+="        return cv;\n";
		s+="    }\n";
		return s;
	}

	private static String generateToString(List<FieldData> fieldList, String modelName) {
		String s="    public String toString() {\n";
		s+="        StringBuilder sb = new StringBuilder();\n";
        s+="        StringBuilder nullVars = new StringBuilder();\n";
		s+="        sb.append(\"Model:"+modelName+"\");\n";

		for (FieldData fieldData : fieldList) {
			String name =fieldData.getModelName();
			s+="        if ("+name+" != null) {\n";
			s+="            sb.append(\""+name+"= \" + " +fieldData.getModelName()+");\n";
            s+="        } else {\n";
            s+="            nullVars.append(\""+name+", \");\n";
            s+="        }\n";

		}
		s+="        if (nullVars.length() > 0) {\n";
		s+="            sb.append(\"elements which are null:\" + nullVars.toString());\n";
		s+="        }\n";
		s+="        return sb.toString();\n";
		s+="    }";
		return s;
	}

}
