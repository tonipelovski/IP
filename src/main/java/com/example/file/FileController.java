package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
        model.addAttribute("file", new File());
        model.addAttribute("files", findRoot());
        return "index";
    }

    public List<File> findRoot(){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<File> noParents = new ArrayList<>();
        for(File file : user.getFiles()){
            if(file.getParent().equals("")){
                noParents.add(file);
            }
        }
        return noParents;
    }

    @PostMapping("/addfile")
    public String addFile(HttpServletRequest request, @Valid File file, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(fileRepository != null) {

            file.setUser(user);
            user.addFile(file);
            fileRepository.save(file);
        }
        model.addAttribute("files", findRoot());
        model.addAttribute("file", new File());
        return "index";
    }

    // additional CRUD methods

    @PostMapping("/subAdd")
    public String subAdd(@Valid File file, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(fileRepository != null) {

            file.setUser(user);
            user.addFile(file);
            fileRepository.save(file);
        }
        model.addAttribute("file", new File());
        model.addAttribute("files", findRoot());
        return "index";
    }


    @GetMapping("/open/{id}")
    public String openFolder(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(file.getType() != 0) {
            List<File> filesToShow = new ArrayList<>();
            for (File file1 : user.getFiles()) {
                if (file1.getParent().equals(file.getName())) {
                    filesToShow.add(file1);
                }
            }
            model.addAttribute("file", new File());
            model.addAttribute("files", filesToShow);
            return "opened";
        }else{
            model.addAttribute("file", new File());
            model.addAttribute("files", findRoot());
            return "index";
        }
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));

        model.addAttribute("file", file);
        return "update-file";
    }





    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable("id") long id, @Valid File file,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            file.setId(id);
            return "update-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        file.setUser(user);
        fileRepository.save(file);
        model.addAttribute("file", new File());
        model.addAttribute("files", findRoot());
        return "index";
    }

    @GetMapping("/delete/{id}")
    public String deleteFile(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        for(File file1 : user.getFiles()){
            if(file1.getId().equals(file.getId())){
                user.getFiles().remove(file1);
                break;
            }
        }

        fileRepository.delete(file);
        model.addAttribute("files", findRoot());
        model.addAttribute("file", new File());

        return "index";
    }



    //upload


}

