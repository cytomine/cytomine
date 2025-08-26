import be.cytomine.client.Cytomine

String host = "https://mycytomine.com"
String publicKey = "AAA"
String privateKey = "ZZZ"

Cytomine.connection(host, publicKey, privateKey)
// We are now connected!

// It will print your username, that has been retrieved from Cytomine.
println Cytomine.getInstance().getCurrentUser().get("username")