package kalenteridatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarDatabaseController {

    private String name;

    @Autowired
    private CalendarRepository calendarRepository;

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("tasks", this.calendarRepository.findAll());
        return "index";
    }

    @PostMapping("/")
    public String post(@RequestParam String name) {
        this.name = name;
        calendarRepository.save(new Task(name));
        // opimme tämän "redirect:/"-loitsun merkityksen ihan kohta!
        return "redirect:/";
    }

}
