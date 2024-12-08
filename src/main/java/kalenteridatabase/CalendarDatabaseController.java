package kalenteridatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class CalendarDatabaseController {

    @Autowired
    private CalendarRepository calendarRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DataSourceTransactionManagerAutoConfiguration dataSourceTransactionManagerAutoConfiguration;

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("events", this.calendarRepository.findAll());
        return "index";
    }

    @PostMapping("/")
    public String post(@RequestParam String title,
                       @RequestParam String desc,
                       @RequestParam String date,
                       @RequestParam String category,
                       RedirectAttributes redirectAttributes) {

        System.out.println("Input Parameters:");
        System.out.println("Title: " + title);
        System.out.println("Description: " + desc);
        System.out.println("Date: " + date);
        System.out.println("Category: " + category);

        //List<Event> events = calendarRepository.findAll();
        //model.addAttribute("events", events);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");  // Format for user input
        LocalDate parsedDate = null;
        try {
            parsedDate = LocalDate.parse(date, formatter);
            System.out.println(parsedDate);
        } catch (DateTimeParseException e) {
            System.out.println("Virhe.");
            redirectAttributes.addFlashAttribute("dateError", "Invalid date format. Please use dd.MM.yyyy.");
            System.out.println("Date Error: " + redirectAttributes.getFlashAttributes());
            return "redirect:/";
        }

        //Kattelee, että onko kategoriaa vielä olemassa. Jos ei ole, luo uuden.
        Category categoryObject = categoryRepository.findByTag(category)
                .orElseGet(() -> {
                    System.out.println("Category not found. Creating a new one.");
                    Category newCategory = new Category(category);
                    categoryRepository.save(newCategory);
                    return newCategory;
                });

        System.out.println("Category object: " + categoryObject);

        Event event = new Event();
        event.setTitle(title);
        event.setDesc(desc);
        event.setDate(parsedDate);
        event.setStatus(false);
        categoryObject.getEvents().add(event);
        //Collections.singletonList-metodi luo listan jossa on yksi kategoriaobjekti.
        //setCategoryTags on automaattisesti luotu lombokin @Data komennolla, eli näitä ei tarvi itse värkätä.
        event.setCategoryTags(Collections.singletonList(categoryObject));
        System.out.println("Event object before category assignment: " + event);

        System.out.println("Event object: " + event);
        calendarRepository.save(event);
        System.out.println("Event saved successfully.");
        return "redirect:/";
    }

}
