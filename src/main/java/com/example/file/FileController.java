package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class FileController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @GetMapping("/fileCreate")
    public String showCreateForm(File file) {
        return "add-file";
    }

    @GetMapping({"/", "/index"})
    public String showFiles(Model model) {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        model.addAttribute("files", user.getFiles());
        return "index";
    }


    @PostMapping("/addfile")
    public String addFile(@Valid File file, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(fileRepository != null) {

            file.setUser(user);
            user.addFile(file);
            fileRepository.save(file);
        }
        model.addAttribute("files", user.getFiles());
        return "index";
    }

    // additional CRUD methods

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));

        model.addAttribute("user", file);
        return "update-file";
    }


    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable("id") long id, @Valid File file,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            file.setId(id);
            return "update-file";
        }

        fileRepository.save(file);
        model.addAttribute("files", fileRepository.findAll());
        return "index";
    }

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));
        fileRepository.delete(file);
        model.addAttribute("files", fileRepository.findAll());
        return "index";
    }
}

