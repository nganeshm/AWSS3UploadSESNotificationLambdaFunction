package com.AWSS3Integration.Repository;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {


    @Column(updatable = false)
    private Long createdAt;

    @Column
    private Long updatedAt;

    @Column
    private String createdBy;

    @Column
    private String updatedBy;
}
