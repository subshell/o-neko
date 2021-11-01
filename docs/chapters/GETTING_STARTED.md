# Getting Started

This document will guide you through the first steps after you've successfully installed O-Neko in your cluster.

## 1. Open O-Neko in your browser

The first step is fairly easy. You need to open O-Neko in your browser. Open the URL you chose in the
O-Neko ingress you deployed in your cluster during installation.

After the login page shows up you can initially log in with `admin/admin`. Please change the admin password as soon as possible.

## 2. Show O-Neko your Docker Registry

In order to know where your Docker images come from, you need to "create" a Docker registry in O-Neko.
To do this, open the "Docker Registries" tab and click on "Create Docker Registry". A dialog should show up. Enter a 
name for your registry, fill out the form and click "Save". The registry URL should start with `https://`.
 
The user you use for authentication at your registry needs to have at least read permissions.

## 3. Show O-Neko your Helm Registry

Similar to your Docker registry, you need to add your Helm registry in O-Neko. We support standard Helm registries and
registries created via [Helm GCS](https://github.com/hayorov/helm-gcs).

## 4. Create a namespace

You need to create at least one namespace in O-Neko, in order to create and deploy projects. You can choose a namespace per project
or even per project version. A good start would be `on-deployments`.

## 5. Create a new O-Neko project

Now that you have set up the Docker registry you can create a new project. Head over to "Projects" and click on
"Create Project". A small wizard will guide you through the steps. When you're done with the wizard, the project will
be created. Now you have to configure your project, so O-Neko knows how to run your app.

## 6. Configure your project

First select the namespace your project should be deployed to. Then you need to configure the template(s). 
It's easiest to configure a project if you already have a working values .yaml file for your Helm chart. First add a new
configuration template via the "..." menu to the right. Then select your Helm registry, the chart and (optionally) the
chart version. Then you can write your Helm values .yaml file in the editor.

### 6.1 Configuring URLs via URL Templates

Your projects probably host one or more web frontends which should be hosted under one or more URLs. You will want every
version to get its own domain. You can add URL templates to a project, which will be available in the following configuration.
Versions can overwrite the list of URL templates.

#### Background

> Why should I use URL templates instead of directly writing the URLs into the Helm values? It makes O-Neko even more complicated!

Using the URL templates helps O-Neko be aware of the URLs that belong to a specific version of a project. It will be used to display
clickable links to your frontends in the O-Neko UI. It will also be possible to start O-Neko deployments by their
URL using an O-Neko API call. This is useful in combination with the O-Neko extension [O-Neko Catnip](https://github.com/subshell/o-neko-catnip),
which can be used as a default backend for all stopped deployments.

#### Example

In most cases a simple URL template consisting of a string that contains the variable `{{ SAFE_VERSION_NAME }}` 
will be sufficient to cover your needs. `SAFE_VERSION_NAME` resolves to a URL compatible string of the version (Docker tag).
The URL template may look like this:

```yaml
my_app-{{ SAFE_VERSION_NAME }}.my-k8s-cluster.my-company.com
```

All URL templates will be available in the following configuration templates via the array-variable `URLS` and their corresponding
index in the list, starting with 0.
The URL from this example will be available in the Helm values templates as `URLS[0]`. If you add more URLs you'll reference them with a
higher index. After defining the URLs here you will have to reference them in the following configuration, for example
to configure an ingress etc. Depending on how your Helm chart is designed it may look similar to this:

```yaml
ingress:
  hosts:
    - host: "{{ URLS[0] }}"
```

### 6.2 Define O-Neko compatible Helm values files

You need to create at least one configuration template, which is a templated abstraction of a Helm values file in which you can make use of
some pre-defined and custom variables. In order to deploy everything correctly, you need to replace some fixed entries 
in your files with a template variable syntax. The most important line is the docker image tag in your deployment.
You will need to set your docker image tag to `{{VERSION_NAME}}`.

`VERSION_NAME` will be replaced with the docker tag you want to deploy, which might resolve to `latest`, `1.0.0`, `bugfix_user_auth`
or basically every docker tag that exists in your project.

**⚠️ Important: ⚠️** Please also make sure to set the `imagePullPolicy` to `Always` via your values. Otherwise O-Neko might instruct
your cluster to re-deploy a version but the cluster will not pull an updated image. Setting the policy to always will force
the cluster to pull your image every time. If your Helm chart does not allow to change the `imagePullPolicy` you need to
extend your Helm chart.

In order to get dynamic URLs to your app you'll have to configure the host name (e.g. in an ingress). In this template 
you should replace the host string with one of your configured URLs (as explained above).

There are some other default template variables you can use:

* `PROJECT_NAME` (the name you chose for your project)
* `ONEKO_PROJECT` (the project's ID)
* `VERSION_NAME` (the name of a version; the docker image tag)
* `SAFE_VERSION_NAME` (the URL compatible version name)
* `ONEKO_VERSION` (the version's ID)
* `URLS[i]` (the URL(s) of your deployment - this is always an array, see the section about URLs)

You can also create your own template variables and override them in specific versions as you like.

### 6.3 Control the lifetime of a deployed version

You can control when new versions should be updated or stopped. In most cases the defaults should be fine. Choose something that
fits for most of your versions. You can still override these settings for specific long-running versions (e.g. your `latest`) version.

Click save to persist your changes.

## 7. Deploy your first version

While you've been setting up your project O-Neko already scanned all the tags for your docker image. To see them either click on "Show Versions"
from the project's edit screen or head over to "Projects" in the main menu and click on the number of versions next to your project.

Choose a version you want to deploy and simply click the play button. If everything has been set up correctly, the deployment status
icon of your app should start to flash and turn green as soon as your cluster has successfully deployed your app.

## 8. Accessing the version

Head over to the "Home" page of O-Neko. This is the dashboard, and you can see all deployed versions here. 
You should see the version you deployed there. Click on the link icon to open the frontend of your app.
If your app has multiple URLs configured in the ingress clicking on that icon will open a drop-down menu, so you can choose
which page to open.

Con-😸-ulations, you just deployed your first project with O-Neko.
