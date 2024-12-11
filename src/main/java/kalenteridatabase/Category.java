package kalenteridatabase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
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
    @JsonIgnore
    private List<Event> events = new ArrayList<>();

    //Foreign key login id:stä.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // This ensures that the category is associated with a user
    private User user;

    //Tarvitaan jotta controlleri toimii ilman lista -parametria.
    public Category(String tag) {
        this.tag = tag;
    }

    //Tämä pitää olla kummassakin, koska muuten voi tulla rekursio-/stackoverflow-virhe.
    @Override
    public String toString() {
        return "Category{" +
                "tag='" + tag + '\'' +
                //jos event lista ei ole tyhjä, palauttaa koon, jos tyhjä, palauttaa nollan.
                ", eventsSize=" + (events != null ? events.size() : 0) +
                '}';
    }
}