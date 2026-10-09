import { h } from 'vue';
import { Bar } from 'vue-chartjs';

import { formatMomentDate } from '@/utils/date';

export default {
  name: 'activity-overview-chart',
  components: { Bar },
  props: {
    cssClasses: { type: String, default: '' },
    project: Object,
    startDate: Number,
    endDate: Number,
    daysRange: Number
  },
  data() {
    return {
      projectConnections: [],
      imageConsultations: [],
      annotationSelections: [],
      chartData: {
        labels: [],
        datasets: [
          {
            label: this.$t('project-connections'),
            data: [],
            backgroundColor: '#4480c4',
            borderWidth: 0,
            categoryPercentage: 0.6,
          },
          {
            label: this.$t('image-consultations'),
            data: [],
            backgroundColor: '#f2418e',
            borderWidth: 0,
            categoryPercentage: 0.6,
          },
          {
            label: this.$t('annotation-selections'),
            data: [],
            backgroundColor: '#ffa500',
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
        accumulate: false
      };
    },
    chartOptions() {
      return {
        maintainAspectRatio: false,
        scales: {
          y: { min: 0 },
          x: { grid: { display: false } },
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
    async fetchProjectConnections() {
      this.projectConnections = await this.project.fetchConnectionsEvolution(this.queryParams);
    },
    async fetchImageConsultations() {
      this.imageConsultations = await this.project.fetchImageConsultationsEvolution(this.queryParams);
    },
    async fetchAnnotationSelections() {
      this.annotationSelections = await this.project.fetchAnnotationActionsEvolution({ action: 'select', ...this.queryParams });
    },
    async fetchData() {
      await Promise.all([
        this.fetchProjectConnections(),
        this.fetchImageConsultations(),
        this.fetchAnnotationSelections()
      ]);

      this.chartData.datasets[0].data = this.projectConnections.map(item => item.size);
      this.chartData.datasets[1].data = this.imageConsultations.map(item => item.size);
      this.chartData.datasets[2].data = this.annotationSelections.map(item => item.size);
      this.updateLabels();
    },
    updateLabels() {
      this.chartData.labels = this.projectConnections.map(item => {
        return this.daysRange === 1 ? formatMomentDate(Number(item.date), 'll')
          : [formatMomentDate(Number(item.date), 'll') + ' - ',  formatMomentDate(Number(item.endDate), 'll')];
      });
    },
  },
  async mounted() {
    await this.fetchData();
  },
  render() {
    return h('div', { class: this.cssClasses }, [
      h(Bar, {
        data: this.chartData,
        options: this.chartOptions,
      }),
    ]);
  },
};
