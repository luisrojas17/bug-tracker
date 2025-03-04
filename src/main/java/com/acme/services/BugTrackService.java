package com.acme.services;

import com.acme.commons.domains.Bug;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Service
public class BugTrackService {

    public static final List<String> APPLICATIONS =
            List.of("App1", "App2", "App3", "App4", "App5");

    // Atomic references are visible across multiple threads
    private final AtomicReference<BugTrackerConfiguration> bugTrackerConfiguration =
            new AtomicReference<>(new BugTrackerConfiguration(APPLICATIONS));

    // Atomic references are visible across multiple threads
    // With this kind of references, it is possible to use the same reference across multiple threads
    // and increment the value without affecting the other threads. All of them will have the same value
    private final AtomicLong idGenerator = new AtomicLong();

    private final List<Bug> bugs = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        long id1 = idGenerator.getAndIncrement();
        Bug bug =
                new Bug(id1, "Jose Luis",
                        "The coverage is less than 70%",
                        "headline",
                        "The report of coverage is incomplete.", Bug.BugSeverity.MAJOR, Bug.BugState.OPEN);
        bugs.add(bug);

        long id2 = idGenerator.getAndIncrement();
        Bug bug2 =
                new Bug(id2, "John Smith",
                        "Integration test with APIs does not work.",
                        "headline",
                        "The calls to external APIs throws timeout exceptions.", Bug.BugSeverity.CRITICAL, Bug.BugState.OPEN);
        bugs.add(bug2);

        long id3 = idGenerator.getAndIncrement();
        Bug bug3 =
                new Bug(id3, "Homer Simpson",
                        "The browser does not load the frontend.",
                        "headline",
                        "Browser latest to load the application.", Bug.BugSeverity.MAJOR, Bug.BugState.OPEN);
        bugs.add(bug3);

    }

    public BugTrackerConfiguration getConfiguration() {
        return this.bugTrackerConfiguration.get();
    }

    public Bug get(Long id) {
        return bugs.stream()
                .filter(bug -> bug.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Bug create(Bug bug) {

        // Spring security store the user authenticated inside the SecurityContextHolder.
        // It does not matter if it was used OAuth or SAML
        // On the other hand, if the application is using OpenID Connect,
        // the ID token associated with the user can be retrieved from the Authentication object.
        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OidcUser principal = (OidcUser) token.getPrincipal();

        long id = idGenerator.getAndIncrement();
        Bug newBug =
                new Bug(id,
                        principal.getPreferredUsername(),
                        bug.headline(),
                        bug.description(),
                        bug.project(),
                        bug.severity(),
                        bug.state());

        bugs.add(newBug);

        return newBug;
    }

    public Bug update(Bug bug) {

        int index = IntStream.range(0, bugs.size())
                .filter(i -> bugs.get(i).id().equals(bug.id()))
                .findFirst()
                .orElseThrow();

        this.bugs.set(index, bug);

        return bug;
    }

    public List<Bug> findAll() {
        // Return an unmodifiable list
        return List.copyOf(this.bugs);
    }

    public boolean delete(Long idToDelete) {
        return bugs.removeIf(bug -> bug.id().equals(idToDelete));
    }

    /**
     * This method is thread safe thankfully to the AtomicReference.
     *
     * @param newProject is an instance of String that represents new project to add.
     */
    public void addProject(String newProject) {
        BugTrackerConfiguration configuration = this.bugTrackerConfiguration.get();

        List<String> currentProjects = configuration.projects();

        List<String> newProjects = new ArrayList<>();
        newProjects.add(newProject);
        newProjects.addAll(currentProjects);

        this.bugTrackerConfiguration.set(new BugTrackerConfiguration(newProjects));

    }

    /**
     * This method is thread safe thankfully to the AtomicReference.
     *
     * @param projectToRemove is an instance of String that represents new project to add.
     */
    public void removeProject(String projectToRemove) {

        BugTrackerConfiguration configuration = this.bugTrackerConfiguration.get();

        List<String> currentProjects = configuration.projects();

        // We get a new list without the project to remove
        List<String> newProjects =
                currentProjects.stream().filter(p -> !p.equals(projectToRemove)).toList();

        this.bugTrackerConfiguration.set(new BugTrackerConfiguration(newProjects));
    }
}
