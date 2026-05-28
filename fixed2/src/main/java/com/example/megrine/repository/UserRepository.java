package com.example.megrine.repository;

import com.example.megrine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByAccountStatus(User.AccountStatus status);
    List<User> findByRoleOrderByPointsDesc(String role);
    long countByAccountStatus(User.AccountStatus status);

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_MEMBER' ORDER BY u.points DESC")
    List<User> findMembersByPoints();
}
