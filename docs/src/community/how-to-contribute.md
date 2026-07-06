# How to contribute

Cytomine is an open-source project and welcomes contributions of all kinds: code, documentation, example tasks, translations, or simply a bug report.

## Code

If you are new, start by exploring issues labelled [good first issue](https://github.com/cytomine/cytomine/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22) in the cytomine repository.

If you are ready to dive in, whether testing, fixing a bug, or adding a feature, follow the instructions in our [Contributing Guide](https://github.com/cytomine/cytomine?tab=contributing-ov-file#cytomine-contributing-guide) to set up the Cytomine development environment.

## Documentation

This documentation website lives in the [`docs`](https://github.com/cytomine/cytomine/tree/main/docs) folder of the [cytomine/cytomine](https://github.com/cytomine/cytomine) repository, and is built with [VuePress v1](https://v1.vuepress.vuejs.org/).

To work on it locally:

1. Fork and clone the repository, then follow the [Contributing Guide](https://github.com/cytomine/cytomine?tab=contributing-ov-file#cytomine-contributing-guide) to set up your branch.

2. Install the dependencies (requires [Node](https://nodejs.org/) v22.22.3+).

    ```bash
    cd docs
    npm install
    ```

3. Start the development server with live reload.

    ```bash
    npm run dev
    ```

    The website will be available at <http://localhost:8080>.

4. Edit the relevant Markdown file(s) under `docs/src`, then open a pull request following the same process as for code contributions.

::: tip
You can generate a static build of the website with `npm run build`. The output is written to `docs/src/.vuepress/dist`.
:::

## Tasks

Cytomine tasks (algorithms integrated through the App Engine) are also a great way to contribute. You can share a task you built by publishing it as its own repository, or submit it for inclusion in the documentation as a reference example.

See the [example tasks](/dev-guide/algorithms/task/examples/others) page for a list of existing tasks to use as a starting point, and the [Task](/dev-guide/algorithms/task/) guide to learn how a task is structured.

## Translations

The Cytomine web interface (`web-ui`) is translated using [vue-i18n](https://kazupon.github.io/vue-i18n/). All translation strings are centralised in [`web-ui/src/locales/translations.csv`](https://github.com/cytomine/cytomine/blob/main/web-ui/src/locales/translations.csv), with one column per language (currently English, French, Spanish, and Dutch).

To contribute:

- **Improve an existing language**: edit `translations.csv` and fill in or correct the entries in the corresponding column.
- **Add a new language**: add a new column to `translations.csv` with the language code as header, translate the existing keys, and register the new locale in [`web-ui/src/lang.js`](https://github.com/cytomine/cytomine/blob/main/web-ui/src/lang.js).

Running `npm run translations-csv2json` in the `web-ui` folder regenerates the per-language JSON files from the CSV (this also runs automatically as part of `npm run build` and `npm run serve`).

## Report a bug or request a feature

If you noticed a bug or would like to suggest a new feature, please [open an issue](https://github.com/cytomine/cytomine/issues/new) on GitHub. Providing as much detail as possible (steps to reproduce, expected vs. actual behaviour, screenshots, environment) helps us address it faster.
