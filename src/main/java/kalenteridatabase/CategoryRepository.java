package kalenteridatabase;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByTag(String tag);
    List<Category> findByTagIn(List<String> tags);
}
