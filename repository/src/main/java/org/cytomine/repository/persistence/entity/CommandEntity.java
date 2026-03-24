package org.cytomine.repository.persistence.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "command")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Version
    private Integer version = 0;

    @Column
    private Date created;

    @Column
    private Date updated;

    @Column(name = "class")
    private String commandType;

    @Column
    private String data;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "print_message")
    private boolean printMessage;

    @Column(name = "action_message")
    private String actionMessage;

    @Column(name = "save_on_undo_redo_stack")
    private boolean saveOnUndoRedoStack;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "refuse_undo")
    private boolean refuseUndo;
}
