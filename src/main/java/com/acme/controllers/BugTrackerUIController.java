package com.acme.controllers;

import com.acme.commons.domains.Bug;
import com.acme.commons.helpers.TokenHelper;
import com.acme.services.BugTrackService;
import com.acme.services.BugTrackerConfiguration;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class BugTrackerUIController {

    private final BugTrackService bugTrackService;

    @GetMapping("/")
    public String home() {
        return "redirect:/bugtracker/ui";
    }

    @GetMapping("/bugtracker/ui")
    public ModelAndView home(OAuth2AuthenticationToken token,
                             @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {

        OidcUser principal = (OidcUser) token.getPrincipal();
        ModelAndView modelAndView = getDefaultModel(token);

        modelAndView.setViewName("home");
        modelAndView.addObject("idToken",
                TokenHelper.prettyBody(principal.getIdToken().getTokenValue()));
        modelAndView.addObject("accessToken",
                TokenHelper.prettyBody(client.getAccessToken().getTokenValue()));
        modelAndView.addObject("refreshToken",
                TokenHelper.prettyBody(client.getRefreshToken().getTokenValue()));


        return modelAndView;

    }

    @GetMapping("/bugtracker/ui/show-bugs")
    public ModelAndView showBugs(OAuth2AuthenticationToken token) {

        ModelAndView modelAndView = getDefaultModel(token);
        modelAndView.setViewName("bugs");
        modelAndView.addObject("bugs", bugTrackService.findAll());

        return modelAndView;

    }

    @GetMapping("/bugtracker/ui/show-create-form")
    public ModelAndView showCreateForm(OAuth2AuthenticationToken token) {

        BugTrackerConfiguration configuration =
                bugTrackService.getConfiguration();

        ModelAndView modelAndView = getDefaultModel(token);
        modelAndView.setViewName("bug-form");
        modelAndView.addObject("bug", Bug.empty(token.getName()));
        modelAndView.addObject("projects", configuration.projects());

        return modelAndView;

    }

    @PostMapping("/bugtracker/ui/save-bug")
    public String saveBug(OAuth2AuthenticationToken token,
                                @ModelAttribute Bug bug) {

        if (Objects.isNull(bug.id())) {
            bugTrackService.create(bug);
        } else {
            bugTrackService.update(bug);
        }

        return "redirect:/bugtracker/ui/show-bugs";

    }

    @GetMapping("/bugtracker/ui/show-update-form")
    public ModelAndView showUpdateForm(OAuth2AuthenticationToken token,
                                       @RequestParam @NotNull Long bugId) {

        Bug bug = bugTrackService.get(bugId);

        BugTrackerConfiguration configuration =
                bugTrackService.getConfiguration();

        ModelAndView modelAndView = getDefaultModel(token);
        modelAndView.setViewName("bug-form");
        modelAndView.addObject("bug", Bug.empty(token.getName()));
        modelAndView.addObject("projects", configuration.projects());

        return modelAndView;

    }

    @GetMapping("/bugtracker/ui/delete-bug")
    public String deleteBug(OAuth2AuthenticationToken token,
                                       @RequestParam @NotNull Long bugId) {

        bugTrackService.delete(bugId);

        return "redirect:/bugtracker/ui/show-bugs";

    }

    @GetMapping("/bugtracker/ui/admin/show-edit-config")
    public ModelAndView showEditConfiguration(OAuth2AuthenticationToken token) {

        BugTrackerConfiguration cfg = bugTrackService.getConfiguration();

        ModelAndView model = getDefaultModel(token);
        model.setViewName("config-form");
        model.addObject("configuration", cfg);
        return model;
    }

    @PostMapping("/bugtracker/ui/admin/add-project")
    public String addProject(OAuth2AuthenticationToken token,
                             @RequestParam @NotEmpty String project) {

        bugTrackService.addProject(project);
        return "redirect:/bugtracker/ui/admin/show-edit-config";
    }

    @GetMapping("/bugtracker/ui/admin/remove-project")
    public String removeProject(OAuth2AuthenticationToken token,
                                @RequestParam @NotEmpty String project) {

        bugTrackService.removeProject(project);
        return "redirect:/bugtracker/ui/admin/show-edit-config";
    }

    /*
     * Sets some basic user information. The call can add more properties
     * to it before passing to the view file.
     */
    private ModelAndView getDefaultModel(OAuth2AuthenticationToken token) {

        OidcUser principal = (OidcUser) token.getPrincipal();

        ModelAndView model = new ModelAndView();
        model.addObject("user", principal);
        return model;
    }
}
