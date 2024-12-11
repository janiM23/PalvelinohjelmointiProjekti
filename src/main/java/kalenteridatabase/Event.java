package kalenteridatabase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Event extends AbstractPersistable<Long> {
    private String title;
    private String desc;
    private LocalDate date;
    //Foreign key login id:stä.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //private String img;
    private Boolean status = false;

    //Join-table kategorialle ja eventille.
    @ManyToMany
    @JoinTable(
            name = "EventCategoryTag",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_tag_id")
    )

    @JsonIgnore
    private List<Category> categoryTags = new ArrayList<>();

    //Tämä pitää olla kummassakin, koska muuten voi tulla rekursio-/stackoverflow-virhe.
    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", date='" + date + '\'' +
                ", status=" + status +
                ", categoryTagsSize=" + (categoryTags != null ? categoryTags.size() : 0) +
                '}';
    }
}
