import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;

public class AuthenticationDemoArgs {
    public static void main(String[] args) throws CytomineException {
        if (args.length != 3) {
            System.exit(-1);
        }
        String host = args[0];
        String publicKey = args[1];
        String privateKey = args[2];

        Cytomine.connection(host, publicKey, privateKey);
        // We are now connected!

        // It will print your username, that has been retrieved from Cytomine.
        System.out.println(Cytomine.getInstance().getCurrentUser().get("username"));
    }
}