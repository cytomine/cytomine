Hey, be careful when declaring stuff in `common`. We have this `beans` package to avoid
instanciating useless beans in our apps. Use
`@SpringBootApplication(scanBasePackages = {"org.cytomine.common.beans.authorizationclient"})`.
`common` is always imported but beans should be selected.
