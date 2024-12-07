package kalenteridatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Event extends AbstractPersistable<Long> {
    private String title;
    private String desc;
    private String date;
    //private String img;
    private Boolean status = false;

    @ManyToMany
    @JoinTable(
            name = "EventCategoryTag",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_tag_id")
    )
    private List<Category> categoryTags = new ArrayList<>();
}
