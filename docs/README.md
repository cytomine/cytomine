# Cytomine Documentation Website

The documentation site is built using VuePress v1.

## Requirements

- [Node](https://nodejs.org/) (v20.19.4+ recommended)

## Usage

Install prerequisite packages for the documentation portal:

```bash
npm install
```

To run the development server with live reload locally, run

```bash
npm run dev
```

The website will be available at <http://localhost:8080>

## Generate the documentation

Generate the website:

```bash
npm run build
```

The generated website is located at `src/.vuepress/dist`
