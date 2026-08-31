package be.cytomine.domain.acl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Immutable;


@Entity(name = "AclObjectIdentity")
@Table(
    name = "acl_object_identity",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_acl_object_identity",
        columnNames = {
            "object_id_class",
            "object_id_identity"
        }
    )
)
@Immutable
public class AclObjectIdentity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "object_id_identity", nullable = false)
    private Long objectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "object_id_class", referencedColumnName = "id", nullable = false)
    private AclClass objectIdClass;

    @Column(name = "entries_inheriting", nullable = false)
    private Boolean entriesInheriting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_object", referencedColumnName = "id")
    private AclObjectIdentity parentObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_sid", referencedColumnName = "id")
    private AclSid ownerSid;

    @OneToMany(
        targetEntity = AclEntry.class,
        fetch = FetchType.LAZY,
        mappedBy = "aclObjectIdentity",
        cascade = CascadeType.REMOVE
    )
    private Set<AclEntry> aclEntries = new HashSet<>();

    public AclObjectIdentity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
