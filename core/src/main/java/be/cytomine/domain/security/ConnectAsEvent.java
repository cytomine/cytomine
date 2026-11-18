package be.cytomine.domain.security;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import be.cytomine.domain.CytomineDomain;
import be.cytomine.service.UrlApi;
import be.cytomine.utils.JsonObject;

@Getter
@Setter
@Entity
@Table(name = "connect_as_event")
public class ConnectAsEvent extends CytomineDomain {

    @Column(name = "acting_admin_id", nullable = false)
    private Long actingAdminId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "target_username", nullable = false)
    private String targetUsername;

    @Column(name = "started_at", nullable = false)
    private Date startedAt;

    @Column(name = "ended_at")
    private Date endedAt;

    @Override
    public JsonObject toJsonObject(UrlApi urlApi) {
        return getDataFromDomain(this);
    }
}
