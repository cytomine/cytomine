import { h } from 'vue';
import { Line } from 'vue-chartjs';
import _ from 'lodash';

export default {
  name: 'histogram-chart',
  components: { Line },
  props: {
    cssClasses: { type: String, default: '' },
    logScale: Boolean,
    color: String,

    histogram: Array,
    nBins: Number,
    firstBin: Number,
    lastBin: Number,

    defaultBounds: Object,
    imageBounds: Object,
    currentBounds: Object,
    gamma: Number,
    inverted: Boolean,
  },
  computed: {
    extendedHistogram() {
      const missingLeft = new Array(this.firstBin).fill(0);
      const missingRight = new Array(this.nBins - this.lastBin - 1).fill(0);
      return missingLeft.concat(this.histogram).concat(missingRight);
    },
    logHistogram() {
      return this.extendedHistogram.map(v => Math.log(v));
    },
    scaledHistogram() {
      return this.logScale ? this.logHistogram : this.extendedHistogram;
    },

    binSize() {
      return (this.defaultBounds.max + 1) / this.nBins;
    },
    integerBinSize() {
      return Math.round(this.binSize);
    },
    labels() {
      return this.extendedHistogram.map((_, idx) => Math.round(idx * this.binSize));
    },
    imageBoundsLabel() {
      return {
        min: this.findLabel(this.imageBounds.min),
        max: this.findLabel(this.imageBounds.max)
      };
    },
    currentBoundsLabel() {
      return {
        min: Math.max(this.defaultBounds.min, this.findLabel(this.currentBounds.min)),
        max: Math.min(this.defaultBounds.max, this.findLabel(this.currentBounds.max))
      };
    },
    boundsLabel() {
      return {
        min: Math.min(this.imageBoundsLabel.min, this.currentBoundsLabel.min),
        max: Math.max(this.imageBoundsLabel.max, this.currentBoundsLabel.max)
      };
    },

    currentLabels() {
      return this.labels.filter(label =>
        label >= this.currentBoundsLabel.min && label <= this.currentBoundsLabel.max
      );
    },

    systemResponse() {
      if (this.currentLabels.length === 1) {
        return [
          { x: this.currentLabels[0], y: 0 },
          { x: this.currentLabels[0], y: 255 }
        ];
      }

      let nbPoints = 100;
      let step = (this.currentLabels.length - 1) / nbPoints;
      let range = _.range(0, this.currentLabels.length - 1 + step, step);

      let ymin = (this.inverted) ? 1 : 0;
      let ymax = (this.inverted) ? 0 : 1;
      let m = (ymin - ymax) / (this.currentBoundsLabel.min - this.currentBoundsLabel.max);
      let p = ymin - m * this.currentBoundsLabel.min;
      let gamma = (this.inverted) ? 1 / this.gamma : this.gamma;
      let response = range.map(idx => {
        let label = this.currentLabels[Math.round(idx)];
        return {
          x: label,
          y: Math.pow((m * label + p), gamma) * 255.0
        };
      });
      return _.uniqBy(response, 'x');
    },

    backgroundColor() {
      if (this.color !== null) {
        return this.color;
      }
      return '#fff';
    },
    datasets() {
      return [
        {
          label: 'response',
          data: this.systemResponse,
          fill: false,
          pointRadius: 0,
          pointHoverRadius: 0,
          borderColor: '#333',
          borderWidth: 1,
          type: 'line',
          order: 1,
          cubicInterpolationMode: 'monotone',
          yAxisID: 'yResponse'
        },
        {
          label: 'histogram',
          data: this.scaledHistogram,
          backgroundColor: this.backgroundColor,
          fill: true,
          pointRadius: 1,
          order: 2,
          yAxisID: 'yHistogram'
        },
      ];
    },
    chartData() {
      return {
        labels: this.labels,
        datasets: this.datasets,
      };
    },
    chartOptions() {
      const logScale = this.logScale;
      const binSize = this.integerBinSize;
      const boundsLabel = this.boundsLabel;

      return {
        maintainAspectRatio: false,
        responsive: true,
        animation: {
          duration: 0,
        },
        plugins: {
          title: {
            display: false
          },
          legend: {
            display: false
          },
          tooltip: {
            filter: (tooltipItem) => tooltipItem.datasetIndex !== 0,
            callbacks: {
              label: (tooltipItem) => {
                if (logScale) {
                  return Math.round(Math.exp(tooltipItem.parsed.y));
                }
                return tooltipItem.parsed.y;
              },
              title: (tooltipItems) => {
                if (tooltipItems.length > 0) {
                  let left = Number(tooltipItems[0].label);
                  if (binSize === 1) {
                    return left;
                  }
                  let right = left + binSize;
                  return `[${left} - ${right}[`;
                }
              }
            }
          }
        },
        scales: {
          x: {
            display: true,
            min: boundsLabel.min,
            max: boundsLabel.max,
            title: {
              display: false
            },
            ticks: {
              font: { size: 10 },
            }
          },
          yHistogram: {
            display: false,
          },
          yResponse: {
            display: false
          },
        },
      };
    },
  },
  methods: {
    findBin(value) {
      return Math.floor(value / this.binSize);
    },
    findLabel(value) {
      return Math.floor(this.findBin(value) * this.binSize);
    },
  },
  render() {
    return h('div', { class: this.cssClasses }, [
      h(Line, {
        data: this.chartData,
        options: this.chartOptions,
      }),
    ]);
  },
};
