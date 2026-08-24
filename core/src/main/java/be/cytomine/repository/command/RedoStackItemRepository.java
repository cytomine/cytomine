package be.cytomine.repository.command;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.command.RedoStackItem;
import be.cytomine.domain.project.Project;

@Repository
public interface RedoStackItemRepository extends JpaRepository<RedoStackItem, Long> {
    void deleteAllByCommandProject(Project project);

    void deleteAllByUserId(long userId);
}
