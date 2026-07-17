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

import {h} from 'vue';
import Chart from 'chart.js';

/**
 * Minimal Vue 3 replacement for the vue-chartjs 3 base components (Vue 2
 * only), keeping chart.js 2 and the `extends` + `renderChart(data, options)`
 * pattern used by the chart components of this codebase.
 */
function makeChartComponent(name, type) {
  return {
    name,
    props: {
      chartId: {type: String, default: name},
      width: {type: Number, default: 400},
      height: {type: Number, default: 400},
      cssClasses: {type: String, default: ''},
      styles: {type: Object, default: null}
    },
    render() {
      return h(
        'div',
        {class: this.cssClasses, style: this.styles},
        [h('canvas', {id: this.chartId, width: this.width, height: this.height, ref: 'canvas'})]
      );
    },
    methods: {
      renderChart(data, options) {
        if (this.$data._chart) {
          this.$data._chart.destroy();
        }
        if (!this.$refs.canvas) {
          throw new Error('Please remove the <template></template> tags from your chart component.');
        }
        this.$data._chart = new Chart(this.$refs.canvas.getContext('2d'), {
          type,
          data,
          options
        });
      }
    },
    data() {
      return {
        _chart: null
      };
    },
    beforeUnmount() {
      if (this.$data._chart) {
        this.$data._chart.destroy();
      }
    }
  };
}

export const Bar = makeChartComponent('bar-chart', 'bar');
export const HorizontalBar = makeChartComponent('horizontalbar-chart', 'horizontalBar');
export const Doughnut = makeChartComponent('doughnut-chart', 'doughnut');
export const Line = makeChartComponent('line-chart', 'line');
export const Pie = makeChartComponent('pie-chart', 'pie');
export const PolarArea = makeChartComponent('polar-chart', 'polarArea');
export const Radar = makeChartComponent('radar-chart', 'radar');
export const Bubble = makeChartComponent('bubble-chart', 'bubble');
export const Scatter = makeChartComponent('scatter-chart', 'scatter');
