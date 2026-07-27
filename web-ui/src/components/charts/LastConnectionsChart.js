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
import {ProjectConnectionCollection} from '@/api';
import moment from 'moment';

export default {
  name: 'last-connections-chart',
  components: {Bar},
  props: {
    cssClasses: {type: String, default: ''},
    startDate: Number,
    endDate: Number,
    period: String,
    project: Number,
    user: Number,
    showDates: {type: Boolean, default: false},
    revision: Number
  },
  data() {
    return {
      projectConnections: null,
      chartData: {
        labels: [],
        datasets: [
          {
            label: this.$t('project-connections'),
            data: [],
            backgroundColor: '#4480c4',
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
      this.revision; // update of revision value will trigger re-evaluation of queryParams => new object => fetch data
      return {
        afterThan: this.startDate,
        beforeThan: this.endDate,
        period: this.period,
        user: this.user,
        project: this.project
      };
    },
    momentPeriod() {
      return this.period + 's';
    },
    chartOptions() {
      return {
        maintainAspectRatio: false,
        plugins: {
          legend: {display: false}
        },
        scales: {
          y: {
            min: 0,
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
    async fetchData() {
      let connections = (await ProjectConnectionCollection.fetchConnectionsFrequency(this.queryParams)).sort();
      connections.sort((a, b) => Number(a.time) < Number(b.time) ? -1 : 1);

      let allConnections = [];
      let iterMoment = moment(this.startDate);
      let lastMoment = this.endDate ? moment(this.endDate) : moment();
      let indexConnection = 0;
      while (iterMoment.isBefore(lastMoment)) {
        if (connections[indexConnection] && iterMoment.isSame(Number(connections[indexConnection].time), this.period)) {
          allConnections.push(connections[indexConnection]);
          indexConnection++;
        } else {
          allConnections.push({time: iterMoment.valueOf(), frequency: 0});
        }
        iterMoment.add(1, this.momentPeriod);
      }
      this.projectConnections = allConnections;
      this.chartData.datasets[0].data = this.projectConnections.map(item => item.frequency);
      this.updateLabels();
    },
    updateLabels() {
      this.chartData.labels = this.projectConnections.map(item => {
        let time = Number(item.time);
        switch (this.period) {
          case 'hour':
            return moment(time).format(this.showDates ? 'll LT' : 'LT') + ' - ' + moment(time).add(1, 'hours').format('LT');
          case 'day':
            return moment(time).format(this.showDates ? 'll' : 'dddd');
          case 'week':
            return moment(time).format('ll') + ' - ' + moment.min(moment(), moment(time).endOf('week')).format('ll');
        }
      });
    },
  },
  async mounted() {
    await this.fetchData();
  },
  render(h) {
    return h('div', {class: this.cssClasses}, [
      h(Bar, {
        props: {
          data: this.chartData,
          options: this.chartOptions,
        },
      }),
    ]);
  },
};
