package com.autohublanka.support.controller;

import com.autohublanka.support.dto.ContactRequest;
import com.autohublanka.support.dto.ContactResponse;
import com.autohublanka.support.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contact Us form - open to everyone, logged in or not. */
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ContactResponse send(@Valid @RequestBody ContactRequest request, HttpServletRequest http) {
        return contactService.send(request, http.getRemoteAddr());
    }
}
