package be.cytomine.repository.command;

/*
 * Copyright (c) 2009-2022. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                + "AND usi.user.id = :userId "
                + "ORDER BY usi.created DESC"
    )
    List<UndoStackItem> findAllUndoOrderByCreatedDesc(Long userId, Transaction transaction);

    @Query(
        value =
            "SELECT rsi "
                + "FROM RedoStackItem rsi "
                + "WHERE rsi.transaction = :transaction "
                + "AND rsi.user.id = :userId "
                + "ORDER BY rsi.created DESC"
    )
    List<RedoStackItem> findAllRedoOrderByCreatedDesc(Long userId, Transaction transaction);

    @Query(
        "SELECT usi FROM UndoStackItem usi WHERE usi.command = :command AND usi.user.id = :userId ORDER BY usi"
            + ".created DESC")
    Page<UndoStackItem> findLastUndoStackItems(Long userId, Command command, Pageable pageable);

    @Query("SELECT usi FROM UndoStackItem usi WHERE usi.user.id = :user ORDER BY usi.created DESC")
    Page<UndoStackItem> findLastUndoStackItems(Long userId, Pageable pageable);

    default Optional<UndoStackItem> findLastUndoStackItem(Long userId, Command command) {
        return findLastUndoStackItems(userId, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<UndoStackItem> findLastUndoStackItem(Long userId) {
        return findLastUndoStackItems(userId, PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query(
        "SELECT usi FROM RedoStackItem usi WHERE usi.command = :command AND usi.user.id = :userId ORDER BY usi"
            + ".created DESC")
    Page<RedoStackItem> findLastRedoStackItems(Long userId, Command command, Pageable pageable);

    @Query("SELECT usi FROM RedoStackItem usi WHERE usi.user.id = :userId ORDER BY usi.created DESC")
    Page<RedoStackItem> findLastRedoStackItems(Long userId, Pageable pageable);

    default Optional<RedoStackItem> findLastRedoStackItem(Long userId, Command command) {
        return findLastRedoStackItems(userId, command, PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<RedoStackItem> findLastRedoStackItem(long userId) {
        return findLastRedoStackItems(userId, PageRequest.of(0, 1)).stream().findFirst();
    }

    void deleteAllByProject(Project project);

    void deleteAllByUser(User user);
}
