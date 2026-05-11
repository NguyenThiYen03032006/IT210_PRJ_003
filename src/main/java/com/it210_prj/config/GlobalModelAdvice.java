package com.it210_prj.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void globalAttributes(Model model, Authentication authentication) {
        if (!model.containsAttribute("searchQuery")) {
            model.addAttribute("searchQuery", "");
        }
        if (!model.containsAttribute("selectedGenreId")) {
            model.addAttribute("selectedGenreId", null);
        }

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("loggedIn", loggedIn);

        boolean isCustomer = false;
        if (loggedIn) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            isCustomer = authorities.stream().anyMatch(a -> {
                String r = a.getAuthority();
                return "CUSTOMER".equals(r) || "ROLE_CUSTOMER".equals(r);
            });
        }
        model.addAttribute("isCustomer", isCustomer);
    }
}
