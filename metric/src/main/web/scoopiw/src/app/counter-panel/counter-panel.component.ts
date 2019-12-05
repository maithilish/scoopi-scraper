import { Component, Input, OnChanges } from '@angular/core';
import { Metric } from '../data-model';

@Component({
  selector: 'app-counter-panel',
  templateUrl: './counter-panel.component.html',
  styleUrls: ['./counter-panel.component.css']
})
export class CounterPanelComponent implements OnChanges {

  @Input() metrics: Metric[];
  @Input() cat: string[];
  @Input() heading: string;

  counters: Metric[];

  constructor() { }

  ngOnChanges() {
    this.filterData();
  }

  filterData() {

    this.counters = this.metrics.filter(metric => {
      if (metric.type === 'counter' && this.cat.includes(metric.cat)) {
        return metric;
      }
    });
  }
}
