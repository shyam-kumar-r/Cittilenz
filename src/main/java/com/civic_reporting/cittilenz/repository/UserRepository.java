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
    
    Optional<User> findByEmailAndActiveTrue(String email);

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
 
 Optional<User> findByRole(UserRole role);
 
 @Query("""
		    SELECT u.id FROM User u
		    WHERE u.role = com.civic_reporting.cittilenz.enums.UserRole.OFFICIAL
		      AND u.wardId = :wardId
		      AND u.departmentId = :departmentId
		      AND u.id <> :currentOfficialId
		      AND u.active = true
		    ORDER BY function('random')
		""")
		List<Integer> findRandomOfficialForAssignmentList(
		        Integer wardId,
		        Integer departmentId,
		        Integer currentOfficialId
		);
 
 @Query(value = """
		    SELECT u.id
		    FROM users u
		    WHERE u.role = 'OFFICIAL'
		      AND u.ward_id = :wardId
		      AND u.department_id = :departmentId
		      AND u.id <> :currentOfficialId
		      AND u.is_active = true
		      AND u.id NOT IN (
		            SELECT ish.changed_by
		            FROM issue_status_history ish
		            WHERE ish.issue_id = :issueId
		              AND ish.changed_by IN (
		                    SELECT id FROM users WHERE role = 'OFFICIAL'
		              )
		            ORDER BY ish.changed_at DESC
		            LIMIT :limit
		      )
		    ORDER BY RANDOM()
		    LIMIT 1
		""", nativeQuery = true)
		Optional<Integer> findSmartOfficialForAssignment(
		        Integer wardId,
		        Integer departmentId,
		        Integer currentOfficialId,
		        Integer issueId,
		        Integer limit
		);
 
 @Query(value = """
		    SELECT u.id
		    FROM users u
		    WHERE u.role = 'OFFICIAL'
		      AND u.ward_id = :wardId
		      AND u.department_id = :departmentId
		      AND u.id <> :currentOfficialId
		      AND u.is_active = true
		    ORDER BY RANDOM()
		    LIMIT 1
		""", nativeQuery = true)
		Optional<Integer> findFallbackOfficial(
		        Integer wardId,
		        Integer departmentId,
		        Integer currentOfficialId
		);
 
 @Query(value = """
		    SELECT u.id
		    FROM users u
		    WHERE u.role = 'OFFICIAL'
		      AND u.ward_id = :wardId
		      AND u.department_id = :departmentId
		      AND u.is_active = true
		    ORDER BY RANDOM()
		    LIMIT 1
		""", nativeQuery = true)
		Optional<Integer> findAnyOfficial(
		        Integer wardId,
		        Integer departmentId
		);
}
