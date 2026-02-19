package com.civic_reporting.cittilenz.repository;

import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.enums.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsernameAndActiveTrue(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByMobile(String mobile);
    
    Optional<User> findByIdAndActiveTrue(Integer id);

    List<User> findByRoleAndWardIdAndDepartmentIdAndActiveTrue(
            UserRole role,
            Integer wardId,
            Integer departmentId
    );

    List<User> findByRoleAndWardIdAndActiveTrue(
            UserRole role,
            Integer wardId
    );
    
    
 // ==============================
 // 📊 ADMIN DASHBOARD USER COUNTS
 // ==============================

 @Query("""
     SELECT COUNT(u)
     FROM User u
     WHERE u.active = true
     AND u.role = com.civic_reporting.cittilenz.enums.UserRole.CITIZEN
 """)
 long countActiveCitizens();


 @Query("""
     SELECT COUNT(u)
     FROM User u
     WHERE u.active = true
     AND u.role = com.civic_reporting.cittilenz.enums.UserRole.OFFICIAL
 """)
 long countActiveOfficials();


 @Query("""
     SELECT COUNT(u)
     FROM User u
     WHERE u.active = true
     AND u.role = com.civic_reporting.cittilenz.enums.UserRole.WARD_SUPERIOR
 """)
 long countActiveWardSuperiors();

}
