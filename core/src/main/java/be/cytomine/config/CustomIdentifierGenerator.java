package be.cytomine.config;

import java.lang.reflect.Member;
import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.persister.entity.EntityPersister;

public class CustomIdentifierGenerator implements BeforeExecutionGenerator {

    private static final String SEQUENCE_NAME = "hibernate_sequence";

    public CustomIdentifierGenerator(CustomId config, Member annotatedMember, GeneratorCreationContext creationContext) {
        // No initialization needed - we use native query to get sequence values
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        // Check if entity already has an ID assigned
        EntityPersister persister = session.getEntityPersister(null, owner);
        Object existingId = persister.getIdentifier(owner, session);
        if (existingId != null) {
            return existingId;
        }
        // Generate new ID from sequence
        return session.createNativeQuery("SELECT nextval('" + SEQUENCE_NAME + "')", Long.class)
                .getSingleResult();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public boolean generatedOnExecution() {
        return false;
    }

    @Override
    public boolean allowAssignedIdentifiers() {
        return true;
    }
}
