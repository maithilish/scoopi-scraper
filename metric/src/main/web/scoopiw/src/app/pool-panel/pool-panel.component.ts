import { Component, Input, OnChanges } from '@angular/core';
import { Metric } from '../data-model';

@Component({
  selector: 'app-pool-panel',
  templateUrl: './pool-panel.component.html',
  styleUrls: ['./pool-panel.component.css']
})
export class PoolPanelComponent implements OnChanges {

  @Input() metrics: Metric[];
  gauges: Metric[];

  constructor() { }

  ngOnChanges() {
    this.filterData();
  }

  filterData() {
    this.gauges = this.metrics.filter(metric => {
      if (metric.type === 'gauge' && metric.cat === 'pool') {
        return metric;
      }
    });
  }
}
