import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;

public class AuthenticationDemo {
    public static void main(String[] args) throws CytomineException {
        String host = "https://mycytomine.com";
        String publicKey = "AAA";
        String privateKey = "ZZZ";

        Cytomine.connection(host, publicKey, privateKey);
        // We are now connected!

        // It will print your username, that has been retrieved from Cytomine.
        System.out.println(Cytomine.getInstance().getCurrentUser().get("username"));
    }
}