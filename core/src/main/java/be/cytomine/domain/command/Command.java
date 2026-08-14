package be.cytomine.domain.command;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.service.ModelService;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "class", discriminatorType = DiscriminatorType.STRING)
public abstract class Command extends CytomineDomain {

    /**
     * JSON string with relevant field data
     */
    @Column(nullable = true)
    protected String data;

    /**
     * JSON object with data
     */
    @Transient
    protected JsonObject json; //TODO: support json array

    @Transient
    protected CytomineDomain domain;

    @Column(name = "user_id")
    protected Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = true)
    protected Transaction transaction;

    /**
     * Project concerned by command
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = true)
    protected Project project;

    /**
     * Flag that indicate that the message will be show or not for undo/redo
     */
    protected boolean printMessage = true;

    /**
     * Message explaining the command
     */
    @Column(nullable = true)
    protected String actionMessage;

    /**
     * Set to false if command is not undo(redo)-able By default, don't save command on stack
     */
    protected boolean saveOnUndoRedoStack = false;

    /**
     * Service name of the relevant domain for the command
     */
    @Column(nullable = true)
    protected String serviceName;

    /**
     * If command is saved on undo stack, refuse undo Usefull for project delete (cannot undo)
     */
    protected boolean refuseUndo = false;

    /**
     * Define fields available for JSON response
     *
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    public static JsonObject getDataFromDomain(CytomineDomain domain) {
        JsonObject returnArray = CytomineDomain.getDataFromDomain(domain);
        Command command = (Command) domain;
        returnArray.put("CLASSNAME", domain.getClass().getSimpleName());
        returnArray.put("serviceName", ((Command) domain).getServiceName());
        returnArray.put(
            "action",
            command.getActionMessage() + " by user with id " + command.getUserId()
        );
        returnArray.put("data", command.getData());
        returnArray.put("user", command.getUserId());
        String type = "UNKNOWN";
        if (domain instanceof AddCommand) {
            type = "ADD";
        } else if (domain instanceof EditCommand) {
            type = "EDIT";
        } else if (domain instanceof DeleteCommand) {
            type = "DELETE";
        }
        returnArray.put("type", type);
        return returnArray;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " " + this.id + "[" + this.created + "]";
    }

    /**
     * Add command info for the new domain concerned by the command
     *
     * @param newObject New domain
     * @param message   Message build for the command
     */
    protected void fillCommandInfo(CytomineDomain newObject, String message, UrlApi urlApi) {
        data = newObject.toJSON(urlApi);
        actionMessage = message;
    }

    /**
     * Add command info for the new domain concerned by the command
     *
     * @param newObject New json domain
     * @param message   Message build for the command
     */
    protected void fillCommandInfoJSON(String newObject, String message) {
        data = newObject;
        actionMessage = message;
    }

    public abstract CommandResponse execute(ModelService service, UrlApi urlApi);

    @Override
    public String toJSON(UrlApi urlApi) {
        return getDataFromDomain(this).toJsonString();
    }

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }
}
