package be.cytomine;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.security.acls.model.Permission;
import org.springframework.stereotype.Component;

import be.cytomine.common.repository.model.command.payload.response.UserResponse;
import be.cytomine.domain.CytomineDomain;
import be.cytomine.domain.annotation.Annotation;
import be.cytomine.domain.annotation.AnnotationLayer;
import be.cytomine.domain.appengine.TaskRun;
import be.cytomine.domain.appengine.TaskRunLayer;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.CompanionFile;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.NestedImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.image.UploadedFile;
import be.cytomine.domain.image.group.ImageGroup;
import be.cytomine.domain.image.group.ImageGroupImageInstance;
import be.cytomine.domain.image.server.Storage;
import be.cytomine.domain.meta.AttachedFile;
import be.cytomine.domain.meta.Configuration;
import be.cytomine.domain.meta.ConfigurationReadingRole;
import be.cytomine.domain.meta.Description;
import be.cytomine.domain.meta.Property;
import be.cytomine.domain.meta.Tag;
import be.cytomine.domain.meta.TagDomainAssociation;
import be.cytomine.domain.ontology.AnnotationDomain;
import be.cytomine.domain.ontology.AnnotationGroup;
import be.cytomine.domain.ontology.AnnotationIndex;
import be.cytomine.domain.ontology.AnnotationLink;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.AnnotationTrack;
import be.cytomine.domain.ontology.Ontology;
import be.cytomine.domain.ontology.Relation;
import be.cytomine.domain.ontology.RelationTerm;
import be.cytomine.domain.ontology.ReviewedAnnotation;
import be.cytomine.domain.ontology.SharedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.ontology.Track;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.processing.ImageFilter;
import be.cytomine.domain.processing.ImageFilterProject;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.project.ProjectDefaultLayer;
import be.cytomine.domain.project.ProjectRepresentativeUser;
import be.cytomine.domain.security.SecRole;
import be.cytomine.domain.security.SecUserSecRole;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.ObjectNotFoundException;
import be.cytomine.mapper.UserMapper;
import be.cytomine.repository.security.SecRoleRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentUserService;
import be.cytomine.service.PermissionService;

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION;

@Component
@Transactional
public class BasicInstanceBuilder {

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ROLE_GUEST = "ROLE_GUEST";

    public static final String ADMIN = "admin";

    public static final String ANOTHER_USER = "anotheruser";

    public static final String SUPER_ADMIN = "superadmin";

    public static final String SUPER_ADMIN_ACL = "SUPER_ADMIN_ACL";

    public static final String ADMIN_ACL = "ADMIN_ACL";

    public static final String ACL_USER_NO_ACL = "ACL_USER_NO_ACL";

    public static final String USER_ACL_READ = "USER_ACL_READ";

    public static final String USER_ACL_WRITE = "USER_ACL_WRITE";

    public static final String USER_ACL_CREATE = "USER_ACL_CREATE";

    public static final String USER_ACL_DELETE = "USER_ACL_DELETE";

    public static final String USER_ACL_ADMIN = "USER_ACL_ADMIN";

    public static final String CREATOR = "CREATOR";

    public static final String GUEST_ACL = "GUEST_ACL";

    EntityManager em;
    PermissionService permissionService;
    SecRoleRepository secRoleRepository;
    UserRepository userRepository;
    UserMapper userMapper;

    public BasicInstanceBuilder(EntityManager em, UserRepository userRepository, PermissionService permissionService,
        SecRoleRepository secRoleRepository, UserMapper userMapper) {
        this.em = em;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.secRoleRepository = secRoleRepository;
        this.userMapper = userMapper;
    }

    public UserResponse givenAdmin() {
        return getUser(ADMIN);
    }

    public UserResponse givenAnotherUser() {
        return getUser(ANOTHER_USER);
    }

    public UserResponse givenSuperAdminAcl() {
        return getUser(SUPER_ADMIN_ACL);
    }

    public UserResponse givenAdminAcl() {
        return getUser(ADMIN_ACL);
    }

    public UserResponse givenAclUserNoAcl() {
        return getUser(ACL_USER_NO_ACL);
    }

    public UserResponse givenUserAclRead() {
        return getUser(USER_ACL_READ);
    }

    public UserResponse givenUserAclWrite() {
        return getUser(USER_ACL_WRITE);
    }

    public UserResponse givenUserAclCreate() {
        return getUser(USER_ACL_CREATE);
    }

    public UserResponse givenUserAclDelete() {
        return getUser(USER_ACL_DELETE);
    }

    public UserResponse givenUserAclAdmin() {
        return getUser(USER_ACL_ADMIN);
    }

    public UserResponse givenCreator() {
        return getUser(CREATOR);
    }

    public UserResponse givenGuestAcl() {
        return getUser(GUEST_ACL);
    }

    public RelationTerm givenANotPersistedRelationTerm(Relation relation, Term term1, Term term2) {
        RelationTerm relationTerm = new RelationTerm();
        relationTerm.setRelation(relation);
        relationTerm.setTerm1(term1);
        relationTerm.setTerm2(term2);

        return relationTerm;
    }

    public UserResponse givenSuperAdmin() {
        return getUser(SUPER_ADMIN);
    }

    public UserResponse getUser(String username) {
        return userMapper.map(getUserEntity(username));
    }

    public User getUserEntity(String username) {
        return userRepository.findByUsernameLikeIgnoreCase(username)
            .orElseThrow(() -> new ObjectNotFoundException(username + " not in db"));
    }

    public User getUserEntity(UserResponse user) {
        return getUserEntity(user.username());
    }

    public UserResponse givenCurrentUser() {
        return CurrentUserService.getSecurityCurrentUser().map(currentUser -> currentUser.getUser().username())
            .flatMap(userRepository::findByUsernameLikeIgnoreCase).map(user -> userMapper.map(user))
            .orElseGet(this::givenSuperAdmin);
    }

    public User givenANotPersistedUser() {
        User user = new User();
        user.setName("firstname lastname");
        user.setReference(UUID.randomUUID().toString());
        user.setUsername(randomString());
        user.generateKeys();
        return user;
    }

    public ImageFilterProject givenANotPersistedImageFilterProject(ImageFilter imageFilter, Project project) {
        ImageFilterProject imageFilterProject = new ImageFilterProject();
        imageFilterProject.setImageFilter(imageFilter);
        imageFilterProject.setProject(project);
        return imageFilterProject;
    }

    public ImageFilterProject givenAnImageFilterProject() {
        return persistAndReturn(givenANotPersistedImageFilterProject(givenAnImageFilter(), givenAProject()));
    }

    public ImageFilterProject givenAnImageFilterProject(ImageFilter imageFilter, Project project) {
        return persistAndReturn(givenANotPersistedImageFilterProject(imageFilter, project));
    }

    public ImageFilter givenANotPersistedImageFilter() {
        ImageFilter imageFilter = new ImageFilter();
        imageFilter.setName(randomString());
        imageFilter.setMethod(randomString());
        return imageFilter;
    }

    public ImageFilter givenAnImageFilter() {
        return persistAndReturn(givenANotPersistedImageFilter());
    }

    public Term givenATerm() {
        return persistAndReturn(givenANotPersistedTerm(givenAnOntology()));
    }

    public Term givenATerm(Ontology ontology) {
        return persistAndReturn(givenANotPersistedTerm(ontology));
    }

    public Term givenANotPersistedTerm(Ontology ontology) {
        Term term = new Term();
        term.setName(randomString());
        term.setOntology(ontology);
        term.setColor(String.format("#%06X", new Random().nextInt(0x1000000)));
        return term;
    }

    public RelationTerm givenARelationTerm() {
        Ontology ontology = givenAnOntology();
        return givenARelationTerm(givenATerm(ontology), givenATerm(ontology));
    }

    public RelationTerm givenARelationTerm(Term term1, Term term2) {
        return givenARelationTerm(givenARelation(), term1, term2);
    }

    public RelationTerm givenARelationTerm(Relation relation, Term term1, Term term2) {
        return persistAndReturn(givenANotPersistedRelationTerm(relation, term1, term2));
    }

    public Relation givenARelation() {
        return (Relation) em.createQuery("SELECT relation FROM Relation relation WHERE relation.name LIKE 'parent'")
            .getResultList().get(0);
    }

    public Ontology givenAnOntology() {
        return persistAndReturn(givenANotPersistedOntology());
    }

    public Ontology givenANotPersistedOntology() {
        Ontology ontology = new Ontology();
        ontology.setName(randomString());
        ontology.setUserId(givenAclUserNoAcl().id());
        return ontology;
    }

    public Project givenAProjectWithUser(String username) {
        Project project = givenAProject();
        addUserToProject(project, username, ADMINISTRATION);
        return project;
    }

    public Project givenAProject() {
        return persistAndReturn(givenAProjectWithOntology(givenAnOntology()));
    }

    public Project givenAProjectWithOntology(Ontology ontology) {
        Project project = givenANotPersistedProject();
        project.setOntology(ontology);
        return persistAndReturn(project);
    }

    public Project givenANotPersistedProject() {
        Project project = new Project();
        project.setName(randomString());
        project.setOntology(null);
        project.setCountAnnotations(0);
        return project;
    }

    public void addUserToProject(Project project, String username, Permission permission) {
        permissionService.addPermission(project, username, permission, this.givenSuperAdmin().username());
    }

    public void addUserToProject(Project project, String username) {
        permissionService.addPermission(project, username, ADMINISTRATION, this.givenSuperAdmin().username());
    }

    public <T> T persistAndReturn(T instance) {
        em.persist(instance);
        em.flush();
        return instance;
    }

    public UploadedFile givenAUploadedFile() {
        UploadedFile uploadedFile = givenANotPersistedUploadedFile();
        return persistAndReturn(uploadedFile);
    }

    public UploadedFile givenANotPersistedUploadedFile(String contentType) {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setStorage(givenAStorage());
        uploadedFile.setUser(getUserEntity(SUPER_ADMIN));
        uploadedFile.setFilename(randomString());
        uploadedFile.setOriginalFilename(randomString());
        uploadedFile.setExt("tif");
        uploadedFile.setContentType(contentType);
        uploadedFile.setSize(100L);
        uploadedFile.setParent(null);
        return uploadedFile;
    }

    public UploadedFile givenANotPersistedUploadedFile() {
        return givenANotPersistedUploadedFile("PYRTIFF");
    }

    public Storage givenAStorage() {
        return givenAStorage(givenSuperAdmin());
    }

    public Storage givenAStorage(UserResponse user) {
        Storage storage = givenANotPersistedStorage(user);
        storage = persistAndReturn(storage);
        permissionService.addPermission(storage, storage.getUser().getUsername(), ADMINISTRATION,
            storage.getUser().getUsername());
        return storage;
    }

    public Storage givenANotPersistedStorage(UserResponse user) {
        Storage storage = new Storage();
        storage.setName(randomString());
        storage.setUser(getUserEntity(user));
        return storage;
    }

    public Storage givenANotPersistedStorage() {
        return givenANotPersistedStorage(givenSuperAdmin());
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    public AbstractImage givenAnAbstractImage() {
        return persistAndReturn(givenANotPersistedAbstractImage());
    }

    public AbstractImage givenANotPersistedAbstractImage() {
        AbstractImage image = new AbstractImage();
        image.setUploadedFile(givenAUploadedFile());
        image.setOriginalFilename(randomString());
        image.setWidth(16000);
        image.setHeight(16000);
        return image;
    }

    public ImageInstance givenANotPersistedImageInstance(Project project) {
        return givenANotPersistedImageInstance(givenAnAbstractImage(), project);
    }

    public ImageInstance givenANotPersistedImageInstance() {
        return givenANotPersistedImageInstance(givenAnAbstractImage(), givenAProject());
    }

    public ImageInstance givenANotPersistedImageInstance(AbstractImage abstractImage, Project project) {
        ImageInstance image = new ImageInstance();
        image.setBaseImage(abstractImage);
        image.setProject(project);
        image.setUser(getUserEntity(SUPER_ADMIN));
        return image;
    }

    public ImageInstance givenAnImageInstance(AbstractImage abstractImage, Project project) {
        ImageInstance imageInstance = givenANotPersistedImageInstance(abstractImage, project);
        return persistAndReturn(imageInstance);
    }

    public ImageInstance givenAnImageInstance(Project project) {
        ImageInstance imageInstance = givenAnImageInstance(givenAnAbstractImage(), project);
        return persistAndReturn(imageInstance);
    }

    public ImageInstance givenAnImageInstance() {
        ImageInstance imageInstance = givenAnImageInstance(givenAnAbstractImage(), givenAProject());
        return persistAndReturn(imageInstance);
    }

    public AbstractSlice givenAnAbstractSlice() {
        AbstractImage abstractImage = givenAnAbstractImage();
        UploadedFile uploadedFile = givenAUploadedFile();
        uploadedFile.setStorage(abstractImage.getUploadedFile().getStorage());
        persistAndReturn(uploadedFile);
        return givenAnAbstractSlice(abstractImage, uploadedFile);
    }

    public AbstractSlice givenAnAbstractSlice(AbstractImage abstractImage, int c, int z, int t) {
        AbstractSlice slice = givenANotPersistedAbstractSlice(abstractImage, abstractImage.getUploadedFile());
        slice.setChannel(c);
        slice.setZStack(z);
        slice.setTime(t);
        return persistAndReturn(slice);
    }

    public AbstractSlice givenAnAbstractSlice(AbstractImage abstractImage, UploadedFile uploadedFile) {
        return persistAndReturn(givenANotPersistedAbstractSlice(abstractImage, uploadedFile));
    }

    public AbstractSlice givenANotPersistedAbstractSlice() {
        return givenANotPersistedAbstractSlice(givenAnAbstractImage(), givenAUploadedFile());
    }

    public AbstractSlice givenANotPersistedAbstractSlice(AbstractImage abstractImage, UploadedFile uploadedFile) {
        AbstractSlice slice = new AbstractSlice();
        slice.setImage(abstractImage);
        slice.setUploadedFile(uploadedFile);
        slice.setChannel(0);
        slice.setZStack(0);
        slice.setTime(0);
        return slice;
    }

    public SliceInstance givenASliceInstance() {
        return givenASliceInstance(givenAnImageInstance(), 0, 0, 0);
    }

    public SliceInstance givenASliceInstance(ImageInstance imageInstance, int c, int z, int t) {
        AbstractSlice abstractSlice = givenAnAbstractSlice(imageInstance.getBaseImage(), c, z, t);
        return persistAndReturn(givenANotPersistedSliceInstance(imageInstance, abstractSlice));
    }

    public SliceInstance givenASliceInstance(ImageInstance imageInstance, AbstractSlice abstractSlice) {
        return persistAndReturn(givenANotPersistedSliceInstance(imageInstance, abstractSlice));
    }

    public SliceInstance givenASliceInstance(Project project) {
        AbstractImage image = givenAnAbstractImage();
        SliceInstance sliceInstance =
            givenASliceInstance(givenAnImageInstance(image, project), givenAnAbstractSlice(image, 0, 0, 0));
        return persistAndReturn(sliceInstance);
    }

    public SliceInstance givenANotPersistedSliceInstance(ImageInstance imageInstance, AbstractSlice abstractSlice) {
        SliceInstance slice = new SliceInstance();
        slice.setImage(imageInstance);
        slice.setProject(imageInstance.getProject());
        slice.setBaseSlice(abstractSlice);
        return slice;
    }

    public SliceInstance givenANotPersistedSliceInstance() {
        return givenANotPersistedSliceInstance(givenAnImageInstance(), givenAnAbstractSlice());
    }

    public NestedImageInstance givenANestedImageInstance() {
        return persistAndReturn(givenANotPersistedNestedImageInstance());
    }

    public NestedImageInstance givenANotPersistedNestedImageInstance() {
        ImageInstance parent = givenAnImageInstance();
        NestedImageInstance nestedImageInstance = new NestedImageInstance();
        nestedImageInstance.setBaseImage(givenAnAbstractImage());
        nestedImageInstance.setProject(parent.getProject());
        nestedImageInstance.setUser(getUserEntity(SUPER_ADMIN));
        nestedImageInstance.setParent(parent);
        nestedImageInstance.setX(1);
        nestedImageInstance.setY(2);
        return nestedImageInstance;
    }

    public Property givenAProperty(CytomineDomain cytomineDomain) {
        return persistAndReturn(givenANotPersistedProperty(cytomineDomain, "key", "value"));
    }

    public Property givenAProperty(CytomineDomain cytomineDomain, String key, String value) {
        return persistAndReturn(givenANotPersistedProperty(cytomineDomain, key, value));
    }

    public Property givenANotPersistedProperty(CytomineDomain cytomineDomain, String key, String value) {
        Property property = new Property();
        property.setDomain(cytomineDomain);
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    public CompanionFile givenACompanionFile(AbstractImage abstractImage) {
        return persistAndReturn(givenANotPersistedCompanionFile(abstractImage));
    }

    public CompanionFile givenANotPersistedCompanionFile(AbstractImage abstractImage) {
        CompanionFile companionFile = new CompanionFile();
        companionFile.setImage(abstractImage);
        companionFile.setUploadedFile(abstractImage.getUploadedFile());
        companionFile.setFilename(randomString());
        companionFile.setOriginalFilename(randomString());
        companionFile.setType(companionFile.getImage().getContentType());
        companionFile.setProgress(50);
        return companionFile;
    }

    public UserAnnotation givenANotPersistedGuestAnnotation() {
        UserAnnotation annotation = new UserAnnotation();
        annotation.setUserId(givenGuestAcl().id());
        try {
            annotation.setLocation(new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"));
        } catch (ParseException ignored) {
            // TODO
        }
        annotation.setSlice(givenASliceInstance());
        annotation.setImage(annotation.getSlice().getImage());
        annotation.setProject(annotation.getImage().getProject());
        return annotation;
    }

    public UserAnnotation givenANotPersistedUserAnnotation() {
        UserAnnotation annotation = new UserAnnotation();
        annotation.setUserId(givenSuperAdmin().id());
        try {
            annotation.setLocation(new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"));
        } catch (ParseException ignored) {
            // TODO
        }
        annotation.setSlice(givenASliceInstance());
        annotation.setImage(annotation.getSlice().getImage());
        annotation.setProject(annotation.getImage().getProject());
        return annotation;
    }

    public UserAnnotation givenANotPersistedUserAnnotation(Project project) {
        UserAnnotation annotation = givenANotPersistedUserAnnotation();
        annotation.getSlice().setProject(project);
        annotation.getImage().setProject(project);
        annotation.setProject(project);
        return annotation;
    }

    public UserAnnotation givenANotPersistedUserAnnotation(SliceInstance sliceInstance) {
        UserAnnotation annotation = new UserAnnotation();
        annotation.setUserId(givenSuperAdmin().id());
        try {
            annotation.setLocation(new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"));
        } catch (ParseException ignored) {
            // Ignored
        }
        annotation.setSlice(sliceInstance);
        annotation.setImage(annotation.getSlice().getImage());
        annotation.setProject(annotation.getImage().getProject());
        return annotation;
    }

    public UserAnnotation givenAUserAnnotation(Project project) {
        UserAnnotation annotation = givenANotPersistedUserAnnotation();
        annotation.getSlice().setProject(project);
        annotation.getImage().setProject(project);
        annotation.setProject(project);
        return persistAndReturn(annotation);
    }

    public UserAnnotation givenAUserAnnotation(SliceInstance sliceInstance) {
        return persistAndReturn(givenAUserAnnotation(sliceInstance, givenSuperAdmin()));
    }

    public UserAnnotation givenAUserAnnotation(SliceInstance sliceInstance, UserResponse user) {
        UserAnnotation annotation = givenANotPersistedUserAnnotation();
        annotation.setSlice(sliceInstance);
        annotation.setImage(sliceInstance.getImage());
        annotation.setProject(sliceInstance.getProject());
        annotation.setUserId(user.id());
        return persistAndReturn(annotation);
    }

    public UserAnnotation givenAUserAnnotation() {
        return persistAndReturn(givenANotPersistedUserAnnotation());
    }

    public UserAnnotation givenAUserAnnotation(SliceInstance sliceInstance, String location, UserResponse user,
        Term term) throws ParseException {
        UserAnnotation annotation = givenANotPersistedUserAnnotation(sliceInstance);
        annotation.setLocation(new WKTReader().read(location));
        annotation.setUserId(user.id());
        persistAndReturn(annotation);

        if (term != null) {
            AnnotationTerm annotationTerm = new AnnotationTerm();
            annotationTerm.setUserAnnotation(annotation);
            annotationTerm.setUser(getUserEntity(user));
            annotationTerm.setTerm(term);
            persistAndReturn(annotationTerm);
            em.refresh(annotation);
        }
        return annotation;
    }

    public AnnotationTerm givenANotPersistedAnnotationTerm(UserAnnotation annotation) {
        return givenANotPersistedAnnotationTerm(annotation, this.givenATerm(annotation.getProject().getOntology()));
    }

    public AnnotationTerm givenANotPersistedAnnotationTerm(UserAnnotation annotation, Term term) {
        AnnotationTerm annotationTerm = new AnnotationTerm();
        annotationTerm.setTerm(term);
        annotationTerm.setUser(getUserEntity(this.givenCurrentUser()));
        annotationTerm.setUserAnnotation(annotation);
        return annotationTerm;
    }

    public AnnotationTerm givenAnAnnotationTerm(UserAnnotation annotation, Term term) {
        return persistAndReturn(givenANotPersistedAnnotationTerm(annotation, term));
    }

    public AnnotationTerm givenAnAnnotationTerm(UserAnnotation annotation) {
        return persistAndReturn(
            givenANotPersistedAnnotationTerm(annotation, this.givenATerm(annotation.getProject().getOntology())));
    }

    public AnnotationTerm givenAnAnnotationTerm() {
        UserAnnotation annotation = givenAUserAnnotation();
        return persistAndReturn(
            givenANotPersistedAnnotationTerm(annotation, this.givenATerm(annotation.getProject().getOntology())));
    }

    public ReviewedAnnotation givenANotPersistedReviewedAnnotation() {
        ReviewedAnnotation annotation = new ReviewedAnnotation();
        annotation.setUserId(givenSuperAdmin().id());
        try {
            annotation.setLocation(new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))"));
        } catch (ParseException ignored) {
            // Ignored
        }
        UserAnnotation userAnnotation = givenAUserAnnotation();
        annotation.setSlice(userAnnotation.getSlice());
        annotation.setImage(userAnnotation.getImage());
        annotation.setProject(userAnnotation.getImage().getProject());
        annotation.putParentAnnotation(userAnnotation);
        annotation.setReviewUser(getUserEntity(SUPER_ADMIN));
        annotation.setStatus(0);
        return annotation;
    }

    public ReviewedAnnotation givenANotPersistedReviewedAnnotation(Project project) {
        ReviewedAnnotation annotation = givenANotPersistedReviewedAnnotation();
        UserAnnotation userAnnotation = givenANotPersistedUserAnnotation(project);
        userAnnotation = persistAndReturn(userAnnotation);
        annotation.putParentAnnotation(userAnnotation);
        annotation.getSlice().setProject(project);
        annotation.getImage().setProject(project);
        annotation.setProject(project);
        annotation.setReviewUser(getUserEntity(SUPER_ADMIN));
        annotation.setStatus(0);
        return annotation;
    }

    public ReviewedAnnotation givenAReviewedAnnotation() {
        return persistAndReturn(givenANotPersistedReviewedAnnotation());
    }

    public ReviewedAnnotation givenAReviewedAnnotation(Project project) throws ParseException {
        SliceInstance sliceInstance = givenASliceInstance(givenAnImageInstance(project), givenAnAbstractSlice());
        return givenAReviewedAnnotation(sliceInstance, "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168))",
            givenSuperAdmin(), givenATerm(project.getOntology()));
    }

    public ReviewedAnnotation givenAReviewedAnnotation(SliceInstance sliceInstance, String location, UserResponse user,
        Term term) throws ParseException {
        UserAnnotation userAnnotation = givenAUserAnnotation(sliceInstance, location, user, term);

        ReviewedAnnotation annotation = new ReviewedAnnotation();
        annotation.setImage(sliceInstance.getImage());
        annotation.setSlice(sliceInstance);
        annotation.setLocation(new WKTReader().read(location));
        annotation.setUserId(user.id());
        annotation.setProject(sliceInstance.getProject());
        annotation.putParentAnnotation(userAnnotation);
        annotation.setReviewUser(getUserEntity(SUPER_ADMIN));
        annotation.setStatus(0);
        persistAndReturn(annotation);

        if (term != null) {
            AnnotationTerm annotationTerm = new AnnotationTerm();
            annotationTerm.setUserAnnotation(userAnnotation);
            annotationTerm.setUser(getUserEntity(user));
            annotationTerm.setTerm(term);
            persistAndReturn(annotationTerm);
            annotation.getTerms().add(term);
            persistAndReturn(annotation);
        }
        persistAndReturn(annotation);
        em.refresh(userAnnotation);
        em.refresh(annotation);
        return annotation;
    }

    public SharedAnnotation givenANotPersistedSharedAnnotation() {
        SharedAnnotation sharedAnnotation = new SharedAnnotation();
        sharedAnnotation.setAnnotation(givenAUserAnnotation());
        sharedAnnotation.setComment("Rech. proj. pr proj. priv. Self Dem. Brt. Poss. S'adr. à l'hô. Mart");
        sharedAnnotation.setSender(getUserEntity(SUPER_ADMIN));
        sharedAnnotation.setReceivers(List.of(getUserEntity(SUPER_ADMIN)));
        return sharedAnnotation;
    }

    public SharedAnnotation givenASharedAnnotation() {
        return persistAndReturn(givenANotPersistedSharedAnnotation());
    }

    public SharedAnnotation givenASharedAnnotation(AnnotationDomain annotationDomain) {
        SharedAnnotation sharedAnnotation = givenANotPersistedSharedAnnotation();
        sharedAnnotation.setAnnotation(annotationDomain);
        return persistAndReturn(sharedAnnotation);
    }

    public Track givenATrack() {
        return persistAndReturn(givenANotPersistedTrack());
    }

    public Track givenANotPersistedTrack() {
        Track track = new Track();
        track.setName(randomString());
        track.setColor(randomString());
        track.setImage(givenAnImageInstance());
        track.setProject(track.getImage().getProject());
        return track;
    }

    public AnnotationTrack givenAnAnnotationTrack() {
        return persistAndReturn(givenANotPersistedAnnotationTrack());
    }

    public AnnotationTrack givenANotPersistedAnnotationTrack() {
        AnnotationTrack annotationTrack = new AnnotationTrack();
        UserAnnotation annotation = givenAUserAnnotation();
        annotationTrack.setAnnotation(annotation);
        annotationTrack.setSlice(annotation.getSlice());
        annotationTrack.setTrack(givenATrack());
        return annotationTrack;
    }

    public AttachedFile givenAnAttachedFile(CytomineDomain domain) {
        return persistAndReturn(givenANotPersistedAttachedFile(domain));
    }

    public AttachedFile givenANotPersistedAttachedFile(CytomineDomain domain) {
        AttachedFile attachedFile = new AttachedFile();
        attachedFile.setData(UUID.randomUUID().toString().getBytes());
        attachedFile.setFilename("test.txt");
        attachedFile.setDomain(domain);
        return attachedFile;
    }

    public AnnotationIndex givenANotPersistedAnnotationIndex() {
        AnnotationIndex annotationIndex = new AnnotationIndex();
        annotationIndex.setSlice(givenASliceInstance());
        annotationIndex.setCountAnnotation(1L);
        annotationIndex.setCountReviewedAnnotation(1L);
        annotationIndex.setUser(getUserEntity(SUPER_ADMIN));
        return annotationIndex;
    }

    public AnnotationIndex givenAnAnnotationIndex() {
        return persistAndReturn(givenANotPersistedAnnotationIndex());
    }

    public Configuration givenAConfiguration(String key) {
        return persistAndReturn(givenANotPersistedConfiguration(key));
    }

    public Configuration givenANotPersistedConfiguration(String key) {
        Configuration configuration = new Configuration();
        configuration.setKey(key);
        configuration.setValue("value");
        configuration.setReadingRole(ConfigurationReadingRole.ALL);
        return configuration;
    }

    public Description givenADescription(CytomineDomain domain) {
        return persistAndReturn(givenANotPersistedDescription(domain));
    }

    public Description givenANotPersistedDescription(CytomineDomain domain) {
        Description description = new Description();
        description.setDomain(domain);
        description.setData("hello!");
        return description;
    }

    public Tag givenATag(String name) {
        return persistAndReturn(givenANotPersistedTag(name));
    }

    public Tag givenATag() {
        return persistAndReturn(givenANotPersistedTag(randomString()));
    }

    public Tag givenANotPersistedTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUser(getUserEntity(SUPER_ADMIN));
        return tag;
    }

    public TagDomainAssociation givenATagAssociation(Tag tag, CytomineDomain domain) {
        return persistAndReturn(givenANotPersistedTagAssociation(tag, domain));
    }

    public TagDomainAssociation givenANotPersistedTagAssociation(Tag tag, CytomineDomain domain) {
        TagDomainAssociation tagDomainAssociation = new TagDomainAssociation();
        tagDomainAssociation.setTag(tag);
        tagDomainAssociation.setDomain(domain);
        return tagDomainAssociation;
    }

    public ProjectRepresentativeUser givenAProjectRepresentativeUser() {
        return persistAndReturn(
            givenANotPersistedProjectRepresentativeUser(givenAProject(), givenSuperAdmin().username(),
                givenSuperAdmin().id()));
    }

    public ProjectRepresentativeUser givenAProjectRepresentativeUser(Project project, String username, long userId) {
        return persistAndReturn(givenANotPersistedProjectRepresentativeUser(project, username, userId));
    }

    public ProjectRepresentativeUser givenANotPersistedProjectRepresentativeUser() {
        return givenANotPersistedProjectRepresentativeUser(givenAProject(), givenSuperAdmin().username(),
            givenSuperAdmin().id());
    }

    public ProjectRepresentativeUser givenANotPersistedProjectRepresentativeUser(Project project, String username,
        long userId) {
        addUserToProject(project, username);
        ProjectRepresentativeUser projectRepresentativeUser = new ProjectRepresentativeUser();
        projectRepresentativeUser.setUserId(userId);
        projectRepresentativeUser.setProject(project);
        return projectRepresentativeUser;
    }

    public ProjectDefaultLayer givenAProjectDefaultLayer() {
        return persistAndReturn(givenANotPersistedProjectDefaultLayer(givenAProject(), givenSuperAdmin()));
    }

    public ProjectDefaultLayer givenAProjectDefaultLayer(Project project, UserResponse user) {
        return persistAndReturn(givenANotPersistedProjectDefaultLayer(project, user));
    }

    public ProjectDefaultLayer givenANotPersistedProjectDefaultLayer() {
        return givenANotPersistedProjectDefaultLayer(givenAProject(), givenSuperAdmin());
    }

    public ProjectDefaultLayer givenANotPersistedProjectDefaultLayer(Project project, UserResponse user) {
        addUserToProject(project, user.username());
        ProjectDefaultLayer projectDefaultLayer = new ProjectDefaultLayer();
        projectDefaultLayer.setUser(getUserEntity(user));
        projectDefaultLayer.setProject(project);
        projectDefaultLayer.setHideByDefault(false);
        return projectDefaultLayer;
    }

    public SecUserSecRole givenAUserRole() {
        return persistAndReturn(givenANotPersistedUserRole(getUserEntity(GUEST_ACL),
            secRoleRepository.findByAuthority(ROLE_USER).get()));
    }

    public SecUserSecRole givenAUserRole(User user) {
        return persistAndReturn(givenANotPersistedUserRole(user, secRoleRepository.findByAuthority(ROLE_USER).get()));
    }

    public SecUserSecRole givenAUserRole(User user, SecRole secRole) {
        return persistAndReturn(givenANotPersistedUserRole(user, secRole));
    }

    public SecUserSecRole givenANotPersistedUserRole(User user, String authority) {
        return givenANotPersistedUserRole(user, secRoleRepository.findByAuthority(authority).get());
    }

    public SecUserSecRole givenANotPersistedUserRole(User secUser, SecRole secRole) {
        SecUserSecRole secSecUserSecRole = new SecUserSecRole();
        secSecUserSecRole.setSecRole(secRole);
        secSecUserSecRole.setSecUser(secUser);
        return secSecUserSecRole;
    }

    public ImageGroup givenANotPersistedImagegroup() {
        return givenANotPersistedImagegroup(givenAProject());
    }

    public ImageGroup givenANotPersistedImagegroup(Project project) {
        ImageGroup imageGroup = new ImageGroup();
        imageGroup.setName(randomString());
        imageGroup.setProject(project);
        return imageGroup;
    }

    public ImageGroup givenAnImageGroup() {
        return persistAndReturn(givenANotPersistedImagegroup(givenAProject()));
    }

    public ImageGroup givenAnImageGroup(Project project) {
        return persistAndReturn(givenANotPersistedImagegroup(project));
    }

    public ImageGroupImageInstance givenANotPersistedImageGroupImageInstance() {
        Project project = givenAProject();
        ImageGroup group = givenAnImageGroup(project);
        ImageInstance image = givenAnImageInstance(project);

        return givenANotPersistedImageGroupImageInstance(group, image);
    }

    public ImageGroupImageInstance givenANotPersistedImageGroupImageInstance(ImageGroup group, ImageInstance image) {
        ImageGroupImageInstance igii = new ImageGroupImageInstance();
        igii.setGroup(group);
        igii.setImage(image);
        return igii;
    }

    public ImageGroupImageInstance givenAnImageGroupImageInstance() {
        ImageGroupImageInstance igii = givenANotPersistedImageGroupImageInstance();
        Project project = givenAProject();
        igii.setGroup(givenAnImageGroup(project));
        igii.setImage(givenAnImageInstance(project));
        return persistAndReturn(igii);
    }

    public ImageGroupImageInstance givenAnImageGroupImageInstance(ImageGroup group, ImageInstance image) {
        ImageGroupImageInstance igii = givenANotPersistedImageGroupImageInstance();
        igii.setGroup(group);
        igii.setImage(image);
        return persistAndReturn(igii);
    }

    public AnnotationGroup givenANotPersistedAnnotationGroup(Project project, ImageGroup imageGroup) {
        AnnotationGroup annotationGroup = new AnnotationGroup();
        annotationGroup.setProject(project);
        annotationGroup.setImageGroup(imageGroup);
        annotationGroup.setType("SAME_OBJECT");
        return annotationGroup;
    }

    public AnnotationGroup givenANotPersistedAnnotationGroup() {
        Project project = givenAProject();
        return givenANotPersistedAnnotationGroup(project, givenAnImageGroup(project));
    }

    public AnnotationGroup givenAnAnnotationGroup(Project project, ImageGroup imageGroup) {
        return persistAndReturn(givenANotPersistedAnnotationGroup(project, imageGroup));
    }

    public AnnotationGroup givenAnAnnotationGroup() {
        return persistAndReturn(givenANotPersistedAnnotationGroup());
    }

    public AnnotationLink givenANotPersistedAnnotationLink(UserAnnotation annotation, AnnotationGroup annotationGroup,
        ImageInstance image) {
        AnnotationLink annotationLink = new AnnotationLink();
        annotationLink.setAnnotationClassName(annotation.getClass().getName());
        annotationLink.setAnnotationIdent(annotation.getId());
        annotationLink.setGroup(annotationGroup);
        annotationLink.setImage(image);

        return annotationLink;
    }

    public AnnotationLink givenANotPersistedAnnotationLink() {
        Project project = givenAProject();

        return givenANotPersistedAnnotationLink(givenAUserAnnotation(project),
            givenAnAnnotationGroup(project, givenAnImageGroup(project)), givenAnImageInstance(project));
    }

    public AnnotationLink givenAnAnnotationLink(UserAnnotation annotation, AnnotationGroup annotationGroup,
        ImageInstance image) {
        return persistAndReturn(givenANotPersistedAnnotationLink(annotation, annotationGroup, image));
    }

    public AnnotationLink givenAnAnnotationLink() {
        return persistAndReturn(givenANotPersistedAnnotationLink());
    }

    public TaskRun givenANotPersistedTaskRun() {
        return givenANotPersistedTaskRun(givenAProject(), UUID.randomUUID(), givenAnImageInstance());
    }

    public TaskRun givenANotPersistedTaskRun(Project project, UUID taskRunId, ImageInstance image) {
        TaskRun taskRun = new TaskRun();
        taskRun.setProject(project);
        taskRun.setUser(getUserEntity(SUPER_ADMIN));
        taskRun.setTaskRunId(taskRunId);
        taskRun.setImage(image);
        return taskRun;
    }

    public TaskRun givenATaskRun() {
        return persistAndReturn(givenANotPersistedTaskRun(givenAProject(), UUID.randomUUID(), givenAnImageInstance()));
    }

    public AnnotationLayer givenANotPersistedAnnotationLayer() {
        AnnotationLayer annotationLayer = new AnnotationLayer();
        annotationLayer.setName(randomString());
        return annotationLayer;
    }

    public AnnotationLayer givenAPersistedAnnotationLayer() {
        return persistAndReturn(givenANotPersistedAnnotationLayer());
    }

    public Annotation givenANotPersistedAnnotation(AnnotationLayer annotationLayer) {
        Annotation annotation = new Annotation();
        annotation.setAnnotationLayer(annotationLayer);
        annotation.setLocation("{\"type\": \"Point\",\"coordinates\": [0, 0]}".getBytes());
        return annotation;
    }

    public Annotation givenANotPersistedAnnotation() {
        return givenANotPersistedAnnotation(givenAPersistedAnnotationLayer());
    }

    public Annotation givenAPersistedAnnotation() {
        return persistAndReturn(givenANotPersistedAnnotation());
    }

    public TaskRunLayer givenANotPersistedTaskRunLayer(AnnotationLayer annotationLayer, TaskRun taskRun,
        ImageInstance image) {
        TaskRunLayer taskRunLayer = new TaskRunLayer();
        taskRunLayer.setAnnotationLayer(annotationLayer);
        taskRunLayer.setTaskRun(taskRun);
        taskRunLayer.setImage(image);
        return taskRunLayer;
    }

    public TaskRunLayer givenANotPersistedTaskRunLayer() {
        return givenANotPersistedTaskRunLayer(givenAPersistedAnnotationLayer(), givenATaskRun(),
            givenAnImageInstance());
    }

    public TaskRunLayer givenAPersistedTaskRunLayer() {
        return persistAndReturn(givenANotPersistedTaskRunLayer());
    }
}
