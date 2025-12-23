package com.example.tinder.repository;

import com.example.tinder.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query(value = """
            SELECT d FROM Device d\s
            WHERE d.user.id = :userId\s
            AND d.deviceName = :deviceName
            AND d.isVerified = true
           \s""")
    Optional<Device> findFirstByUserIdAndDeviceName(@Param("userId") Long userId, @Param("deviceName") String deviceName);

    List<Device> findAllByUserIdAndDeviceName(Long userId, String deviceName);


    @Query(value = """
            SELECT d FROM Device d
                 WHERE d.user.id = :userId
                 AND d.deviceName = :deviceName
                 AND d.isVerified = true
            """)
     Optional<Device> findByUserIdAndDeviceName(@Param("userId") Long userId, @Param("deviceName") String deviceName);



    @Query(value = """
            SELECT d FROM Device d\s
            WHERE d.user.id = :userId\s
            AND d.deviceName = :deviceName
           \s""")
    Optional<Device> findForVerifyFirstByUserIdAndDeviceName(@Param("userId") Long userId, @Param("deviceName") String deviceName);
}
