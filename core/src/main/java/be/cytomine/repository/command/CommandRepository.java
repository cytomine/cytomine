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
                + "AND usi.userId = :userId "
                + "ORDER BY usi.created DESC"
    )
    List<UndoStackItem> findAllUndoOrderByCreatedDesc(Long userId, Transaction transaction);

    @Query(
        value =
            "SELECT rsi "
                + "FROM RedoStackItem rsi "
                + "WHERE rsi.transaction = :transaction "
                + "AND rsi.userId = :userId "
                + "ORDER BY rsi.created DESC"
    )
    List<RedoStackItem> findAllRedoOrderByCreatedDesc(Long userId, Transaction transaction);

    @Query(
        "SELECT usi FROM UndoStackItem usi WHERE usi.command = :command AND usi.userId = :userId ORDER BY usi"
            + ".created DESC")
    Page<UndoStackItem> findLastUndoStackItems(Long userId, Command command, Pageable pageable);

    @Query("SELECT usi FROM UndoStackItem usi WHERE usi.userId = :user ORDER BY usi.created DESC")
    Page<UndoStackItem> findLastUndoStackItems(Long userId, Pageable pageable);

    default Optional<UndoStackItem> findLastUndoStackItem(Long userId, Command command) {
        return findLastUndoStackItems(userId, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<UndoStackItem> findLastUndoStackItem(Long userId) {
        return findLastUndoStackItems(userId, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query(
        "SELECT usi FROM RedoStackItem usi WHERE usi.command = :command AND usi.userId = :userId ORDER BY usi"
            + ".created DESC")
    Page<RedoStackItem> findLastRedoStackItems(Long userId, Command command, Pageable pageable);

    @Query("SELECT usi FROM RedoStackItem usi WHERE usi.userId = :userId ORDER BY usi.created DESC")
    Page<RedoStackItem> findLastRedoStackItems(Long userId, Pageable pageable);

    default Optional<RedoStackItem> findLastRedoStackItem(Long userId, Command command) {
        return findLastRedoStackItems(userId, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<RedoStackItem> findLastRedoStackItem(long userId) {
        return findLastRedoStackItems(userId, PageRequest.of(0, 1)).stream().findFirst();
    }

    void deleteAllByProject(Project project);

    void deleteAllByUserId(long userId);
}
