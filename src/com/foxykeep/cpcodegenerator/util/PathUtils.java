package com.foxykeep.cpcodegenerator.util;

public class PathUtils {

    private PathUtils() {
    }

    public static final String PROVIDER = "data.provider";
    public static final String PROVIDER_UTIL = "data.provider.util";

    public static String getAndroidFullPath(final String fileName, final String classPackage, final String path) {
        return getOutputPath(fileName) + classPackage.replace(".", "/") + "/" + path.replace(".", "/") + "/";
    }

    private static String getOutputPath(final String fileName) {
        return "output/" + fileName + "/";
    }
}