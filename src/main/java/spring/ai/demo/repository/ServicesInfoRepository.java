package spring.ai.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import spring.ai.demo.entity.ServicesInfoEntity;

import java.util.List;

public interface ServicesInfoRepository extends JpaRepository<ServicesInfoEntity, String> {
    @Query("SELECT s.name FROM ServicesInfoEntity s WHERE s.isactive = :isActive ORDER BY s.name")
    List<String> findNameByIsActive(String isActive);

    List<ServicesInfoEntity> findByName(String name);
    List<ServicesInfoEntity> findByNameContaining(String name);
}
