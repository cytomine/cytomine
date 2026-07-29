package be.cytomine.repository.command;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import be.cytomine.domain.command.Command;
import be.cytomine.domain.command.RedoStackItem;
import be.cytomine.domain.command.Transaction;
import be.cytomine.domain.command.UndoStackItem;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {

    List<Command> findAllByServiceName(String serviceName);

    List<Command> findAllByServiceNameAndCreatedGreaterThan(String serviceName, Date created);

    @Query(
        value =
            "SELECT usi "
                + "FROM UndoStackItem usi "
                + "WHERE usi.transaction = :transaction "
                + "AND usi.user = :user "
                + "ORDER BY usi.created DESC"
    )
    List<UndoStackItem> findAllUndoOrderByCreatedDesc(User user, Transaction transaction);

    @Query(
        value =
            "SELECT rsi "
                + "FROM RedoStackItem rsi "
                + "WHERE rsi.transaction = :transaction "
                + "AND rsi.user = :user "
                + "ORDER BY rsi.created DESC"
    )
    List<RedoStackItem> findAllRedoOrderByCreatedDesc(User user, Transaction transaction);

    @Query(
        "SELECT usi FROM UndoStackItem usi WHERE usi.command = :command AND usi.user = :user ORDER BY usi.created DESC")
    Page<UndoStackItem> findLastUndoStackItems(User user, Command command, Pageable pageable);

    @Query("SELECT usi FROM UndoStackItem usi WHERE usi.user = :user ORDER BY usi.created DESC")
    Page<UndoStackItem> findLastUndoStackItems(User user, Pageable pageable);

    default Optional<UndoStackItem> findLastUndoStackItem(User user, Command command) {
        return findLastUndoStackItems(user, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<UndoStackItem> findLastUndoStackItem(User user) {
        return findLastUndoStackItems(user, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query(
        "SELECT usi FROM RedoStackItem usi WHERE usi.command = :command AND usi.user = :user ORDER BY usi.created DESC")
    Page<RedoStackItem> findLastRedoStackItems(User user, Command command, Pageable pageable);

    @Query("SELECT usi FROM RedoStackItem usi WHERE usi.user = :user ORDER BY usi.created DESC")
    Page<RedoStackItem> findLastRedoStackItems(User user, Pageable pageable);

    default Optional<RedoStackItem> findLastRedoStackItem(User user, Command command) {
        return findLastRedoStackItems(user, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<RedoStackItem> findLastRedoStackItem(User user) {
        return findLastRedoStackItems(user, PageRequest.of(0, 1)).stream().findFirst();
    }

    void deleteAllByProject(Project project);

    void deleteAllByUser(User user);
}
