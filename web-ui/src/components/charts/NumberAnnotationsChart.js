import { Bar } from 'vue-chartjs';

import { AnnotationType } from '@/api';
import { formatMomentDate } from '@/utils/date';
export default {
  name: 'number-annotations-chart',
  components: { Bar },
  props: {
    cssClasses: { type: String, default: '' },
    project: Object,
    term: Number,
    startDate: Number,
    endDate: Number,
    daysRange: Number
  },
  data() {
    return {
      annotationsEvolution: {
        [AnnotationType.USER]: [],
        [AnnotationType.REVIEWED]: []
      },
      chartData: {
        labels: [],
        datasets: [
          {
            label: this.$t('user-annotations'),
            data: [],
            backgroundColor: '#4480c4',
            borderWidth: 0,
            categoryPercentage: 0.6,
          },
          {
            label: this.$t('reviewed-annotations'),
            data: [],
            backgroundColor: '#42ce77',
            borderWidth: 0,
            categoryPercentage: 0.6,
          }
        ]
      },
    };
  },
  computed: {
    locale() {
      return this.$i18n.locale;
    },
    queryParams() {
      return {
        daysRange: this.daysRange,
        startDate: this.startDate,
        endDate: this.endDate,
        accumulate: false,
        reverseOrder: false,
        term: this.term
      };
    },
    chartOptions() {
      return {
        maintainAspectRatio: false,
        scales: {
          y: {
            min: 0
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      };
    },
  },
  watch: {
    async queryParams() {
      await this.fetchData();
    },
    locale() {
      this.updateLabels();
    }
  },
  methods: {
    async fetchAnnotationsEvolution(type) {
      this.annotationsEvolution[type] = await this.project.fetchAnnotationsEvolution({ annotationType: type, ...this.queryParams });
    },
    async fetchData() {
      await Promise.all([
        this.fetchAnnotationsEvolution(AnnotationType.USER),
        this.fetchAnnotationsEvolution(AnnotationType.REVIEWED)
      ]);

      this.chartData.datasets[0].data = this.annotationsEvolution[AnnotationType.USER].map(item => item.size);
      this.chartData.datasets[1].data = this.annotationsEvolution[AnnotationType.REVIEWED].map(item => item.size);
      this.updateLabels();
    },
    updateLabels() {
      this.chartData.labels = this.annotationsEvolution[AnnotationType.USER].map(item => {
        return this.daysRange === 1 ? formatMomentDate(Number(item.date), 'll')
          : [formatMomentDate(Number(item.date), 'll') + ' - ',  formatMomentDate(Number(item.endDate), 'll')];
      });
    },
  },
  async mounted() {
    await this.fetchData();
  },
  render(h) {
    return h('div', { class: this.cssClasses }, [
      h(Bar, {
        props: {
          data: this.chartData,
          options: this.chartOptions,
        },
      }),
    ]);
  },
};
