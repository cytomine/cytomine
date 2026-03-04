package be.cytomine.repository.command;

import be.cytomine.domain.command.RedoStackItem;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedoStackItemRepository extends JpaRepository<RedoStackItem, Long> {
    void deleteAllByCommand_Project(Project project);

    void deleteAllByUser(User user);
}
