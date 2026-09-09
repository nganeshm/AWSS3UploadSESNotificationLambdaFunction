package com.AWSS3Integration.Service;

import com.TPI.AWSS3Integration.Client.DigitalContentDTO;
import com.TPI.AWSS3Integration.Client.ResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DigitalContentService {

   ResponseDTO uploadDigitalContentToS3(DigitalContentDTO digitalContentDTO, MultipartFile multipartFile);

    Resource downLoadDigitalContentFromS3(String contentId) throws IOException;

    byte[] exportContentToExcel(String contentId);

    byte[] exportContentListToExcel();

    ResponseDTO exportContentDtoList();
}
