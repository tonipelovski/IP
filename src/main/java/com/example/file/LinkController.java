package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LinkController {
    @Autowired
    LinkRepository linkRepository;

    @Autowired
    FileRepository fileRepository;

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
        model.addAttribute("fileName", link.getFile().getName());
        return "link";
    }
}
