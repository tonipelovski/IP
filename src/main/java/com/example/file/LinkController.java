package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LinkController {
    @Autowired
    LinkRepository linkRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    UserRepository userRepository;

    private Link findLinkByCode(String code){
        for(Link link : linkRepository.findAll()){
            if(link.getLinkCode().equals(code)){
                return link;
            }
        }
        return null;
    }

    @GetMapping({"/link/{linkCode}"})
    public String showFile(@PathVariable("linkCode") String code, Model model) {
        Link link = findLinkByCode(code);
        if(link.getFile().getType() == 0) {
            model.addAttribute("fileName", link.getFile().getName());
            return "link";
        }else{
            User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            File file = link.getFile();
            List<File> filesToShow = new ArrayList<>();
                for (File file1 : user.getFiles()) {
                    if (file1.getParentId() != null) {
                        if (file1.getParentId().equals(file.getId())) {
                            filesToShow.add(file1);
                        }
                    }
                }
                model.addAttribute("file", new File());
                model.addAttribute("files", filesToShow);
                return "opened";
            }

    }
}
