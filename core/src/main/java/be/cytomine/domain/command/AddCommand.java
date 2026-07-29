package be.cytomine.domain.command;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.service.ModelService;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.CommandResponse;

/**
 * @author Cytomine Team The AddCommand class is a command that create a new domain It provide an execute method that
 * create domain from command, an undo method that drop domain and an redo method that recreate domain
 */
@Entity
@DiscriminatorValue("be.cytomine.domain.command.AddCommand")
public class AddCommand extends Command {

    public AddCommand(User currentUser) {
        this.user = currentUser;
    }

    public AddCommand(User currentUser, Transaction transaction) {
        this.user = currentUser;
        this.transaction = transaction;
    }

    public AddCommand() {
        super();
    }

    /**
     * Process an Add operation for this command
     *
     * @return Message
     */
    public CommandResponse execute(ModelService service, UrlApi urlApi) {
        //Create new domain from json data
        json.put("id", null);
        CytomineDomain newDomain = service.createFromJSON(json);
        //Save new domain in database
        CommandResponse response = service.create(newDomain, printMessage);
        //Init command domain
        newDomain = response.getObject();
        fillCommandInfo(newDomain, (String) response.getData().get("message"), urlApi);
        if (newDomain != null) {
            CytomineDomain container = newDomain.container();
            if (container != null && container instanceof Project) {
                super.setProject((Project) container);
            }
        }

        return response;
    }
}
