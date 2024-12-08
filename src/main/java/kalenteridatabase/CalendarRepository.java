package kalenteridatabase;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategoryTags(Category category);
}