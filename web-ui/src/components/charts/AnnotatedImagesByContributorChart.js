import { Bar } from 'vue-chartjs';
import ChartDataLabels from 'chartjs-plugin-datalabels';

export default {
  name: 'annotated-images-by-contributor-chart',
  components: { Bar },
  props: {
    cssClasses: { type: String, default: '' },
    project: Object,
    startDate: Number,
    endDate: Number
  },
  data() {
    return {
      chartData: { labels: [], datasets: [] },
    };
  },
  computed: {
    queryParams() {
      return {
        startDate: this.startDate,
        endDate: this.endDate
      };
    },
    chartOptions() {
      let data = this.chartData.datasets[0] ? this.chartData.datasets[0].data : [];
      return {
        indexAxis: 'y',
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          datalabels: {
            anchor: 'end',
            align: 'end',
            offset: 5,
            clamp: true,
          },
        },
        scales: {
          x: {
            min: 0,
            suggestedMax: data.length ? Math.round(Math.max(...data) * 1.2) + 1 : undefined
          },
          y: {
            grid: {
              display: false
            }
          }
        }
      };
    },
  },
  watch: {
    queryParams() {
      this.updateData();
    }
  },
  methods: {
    async updateData() {
      let contribs = await this.project.fetchStatsAnnotatedImagesByCreator(this.queryParams);
      let data = contribs.map(c => c.value);
      this.$emit('nbElems', data.length);

      this.chartData = {
        labels: contribs.map(c => c.key),
        datasets: [
          {
            data,
            backgroundColor: '#4480c4',
            borderWidth: 0,
            categoryPercentage: 0.6,
          }
        ]
      };
    }
  },
  async mounted() {
    await this.updateData();
  },
  render(h) {
    return h('div', { class: this.cssClasses }, [
      h(Bar, {
        props: {
          data: this.chartData,
          options: this.chartOptions,
          plugins: [ChartDataLabels],
        },
      }),
    ]);
  },
};
