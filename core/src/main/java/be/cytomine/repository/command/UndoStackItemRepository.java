package be.cytomine.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.command.UndoStackItem;
import be.cytomine.domain.project.Project;

@Repository
public interface UndoStackItemRepository extends JpaRepository<UndoStackItem, Long> {

    void deleteAllByCommandProject(Project project);

    void deleteAllByUserId(long userId);
}
