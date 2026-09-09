package com.AWSS3Integration.controller;

import com.AWSS3Integration.Client.DigitalContentDTO;
import com.AWSS3Integration.Client.ResponseDTO;
import com.AWSS3Integration.Service.DigitalContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/digital-content")
public class DigitalContentController {

    @Autowired
    private DigitalContentService digitalContentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseDTO uploadDigitalContentToS3(
            @RequestPart("digitalContentDTO") DigitalContentDTO digitalContentDTO,
            @RequestPart("multipartFile") MultipartFile multipartFile) {

        return digitalContentService.uploadDigitalContentToS3(
                digitalContentDTO,
                multipartFile
        );
    }


    @GetMapping("/export-content-dto-list")
    public ResponseDTO exportAllDigitalContentDtoList() throws IOException {
        return digitalContentService.exportContentDtoList();
    }

    @GetMapping("/download/{contentId}")
    public ResponseEntity<Resource> getDigitalContentService(
            @PathVariable String contentId) {

        try {
            Resource resource =
                    digitalContentService.downLoadDigitalContentFromS3(contentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"document.pdf\""
                    )
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/export-content/{contentId}")
    public ResponseEntity<byte[]> exportDigitalContent(@PathVariable String contentId) throws IOException {

        byte[] excelData = digitalContentService.exportContentToExcel(contentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("contentDetails.xlsx")
                        .build());

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }


    @GetMapping("/export-content-list")
    public ResponseEntity<byte[]> exportAllDigitalContentList() throws IOException {

        byte[] excelData = digitalContentService.exportContentListToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("contentDetails.xlsx")
                        .build());

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/test")
    public String test() {
        return "working";
    }

}
