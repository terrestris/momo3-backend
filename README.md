# About `momo3-backend`

The backend-code for the third phase of MoMo, http://www.bmbf.wasserressourcen-management.de/en/111.php

This repository will contain the sources for building the backend of the MoMo 3 project. It is intended to be
used alongside its frontend companion https://github.com/terrestris/momo3-frontend.

# MoMo3 Webapplication

This README contains a quickstart tutorial for **developers**.

## Getting started

Prerequisites:

* Java 1.7 or 1.8
* [Maven 3.x](https://maven.apache.org/)
* Java Webapplication Server
* [Sencha Cmd 6.0.2.14](https://www.sencha.com/products/sencha-cmd/)
* [ExtJS 6](https://www.sencha.com/products/extjs/#overview)

Recommended:

* [Eclipse IDE](https://eclipse.org/)
* [m2e plugin for Eclipse](http://www.eclipse.org/m2e/)
* [Tomcat 8](http://tomcat.apache.org/)

### 1. Checkout sources

Fork and checkout [the sources of this project](https://github.com/ terrestris/momo3-backend):

1. Fork this project on github.
2. Go to your local workspace directory (assuming you work on Linux):

    `$ cd ~/workspace`

3. Clone your fork to your device, e.g.:

    `$ git clone https://github.com/{{YOUR_GITHUB_ID}}/momo3-backend.git`

4. Next, go into the just created directory:

    `$ cd momo3-backend/`

5. (Not needed at the moment) In order to get all needed dependencies (e.g. the doc/wiki), you need to sync
   all referenced submodules:

    `$ git submodule update --init --recursive`

6. Finally add the remote repo as upstream:

    `$ git remote add upstream https://github.com/terrestris/momo3-backend.git`

7. Verify the remote exists:

    `$ git remote -v`

8. In order to get all referenced git repositories ([momo3-frontend](https://github.com/terrestris/momo3-frontend)
open the `pom.xml` and set your git username to the property `<maven-scm-plugin.git-username>`.
**Note:** This assumes that you have forked the referenced repositories to your git account!

9. Clone the frontend code by running `mvn scm:checkout` in project directory

10. Go to `{projectDir}/src/main/webapp/client` and run:

  `git remote add upstream https://github.com/terrestris/momo3-frontend.git`

### 2. Import project to the Eclipse IDE

1. Make sure that the latest version of the m2e plugin is installed in your
   Eclipse IDE.
2. `File` &rarr; `Import` &rarr; `Existing Maven Projects`
3. Browse your project root directory, e.g. `~/workspace/momo3-backend`
4. Click `Finish` to import the project to Eclipse
5. Right click on the imported project &rarr; `Run As` &rarr; `Maven install`

> **Note:** You can import the SHOGun2 sources to Eclipse in the same way as
> described above. In this case, the m2e plugin recognizes that your SHOGun2
> dependencies are in the same workspace and changes to the SHOGun2 sources will
> immediately be available in this project (if `Project` &rarr; `Build
> Automatically` is enabled)

### 3. ExtJS/momo3-frontend setup

1. Download the [ExtJS 6 sources](https://www.sencha.com/products/extjs/#overview) to a location of your choice: `{{PATH_TO_EXT}}`.
2. Go to the directory with the ExtJS application (momo3-frontend): `cd src/main/webapp/client`
3. Inside this folder run `sencha app upgrade {{PATH_TO_EXT}}`
4. If you get a warning "Failed to resolve package BasiGX" please do follow steps:

   `sencha package repo add GeoExt http://geoext.github.io/geoext3/cmd/pkgs`
   
   `sencha package repo add BasiGX http://terrestris.github.io/BasiGX/cmd/pkgs` 

5. Run `sencha app refresh`
6. Run `sencha app build`
7. Refresh the project in Eclipse

### 4. Application setup

1. In the case you haven't run the application before, you have to initialize
   the application database including some default entities by enabling the
   following properties (in folder `src/main/resources/META-INF`):

   * `hibernate.properties`:

     * `hibernate.hbm2ddl.auto=create`

   * `momo-init.properties`:

     * `init.shogunInitEnabled=true`

     * `init.projectInitEnabled=true`

> **Note:** Unless you're developing application models, it's recommended
to reset the values from above to its defaults after the first startup.
Otherwise the database is being initialized/overwritten on every application
startup.

### 5. Run the project on a local Server

1. Add a server runtime environment to Eclipse:

   `Window` &rarr; `Preferences` &rarr; `Server` &rarr; `Runtime Environments`
   &rarr; `Add Tomcat 8 (or 7)`

2. Set the `app.profile` variable

  1. Select your project in Eclipse
  2. `Run` &rarr; `Run Configurations` &rarr; Select your `Apache Tomcat`
      runtime &rarr; Select the `Arguments` tab
  3. Add `-Dapp.profile=dev` to your `VM arguments` section

3. To run the project on this server:

   Right click on the project: `Run as` &rarr; `Run on Server` &rarr;
   `Select your Tomcat`

4. Open

   `http://localhost:8080/momo/client/index.html?id={applicationId}`

    (If you haven't logged in yet, you will be asked to login as a valid user before)

 Note: To get a valid `{applicationId}` just follow these steps:

   * Adapt and open this URL:

     `http://localhost:8080/momo/application/findAll.action`

   * The response will list all of your current SHOGun2 applications (and normally it should return the default one named _Default Application_ only)

   * Find the entry `id` containing the ID of the application (e.g. `"id": 50`)

   * Use this ID as GET parameter (see above)
