const path = require('path');
const config = {
  mode: 'development',
  entry: ['idempotent-babel-polyfill', './src/index.js'],
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'cytomine-client.js',
    libraryTarget: 'umd'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: ['@babel/env'],
              plugins: ['@babel/plugin-transform-object-rest-spread']
            }
          },
        ]
      }
    ]
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  devtool: 'source-map'
};

module.exports = (env, argv) => {
  if (argv && argv.mode === 'production') {
    config.output.filename = 'cytomine-client.min.js';
  }
  return config;
};
