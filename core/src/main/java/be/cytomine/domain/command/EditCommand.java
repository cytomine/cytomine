package be.cytomine.domain.command;

import java.util.HashMap;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.service.ModelService;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.ClassUtils;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

@Entity
@DiscriminatorValue("be.cytomine.domain.command.EditCommand")
public class EditCommand extends Command {

    public EditCommand(User currentUser, Transaction transaction) {
        this.user = currentUser;
        this.transaction = transaction;
    }

    public EditCommand() {
    }

    /**
     * Add command info for the new domain concerned by the command
     *
     * @param newObject domain after update
     * @param oldObject domain before update
     * @param message   Message build for the command
     */
    protected void fillCommandInfo(CytomineDomain newObject, String oldObject, UrlApi urlApi, String message) {
        HashMap<String, Object> paramsData = new HashMap<>();
        paramsData.put("previous" + ClassUtils.getClassName(newObject), JsonObject.toMap(oldObject));
        paramsData.put("new" + ClassUtils.getClassName(newObject), newObject.toJsonObject(urlApi));
        data = JsonObject.toJsonString(paramsData);
        actionMessage = message;
    }

    /**
     * Get domain name
     *
     * @return domaine name
     */
    public String domainName() {
        String domain = serviceName.replace("Service", "");
        return domain.substring(0, 1).toUpperCase() + domain.substring(1);
    }

    /**
     * Process an Add operation for this command
     *
     * @return Message
     */
    public CommandResponse execute(ModelService service, UrlApi urlApi) {
        //Retrieve domain to update it
        CytomineDomain updatedDomain = this.domain;
        String oldDomain = updatedDomain.toJSON(urlApi);
        updatedDomain.buildDomainFromJson(json, service.getEntityManager());
        //Init command info TODO
        CytomineDomain container = updatedDomain.container();
        if (container != null && container instanceof Project) {
            super.setProject((Project) container);
        }
        CommandResponse response = service.edit(updatedDomain, printMessage);
        fillCommandInfo(updatedDomain, oldDomain, urlApi, (String) response.getData().get("message"));
        return response;
    }
}
