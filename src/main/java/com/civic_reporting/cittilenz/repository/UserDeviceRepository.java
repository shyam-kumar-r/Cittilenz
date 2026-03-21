package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.UserDevice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceRepository
        extends JpaRepository<UserDevice, Integer> {

    List<UserDevice> findByUserIdAndActiveTrue(Integer userId);

}