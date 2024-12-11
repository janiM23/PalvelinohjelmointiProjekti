package kalenteridatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    private LocalDate currentTime = LocalDate.now();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CalendarRepository calendarRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DataSourceTransactionManagerAutoConfiguration dataSourceTransactionManagerAutoConfiguration;

    @GetMapping("/")
    public String login() {
        return ("redirect:/login");
    }

    @GetMapping("/calendar")
    public String list(Model model) {
        //Haetaan eventit ja kategoriat listaan.
        List<Event> events = calendarRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        //Järjestetään eventit ajan mukaan(yksi vaatimuksista).
        events = events.stream()
                .sorted(Comparator.comparing(Event::getDate)) // Sorting by localtime field
                .collect(Collectors.toList());
        //Onko myöhässä


        //Attribuutit html:ään.
        System.out.println("eventit: " + events);
        model.addAttribute("currentTime", currentTime);
        model.addAttribute("events", events);
        model.addAttribute("categories", categories);
        return "index";
    }

    //Näyttää yksittäisen eventin.
    @GetMapping("/calendar/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        model.addAttribute("event", event);
        //eventDetail kohta, jossa thymeleaf tarkistaa onko editti päällä.
        //Tämä GET hoitaa vaan näyttämistä, joten se asetetaan false.
        model.addAttribute("editMode", false);
        return "eventDetail";
    }

    //Yksittäisen eventin editointi.
    @GetMapping("/calendar/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        model.addAttribute("event", event);
        //eventDetail kohta, jossa thymeleaf tarkistaa onko editti päällä.
        //Tämä osio hoitaa editoinnin, joten se asetetaan true.
        model.addAttribute("editMode", true); // Switch to edit mode
        return "eventDetail";
    }

    //Näyttää kategoriassa olevat eventit.
    @GetMapping("/calendar/categories/{id}")
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
    @PostMapping("/calendar/events/{id}/edit")
    public String saveEditedEvent(@PathVariable Long id, @ModelAttribute Event event) {
        Event existingEvent = calendarRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        //Päivitetään arvot. eventDetail.html hoitaa logiikan, ettei käyttäjä syötä väärää päivämäärä.
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDesc(event.getDesc());
        existingEvent.setDate(event.getDate());
        existingEvent.setStatus(event.getStatus());
        calendarRepository.save(existingEvent);

        return "redirect:/calendar/";
    }

    //Listaa eventin kategoriat muokattavaksi tai lisättäväksi.
    @GetMapping("/calendar/events/{id}/edit-categories")
    public String editCategories(@PathVariable Long id, Model model) {
        Event event = calendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        model.addAttribute("event", event);
        model.addAttribute("categories", event.getCategoryTags());

        return "categoriesEdit";
    }

    //Laittaa kategorian eventille, vaikka kategoria olisi jo olemassa.
    @PostMapping("/calendar/events/{id}/edit-categories")
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

        return "redirect:/calendar/events/" + id;
    }

    //Poistaa tietyn kategorian eventistä.
    //Pitää muistaa join table, joten ei niin yksinkertainen homma.
    @PostMapping("/calendar/events/{eventId}/remove-category/{categoryId}")
    public String removeCategoryFromEvent(@PathVariable Long eventId, @PathVariable Long categoryId) {

        //Etsitään eventti mistä pitäisi poistaa kategoria.
        Event event = calendarRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        //Etsitään kategoria.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        //Poistetaan kategoria eventistä tuon ylemmän category olennon avulla.
        event.getCategoryTags().remove(category);

        //Vielä muutoksen tallennus eventtiin.
        calendarRepository.save(event);

        //Vie takaisin siihen category edit -sivulle.
        return "redirect:/calendar/events/" + eventId + "/edit-categories";
    }

    //Poisto metodi.
    @GetMapping("/calendar/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        //Haetaan eventti id:llä.
        Optional<Event> eventOptional = calendarRepository.findById(id);
        System.out.println(eventOptional);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            //Poistetaan event kategorioista.
            for (Category category : event.getCategoryTags()) {
                category.getEvents().remove(event);
                categoryRepository.save(category);
            }
            System.out.println(event);
            calendarRepository.delete(event);
            System.out.println("Event deleted successfully.");
        }

        return "redirect:/calendar";
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

        //Tarkistaa onko päivämäärän syntaksi hyvä.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate parsedDate = null;
        try {
            parsedDate = LocalDate.parse(date, formatter);
            System.out.println(parsedDate);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            redirectAttributes.addFlashAttribute("dateError", "Invalid date format. Please use dd.MM.yyyy.");
            return "redirect:/";
        }

        //Tämä hakee kirjautumistiedoista id:n, jotta se voidaan asettaa event ja category.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = null;
        User loggedInUser = null;
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            loggedInUser = userRepository.findByLogin(username);
            if (loggedInUser != null) {
                userId = loggedInUser.getId();
                System.out.println("Logged-in user ID: " + userId);
            }
        }

        //Pakko olla tämä kikka, tai alkaa javan kootut itkut. Luota sanaani.
        final User finalUser = loggedInUser;

        //Etsii kategorian ja jos ei löydy luo uuden, ja laittaa sen userid:n sinne.
        Category categoryObject = categoryRepository.findByTag(category)
                .orElseGet(() -> {
                    System.out.println("Category not found. Creating a new one.");
                    Category newCategory = new Category(category);
                    newCategory.setUser(finalUser);
                    categoryRepository.save(newCategory);
                    return newCategory;
                });

        System.out.println("Category object: " + categoryObject);


        //Tehdään uus eventi-olento ja laitetaan tavarat sisään.
        Event event = new Event();
        event.setTitle(title);
        event.setDesc(desc);
        event.setDate(parsedDate);
        event.setStatus(false);
        event.setUser(loggedInUser);
        categoryObject.getEvents().add(event);

        //Pitää laittaa ne tagit manytomany suhteen takia.
        event.setCategoryTags(Collections.singletonList(categoryObject));
        System.out.println("Event object before saving: " + event);

        calendarRepository.save(event);
        System.out.println("Event saved successfully.");
        return "redirect:/calendar";
    }

}