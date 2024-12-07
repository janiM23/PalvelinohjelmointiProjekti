package kalenteridatabase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Category extends AbstractPersistable<Long>{

    @Column(unique = true, nullable = false)
    private String tag;

    @ManyToMany(mappedBy = "categoryTags")
    private List<Event> events = new ArrayList<>();

    //Tarvitaan jotta controlleri toimii ilman lista -parametria.
    public Category(String tag) {
        this.tag = tag;
    }
}
