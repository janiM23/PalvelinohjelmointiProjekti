package kalenteridatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
        // Retrieve the list of events
        List<Event> events = calendarRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        // Sort the events by localtime (oldest to newest)
        events = events.stream()
                .sorted(Comparator.comparing(Event::getDate)) // Sorting by localtime field
                .collect(Collectors.toList());

        // Add sorted events to the model
        System.out.println("eventit: " + events);
        model.addAttribute("events", events);
        model.addAttribute("categories", categories);
        return "index";
    }

    //Näyttää yksittäisen eventin.
    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        model.addAttribute("event", event);
        //eventDetail kohta, jossa thymeleaf tarkistaa onko editti päällä.
        //Tämä GET hoitaa vaan näyttämistä, joten se asetetaan false.
        model.addAttribute("editMode", false);
        return "eventDetail";
    }

    //Yksittäisen eventin editointi.
    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        model.addAttribute("event", event);
        //eventDetail kohta, jossa thymeleaf tarkistaa onko editti päällä.
        //Tämä osio hoitaa editoinnin, joten se asetetaan true.
        model.addAttribute("editMode", true); // Switch to edit mode
        return "eventDetail";
    }

    //Näyttää kategoriassa olevat eventit.
    @GetMapping("/categories/{id}")
    public String showCategoryEvents(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);

        if (category != null) {
            List<Event> events = calendarRepository.findByCategoryTags(category);
            model.addAttribute("category", category);
            model.addAttribute("events", events);
        }
        return "category";
    }

    //Yksittäisen eventin editointi.
    @PostMapping("/events/{id}/edit")
    public String saveEditedEvent(@PathVariable Long id, @ModelAttribute Event event) {
        Event existingEvent = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        //Päivitetään arvot. eventDetail.html hoitaa logiikan, ettei käyttäjä syötä väärää päivämäärä.
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDesc(event.getDesc());
        existingEvent.setDate(event.getDate());
        existingEvent.setStatus(event.getStatus());
        calendarRepository.save(existingEvent);

        return "redirect:/";
    }

    //Listaa eventin kategoriat muokattavaksi tai lisättäväksi.
    @GetMapping("/events/{id}/edit-categories")
    public String editCategories(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        model.addAttribute("event", event);
        model.addAttribute("categories", event.getCategoryTags());

        return "categoriesEdit";
    }

    //Laittaa kategorian eventille, vaikka kategoria olisi jo olemassa.
    @PostMapping("/events/{id}/edit-categories")
    public String saveCategories(@PathVariable Long id, @RequestParam List<String> categoryNames) {

        //Etsii oikean eventin id:n avulla.
        Event event = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        //Etsii eventistä jo olemassa olevat kategoriat.
        List<Category> currentCategories = event.getCategoryTags();

        //Etsii onko kategorioissa jo annettua kategoriaa.
        List<Category> updatedCategories = categoryRepository.findByTagIn(categoryNames);
        System.out.println("updatedcategories: " + updatedCategories);

        //Suodattaa kaikki jotka ei ole olemassa tietokannassa.
        //Jos on uusi luodaan sille kategoria.
        //Lopuksi kerätään listaan kaikki uudet kategoriat.
        List<Category> missingCategories = categoryNames.stream()
                .filter(name -> updatedCategories.stream().noneMatch(category -> category.getTag().equalsIgnoreCase(name)))
                .map(tag -> new Category(tag))
                .collect(Collectors.toList());

        System.out.println("missingcategories: " + missingCategories);

        //Uudet kategoriat tietokantaan.
        List<Category> savedCategories = categoryRepository.saveAll(missingCategories);
        updatedCategories.addAll(savedCategories);

        //Yhdistää eventin vanhat tagit + uudet.
        Set<Category> mergedCategories = new HashSet<>(currentCategories);
        mergedCategories.addAll(updatedCategories);

        //Laittaa eventtiin oikeat kategoria tägit.
        event.setCategoryTags(new ArrayList<>(mergedCategories));
        System.out.println("eventti uudestaan: " + event);

        calendarRepository.save(event);

        return "redirect:/events/" + id;  // Redirect back to the event details page
    }

    //Poistaa tietyn kategorian eventistä.
    @PostMapping("/events/{eventId}/remove-category/{categoryId}")
    public String removeCategoryFromEvent(@PathVariable Long eventId, @PathVariable Long categoryId) {
        Event event = calendarRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        event.getCategoryTags().remove(category);  // Remove the category from the event

        // Save the updated event
        calendarRepository.save(event);

        return "redirect:/events/" + eventId + "/edit-categories";  // Redirect back to category editing page
    }

    //Uuden eventin luominen ja lisäys tietokantaan.
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