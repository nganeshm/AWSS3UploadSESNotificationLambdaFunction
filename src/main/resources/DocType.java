package com.TPI.AWSS3Integration.Enums;

import org.springframework.http.MediaType;

public enum DocType {

    XML("xml", MediaType.APPLICATION_XML_VALUE),
    PDF("pdf", MediaType.APPLICATION_PDF_VALUE),
    JPEG("jpg", MediaType.IMAGE_JPEG_VALUE),
    PNG("png",MediaType.IMAGE_PNG_VALUE),
    GIF("gif", MediaType.IMAGE_GIF_VALUE),
    BMP("bmp","image/bmp"),
    EXCEL("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    EXCELS("xls","application/vnd.ms-excel"),
    EXCELSM("xlsm","application/vnd.ms-excel.sheet.macroEnabled.12"),
    ZIP("zip","application/zip"),
    RAR("rar","application/x-rar-compressed"),
    TAR("tar","application/x-tar"),
    GZIP("gz","application/gzip"),
    DOC("doc","application/msword"),
    DOCX("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPT("ppt","application/vnd.ms-powerpoint"),
    PPTX("pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    MP3("mp3","audio/mpeg"),
    MPEG("mpeg","video/mpeg"),
    WAV("wav","audio/wav"),
    OGA("oga","audio/ogg"),
    TXT("txt","text/plain"),
    WEBM("webm","video/webm"),
    MPEG4("mp4","video/mp4"),
    THREEGP("3gp","video/3gpp"),
    QUICK_TIME("mov","video/quicktime"),
    AVI("avi","video/x-msvideo"),
    WMV("wmv","video/x-ms-wmv"),
    FLV("flv","video/x-flv"),
    HTML("html","text/html"),
    VM("vm","text/html"),
    OGV("ogv","video/ogg"),
    CSV("csv","text/csv"),
    JSON("json", MediaType.APPLICATION_JSON_VALUE);

    String extension;

    String contentType;

    private DocType(String extension, String contentType) {

        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    public static DocType fromExtension(String extn) {
        extn = extn.toLowerCase();
        if("jpeg".equals(extn)) {
            extn = "jpg";
        }
        for(DocType type : DocType.values()) {
            if(type.getExtension().equals(extn))
                return type;
        }

        return null;
    }

    public static DocType fromContentType(String contentType) {
        for (DocType type : DocType.values()) {
            if (type.getContentType().equalsIgnoreCase(contentType))
                return type;
        }
        return null;
    }
}
