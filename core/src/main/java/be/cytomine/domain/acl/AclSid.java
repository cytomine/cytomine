package be.cytomine.domain.acl;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity(name = "AclSid")
@Table(name = "acl_sid")
@Immutable
public class AclSid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "principal", nullable = false, unique = false)
    private Boolean principal;

    @Column(name = "sid", length = 100, nullable = false, unique = false)
    private String sid;

    public AclSid() {
    }

    public AclSid(Long id, Boolean principal, String sid) {
        setId(id);
        setPrincipal(principal);
        setSid(sid);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "AclSid [id=" + id + ", principal=" + principal + ", sid=" + sid
            + "]";
    }

}
