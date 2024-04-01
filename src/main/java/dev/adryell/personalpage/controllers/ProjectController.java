package dev.adryell.personalpage.controllers;

import dev.adryell.personalpage.dtos.PostDTO;
import dev.adryell.personalpage.dtos.ProjectDTO;
import dev.adryell.personalpage.dtos.UpdateProjectDTO;
import dev.adryell.personalpage.models.Post;
import dev.adryell.personalpage.models.Project;
import dev.adryell.personalpage.models.Tag;
import dev.adryell.personalpage.models.User;
import dev.adryell.personalpage.projections.PostProjection;
import dev.adryell.personalpage.projections.ProjectProjection;
import dev.adryell.personalpage.repositories.AuthTokenRepository;
import dev.adryell.personalpage.repositories.ProjectRepository;
import dev.adryell.personalpage.repositories.TagRepository;
import dev.adryell.personalpage.repositories.UserRepository;
import dev.adryell.personalpage.services.RequiresPermission;
import dev.adryell.personalpage.utils.enums.Permissions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthTokenRepository authTokenRepository;

    @PostMapping("/create")
    ResponseEntity<Object> createProject(@RequestBody @Valid ProjectDTO projectData, HttpServletRequest request) {
        Project project = new Project();

        project.setTitle(projectData.title());
        project.setDescription(projectData.description());

        UUID token = UUID.fromString(request.getHeader("Authorization").replace("Bearer ", ""));
        User user = authTokenRepository.findByTokenAndActiveTrue(token).getUser();
        project.setCreator(user);

        if (projectData.tagIds() != null) {
            Set<Tag> tags = this.findTagsByIds(projectData.tagIds());

            if (tags == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more tags not found.");
            }

            project.setTags(tags);
        }

        projectRepository.save(project);

        return ResponseEntity.status(HttpStatus.CREATED).body(createProjectProjection(project));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProjectById(@PathVariable Long id){
        Optional<Project> existingProject = projectRepository.findById(id);

        return existingProject.
                <ResponseEntity<Object>>map(project -> ResponseEntity.status(HttpStatus.OK).body(createProjectProjection(project)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found."));
    }

    @GetMapping("/")
    public Page<ProjectProjection> getProjects(
            @RequestParam(required = false) Boolean active,
            Pageable pageable
    ) {
        Specification<Project> specification = Specification.where(null);

        if (active != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("active"), active));
        }

        Page<Project> projects = projectRepository.findAll(specification, pageable);

        return mapProjectsToProjection(projects);
    }

    @RequiresPermission(Permissions.UPDATE_PROJECT)
    @PutMapping("/update/{id}")
    ResponseEntity<Object> updateProject(@RequestBody @Valid UpdateProjectDTO projectData, @PathVariable Long id) {
        Optional<Project> existingProject = projectRepository.findById(id);

        if (existingProject.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
        }

        Project project = existingProject.get();

        if (projectData.title() != null) {
            project.setTitle(projectData.title());
        }

        if (projectData.description() != null) {
            project.setDescription(projectData.description());
        }

        if (projectData.active() != null) {
            project.setActive(projectData.active());
        }

        if (projectData.creatorId() != null) {
            Optional<User> creator = userRepository.findById(projectData.creatorId());

            if (creator.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Creator not found.");
            }

            project.setCreator(creator.get());
        }

        if (projectData.tagIds() != null) {
            Set<Tag> tags = this.findTagsByIds(projectData.tagIds());

            if (tags == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("One or more tags not found.");
            }

            project.setTags(tags);
        }

        projectRepository.save(project);

        return ResponseEntity.status(HttpStatus.OK).body(createProjectProjection(project));
    }

    @RequiresPermission(Permissions.DELETE_PROJECT)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteProject(@PathVariable Long id) {
        Optional<Project> existingProject = projectRepository.findById(id);

        if (existingProject.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
        }

        projectRepository.delete(existingProject.get());

        return ResponseEntity.ok("Deleted successfully.");
    }

    private Set<Tag> findTagsByIds(List<Long> tagIds) {
        return tagIds.stream()
                .map(tagRepository::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private Page<ProjectProjection> mapProjectsToProjection(Page<Project> projects) {
        List<ProjectProjection> projections = new ArrayList<>();
        for (Project project : projects.getContent()) {
            projections.add(this.createProjectProjection(project));
        }
        return new PageImpl<>(projections, projects.getPageable(), projects.getTotalElements());
    }

    private ProjectProjection createProjectProjection(Project project){
        return new ProjectProjectionImpl(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.isActive(),
                project.getCreator().getId(),
                project.getTags().stream().map(Tag::getName).collect(Collectors.toList())
        );
    }

    private record ProjectProjectionImpl(
            Long id,
            String title,
            String description,
            Boolean active,
            UUID creatorId,
            List<String> tags
    ) implements ProjectProjection {
    }
}
