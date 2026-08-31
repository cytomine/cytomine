package be.cytomine.domain.acl;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity(name = "AclEntry")
@Table(name = "acl_entry")
@Immutable
public class AclEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "ace_order", nullable = false, unique = false)
    private Integer aceOrder;

    @Column(name = "mask", nullable = false, unique = false)
    private Integer mask;

    @Column(name = "granting", nullable = false, unique = false)
    private Boolean granting;

    @Column(name = "audit_success", nullable = false, unique = false)
    private Boolean auditSuccess;

    @Column(name = "audit_failure", nullable = false, unique = false)
    private Boolean auditFailure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acl_object_identity", referencedColumnName = "id", nullable = false)
    private AclObjectIdentity aclObjectIdentity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sid", referencedColumnName = "id", nullable = false)
    private AclSid sid;

    public AclEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    public AclSid getSid() {
        return sid;
    }

    public void setSid(AclSid sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "AclEntry [id=" + id + ", aceOrder=" + aceOrder + ", mask="
            + mask + ", granting=" + granting + ", auditSuccess="
            + auditSuccess + ", auditFailure=" + auditFailure
            + ", aclObjectIdentity=" + aclObjectIdentity + ", sid=" + sid
            + "]";
    }

}
