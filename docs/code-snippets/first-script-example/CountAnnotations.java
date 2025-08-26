import be.cytomine.client.*;
import be.cytomine.client.collections.*;
import be.cytomine.client.models.*;

public class AnnotationCounter {
  public static void main(String[] args){

    // Replace with actual values or -better- use args.
    String publicKey = "AAA";
    String privateLey = "ZZZ";
    String cytomineCoreUrl = "https://mycytomine.com/"; //Fictive: use your Cytomine server
    Long termId = 0L;

    // Connection to Cytomine
    Cytomine.connection(cytomineCoreUrl, publicKey, privateKey);

    // Get all the available projects you have access to
    Collection<Project> projects = Collection.fetch(Project.class);

    // Instantiate a new list
    List projectsStartingWithP = new ArrayList<>();

    // Filter the projects
    for (int i= 0; i<projects.size();i++){
      Project project = projects.get(i);
      if(project.get("name").startsWith("P") {
        projectsStartingWithP.add(project);
      }
    }

    // Get the term
    Term term = new Term().fetch(termId);

    // Count annotations having that term in filtered projects
    AnnotationCollection ac;
    int count = 0;
    for (Project p : projectsStartingWithP){
      ac = AnnotationCollection.fetchByTermAndProject(term, p);
      count += ac.size();
    }

    System.out.println("There are " + new String(count) + " annotations with term " + term.getStr("name"))
  }
}