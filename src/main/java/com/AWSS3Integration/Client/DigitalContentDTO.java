package com.AWSS3Integration.Client;

import com.TPI.AWSS3Integration.Enums.DocType;
import lombok.Data;

import java.io.Serializable;

@Data
public class DigitalContentDTO implements Serializable {


    private String contentName;
    private String Description;
    private DocType contentType;
    private Long fileSize;
    private String contentDownloadUrl;

}
