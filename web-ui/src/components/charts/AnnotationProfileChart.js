import {Line} from 'vue-chartjs';
import ChartZoom from 'chartjs-plugin-zoom';

export default {
  name: 'annotation-profile-chart',
  components: {Line},
  props: {
    cssClasses: {type: String, default: ''},
    annotation: Object,
    bpc: {type: Number, default: 8},
  },
  data() {
    return {
      chartData: {labels: [], datasets: []},
    };
  },
  computed: {
    queryParams() {
      return {
        bpc: this.bpc
      };
    },
    label() {
      return `(${Math.round(this.annotation.centroid.x)}, ${Math.round(this.annotation.centroid.y)})`;
    },
    chartOptions() {
      return {
        maintainAspectRatio: false,
        responsive: true,
        plugins: {
          legend: {
            display: false
          },
          zoom: {
            pan: {
              enabled: true,
              mode: 'xy',
            },
            zoom: {
              wheel: {enabled: true},
              drag: {enabled: false},
              mode: 'xy',
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: this.$t('slice')
            }
          },
          y: {
            title: {
              display: true,
              text: this.$t('pixel-intensity')
            },
            beginAtZero: true,
            max: Math.pow(2, this.bpc) - 1
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
        let data = (await this.annotation.fetchProfile())['profile'];

        this.chartData = {
          labels: [...Array(data.length).keys()],
          datasets: [
            {
              data,
              fill: false,
              label: this.label,
              borderColor: '#2778ad',
              backgroundColor: '#61b2e8'
            }
          ]
        };
      } catch (error) {
        console.log(error);
        this.$emit('error', true);
      }
    }
  },
  async mounted() {
    await this.updateData();
  },
  render(h) {
    return h('div', {class: this.cssClasses}, [
      h(Line, {
        ref: 'chartRef',
        props: {
          data: this.chartData,
          options: this.chartOptions,
          plugins: [ChartZoom],
        },
      }),
    ]);
  },
};
