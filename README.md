[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Reactome Content Service

## What is the Reactome Content Service

The Content Service is the Reactome API to access the data. It is based on Spring MVC, based on REST and fully documented in Open API (previously Swagger). Through the ContentService API you'll have access to the Graph Database, Interactors and SolR Search.

#### Installation Guide

* :warning: Pre-Requirement (in the given order)
    1. Maven 3.X - [Installation Guide](http://maven.apache.org/install.html)
    2. Reactome Graph Database - [Installation Guide](https://reactome.org/dev/graph-database/)
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
    <id>reactome</id>
    <properties>
        <mail.host>localhost</mail.host>
        <mail.port>8181</mail.port>
        <mail.username>username</mail.username>
        <mail.password>password</mail.password>
        <mail.enable.auth>true</mail.enable.auth>

        <!-- Neo4J Configuration -->
        <neo4j.host>localhost</neo4j.host>
        <neo4j.port>7474</neo4j.port>
        <neo4j.user>neo4j</neo4j.user>
        <neo4j.password>password</neo4j.password>

        <!-- Solr Configuration -->
        <solr.host>http://localhost:8983/solr</solr.host>
        <solr.core>reactome</solr.core>
        <solr.user>admin</solr.user>
        <solr.password>password</solr.password>

        <!-- MySQL Configuration -->
        <mysql.host>localhost</mysql.host>
        <mysql.port>3306</mysql.port>
        <mysql.reactome.database>reactome</mysql.reactome.database>
        <mysql.report.database>report</mysql.report.database>
        <mysql.user>curator</mysql.user>
        <mysql.password>password</mysql.password>

        <template.server>https://localhost/</template.server>

        <!-- Log report -->
        <cnf.mail.error.subject>Automated Error Report</cnf.mail.error.subject>
        <cnf.mail.error.name>Error Report Agent</cnf.mail.error.name>
        <cnf.mail.error.from>noreply@reactome.org</cnf.mail.error.from>
        <cnf.mail.error.to>mail@domain.ac.uk</cnf.mail.error.to>

        <!-- Report mail Configuration -->
        <cnf.mail.report.hostname>reactome.org</cnf.mail.report.hostname>
        <cnf.mail.report.from>noreply@reactome.org</cnf.mail.report.from>
        <cnf.mail.report.to>mail@domain.ac.uk</cnf.mail.report.to>

        <!-- Results not found internal Report -->
        <report.user>reportadmin</report.user>
        <report.password>password</report.password>

        <!-- Logging -->
        <logging.level>ERROR</logging.level>
        <logging.dir>/var/log/tomcat7</logging.dir>

        <!-- Analysis intermediate file  -->
        <analysis.structure.file>/usr/local/reactomes/Reactome/production/AnalysisService/input/analysis.bin</analysis.structure.file>
        <analysis.result.root>/usr/local/reactomes/Reactome/production/AnalysisService/temp</analysis.result.root>

        <!-- Common folders and file locations -->
        <fireworks.json.folder>/usr/local/reactomes/Reactome/production/Website/static/download/current/fireworks</fireworks.json.folder>
        <diagram.json.folder>/usr/local/reactomes/Reactome/production/Website/static/download/current/diagram</diagram.json.folder>
        <diagram.exporter.temp.folder>/usr/local/reactomes/Reactome/production/ContentService/exporter/</diagram.exporter.temp.folder>
        <tuples.uploaded.files.folder>/usr/local/reactomes/Reactome/production/ContentService/custom</tuples.uploaded.files.folder>

        <!-- Needed for the Content-Service / Raster exporter -->
        <ehld.folder>/usr/local/reactomes/Reactome/production/Website/static/download/current/ehld</ehld.folder>
        <svg.summary.file>/usr/local/reactomes/Reactome/production/Website/static/download/current/ehld/svgsummary.txt</svg.summary.file>

        <!-- Path to the experiments binary file -->
        <experiments.data.file>/usr/local/reactomes/Reactome/production/AnalysisService/digester/experiments.bin</experiments.data.file>
    </properties>
</profile>
```

##### Running ContentService activating ```reactome``` profile.
```console
mvn tomcat7:run -P reactome
```

Check if Tomcat has been initialised
```rb
[INFO] Using existing Tomcat server configuration at /Users/reactome/content-service/target/tomcat
INFO: Starting ProtocolHandler ["http-bio-8686"]
```

#### Usage

* :computer: Access your local [installation](http://localhost:8686/)

<img width="966" alt="content-service-1" src="https://cloud.githubusercontent.com/assets/6883670/23139777/05b17160-f7a6-11e6-9199-bfad5d179268.png">
<img width="969" alt="content-service-2" src="https://cloud.githubusercontent.com/assets/6883670/23139778/05b24c5c-f7a6-11e6-89c6-a833c2a5f133.png">
<img width="976" alt="content-service-3" src="https://cloud.githubusercontent.com/assets/6883670/23139779/05b62c64-f7a6-11e6-811e-47987e28e23d.png">
