package com.AWSS3Integration.Repository;

import com.TPI.AWSS3Integration.Enums.ContentStatus;
import com.TPI.AWSS3Integration.Enums.DocType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "digital_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalContentEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "content_uuid", nullable = false, updatable = false)
        private UUID contentUuid;

    @Column(name = "content_name", nullable = false)
    private String contentName;

    @Column(name = "content_doc_type", nullable = false)
    private DocType contentDocType;

    @Column(name = "s3_object_key", nullable = false, unique = true)
    private String s3ObjectKey;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContentStatus status;

}