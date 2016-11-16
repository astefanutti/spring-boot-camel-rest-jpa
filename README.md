# Spring Boot Camel REST / JPA Example

This example demonstrates how to use JPA and Camel's REST DSL
to expose a RESTful API that performs CRUD operations on a database.

It generates orders for books referenced in database at a regular pace.
Orders are processed asynchronously by another Camel route. Books available
in database as well as the status of the generated orders can be retrieved
via the REST API.

It relies on Swagger to expose the API documentation of the REST service.

This example relies on the [Fabric8 Maven plugin](https://maven.fabric8.io)
for its build configuration and uses the
[fabric8 Java base image](https://github.com/fabric8io/base-images#java-base-images).

### Building

The example can be built with:

    $ mvn install

This automatically generates the application resource descriptors and builds
the Docker image, so it requires access to a Docker daemon, relying on the
`DOCKER_HOST` environment variable by default.

### Running the example locally

The example can be run locally using the following Maven goal:

    $ mvn spring-boot:run

Alternatively, you can run the application locally using the executable
JAR produced:

    $ java -jar -Dspring.profiles.active=dev target/spring-boot-camel-rest-jpa-${project.version}.jar

This uses an embedded in-memory HSQLDB database. You can use the default
Spring Boot profile in case you have a MySQL server available for you to test.

You can then access the REST API directly from your Web browser, e.g.:

- <http://localhost:8080/camel-rest-jpa/books>
- <http://localhost:8080/camel-rest-jpa/books/order/1>

### Running the example in Kubernetes / OpenShift

It is assumed a Kubernetes platform is already running. If not, you can
find details how to [get started](http://fabric8.io/guide/getStarted/index.html).

Besides, it is assumed that a MySQL service is already running on the platform.
You can deploy it using the provided deployment by executing in Kubernetes:

    $ kubectl create -f mysql-deployment.yml

or in OpenShift:

    $ oc create -f https://raw.githubusercontent.com/openshift/origin/master/examples/db-templates/mysql-ephemeral-template.json
    $ oc new-app --template=mysql-ephemeral

More information can be found in [using the MySQL database image](https://docs.openshift.com/container-platform/3.3/using_images/db_images/mysql.html).

You may need to pass `MYSQL_RANDOM_ROOT_PASSWORD=true` as environment variable
to the deployment.
Besides, you may need to relax the security in your cluster as the MySQL container
requires the `setgid` access right permission. This can be achieved by running the
following command:
 
    $ oadm policy add-scc-to-group anyuid system:authenticated

That grants all authenticated users access to the `anyuid` SCC. You can find
more information in [Managing Security Context Constraints](https://docs.openshift.org/latest/admin_guide/manage_scc.html).

The example can then be built and deployed using a single goal:

    $ mvn fabric8:run -Dmysql-service-username=<username> -Dmysql-service-password=<password>

The `username` and `password` system properties correspond to the credentials
used when deploying the MySQL database service.

You can use the Kubernetes or OpenShift client tool to inspect the status, e.g.:

- To list all the running pods:
    ```
    $ kubectl get pods
    ```

- or on OpenShift:
    ```
    $ oc get pods
    ```

- Then find the name of the pod that runs this example, and output the logs from the running pod with:
    ```
    $ kubectl logs <pod_name>
    ```

- or on OpenShift:
    ```
    $ oc logs <pod_name>
    ```

### Accessing the REST service

When the example is running, a REST service is available to list the books
that can be ordered, and as well the order statuses.

As it depends on your Kubernetes / OpenShift setup, the hostname for the route
may vary. You can retrieve it by running the following command in OpenShift:

    $ oc get routes -o jsonpath='{range .items[?(@.spec.to.name == "camel-rest-jpa")]}{.spec.host}{"\n"}{end}'

The actual endpoint is using the _context-path_ `camel-rest-jpa/books` and
the REST service provides two services:

- `books`: to list all the available books that can be ordered,
- `order/{id}`: to output order status for the given order `id`.

The example automatically creates new orders with a running order `id`
starting from 1.

You can then access these services from your Web browser, e.g.:

- <http://\<route_hostname\>/camel-rest-jpa/books>
- <http://\<route_hostname\>/camel-rest-jpa/books/order/1>

### Swagger API

The example provides API documentation of the service using Swagger using
the _context-path_ `camel-rest-jpa/api-doc`. You can access the API documentation
from your Web browser at <http://\<route_hostname\>/camel-rest-jpa/api-doc>.

### More details

You can find more details about running this [quickstart](http://fabric8.io/guide/quickstarts/running.html)
on the website. This also includes instructions how to change the Docker
image user and registry.