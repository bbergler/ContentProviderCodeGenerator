package com.foxykeep.cpcodegenerator.model;

import com.foxykeep.cpcodegenerator.util.NameUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class FieldData {

    public String name;
    public String type;

    public int version;

    public String dbName;
    public String annotation;
    public String dbConstantName;
    public String dbType = null;
    public boolean dbIsPrimaryKey;
    public boolean dbIsId;
    public boolean dbHasIndex;
    public boolean dbSkipBulkInsert;
    public boolean dbIsModelOnly;
	public String custom_value;
    public FieldData(final JSONObject json) throws JSONException {
        name = json.getString("name");
        setType(json.getString("type"));

        version = json.optInt("version", 1);
        dbIsModelOnly = json.optBoolean("is_model_only", false);
        dbConstantName = NameUtils.createConstantName(name);
        dbIsPrimaryKey = json.optBoolean("is_primary_key", false);
        dbIsId = json.optBoolean("is_id", false);
        annotation = json.optString("annotated_with","");
        custom_value = json.optString("custom_value","");
        if (dbIsId) {
            dbName = "_id";
        } else {
            dbName = name;
        }
        dbHasIndex = !dbIsPrimaryKey && json.optBoolean("is_index", false);

        dbSkipBulkInsert = json.optBoolean("skip_bulk_insert", false);
    }

    private void setType(final String type) {
        this.type = type;

        if (type.equals("int") || type.equals("integer") || type.equals("long")
                || type.equals("boolean") || type.equals("date")) {
            dbType = "integer";
        } else if (type.equals("float") || type.equals("double") || type.equals("real")) {
            dbType = "real";
        } else if (type.equals("string") || type.equals("text") || type.equals("String") ||  type.contains("enum|")) {
            dbType = "text";
        }
        else
        	dbType=type;
    }

    public static String getDefaultValue(final String type) {

        if (type.equals("string") || type.equals("text") || type.equals("String")) {
            return "''";
        } else {
            return "-1";
        }
    }
}
