package be.cytomine.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.utils.CommandResponse;
import be.cytomine.utils.JsonObject;

import static be.cytomine.utils.ClassUtils.getClassName;

@RequiredArgsConstructor
@Service
@Transactional()
public class ResponseService {

    private final MessageSource messageSource;

    /**
     * Create response message structure for a target result E.g. if we try to add a new annotation "object"
     *
     * @param object                   Object updated (add/edit/delete) by target  (e.g. annotation x)
     * @param messageParams            Params for i18n message (e.g. annotation id, annotation image id)
     * @param printMessage             Flag for client, indicate if client must print or not a confirmation message
     * @param commandType              Command type: add, edit or delete
     * @param additionalCallbackParams (optional): call back to append in response for client (e.g. image id as
     *                                 callback, to refresh image view in web UI client)
     *
     * @return Response stucture
     */
    public CommandResponse createResponseMessage(
        CytomineDomain object,
        List<Object> messageParams,
        boolean printMessage,
        String commandType,
        Map<String, Object> additionalCallbackParams
    ) {
        String objectName = getClassName(object);
        String command = "be.cytomine." + commandType + objectName + "Command";
        String idName = objectName.toLowerCase() + "ID"; //termID, annotationID,...
        String id = String.valueOf(object.getId());
        HashMap<String, Object> paramsCallback = new HashMap<>();
        paramsCallback.put("method", command);
        paramsCallback.put(idName, id);
        if (additionalCallbackParams != null) {
            paramsCallback.putAll(additionalCallbackParams);
        }

        //load message from i18n filel
        String message = messageSource.getMessage(command, messageParams.toArray(), Locale.ENGLISH);

        HashMap<String, Object> params = new HashMap<>();
        params.put("message", message);
        params.put("callback", paramsCallback);
        params.put("printMessage", printMessage);
        params.put(objectName.toLowerCase(), JsonObject.toMap(object.toJSON()));     //Project.getDataFromDomain(object)

        CommandResponse commandResponse = new CommandResponse();
        commandResponse.setData(params);
        commandResponse.setStatus(200);
        commandResponse.setObject(object);
        return commandResponse;
    }

    public Map<String, Object> createResponseData(
        Boolean success,
        String messageKey,
        HashMap<String, Object> callback,
        boolean printMessage
    ) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("success", success);
        params.put("message", messageSource.getMessage(messageKey, new Object[]{}, Locale.ENGLISH));
        params.put("callback", callback);
        params.put("printMessage", printMessage);
        return params;
    }

}
