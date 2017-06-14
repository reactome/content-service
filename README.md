<img src=https://cloud.githubusercontent.com/assets/6883670/22938783/bbef4474-f2d4-11e6-92a5-07c1a6964491.png width=220 height=100 />

# Reactome Content Service

## What is the Reactome Content Service

The Content Service is the Reactome API to access the data. It is based on Spring MVC, based on REST and fully documented in Open API (previously Swagger). Through the ContentService API you'll have access to the Graph Database, Interactors and SolR Search.

#### Installation Guide

* :warning: Pre-Requirement (in the given order)
    1. Maven 3.X - [Installation Guide](http://maven.apache.org/install.html)
    2. Reactome Graph Database - [Installation Guide](http://www.reactome.org/pages/documentation/developer-guide/graph-database/)
    3. Interactor Database - [Installation Guide](https://github.com/reactome-pwp/interactors-core)
    4. Search SolR Index - [search-indexer](http://github.com/reactome/search-indexer)
    5. Mail Server (if you don't have a valid SMTP Server, please refer to [FakeSMTP](http://nilhcem.com/FakeSMTP/index.html)

##### Git Clone

```console
git clone https://github.com/reactome/content-service.git
cd content-service
```

##### Configuring Maven Profile :memo:

Maven Profile is a set of configuration values which can be used to set or override default values of Maven build. Using a build profile, you can customize build for different environments such as Production v/s Development environments.
Add the following code-snippet containing all the Reactome properties inside the tag ```<profiles>``` into your ```~/.m2/settings.xml```.
Please refer to Maven Profile [Guideline](http://maven.apache.org/guides/introduction/introduction-to-profiles.html) if you don't have settings.xml


```html
<profile>
    <id>ContentService-Local</id>
    <properties>
        <!-- Neo4j Configuration -->
        <neo4j.host>localhost</neo4j.host>
        <neo4j.port>7474</neo4j.port>
        <neo4j.user>neo4j</neo4j.user>
        <neo4j.password>neo4j</neo4j.password>

        <!-- SolR Configuration -->
        <solr.host>http://localhost:8983/solr/reactome</solr.host>
        <solr.user>solr</solr.user>
        <solr.password>solr</solr.password>

        <!-- Interators Database -->
        <interactors.SQLite>/Users/reactome/Reactome/interactors/interactors.db</interactors.SQLite>

        <!-- Logging -->
        <logging.dir>/Users/reactome/Reactome/search</logging.dir>
        <logging.database>${logging.dir}/search.db</logging.database>

        <!-- Mail Configuration, using FakeSMTP -->
        <!-- Properties are ready to use GMail, etc. -->
        <mail.host>localhost</mail.host>
        <mail.port>8081</mail.port>
        <mail.username>username</mail.username>
        <mail.password>password</mail.password>
        <mail.enable.auth>false</mail.enable.auth>
        <mail.error.dest>bug-fixing-team@mycompany.co.uk</mail.error.dest>
        <mail.support.dest>helpdesk@mycompany.co.uk</mail.support.dest>

        <!-- Reactome Server to query header and footer -->
        <template.server>http://reactomedev.oicr.on.ca/</template.server>

        <!-- Interactor custom folder -->
        <tuples.uploaded.files.folder>/Users/reactome/Reactome/interactors/tuple</tuples.uploaded.files.folder>

        <!--
            The cron has to match 6 fields which are: second, minute, hour, day of month, month, day(s) of week
            e.g run every 10 minutes =>  0 */10 * * * *
                (*) - match any
                */X - means every "X"
        -->
        <psicquic.resources.cache.cron>0 */59 * * * *</psicquic.resources.cache.cron>

        <!-- PPTX Exporter -->
        <diagram.json.folder>/Users/reactome/Reactome/diagram/static</diagram.json.folder>
        <diagram.exporter.temp.folder>/Users/reactome/Reactome/diagram/exporter</diagram.exporter.temp.folder>

        <!-- AOP: Do not enable. -->
        <aop.enabled>false</aop.enabled>
    </properties>
</profile>
```

##### Running ContentService activating ```ContentService-Local``` profile
```console
mvn tomcat7:run -P ContentService-Local
```

in case you didn't set up the profile it is still possible to run Reactome Content Service. You may need to add all the properties into a command-line call.
```console
mvn tomcat7:run \
    -Dneo4j.user=neo4j -Dneo4j.password=neo4j -Dneo4j.host=localhost -Dneo4j.port=7474 \
    -Dsolr.host=http://localhost:8983/solr/reactome -Dsolr.user=solr -Dsolr.password=solr \
    -Dinteractors.SQLite=/Users/reactome/Reactome/interactors/interactors.db \
    -Dlogging.dir=/Users/reactome/Reactome/search \
    -Dlogging.database=/Users/reactome/Reactome/search/search.db \
    -Dmail.host=localhost -Dmail.port=8081 -Dmail.username=username -Dmail.password=password \
    -Dmail.enable.auth=false -Dmail.error.dest=bug-fixing-team@mycompany.co.uk \
    -Dmail.support.dest=helpdesk@mycompany.co.uk \
    -Dtemplate.server=http://reactomedev.oicr.on.ca/ \
    -Dtuples.uploaded.files.folder=/Users/reactome/Reactome/interactors/tuple \
    -Dpsicquic.resources.cache.cron="0 */59 * * * *" \
    -Ddiagram.json.folder=/Users/reactome/Reactome/diagram/static \
    -Ddiagram.exporter.temp.folder=/Users/reactome/Reactome/diagram/exporter \
    -Daop.enabled=false
```

Check if Tomcat has been initialised
```rb
[INFO] Using existing Tomcat server configuration at /Users/reactome/content-service/target/tomcat
INFO: Starting ProtocolHandler ["http-bio-8585"]
```

#### Usage

* :computer: Access your local [installation](http://localhost:8585/)

<img width="966" alt="content-service-1" src="https://cloud.githubusercontent.com/assets/6883670/23139777/05b17160-f7a6-11e6-9199-bfad5d179268.png">
<img width="969" alt="content-service-2" src="https://cloud.githubusercontent.com/assets/6883670/23139778/05b24c5c-f7a6-11e6-89c6-a833c2a5f133.png">
<img width="976" alt="content-service-3" src="https://cloud.githubusercontent.com/assets/6883670/23139779/05b62c64-f7a6-11e6-811e-47987e28e23d.png">