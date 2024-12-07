package kalenteridatabase;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<Event, Long> {
}