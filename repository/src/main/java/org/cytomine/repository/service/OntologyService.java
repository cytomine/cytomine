package org.cytomine.repository.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import be.cytomine.common.repository.model.command.payload.request.OntologyCommandPayload;
import be.cytomine.common.repository.model.command.payload.response.HttpCommandResponse;
import be.cytomine.common.repository.model.ontology.payload.CreateOntology;
import be.cytomine.common.repository.model.ontology.payload.UpdateOntology;

public class OntologyService implements CRUDCommandService<CreateOntology, UpdateOntology, OntologyCommandPayload> {

}
