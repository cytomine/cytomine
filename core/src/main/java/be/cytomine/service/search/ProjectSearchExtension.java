package be.cytomine.service.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectSearchExtension {
    private boolean withDescription;
    private boolean withMembersCount;
    private boolean withLastActivity;
    private boolean withCurrentUserRoles;
}
