[conventional commits]: https://www.conventionalcommits.org/en/v1.0.0/
[this guide on how to squash commits]: https://www.internalpointers.com/post/squash-commits-into-one-git

# Contributing Guidelines

## Code Style

If you're using IntelliJ, or a compatible IDE, the project's code style should already be set in your project settings. If you want your changes to be automatically formatted, see `Settings > Actions On Save > Reformat Code (Changed Lines instead of Whole File)`.

Otherwise, the project's code is formatted in Google Style, albeit using tabs instead of spaces and a tab and indentation size of 4.

## Branches and Forks

It's recommended for you to fork the repository, create a new branch for your new feature/fix and open a pull request when you've committed your changes. It's recommended that you do this so you're able to force push and delete branches and the repository at your whim.

## Duplicate Commits

If you need to commit something to quickly fix something, instead of creating a new commit, please use `git commit --amend` and force-push to your fork (`git push -f`). If you've already

## Commit and Pull Request Naming

Your pull request names MUST follow [Conventional Commits] and be lowercase. If you're not changing any source code (i.e. a pull request to fix a typo in documentation), please prefix your commit(s) with [skip ci]. This tells GitHub actions that you don't want your commit to be processed. If you've already done this/want to squash some/all of your commits into one, please follow [this guide on how to squash commits].

## Common Sense

Please make sure your commits follow common sense. Some examples of this include not stealing code from other repositories where such code stealing isn't appreciated (for example, if the project is under a less permissive license than ours).
