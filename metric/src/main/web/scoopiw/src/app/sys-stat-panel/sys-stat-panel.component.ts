import { Component, OnChanges, Input } from '@angular/core';
import { Metric } from '../data-model';

@Component({
  selector: 'app-sys-stat-panel',
  templateUrl: './sys-stat-panel.component.html',
  styleUrls: ['./sys-stat-panel.component.css']
})
export class SysStatPanelComponent implements OnChanges {

  @Input() metrics!: Metric[];
  gauges: Metric[] = [];
  errors: Metric[] = [];

  constructor() { }

  ngOnChanges() {
    this.filterData();
  }

  filterData() {
    this.gauges = this.metrics.filter(metric => {
      if (metric.type === 'gauge' && metric.cat === 'system') {
        return metric;
      } else {
        return undefined;
      }
    });

    this.errors = this.metrics.filter(metric => {
      if (metric.type === 'counter' && metric.cat === 'system') {
        return metric;
      } else {
        return undefined;
      }
    });
  }
}
