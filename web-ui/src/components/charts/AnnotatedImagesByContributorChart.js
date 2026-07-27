/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import {Bar} from 'vue-chartjs';
import ChartDataLabels from 'chartjs-plugin-datalabels';

export default {
  name: 'annotated-images-by-contributor-chart',
  components: {Bar},
  props: {
    cssClasses: {type: String, default: ''},
    project: Object,
    startDate: Number,
    endDate: Number
  },
  data() {
    return {
      chartData: {labels: [], datasets: []},
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
          legend: {display: false},
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
    return h(Bar, {
      props: {
        chartData: this.chartData,
        chartOptions: this.chartOptions,
        cssClasses: this.cssClasses,
        plugins: [ChartDataLabels],
      },
    });
  },
};
