import { h } from 'vue';
import { Line } from 'vue-chartjs';
import ChartZoom from 'chartjs-plugin-zoom';

export default {
  name: 'annotation-profile-projection-chart',
  components: { Line },
  props: {
    cssClasses: { type: String, default: '' },
    annotation: Object,
    data: Array,
    spatialAxis: Boolean,
    dimension: String,
    slices: Array
  },
  data() {
    return {
      chartData: { labels: [], datasets: [] },
    };
  },
  computed: {
    queryParams() {
      return {};
    },
    sortedData() {
      if (this.spatialAxis) {
        return this.data;
      }
      return this.data.sort((a, b) => {
        if (a.x === b.x) {
          return a.y - b.y;
        }
        return a.x - b.x;
      });
    },
    labels() {
      if (this.spatialAxis && this.dimension === 'channels') {
        return this.sortedData.map(item => this.channelName(item.channel));
      } else if (this.spatialAxis && this.dimension === 'depth') {
        return this.sortedData.map(item => item.zStack);
      } else if (this.spatialAxis && this.dimension === 'duration') {
        return this.sortedData.map(item => item.time);
      } else {
        return this.sortedData.map(item => `(${item.x}, ${item.y})`);
      }
    },
    chartOptions() {
      return {
        maintainAspectRatio: false,
        responsive: true,
        plugins: {
          legend: {
            display: true
          },
          zoom: {
            pan: {
              enabled: true,
              mode: 'xy',
            },
            zoom: {
              wheel: { enabled: true },
              drag: { enabled: false },
              mode: 'xy',
            }
          }
        },
        scales: {
          y: {
            title: {
              display: true,
              text: this.$t('pixel-intensity')
            },
            beginAtZero: true,
          }
        },
      };
    },
  },
  watch: {
    async queryParams() {
      this.updateData();
    }
  },
  methods: {
    resetZoom() {
      this.$refs.chartRef.chart.resetZoom();
    },
    async updateData() {
      try {
        let data = this.sortedData;

        this.chartData = {
          labels: this.labels,
          datasets: [
            {
              data: data.map(item => item.average),
              fill: false,
              label: this.$t('average'),
              borderColor: '#2778ad',
              backgroundColor: '#61b2e8'
            },
          ]
        };
      } catch (error) {
        console.log(error);
        this.$emit('error', true);
      }
    },
    channelName(value) {
      if (!this.slices || this.slices.length === 0) {
        return value;
      }

      let slice = this.slices.find(slice => slice.channel === value);
      if (!slice) {
        return value;
      }

      return slice.channelName;
    },
  },
  async mounted() {
    await this.updateData();
  },
  render() {
    return h('div', { class: this.cssClasses }, [
      h(Line, {
        ref: 'chartRef',
        data: this.chartData,
        options: this.chartOptions,
        plugins: [ChartZoom],
      }),
    ]);
  },
};
