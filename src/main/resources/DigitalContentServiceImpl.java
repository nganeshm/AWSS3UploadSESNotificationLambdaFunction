package com.AWSS3Integration.Service;

import com.AWSS3Integration.Client.DigitalContentDTO;
import com.AWSS3Integration.Client.ResponseDTO;
import com.AWSS3Integration.Enums.ContentStatus;
import com.AWSS3Integration.Enums.DocType;
import com.AWSS3Integration.Repository.DigitalContentEntity;
import com.AWSS3Integration.Repository.DigitalContentRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class DigitalContentServiceImpl implements DigitalContentService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    @Value("${templatesLocation:null}")
    private String templatesLocation;

    @Autowired
    private DigitalContentRepository digitalContentRepository;

    private final S3Presigner presigner;

    private static final Logger logger = LoggerFactory.getLogger(DigitalContentServiceImpl.class);

    public DigitalContentServiceImpl(S3Presigner presigner) {
        this.presigner = presigner;
    }


    @Override
    public ResponseDTO uploadDigitalContentToS3(
            DigitalContentDTO digitalContentDTO,
            MultipartFile multipartFile) {

        if (digitalContentDTO == null || multipartFile == null || multipartFile.isEmpty()) {
            return new ResponseDTO(false, "Invalid request", null, null);
        }

        String objectKey = getObjectKey(digitalContentDTO.getContentName());
        try {

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(multipartFile.getContentType())
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            multipartFile.getInputStream(),
                            multipartFile.getSize()
                    )
            );
            digitalContentDTO.setFileSize(multipartFile.getSize());
            digitalContentRepository.save(buildEntityFromDTO(digitalContentDTO));
        } catch (IOException e) {
            return new ResponseDTO(false, e.getMessage(), null, null);
        }
        return new ResponseDTO(true, "File uploaded successfully", objectKey, null);
    }


    @Override
    public Resource downLoadDigitalContentFromS3(String contentId) throws IOException {
        UUID contentUuid = ValidateUuidParam(contentId);
        if (contentUuid == null) return null;

        Optional<DigitalContentEntity> digitalContentEntity = fetchDigitalContentEntityById(contentUuid);
        if (digitalContentEntity == null) return null;
        DigitalContentDTO digitalContentDTO = buildDTOFromEntity(digitalContentEntity.get());
        String objectKey = getObjectKey(digitalContentDTO.getContentName());
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseInputStream<GetObjectResponse> response =
                s3Client.getObject(getObjectRequest);

        byte[] bytes = response.readAllBytes();

        return new ByteArrayResource(bytes);
    }

    private String getObjectKey(String contentName) {
        return templatesLocation + "/" + contentName;
    }

    private Optional<DigitalContentEntity> fetchDigitalContentEntityById(UUID contentUuid) {
        Optional<DigitalContentEntity> digitalContentEntity = digitalContentRepository.findById(contentUuid);
        if (digitalContentEntity.isEmpty()) {
            return null;
        }
        return digitalContentEntity;
    }


    private List<DigitalContentEntity> fetchDigitalContentEntityList() {
        List<DigitalContentEntity> digitalContentEntityList = digitalContentRepository.findAll();
        if (CollectionUtils.isEmpty(digitalContentEntityList)) {
            return null;
        }
        return digitalContentEntityList;
    }


    private static UUID ValidateUuidParam(String contentId) {
        if (StringUtils.isEmpty(contentId)) return null;
        UUID contentUuid = null;
        try {
            contentUuid = UUID.fromString(contentId);
        } catch (Exception e) {
            logger.error("InValid Content Id: {}", contentId);
            return null;
        }
        return contentUuid;
    }

    @Override
    public byte[] exportContentToExcel(String contentId) {
        UUID contentUuid = ValidateUuidParam(contentId);
        if (contentUuid == null) return null;
        Optional<DigitalContentEntity> digitalContentEntity = fetchDigitalContentEntityById(contentUuid);
        if (digitalContentEntity == null) return null;
        DigitalContentDTO digitalContentDTO = buildDTOFromEntity(digitalContentEntity.get());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("FileDetailSheet");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Content Name");
        header.createCell(1).setCellValue("Content Description");
        header.createCell(2).setCellValue("Content Type");
        header.createCell(3).setCellValue("Content Download Link");

        int rowNum = 1;

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(digitalContentDTO.getContentName());
        row.createCell(1).setCellValue(digitalContentDTO.getDescription());
        row.createCell(2).setCellValue(digitalContentDTO.getDescription());
        row.createCell(3).setCellValue(getS3ObjDownloadLink(digitalContentDTO));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportContentListToExcel() {
        List<DigitalContentEntity> digitalContentEntities = fetchDigitalContentEntityList();
        if (CollectionUtils.isEmpty(digitalContentEntities)) return null;
        List<DigitalContentDTO> digitalContentDTOList = digitalContentEntities.stream()
                .map(entity -> {return buildDTOFromEntity(entity);}).toList();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("FileDetailSheet");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Content Name");
        header.createCell(1).setCellValue("Content Description");
        header.createCell(2).setCellValue("Content Type");
        header.createCell(3).setCellValue("Content Download Link");
        AtomicInteger rowNum = new AtomicInteger(1);

        digitalContentDTOList.forEach(dto -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());

            row.createCell(0).setCellValue(dto.getContentName());
            row.createCell(1).setCellValue(dto.getDescription());
            row.createCell(2).setCellValue(String.valueOf(dto.getContentType()));
            row.createCell(3).setCellValue(getS3ObjDownloadLink(dto));
        });
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public ResponseDTO exportContentDtoList() {
        ResponseDTO responseDTO = new ResponseDTO(false, null, "Not Found any data", HttpStatus.BAD_REQUEST);
        List<DigitalContentEntity> digitalContentEntities = fetchDigitalContentEntityList();
        if(CollectionUtils.isEmpty(digitalContentEntities)) return responseDTO;

        List<DigitalContentDTO> contentDTOList = digitalContentEntities.stream().map(digitalContentEntity -> {
            return buildDTOFromEntity(digitalContentEntity);
        }).collect(Collectors.toList());


        return new ResponseDTO(true, contentDTOList, null, HttpStatus.OK);
    }


    private DigitalContentDTO buildDTOFromEntity(DigitalContentEntity digitalContentEntity) {
        if (digitalContentEntity == null) return null;
        DigitalContentDTO digitalContentDTO = new DigitalContentDTO();
        digitalContentDTO.setContentName(digitalContentEntity.getContentName());
        digitalContentDTO.setContentType(digitalContentEntity.getContentDocType());
        digitalContentDTO.setDescription(digitalContentEntity.getContentName());
        digitalContentDTO.setFileSize(digitalContentEntity.getFileSize());
        String downloadUrl = getS3ObjDownloadLink(digitalContentDTO);
        digitalContentDTO.setContentDownloadUrl(downloadUrl);
        return digitalContentDTO;
    }

    private DigitalContentEntity buildEntityFromDTO(DigitalContentDTO digitalContentDTO) {
        if (digitalContentDTO == null) return null;
        DigitalContentEntity digitalContentEntity = new DigitalContentEntity();
        digitalContentEntity.setContentName(digitalContentDTO.getContentName());
        digitalContentEntity.setCreatedBy("Ganesh");
        digitalContentEntity.setCreatedAt(System.currentTimeMillis());
        digitalContentEntity.setUpdatedAt(System.currentTimeMillis());
        digitalContentEntity.setStatus(ContentStatus.UPLOAD_SUCCESS);
        digitalContentEntity.setFileSize(digitalContentDTO.getFileSize());
        if (digitalContentDTO.getContentType() != null)
            digitalContentEntity.setContentDocType(DocType.fromContentType(String.valueOf(digitalContentDTO.getContentType().getContentType())));
        digitalContentEntity.setBucketName(bucketName);
        return digitalContentEntity;
    }


    private String getS3ObjDownloadLink(DigitalContentDTO contentDTO) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(getObjectKey(contentDTO.getContentName()))
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(request)
                        .build();
        try {
            return presigner.presignGetObject(presignRequest)
                    .url()
                    .toString();
        } catch (Exception e) {
            return null;
        }

    }
 }


