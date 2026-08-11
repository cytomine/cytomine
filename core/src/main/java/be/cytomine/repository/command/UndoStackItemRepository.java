package be.cytomine.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.command.UndoStackItem;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;

@Repository
public interface UndoStackItemRepository extends JpaRepository<UndoStackItem, Long> {

    void deleteAllByCommandProject(Project project);

    void deleteAllByUser(User user);
}
