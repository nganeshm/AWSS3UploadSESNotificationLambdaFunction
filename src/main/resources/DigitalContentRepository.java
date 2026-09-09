package com.TPI.AWSS3Integration.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DigitalContentRepository extends JpaRepository<DigitalContentEntity, UUID> {

    @Override
    <S extends DigitalContentEntity> S save(S entity);

    @Override
    Optional<DigitalContentEntity> findById(UUID contentUUid);
}
