package be.cytomine.config;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

public class CustomIdentifierGenerator extends SequenceStyleGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        Serializable id = (Serializable) session.getEntityPersister(null, object)
                .getIdentifier(object, session);
        return id != null ? id : (Serializable) super.generate(session, object);
    }
}
