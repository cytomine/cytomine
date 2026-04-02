package be.cytomine.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

public class CustomIdentifierGenerator extends SequenceStyleGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object)
        throws HibernateException {
        // In Hibernate 6, use getIdentifier() directly on EntityPersister (getClassMetadata() was removed)
        Object id = session.getEntityPersister(null, object).getIdentifier(object, session);
        return id != null ? id : super.generate(session, object);
    }

    @Override
    public boolean allowAssignedIdentifiers() {
        // Allow entities to have pre-assigned IDs
        return true;
    }
}
