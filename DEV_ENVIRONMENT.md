# How to set up your development environment

1. Make sure you have the following tools installed on your computer: `Docker`, `node` >= 12, a JDK 11 and Maven. 
We recommend to use [`nvm`](https://github.com/nvm-sh/nvm) to install and manage `node` versions and 
[`jabba`](https://github.com/shyiko/jabba) to install and manage JDK versions, but this is optional.

2. You can use any IDE you prefer, but we recommend on using one that supports our backend and frontend code (Java + Maven, TypeScript, SCSS)
and ideally offers support for the Spring framework (backend) and Angular framework (frontend) as this really improves
the developing experience.

3. Fork the O-Neko project on GitHub and clone your O-Neko git repository.

4. Open the project in your IDE or editor and navigate in a terminal to the `/frontend` directory and run `npm install`.
This will install all frontend dependencies. You should do this regularly to always have the latest dependencies installed.
The Java dependencies are managed by Maven and should be downloaded automatically, depending on your editor.

5. O-Neko relies on a MongoDB. You can run it anywhere you want, but by default the application will try to connect to a database
running on localhost with MongoDB's default port 27017. You can easily start that MongoDB by running `docker-compose up -d` in the
project's root directory. If you want to use another MongoDB make sure to change the settings in the `/src/main/resources/application.yaml`.

6. O-Neko uses the [fabric8.io Kubernetes client library](https://github.com/fabric8io/kubernetes-client) to connect to Kubernetes. 
O-Neko will connect to the cluster configured in the `application.yaml` file or, if present, use your `~/.kube/config` file to
choose a cluster to connect to. Make sure one of the two options is available.

7. Start the backend. This step may vary, depending on your IDE/editor. The main class you want to run is `io.oneko.ONekoApplication`.
We use Spring profiles to separate useful default settings from settings we use during development. This is why you need to
run the application with the following VM option: `-Dspring.profiles.active=development`.

8. Start the frontend. This is done by running `npm start` in the `/frontend` directory. This should compile the frontend
and open your default browser. O-Neko will be available on port 4200. This will also start a proxy server that proxies
all requests to `/api` and `/ws` to the O-Neko backend, which runs on port 8080.

9. You can now log in to O-Neko with the credentials `admin`/`admin` (if you started with a clean database) and are 
ready to start developing.