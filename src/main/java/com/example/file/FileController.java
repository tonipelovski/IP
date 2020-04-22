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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class FileController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    LinkRepository linkRepository;

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

    public Long findParentId(String name){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        for(File file : user.getFiles()){
            if(file.getParent().equals(name)){
                return file.getId();
            }
        }
        return null;
    }


    public void findFake(File file){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(user.getFiles().isEmpty()){
            file.setParent("");
            return;
        }
        for(File file1 : user.getFiles()){
            if(file1.getName().equals(file.getParent()) && file1.getType() == 0){
                file.setParent("");
            }
        }
    }

    @PostMapping("/addfile")
    public String addFile(HttpServletRequest request, @Valid File file, BindingResult result, Model model,
                          @RequestParam(value = "file") MultipartFile fileUploaded) throws IOException {
        if (result.hasErrors()) {
            return "add-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(fileRepository != null) {
            if(file.getType() == 0) {
                file.setData(fileUploaded.getBytes());
            }

            findFake(file);
            findParentId(file.getParent());
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
    public String subAdd(@Valid File file, BindingResult result, Model model, @RequestParam(value = "file") MultipartFile fileUploaded) throws IOException {
        if (result.hasErrors()) {
            return "add-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if(fileRepository != null) {
            if(file.getType() == 0) {
                file.setData(fileUploaded.getBytes());
            }

            findFake(file);
            file.setUser(user);
            user.addFile(file);
            fileRepository.save(file);
        }
        model.addAttribute("file", new File());
        model.addAttribute("files", findRoot());
        return "index";
    }

    @GetMapping("/addLink/{id}")
    public String addLink(@PathVariable("id") long id, Model model) throws IOException {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));

        Link link = new Link();
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        link.setLinkCode(generatedString);
        link.setFile(file);

        for(Link link1 : linkRepository.findAll()){
            if(link1.getFile().getId().equals(link.getFile().getId())){
                linkRepository.delete(link1);
            }
        }

        linkRepository.save(link);
        model.addAttribute("link", "/link/" + link.getLinkCode());
        return "link_created";
    }


    @GetMapping("/open/{id}")
    public String openFolder(@PathVariable("id") long id, Model model) throws Throwable {
        File file = (File) fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid file Id:" + id));
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        List<File> filesToShow = new ArrayList<>();
        if(file.getType() != 0) {
            for (File file1 : user.getFiles()) {
                if(file1.getParentId() != null) {
                    if (file1.getParentId().equals(file.getId())) {
                        filesToShow.add(file1);
                    }
                }
            }
            model.addAttribute("file", new File());
            model.addAttribute("files", filesToShow);
            return "opened";
        }else{
            String s = new String(file.getData(), StandardCharsets.UTF_8);
            model.addAttribute("fileData", s);
            model.addAttribute("id", file.getId());
            model.addAttribute("fileName", file.getName());
            return "opened_file";
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
                             BindingResult result, Model model, @RequestParam(value = "file") MultipartFile fileUploaded) throws IOException {
        if (result.hasErrors()) {
            file.setId(id);
            return "update-file";
        }
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(file.getType() == 0) {
            file.setData(fileUploaded.getBytes());
        }
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



}

