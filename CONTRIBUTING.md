# Contributing to O-Neko

*Disclaimer: We created O-Neko as a "lab day" project and had to deal with time limits during our regular
work hours. This led to us neglecting some best practices in order to get the project to a usable state in an adequate
time (it still took us almost two years from the idea to now). This trade off we had to make means the project is 
currently in a state we would like to improve with coming contributions from ourselves and the community.*

## Found a bug? Have a feature request? Just have a question?

Please feel free to create issues here on GitHub if you think you found a bug or want to request a feature that is not 
currently in O-Neko. You can also ask questions via GitHub issues.

## Want to work on an issue?

We welcome every contribution, regardless of whether you created the issue or not. While we cannot guarantee that we'll 
accept all feature requests, we'll have a thorough look on every request we receive.

If you want to work on a **major feature** please create an issue first and let us have a discussion, before investing
a lot of time into developing it. **Small features** are welcome as pull requests without prior discussion. To create a 
pull request please:

* See whether a similar issue has already been worked on or is currently being worked on, to prevent duplicated effort.
* Fork the O-Neko repository and checkout a new branch for your changes. Follow our [dev environment setup guide](./DEV_ENVIRONMENT.md) in case you need help setting up your development environment.
* Create your changes. Ideally you make sure to include appropriate test cases.
* Test your changes and make sure the backend and frontend are behaving as expected.
* Commit your changes. While we don't enforce a template for commit messages yet, we're fans of 
[Conventional Commits](https://www.conventionalcommits.org).
* Push your changes to your branch and open a pull request to the master branch of O-Neko.
* We will review your pull request and might suggest changes.
  * If we do so please update your pull request according to
the review while following this contribution guide. 
  * We don't enforce this yet, but if you can, please rebase your branch and force-push to your repository:
      ```
      git rebase master -i
      git push -f
      ```
* Done. Thank you for your contribution!

After we have merged your pull request you are free to delete your branch and update your master with the latest version
containing your changes.

## Coding guidelines

We want to ensure a consistent style throughout the source code. While we have no strict code style yet, we roughly
orient our style on the standard style in Java and JavaScript/TypeScript. Please make sure that the style of your code
is similar to the rest of the backend and/or frontend code. Node that the backend and the frontend may use a different 
code style different and formatting rules. 