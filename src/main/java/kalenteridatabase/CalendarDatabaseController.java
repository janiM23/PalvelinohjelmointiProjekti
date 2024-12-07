package kalenteridatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class CalendarDatabaseController {

    @Autowired
    private CalendarRepository calendarRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("events", this.calendarRepository.findAll());
        return "index";
    }

    @PostMapping("/")
    public String post(@RequestParam String title,
                       @RequestParam String desc,
                       @RequestParam String date,
                       @RequestParam String category) {

        //Kattelee, että onko kategoriaa vielä olemassa. Jos ei ole, luo uuden.
        Category categoryObject = categoryRepository.findByTag(category)
                .orElseGet(() -> {
                    Category newCategory = new Category(category);
                    categoryRepository.save(newCategory);
                    return newCategory;
                });

        Event event = new Event();
        event.setTitle(title);
        event.setDesc(desc);
        event.setDate(date);
        event.setStatus(false);

        //Collections.singletonList-metodi luo listan jossa on yksi kategoriaobjekti.
        //setCategoryTags on automaattisesti luotu lombokin @Data komennolla, eli näitä ei tarvi itse värkätä.
        event.setCategoryTags(Collections.singletonList(categoryObject));

        calendarRepository.save(event);
        return "redirect:/";
    }

}
