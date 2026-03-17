package be.cytomine.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.command.RedoStackItem;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;

@Repository
public interface RedoStackItemRepository extends JpaRepository<RedoStackItem, Long> {
    void deleteAllByCommand_Project(Project project);

    void deleteAllByUser(User user);
}
