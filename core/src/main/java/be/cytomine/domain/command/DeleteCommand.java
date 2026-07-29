package be.cytomine.domain.command;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.service.ModelService;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.CommandResponse;

@Getter
@Setter
@Entity
@DiscriminatorValue("be.cytomine.domain.command.DeleteCommand")
public class DeleteCommand extends Command {

    @Transient
    Object backup;
    /**
     * Add project link in command
     */
    boolean linkProject = true;

    public DeleteCommand(User currentUser, Transaction transaction) {
        this.user = currentUser;
        this.transaction = transaction;
    }

    public DeleteCommand() {
    }

    /**
     * Process an Add operation for this command
     *
     * @return Message
     */
    public CommandResponse execute(ModelService service, UrlApi urlApi) {
        //Retrieve domain to delete it
        CytomineDomain oldDomain = domain;
        //Init command info
        CytomineDomain container = oldDomain.container();
        if (container != null && container instanceof Project && linkProject) {
            super.setProject((Project) container);
        }
        CommandResponse response = service.destroy(oldDomain, printMessage);
        fillCommandInfoJSON(backup.toString(), response.getData().get("message").toString());
        return response;
    }
}
