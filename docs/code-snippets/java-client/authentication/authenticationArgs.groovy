import be.cytomine.client.Cytomine

if (args.length != 3) {
    System.exit(-1)
}
String host = args[0]
String publicKey = args[1]
String privateKey = args[2]

Cytomine.connection(host, publicKey, privateKey)
// We are now connected!

// It will print your username, that has been retrieved from Cytomine.
println Cytomine.getInstance().getCurrentUser().get("username")